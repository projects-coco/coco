package com.coco.presentation.utils

import arrow.core.Either
import arrow.core.raise.Effect
import arrow.core.raise.effect
import arrow.core.raise.either
import com.coco.domain.model.ExcelDtoBase
import com.poiji.bind.Poiji
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.asFlux
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Flux
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime

interface Excel<T> {
    data object ExcelConvertFail

    fun add(data: ExcelDtoBase)

    fun add(data: List<String>)

    suspend fun response(filename: String): ResponseEntity<Flux<DataBuffer>>

    fun toList(clazz: Class<T>): Effect<ExcelConvertFail, List<T>>
}

private class PoiExcel<T>(
    private val workbook: Workbook,
) : Excel<T> {
    private val defaultSheet: Sheet
    private var defaultSheetRowNumber: Int = 0

    init {
        if (workbook.numberOfSheets == 0) {
            this.defaultSheet = workbook.createSheet()
        } else {
            this.defaultSheet = workbook.getSheetAt(0)
        }
    }

    private fun nextRowNumber() = defaultSheetRowNumber++

    override fun toList(clazz: Class<T>): Effect<Excel.ExcelConvertFail, List<T>> =
        effect {
            try {
                Poiji.fromExcel(defaultSheet, clazz)
            } catch (_: Exception) {
                raise(Excel.ExcelConvertFail)
            }
        }

    override fun add(data: ExcelDtoBase) {
        val nextRowNumber = this.nextRowNumber()
        val row = defaultSheet.createRow(nextRowNumber)
        data.javaClass.declaredFields.filter { field -> field.name != "Companion" }.forEachIndexed { index, field ->
            field.trySetAccessible()
            when (val value = field.get(data)) {
                is LocalDate -> row.createCell(index).setCellValue(value)
                is LocalDateTime -> row.createCell(index).setCellValue(value)
                is Boolean -> row.createCell(index).setCellValue(value)
                is Int -> row.createCell(index).setCellValue(value.toDouble())
                is Long -> row.createCell(index).setCellValue(value.toDouble())
                is Double -> row.createCell(index).setCellValue(value)
                is String -> row.createCell(index).setCellValue(value)
                else -> row.createCell(index).setCellValue(value.toString())
            }
        }
    }

    override fun add(data: List<String>) {
        val nextRowNumber = this.nextRowNumber()
        val row = defaultSheet.createRow(nextRowNumber)
        data.forEachIndexed { index, value ->
            row.createCell(index).setCellValue(value)
        }
    }

    override suspend fun response(filename: String): ResponseEntity<Flux<DataBuffer>> {
        val outputStream = ByteArrayOutputStream()
        try {
            workbook.write(outputStream)
            workbook.close()
        } catch (_: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
        val buffers: Flux<DataBuffer> =
            flow {
                val factory = DefaultDataBufferFactory()
                emit(factory.wrap(outputStream.toByteArray()))
            }.asFlux()
        return ResponseEntity.ok()
            .headers {
                it.contentDisposition =
                    ContentDisposition.attachment()
                        .filename(if (filename.endsWith(".xlsx")) filename else "$filename.xlsx").build()
                it.contentType = MediaType.APPLICATION_OCTET_STREAM
            }
            .contentLength(outputStream.size().toLong())
            .body(buffers)
    }
}

class ExcelUtils {
    companion object {
        data object ExcelParsingError

        fun <T> parse(filepath: String): Either<ExcelParsingError, Excel<T>> {
            val inputStream = FileInputStream(filepath)
            return parse(inputStream)
        }

        fun <T> parse(inputStream: InputStream): Either<ExcelParsingError, Excel<T>> =
            either {
                try {
                    PoiExcel(XSSFWorkbook(inputStream))
                } catch (_: Exception) {
                    raise(ExcelParsingError)
                }
            }

        fun <T> create(): Excel<T> {
            return PoiExcel(SXSSFWorkbook(XSSFWorkbook(), 100, true))
        }
    }
}

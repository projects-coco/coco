package com.coco.domain.core

data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Long,
    val pageRequest: PageRequest,
) {
    fun get(index: Int): T = content[index]

    inline fun <V> map(mapper: (T) -> V): Page<V> =
        Page(
            content = content.map(mapper),
            totalElements = totalElements,
            totalPages = totalPages,
            pageRequest = pageRequest,
        )
}

data class PageRequest(
    val pageSize: Int,
    val pageNumber: Int,
    val sort: Sort,
) {
    val limit get() = pageSize
    val offset get() = (pageNumber - 1) * pageSize

    data class Sort internal constructor(
        val field: String,
        val direction: Direction,
    ) {
        sealed class Direction {
            data object ASC : Direction()

            data object DESC : Direction()
        }

        companion object {
            fun of(payload: String): Sort {
                if (payload.isNotEmpty()) {
                    val (field, direction) = payload.split(",")
                    return Sort(
                        field = field.trim(),
                        direction = safeDirection(direction.trim()),
                    )
                } else {
                    return Sort(
                        field = "id",
                        direction = Direction.DESC,
                    )
                }
            }

            private fun safeDirection(str: String): Direction =
                when (str.uppercase()) {
                    "ASC" -> Direction.ASC
                    "DESC" -> Direction.DESC
                    else -> Direction.DESC
                }
        }
    }
}

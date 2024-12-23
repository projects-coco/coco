/*
 * This file is generated by jOOQ.
 */
package com.jooq.tables


import com.jooq.keys.PK_SAMPLE
import com.jooq.tables.records.SampleRecord

import java.time.LocalDateTime

import kotlin.collections.Collection

import org.jooq.Condition
import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.InverseForeignKey
import org.jooq.Name
import org.jooq.PlainSQL
import org.jooq.QueryPart
import org.jooq.Record
import org.jooq.SQL
import org.jooq.Schema
import org.jooq.Select
import org.jooq.Stringly
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Sample(
    alias: Name,
    path: Table<out Record>?,
    childPath: ForeignKey<out Record, SampleRecord>?,
    parentPath: InverseForeignKey<out Record, SampleRecord>?,
    aliased: Table<SampleRecord>?,
    parameters: Array<Field<*>?>?,
    where: Condition?
): TableImpl<SampleRecord>(
    alias,
    com.jooq.Sample.SAMPLE,
    path,
    childPath,
    parentPath,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table(),
    where,
) {
    companion object {

        /**
         * The reference instance of <code>sample.sample</code>
         */
        val SAMPLE_: Sample = Sample()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<SampleRecord> = SampleRecord::class.java

    /**
     * The column <code>sample.sample.id</code>.
     */
    val ID: TableField<SampleRecord, ByteArray?> = createField(DSL.name("id"), SQLDataType.BINARY(16).nullable(false), this, "")

    /**
     * The column <code>sample.sample.changeable_value1</code>.
     */
    val CHANGEABLE_VALUE1: TableField<SampleRecord, String?> = createField(DSL.name("changeable_value1"), SQLDataType.VARCHAR(255), this, "")

    /**
     * The column <code>sample.sample.changeable_value2</code>.
     */
    val CHANGEABLE_VALUE2: TableField<SampleRecord, String?> = createField(DSL.name("changeable_value2"), SQLDataType.VARCHAR(255), this, "")

    /**
     * The column <code>sample.sample.changeable_value3</code>.
     */
    val CHANGEABLE_VALUE3: TableField<SampleRecord, String?> = createField(DSL.name("changeable_value3"), SQLDataType.VARCHAR(255), this, "")

    /**
     * The column <code>sample.sample.changeable_int1</code>.
     */
    val CHANGEABLE_INT1: TableField<SampleRecord, Int?> = createField(DSL.name("changeable_int1"), SQLDataType.INTEGER, this, "")

    /**
     * The column <code>sample.sample.changeable_int2</code>.
     */
    val CHANGEABLE_INT2: TableField<SampleRecord, Int?> = createField(DSL.name("changeable_int2"), SQLDataType.INTEGER, this, "")

    /**
     * The column <code>sample.sample.created_at</code>.
     */
    val CREATED_AT: TableField<SampleRecord, LocalDateTime?> = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "")

    /**
     * The column <code>sample.sample.updated_at</code>.
     */
    val UPDATED_AT: TableField<SampleRecord, LocalDateTime?> = createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<SampleRecord>?): this(alias, null, null, null, aliased, null, null)
    private constructor(alias: Name, aliased: Table<SampleRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, null, aliased, parameters, null)
    private constructor(alias: Name, aliased: Table<SampleRecord>?, where: Condition): this(alias, null, null, null, aliased, null, where)

    /**
     * Create an aliased <code>sample.sample</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>sample.sample</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>sample.sample</code> table reference
     */
    constructor(): this(DSL.name("sample"), null)
    override fun getSchema(): Schema? = if (aliased()) null else com.jooq.Sample.SAMPLE
    override fun getPrimaryKey(): UniqueKey<SampleRecord> = PK_SAMPLE
    override fun `as`(alias: String): Sample = Sample(DSL.name(alias), this)
    override fun `as`(alias: Name): Sample = Sample(alias, this)
    override fun `as`(alias: Table<*>): Sample = Sample(alias.qualifiedName, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Sample = Sample(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Sample = Sample(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): Sample = Sample(name.qualifiedName, null)

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Condition): Sample = Sample(qualifiedName, if (aliased()) this else null, condition)

    /**
     * Create an inline derived table from this table
     */
    override fun where(conditions: Collection<Condition>): Sample = where(DSL.and(conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(vararg conditions: Condition): Sample = where(DSL.and(*conditions))

    /**
     * Create an inline derived table from this table
     */
    override fun where(condition: Field<Boolean?>): Sample = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(condition: SQL): Sample = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String): Sample = where(DSL.condition(condition))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg binds: Any?): Sample = where(DSL.condition(condition, *binds))

    /**
     * Create an inline derived table from this table
     */
    @PlainSQL override fun where(@Stringly.SQL condition: String, vararg parts: QueryPart): Sample = where(DSL.condition(condition, *parts))

    /**
     * Create an inline derived table from this table
     */
    override fun whereExists(select: Select<*>): Sample = where(DSL.exists(select))

    /**
     * Create an inline derived table from this table
     */
    override fun whereNotExists(select: Select<*>): Sample = where(DSL.notExists(select))
}

/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables;


import jslx.dbutilities.jsldbsrc.JslDb;
import jslx.dbutilities.jsldbsrc.tables.records.WithinRepResponseViewRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row5;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WithinRepResponseView extends TableImpl<WithinRepResponseViewRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW</code>
     */
    public static final WithinRepResponseView WITHIN_REP_RESPONSE_VIEW = new WithinRepResponseView();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<WithinRepResponseViewRecord> getRecordType() {
        return WithinRepResponseViewRecord.class;
    }

    /**
     * The column <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW.SIM_RUN_ID_FK</code>.
     */
    public final TableField<WithinRepResponseViewRecord, Integer> SIM_RUN_ID_FK = createField(DSL.name("SIM_RUN_ID_FK"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW.EXP_NAME</code>.
     */
    public final TableField<WithinRepResponseViewRecord, String> EXP_NAME = createField(DSL.name("EXP_NAME"), SQLDataType.VARCHAR(510).nullable(false), this, "");

    /**
     * The column <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW.STAT_NAME</code>.
     */
    public final TableField<WithinRepResponseViewRecord, String> STAT_NAME = createField(DSL.name("STAT_NAME"), SQLDataType.VARCHAR(510), this, "");

    /**
     * The column <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW.REP_NUM</code>.
     */
    public final TableField<WithinRepResponseViewRecord, Integer> REP_NUM = createField(DSL.name("REP_NUM"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW.AVERAGE</code>.
     */
    public final TableField<WithinRepResponseViewRecord, Double> AVERAGE = createField(DSL.name("AVERAGE"), SQLDataType.DOUBLE, this, "");

    private WithinRepResponseView(Name alias, Table<WithinRepResponseViewRecord> aliased) {
        this(alias, aliased, null);
    }

    private WithinRepResponseView(Name alias, Table<WithinRepResponseViewRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.view("CREATE VIEW JSL_DB.WITHIN_REP_RESPONSE_VIEW (SIM_RUN_ID_FK, EXP_NAME, STAT_NAME, REP_NUM, AVERAGE) AS (SELECT JSL_DB.WITHIN_REP_STAT.SIM_RUN_ID_FK, EXP_NAME, STAT_NAME, REP_NUM, AVERAGE FROM JSL_DB.SIMULATION_RUN, JSL_DB.MODEL_ELEMENT,JSL_DB.WITHIN_REP_STAT WHERE JSL_DB.SIMULATION_RUN.ID = JSL_DB.WITHIN_REP_STAT.SIM_RUN_ID_FK AND JSL_DB.SIMULATION_RUN.ID = JSL_DB.MODEL_ELEMENT.SIM_RUN_ID_FK AND JSL_DB.MODEL_ELEMENT.ELEMENT_ID = JSL_DB.WITHIN_REP_STAT.ELEMENT_ID_FK AND JSL_DB.MODEL_ELEMENT.ELEMENT_NAME = JSL_DB.WITHIN_REP_STAT.STAT_NAME ORDER BY JSL_DB.WITHIN_REP_STAT.SIM_RUN_ID_FK, EXP_NAME, STAT_NAME, REP_NUM)"));
    }

    /**
     * Create an aliased <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW</code> table reference
     */
    public WithinRepResponseView(String alias) {
        this(DSL.name(alias), WITHIN_REP_RESPONSE_VIEW);
    }

    /**
     * Create an aliased <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW</code> table reference
     */
    public WithinRepResponseView(Name alias) {
        this(alias, WITHIN_REP_RESPONSE_VIEW);
    }

    /**
     * Create a <code>JSL_DB.WITHIN_REP_RESPONSE_VIEW</code> table reference
     */
    public WithinRepResponseView() {
        this(DSL.name("WITHIN_REP_RESPONSE_VIEW"), null);
    }

    public <O extends Record> WithinRepResponseView(Table<O> child, ForeignKey<O, WithinRepResponseViewRecord> key) {
        super(child, key, WITHIN_REP_RESPONSE_VIEW);
    }

    @Override
    public Schema getSchema() {
        return JslDb.JSL_DB;
    }

    @Override
    public WithinRepResponseView as(String alias) {
        return new WithinRepResponseView(DSL.name(alias), this);
    }

    @Override
    public WithinRepResponseView as(Name alias) {
        return new WithinRepResponseView(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public WithinRepResponseView rename(String name) {
        return new WithinRepResponseView(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public WithinRepResponseView rename(Name name) {
        return new WithinRepResponseView(name, null);
    }

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row5<Integer, String, String, Integer, Double> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}

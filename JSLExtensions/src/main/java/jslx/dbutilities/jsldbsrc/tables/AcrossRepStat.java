/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables;


import java.util.Arrays;
import java.util.List;

import jslx.dbutilities.jsldbsrc.Indexes;
import jslx.dbutilities.jsldbsrc.JslDb;
import jslx.dbutilities.jsldbsrc.Keys;
import jslx.dbutilities.jsldbsrc.tables.records.AcrossRepStatRecord;

import org.jooq.Check;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row21;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AcrossRepStat extends TableImpl<AcrossRepStatRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>JSL_DB.ACROSS_REP_STAT</code>
     */
    public static final AcrossRepStat ACROSS_REP_STAT = new AcrossRepStat();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AcrossRepStatRecord> getRecordType() {
        return AcrossRepStatRecord.class;
    }

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.ID</code>.
     */
    public final TableField<AcrossRepStatRecord, Integer> ID = createField(DSL.name("ID"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.ELEMENT_ID_FK</code>.
     */
    public final TableField<AcrossRepStatRecord, Integer> ELEMENT_ID_FK = createField(DSL.name("ELEMENT_ID_FK"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.SIM_RUN_ID_FK</code>.
     */
    public final TableField<AcrossRepStatRecord, Integer> SIM_RUN_ID_FK = createField(DSL.name("SIM_RUN_ID_FK"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.STAT_NAME</code>.
     */
    public final TableField<AcrossRepStatRecord, String> STAT_NAME = createField(DSL.name("STAT_NAME"), SQLDataType.VARCHAR(510), this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.STAT_COUNT</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> STAT_COUNT = createField(DSL.name("STAT_COUNT"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.AVERAGE</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> AVERAGE = createField(DSL.name("AVERAGE"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.STD_DEV</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> STD_DEV = createField(DSL.name("STD_DEV"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.STD_ERR</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> STD_ERR = createField(DSL.name("STD_ERR"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.HALF_WIDTH</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> HALF_WIDTH = createField(DSL.name("HALF_WIDTH"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.CONF_LEVEL</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> CONF_LEVEL = createField(DSL.name("CONF_LEVEL"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.MINIMUM</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> MINIMUM = createField(DSL.name("MINIMUM"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.MAXIMUM</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> MAXIMUM = createField(DSL.name("MAXIMUM"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.SUM_OF_OBS</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> SUM_OF_OBS = createField(DSL.name("SUM_OF_OBS"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.DEV_SSQ</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> DEV_SSQ = createField(DSL.name("DEV_SSQ"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.LAST_VALUE</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> LAST_VALUE = createField(DSL.name("LAST_VALUE"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.KURTOSIS</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> KURTOSIS = createField(DSL.name("KURTOSIS"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.SKEWNESS</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> SKEWNESS = createField(DSL.name("SKEWNESS"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.LAG1_COV</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> LAG1_COV = createField(DSL.name("LAG1_COV"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.LAG1_CORR</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> LAG1_CORR = createField(DSL.name("LAG1_CORR"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.VON_NEUMAN_LAG1_STAT</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> VON_NEUMAN_LAG1_STAT = createField(DSL.name("VON_NEUMAN_LAG1_STAT"), SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>JSL_DB.ACROSS_REP_STAT.NUM_MISSING_OBS</code>.
     */
    public final TableField<AcrossRepStatRecord, Double> NUM_MISSING_OBS = createField(DSL.name("NUM_MISSING_OBS"), SQLDataType.DOUBLE, this, "");

    private AcrossRepStat(Name alias, Table<AcrossRepStatRecord> aliased) {
        this(alias, aliased, null);
    }

    private AcrossRepStat(Name alias, Table<AcrossRepStatRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>JSL_DB.ACROSS_REP_STAT</code> table reference
     */
    public AcrossRepStat(String alias) {
        this(DSL.name(alias), ACROSS_REP_STAT);
    }

    /**
     * Create an aliased <code>JSL_DB.ACROSS_REP_STAT</code> table reference
     */
    public AcrossRepStat(Name alias) {
        this(alias, ACROSS_REP_STAT);
    }

    /**
     * Create a <code>JSL_DB.ACROSS_REP_STAT</code> table reference
     */
    public AcrossRepStat() {
        this(DSL.name("ACROSS_REP_STAT"), null);
    }

    public <O extends Record> AcrossRepStat(Table<O> child, ForeignKey<O, AcrossRepStatRecord> key) {
        super(child, key, ACROSS_REP_STAT);
    }

    @Override
    public Schema getSchema() {
        return JslDb.JSL_DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.ARS_ME_FK_INDEX);
    }

    @Override
    public Identity<AcrossRepStatRecord, Integer> getIdentity() {
        return (Identity<AcrossRepStatRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<AcrossRepStatRecord> getPrimaryKey() {
        return Keys.SQL0000000095_736A8145_0179_91C9_A2FA_00000F317028;
    }

    @Override
    public List<UniqueKey<AcrossRepStatRecord>> getKeys() {
        return Arrays.<UniqueKey<AcrossRepStatRecord>>asList(Keys.SQL0000000095_736A8145_0179_91C9_A2FA_00000F317028);
    }

    @Override
    public List<ForeignKey<AcrossRepStatRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<AcrossRepStatRecord, ?>>asList(Keys.ARS_MODEL_ELEMENT_FK, Keys.ARS_SIMRUN_FK);
    }

    private transient ModelElement _modelElement;
    private transient SimulationRun _simulationRun;

    public ModelElement modelElement() {
        if (_modelElement == null)
            _modelElement = new ModelElement(this, Keys.ARS_MODEL_ELEMENT_FK);

        return _modelElement;
    }

    public SimulationRun simulationRun() {
        if (_simulationRun == null)
            _simulationRun = new SimulationRun(this, Keys.ARS_SIMRUN_FK);

        return _simulationRun;
    }

    @Override
    public List<Check<AcrossRepStatRecord>> getChecks() {
        return Arrays.<Check<AcrossRepStatRecord>>asList(
              Internal.createCheck(this, DSL.name("SQL0000000096-1bbbc146-0179-91c9-a2fa-00000f317028"), "(STAT_COUNT >=0)", true)
            , Internal.createCheck(this, DSL.name("SQL0000000097-140d4147-0179-91c9-a2fa-00000f317028"), "(STD_DEV >=0)", true)
            , Internal.createCheck(this, DSL.name("SQL0000000098-6c5f0148-0179-91c9-a2fa-00000f317028"), "(STD_ERR >=0)", true)
            , Internal.createCheck(this, DSL.name("SQL0000000099-34b10149-0179-91c9-a2fa-00000f317028"), "(HALF_WIDTH >=0)", true)
        );
    }

    @Override
    public AcrossRepStat as(String alias) {
        return new AcrossRepStat(DSL.name(alias), this);
    }

    @Override
    public AcrossRepStat as(Name alias) {
        return new AcrossRepStat(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AcrossRepStat rename(String name) {
        return new AcrossRepStat(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AcrossRepStat rename(Name name) {
        return new AcrossRepStat(name, null);
    }

    // -------------------------------------------------------------------------
    // Row21 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row21<Integer, Integer, Integer, String, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> fieldsRow() {
        return (Row21) super.fieldsRow();
    }
}

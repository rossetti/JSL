/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables.records;


import jslx.dbutilities.jsldbsrc.tables.WithinRepCounterStat;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WithinRepCounterStatRecord extends UpdatableRecordImpl<WithinRepCounterStatRecord> implements Record6<Integer, Integer, Integer, Integer, String, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.ID</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.ID</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.ELEMENT_ID_FK</code>.
     */
    public void setElementIdFk(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.ELEMENT_ID_FK</code>.
     */
    public Integer getElementIdFk() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.SIM_RUN_ID_FK</code>.
     */
    public void setSimRunIdFk(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.SIM_RUN_ID_FK</code>.
     */
    public Integer getSimRunIdFk() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.REP_NUM</code>.
     */
    public void setRepNum(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.REP_NUM</code>.
     */
    public Integer getRepNum() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.STAT_NAME</code>.
     */
    public void setStatName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.STAT_NAME</code>.
     */
    public String getStatName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.LAST_VALUE</code>.
     */
    public void setLastValue(Double value) {
        set(5, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_STAT.LAST_VALUE</code>.
     */
    public Double getLastValue() {
        return (Double) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Integer, Integer, Integer, Integer, String, Double> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Integer, Integer, Integer, Integer, String, Double> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.ID;
    }

    @Override
    public Field<Integer> field2() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.ELEMENT_ID_FK;
    }

    @Override
    public Field<Integer> field3() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.SIM_RUN_ID_FK;
    }

    @Override
    public Field<Integer> field4() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.REP_NUM;
    }

    @Override
    public Field<String> field5() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.STAT_NAME;
    }

    @Override
    public Field<Double> field6() {
        return WithinRepCounterStat.WITHIN_REP_COUNTER_STAT.LAST_VALUE;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public Integer component2() {
        return getElementIdFk();
    }

    @Override
    public Integer component3() {
        return getSimRunIdFk();
    }

    @Override
    public Integer component4() {
        return getRepNum();
    }

    @Override
    public String component5() {
        return getStatName();
    }

    @Override
    public Double component6() {
        return getLastValue();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public Integer value2() {
        return getElementIdFk();
    }

    @Override
    public Integer value3() {
        return getSimRunIdFk();
    }

    @Override
    public Integer value4() {
        return getRepNum();
    }

    @Override
    public String value5() {
        return getStatName();
    }

    @Override
    public Double value6() {
        return getLastValue();
    }

    @Override
    public WithinRepCounterStatRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord value2(Integer value) {
        setElementIdFk(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord value3(Integer value) {
        setSimRunIdFk(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord value4(Integer value) {
        setRepNum(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord value5(String value) {
        setStatName(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord value6(Double value) {
        setLastValue(value);
        return this;
    }

    @Override
    public WithinRepCounterStatRecord values(Integer value1, Integer value2, Integer value3, Integer value4, String value5, Double value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WithinRepCounterStatRecord
     */
    public WithinRepCounterStatRecord() {
        super(WithinRepCounterStat.WITHIN_REP_COUNTER_STAT);
    }

    /**
     * Create a detached, initialised WithinRepCounterStatRecord
     */
    public WithinRepCounterStatRecord(Integer id, Integer elementIdFk, Integer simRunIdFk, Integer repNum, String statName, Double lastValue) {
        super(WithinRepCounterStat.WITHIN_REP_COUNTER_STAT);

        setId(id);
        setElementIdFk(elementIdFk);
        setSimRunIdFk(simRunIdFk);
        setRepNum(repNum);
        setStatName(statName);
        setLastValue(lastValue);
    }
}

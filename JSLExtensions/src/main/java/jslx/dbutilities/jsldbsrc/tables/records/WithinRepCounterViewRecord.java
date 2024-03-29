/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables.records;


import jslx.dbutilities.jsldbsrc.tables.WithinRepCounterView;

import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WithinRepCounterViewRecord extends TableRecordImpl<WithinRepCounterViewRecord> implements Record5<Integer, String, String, Integer, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.SIM_RUN_ID_FK</code>.
     */
    public void setSimRunIdFk(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.SIM_RUN_ID_FK</code>.
     */
    public Integer getSimRunIdFk() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.EXP_NAME</code>.
     */
    public void setExpName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.EXP_NAME</code>.
     */
    public String getExpName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.STAT_NAME</code>.
     */
    public void setStatName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.STAT_NAME</code>.
     */
    public String getStatName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.REP_NUM</code>.
     */
    public void setRepNum(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.REP_NUM</code>.
     */
    public Integer getRepNum() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.LAST_VALUE</code>.
     */
    public void setLastValue(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_COUNTER_VIEW.LAST_VALUE</code>.
     */
    public Double getLastValue() {
        return (Double) get(4);
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Integer, String, String, Integer, Double> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Integer, String, String, Integer, Double> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return WithinRepCounterView.WITHIN_REP_COUNTER_VIEW.SIM_RUN_ID_FK;
    }

    @Override
    public Field<String> field2() {
        return WithinRepCounterView.WITHIN_REP_COUNTER_VIEW.EXP_NAME;
    }

    @Override
    public Field<String> field3() {
        return WithinRepCounterView.WITHIN_REP_COUNTER_VIEW.STAT_NAME;
    }

    @Override
    public Field<Integer> field4() {
        return WithinRepCounterView.WITHIN_REP_COUNTER_VIEW.REP_NUM;
    }

    @Override
    public Field<Double> field5() {
        return WithinRepCounterView.WITHIN_REP_COUNTER_VIEW.LAST_VALUE;
    }

    @Override
    public Integer component1() {
        return getSimRunIdFk();
    }

    @Override
    public String component2() {
        return getExpName();
    }

    @Override
    public String component3() {
        return getStatName();
    }

    @Override
    public Integer component4() {
        return getRepNum();
    }

    @Override
    public Double component5() {
        return getLastValue();
    }

    @Override
    public Integer value1() {
        return getSimRunIdFk();
    }

    @Override
    public String value2() {
        return getExpName();
    }

    @Override
    public String value3() {
        return getStatName();
    }

    @Override
    public Integer value4() {
        return getRepNum();
    }

    @Override
    public Double value5() {
        return getLastValue();
    }

    @Override
    public WithinRepCounterViewRecord value1(Integer value) {
        setSimRunIdFk(value);
        return this;
    }

    @Override
    public WithinRepCounterViewRecord value2(String value) {
        setExpName(value);
        return this;
    }

    @Override
    public WithinRepCounterViewRecord value3(String value) {
        setStatName(value);
        return this;
    }

    @Override
    public WithinRepCounterViewRecord value4(Integer value) {
        setRepNum(value);
        return this;
    }

    @Override
    public WithinRepCounterViewRecord value5(Double value) {
        setLastValue(value);
        return this;
    }

    @Override
    public WithinRepCounterViewRecord values(Integer value1, String value2, String value3, Integer value4, Double value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WithinRepCounterViewRecord
     */
    public WithinRepCounterViewRecord() {
        super(WithinRepCounterView.WITHIN_REP_COUNTER_VIEW);
    }

    /**
     * Create a detached, initialised WithinRepCounterViewRecord
     */
    public WithinRepCounterViewRecord(Integer simRunIdFk, String expName, String statName, Integer repNum, Double lastValue) {
        super(WithinRepCounterView.WITHIN_REP_COUNTER_VIEW);

        setSimRunIdFk(simRunIdFk);
        setExpName(expName);
        setStatName(statName);
        setRepNum(repNum);
        setLastValue(lastValue);
    }
}

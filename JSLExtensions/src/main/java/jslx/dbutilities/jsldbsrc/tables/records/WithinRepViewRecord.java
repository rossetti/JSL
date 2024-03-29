/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables.records;


import jslx.dbutilities.jsldbsrc.tables.WithinRepView;

import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WithinRepViewRecord extends TableRecordImpl<WithinRepViewRecord> implements Record5<Integer, String, String, Integer, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_VIEW.SIM_RUN_ID_FK</code>.
     */
    public void setSimRunIdFk(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_VIEW.SIM_RUN_ID_FK</code>.
     */
    public Integer getSimRunIdFk() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_VIEW.EXP_NAME</code>.
     */
    public void setExpName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_VIEW.EXP_NAME</code>.
     */
    public String getExpName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_VIEW.STAT_NAME</code>.
     */
    public void setStatName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_VIEW.STAT_NAME</code>.
     */
    public String getStatName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_VIEW.REP_NUM</code>.
     */
    public void setRepNum(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_VIEW.REP_NUM</code>.
     */
    public Integer getRepNum() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_VIEW.VALUE</code>.
     */
    public void setValue(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_VIEW.VALUE</code>.
     */
    public Double getValue() {
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
        return WithinRepView.WITHIN_REP_VIEW.SIM_RUN_ID_FK;
    }

    @Override
    public Field<String> field2() {
        return WithinRepView.WITHIN_REP_VIEW.EXP_NAME;
    }

    @Override
    public Field<String> field3() {
        return WithinRepView.WITHIN_REP_VIEW.STAT_NAME;
    }

    @Override
    public Field<Integer> field4() {
        return WithinRepView.WITHIN_REP_VIEW.REP_NUM;
    }

    @Override
    public Field<Double> field5() {
        return WithinRepView.WITHIN_REP_VIEW.VALUE;
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
        return getValue();
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
        return getValue();
    }

    @Override
    public WithinRepViewRecord value1(Integer value) {
        setSimRunIdFk(value);
        return this;
    }

    @Override
    public WithinRepViewRecord value2(String value) {
        setExpName(value);
        return this;
    }

    @Override
    public WithinRepViewRecord value3(String value) {
        setStatName(value);
        return this;
    }

    @Override
    public WithinRepViewRecord value4(Integer value) {
        setRepNum(value);
        return this;
    }

    @Override
    public WithinRepViewRecord value5(Double value) {
        setValue(value);
        return this;
    }

    @Override
    public WithinRepViewRecord values(Integer value1, String value2, String value3, Integer value4, Double value5) {
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
     * Create a detached WithinRepViewRecord
     */
    public WithinRepViewRecord() {
        super(WithinRepView.WITHIN_REP_VIEW);
    }

    /**
     * Create a detached, initialised WithinRepViewRecord
     */
    public WithinRepViewRecord(Integer simRunIdFk, String expName, String statName, Integer repNum, Double value) {
        super(WithinRepView.WITHIN_REP_VIEW);

        setSimRunIdFk(simRunIdFk);
        setExpName(expName);
        setStatName(statName);
        setRepNum(repNum);
        setValue(value);
    }
}

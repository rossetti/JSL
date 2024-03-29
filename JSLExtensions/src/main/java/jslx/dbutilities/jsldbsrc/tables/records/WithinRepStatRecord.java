/*
 * This file is generated by jOOQ.
 */
package jslx.dbutilities.jsldbsrc.tables.records;


import jslx.dbutilities.jsldbsrc.tables.WithinRepStat;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record14;
import org.jooq.Row14;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class WithinRepStatRecord extends UpdatableRecordImpl<WithinRepStatRecord> implements Record14<Integer, Integer, Integer, Integer, String, Double, Double, Double, Double, Double, Double, Double, Double, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.ID</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.ID</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.ELEMENT_ID_FK</code>.
     */
    public void setElementIdFk(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.ELEMENT_ID_FK</code>.
     */
    public Integer getElementIdFk() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.SIM_RUN_ID_FK</code>.
     */
    public void setSimRunIdFk(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.SIM_RUN_ID_FK</code>.
     */
    public Integer getSimRunIdFk() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.REP_NUM</code>.
     */
    public void setRepNum(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.REP_NUM</code>.
     */
    public Integer getRepNum() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.STAT_NAME</code>.
     */
    public void setStatName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.STAT_NAME</code>.
     */
    public String getStatName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.STAT_COUNT</code>.
     */
    public void setStatCount(Double value) {
        set(5, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.STAT_COUNT</code>.
     */
    public Double getStatCount() {
        return (Double) get(5);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.AVERAGE</code>.
     */
    public void setAverage(Double value) {
        set(6, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.AVERAGE</code>.
     */
    public Double getAverage() {
        return (Double) get(6);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.MINIMUM</code>.
     */
    public void setMinimum(Double value) {
        set(7, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.MINIMUM</code>.
     */
    public Double getMinimum() {
        return (Double) get(7);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.MAXIMUM</code>.
     */
    public void setMaximum(Double value) {
        set(8, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.MAXIMUM</code>.
     */
    public Double getMaximum() {
        return (Double) get(8);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.WEIGHTED_SUM</code>.
     */
    public void setWeightedSum(Double value) {
        set(9, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.WEIGHTED_SUM</code>.
     */
    public Double getWeightedSum() {
        return (Double) get(9);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.SUM_OF_WEIGHTS</code>.
     */
    public void setSumOfWeights(Double value) {
        set(10, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.SUM_OF_WEIGHTS</code>.
     */
    public Double getSumOfWeights() {
        return (Double) get(10);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.WEIGHTED_SSQ</code>.
     */
    public void setWeightedSsq(Double value) {
        set(11, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.WEIGHTED_SSQ</code>.
     */
    public Double getWeightedSsq() {
        return (Double) get(11);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.LAST_VALUE</code>.
     */
    public void setLastValue(Double value) {
        set(12, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.LAST_VALUE</code>.
     */
    public Double getLastValue() {
        return (Double) get(12);
    }

    /**
     * Setter for <code>JSL_DB.WITHIN_REP_STAT.LAST_WEIGHT</code>.
     */
    public void setLastWeight(Double value) {
        set(13, value);
    }

    /**
     * Getter for <code>JSL_DB.WITHIN_REP_STAT.LAST_WEIGHT</code>.
     */
    public Double getLastWeight() {
        return (Double) get(13);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record14 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row14<Integer, Integer, Integer, Integer, String, Double, Double, Double, Double, Double, Double, Double, Double, Double> fieldsRow() {
        return (Row14) super.fieldsRow();
    }

    @Override
    public Row14<Integer, Integer, Integer, Integer, String, Double, Double, Double, Double, Double, Double, Double, Double, Double> valuesRow() {
        return (Row14) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return WithinRepStat.WITHIN_REP_STAT.ID;
    }

    @Override
    public Field<Integer> field2() {
        return WithinRepStat.WITHIN_REP_STAT.ELEMENT_ID_FK;
    }

    @Override
    public Field<Integer> field3() {
        return WithinRepStat.WITHIN_REP_STAT.SIM_RUN_ID_FK;
    }

    @Override
    public Field<Integer> field4() {
        return WithinRepStat.WITHIN_REP_STAT.REP_NUM;
    }

    @Override
    public Field<String> field5() {
        return WithinRepStat.WITHIN_REP_STAT.STAT_NAME;
    }

    @Override
    public Field<Double> field6() {
        return WithinRepStat.WITHIN_REP_STAT.STAT_COUNT;
    }

    @Override
    public Field<Double> field7() {
        return WithinRepStat.WITHIN_REP_STAT.AVERAGE;
    }

    @Override
    public Field<Double> field8() {
        return WithinRepStat.WITHIN_REP_STAT.MINIMUM;
    }

    @Override
    public Field<Double> field9() {
        return WithinRepStat.WITHIN_REP_STAT.MAXIMUM;
    }

    @Override
    public Field<Double> field10() {
        return WithinRepStat.WITHIN_REP_STAT.WEIGHTED_SUM;
    }

    @Override
    public Field<Double> field11() {
        return WithinRepStat.WITHIN_REP_STAT.SUM_OF_WEIGHTS;
    }

    @Override
    public Field<Double> field12() {
        return WithinRepStat.WITHIN_REP_STAT.WEIGHTED_SSQ;
    }

    @Override
    public Field<Double> field13() {
        return WithinRepStat.WITHIN_REP_STAT.LAST_VALUE;
    }

    @Override
    public Field<Double> field14() {
        return WithinRepStat.WITHIN_REP_STAT.LAST_WEIGHT;
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
        return getStatCount();
    }

    @Override
    public Double component7() {
        return getAverage();
    }

    @Override
    public Double component8() {
        return getMinimum();
    }

    @Override
    public Double component9() {
        return getMaximum();
    }

    @Override
    public Double component10() {
        return getWeightedSum();
    }

    @Override
    public Double component11() {
        return getSumOfWeights();
    }

    @Override
    public Double component12() {
        return getWeightedSsq();
    }

    @Override
    public Double component13() {
        return getLastValue();
    }

    @Override
    public Double component14() {
        return getLastWeight();
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
        return getStatCount();
    }

    @Override
    public Double value7() {
        return getAverage();
    }

    @Override
    public Double value8() {
        return getMinimum();
    }

    @Override
    public Double value9() {
        return getMaximum();
    }

    @Override
    public Double value10() {
        return getWeightedSum();
    }

    @Override
    public Double value11() {
        return getSumOfWeights();
    }

    @Override
    public Double value12() {
        return getWeightedSsq();
    }

    @Override
    public Double value13() {
        return getLastValue();
    }

    @Override
    public Double value14() {
        return getLastWeight();
    }

    @Override
    public WithinRepStatRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value2(Integer value) {
        setElementIdFk(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value3(Integer value) {
        setSimRunIdFk(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value4(Integer value) {
        setRepNum(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value5(String value) {
        setStatName(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value6(Double value) {
        setStatCount(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value7(Double value) {
        setAverage(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value8(Double value) {
        setMinimum(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value9(Double value) {
        setMaximum(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value10(Double value) {
        setWeightedSum(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value11(Double value) {
        setSumOfWeights(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value12(Double value) {
        setWeightedSsq(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value13(Double value) {
        setLastValue(value);
        return this;
    }

    @Override
    public WithinRepStatRecord value14(Double value) {
        setLastWeight(value);
        return this;
    }

    @Override
    public WithinRepStatRecord values(Integer value1, Integer value2, Integer value3, Integer value4, String value5, Double value6, Double value7, Double value8, Double value9, Double value10, Double value11, Double value12, Double value13, Double value14) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached WithinRepStatRecord
     */
    public WithinRepStatRecord() {
        super(WithinRepStat.WITHIN_REP_STAT);
    }

    /**
     * Create a detached, initialised WithinRepStatRecord
     */
    public WithinRepStatRecord(Integer id, Integer elementIdFk, Integer simRunIdFk, Integer repNum, String statName, Double statCount, Double average, Double minimum, Double maximum, Double weightedSum, Double sumOfWeights, Double weightedSsq, Double lastValue, Double lastWeight) {
        super(WithinRepStat.WITHIN_REP_STAT);

        setId(id);
        setElementIdFk(elementIdFk);
        setSimRunIdFk(simRunIdFk);
        setRepNum(repNum);
        setStatName(statName);
        setStatCount(statCount);
        setAverage(average);
        setMinimum(minimum);
        setMaximum(maximum);
        setWeightedSum(weightedSum);
        setSumOfWeights(sumOfWeights);
        setWeightedSsq(weightedSsq);
        setLastValue(lastValue);
        setLastWeight(lastWeight);
    }
}

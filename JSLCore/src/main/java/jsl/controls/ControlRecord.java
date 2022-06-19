package jsl.controls;

public class ControlRecord {
    String key;
    Object value;
    double lowerBound;
    double upperBound;
    String comment;
    ControlType controlType;

    public ControlRecord(Control<?> c) {
        this.key = c.getKey();
        this.value = c.getLastValue();
        this.comment = c.getComment();
        this.lowerBound = c.getLowerBound();
        this.upperBound = c.getUpperBound();
        this.controlType = c.getAnnotationType();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[key = ").append(key == null ? "[null]" : key);
        str.append(", control type = ").append(controlType == null ? "[null]" : controlType);
        str.append(", value = ").append(value == null ? "[null]" : value);
        str.append(", lower bound = ").append(lowerBound);
        str.append(", upper bound = ").append(upperBound);
        str.append(", comment = ").append(comment == null ? "[null]" : comment);
        str.append("]");
        return str.toString();
    }
}

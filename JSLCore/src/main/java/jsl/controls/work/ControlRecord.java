package jsl.controls.work;

public class ControlRecord {
    String key;
    Object value;
    String comment;

    public ControlRecord(Control<?> c) {
        this.key = c.getKey();
        this.value = c.getLastValue();
        this.comment = c.getComment();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[key = ").append(key == null ? "[null]" : key);
        str.append(", value = ").append(value == null ? "[null]" : value);
        str.append(", comment = ").append(comment == null ? "[null]" : comment);
        str.append("]");
        return str.toString();
    }
}

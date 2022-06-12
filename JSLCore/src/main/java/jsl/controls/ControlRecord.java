package jsl.controls;

public class ControlRecord {
    String key;
    Object value;
    String domain;
    String comment;

    public ControlRecord(ControlType<?> c) {
        this.key = c.getKey();
        this.value = c.getLastValue();
        this.domain = c.getDomainAsString();
        this.comment = c.getComment();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[key = ").append(key == null ? "[null]" : key);
        str.append(", value = ").append(value == null ? "[null]" : value);
        str.append(", domain = ").append(domain == null ? "[null]" : domain);
        str.append(", comment = ").append(comment == null ? "[null]" : comment);
        str.append("]");
        return str.toString();
    }
}

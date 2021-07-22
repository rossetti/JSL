package jslx.tabularfiles;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 *  A cell that holds text data.  A couple of convenience methods are provided for converting date time
 *  In general, see java.time for string conversion possibilities.
 */
public class TextCell extends Cell {

    private String value;

    public TextCell(){
        this(null);
    }

    public TextCell(String text) {
        super(DataType.TEXT);
        this.value = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String text) {
        this.value = text;
    }

    public void setText(ZonedDateTime zdt){
        if (zdt == null){
            this.value = null;
        } else {
            this.value = zdt.toString();
        }
    }

    public void setText(LocalDateTime ldt){
        if (ldt == null){
            this.value = null;
        } else {
            this.value = ldt.toString();
        }
    }
}

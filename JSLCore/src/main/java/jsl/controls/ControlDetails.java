package jsl.controls;

import java.util.ArrayList;

/**
 * class to represent a tabular definition of controls
 */
public class ControlDetails
        extends ArrayList<ControlDetailsRecord> {

    /**
     * override toString to get pretty parsed output (with nulls as [null])
     *
     * @return toString to get pretty parsed output (with nulls as [null])
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (this.size() == 0) str.append("{empty}");
        for (ControlDetailsRecord cdr : this) {
            str.append(cdr);
            str.append(System.lineSeparator());
        }
        return str.toString();
    }
}

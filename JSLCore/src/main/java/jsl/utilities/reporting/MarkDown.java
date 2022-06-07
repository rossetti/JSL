package jsl.utilities.reporting;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.NormalRV;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A class to facilitate construction of markdown elements
 */
public class MarkDown {

    public static final DecimalFormat D2FORMAT = new DecimalFormat("0.##");
    public static final DecimalFormat D3FORMAT = new DecimalFormat("0.###");

    enum ColFmt {
        LEFT(":---"),
        CENTER(":---:"),
        RIGHT("---:");

        ColFmt(String fmt) {
            this.fmt = fmt;
        }

        final String fmt;
    }

    static public String header(String header, int level) {
        if (level <= 0) {
            level = 1;
        }
        StringBuilder sb = new StringBuilder(header);
        sb.insert(0, " ");
        for (int i = 1; i <= level; i++) {
            sb.insert(0, "#");
        }
        return sb.toString();
    }

    static public String blockQuote(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.insert(0, "> ");
        return sb.toString();
    }

    static public String bold(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.insert(0, "**");
        sb.append("**");
        return sb.toString();
    }

    static public String italic(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.insert(0, "*");
        sb.append("*");
        return sb.toString();
    }

    static public String code(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.insert(0, "`");
        sb.append("`");
        return sb.toString();
    }

    static public String hRule() {
        StringBuilder sb = new StringBuilder();
        sb.append("___");
        sb.append(System.lineSeparator());
        return sb.toString();
    }


    static public String boldAndItalic(String text) {
        return italic(bold(text));
    }

    static public String nList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (String s : list) {
            sb.append(i);
            sb.append(". ");
            sb.append(s);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    static public String unList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append("- ");
            sb.append(s);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    static public String link(String linkText, String linkURL) {
        StringBuilder sb = new StringBuilder(linkText);
        sb.insert(0, "[");
        sb.append("]");
        sb.append("(");
        sb.append(linkURL);
        sb.append(")");
        return sb.toString();
    }

    static public String image(String altText, String imageURL) {
        StringBuilder sb = new StringBuilder(link(altText, imageURL));
        sb.insert(0, "!");
        return sb.toString();
    }

    static public List<ColFmt> allLeft(int nCols) {
        return allSame(nCols, ColFmt.LEFT);
    }

    static public List<ColFmt> allRight(int nCols) {
        return allSame(nCols, ColFmt.RIGHT);
    }

    static public List<ColFmt> allCentered(int nCols) {
        return allSame(nCols, ColFmt.CENTER);
    }

    static public List<ColFmt> allSame(int nCols, ColFmt format) {
        if (nCols <= 0) {
            throw new IllegalArgumentException("The number of column must be >= 1");
        }
        List<ColFmt> list = new ArrayList<>();
        for (int i = 1; i <= nCols; i++) {
            list.add(format);
        }
        return list;
    }

    static public String tableHeader(List<String> colHeaders) {
        return tableHeader(colHeaders, ColFmt.LEFT);
    }

    static public String tableHeader(List<String> colHeaders, ColFmt format) {
        Objects.requireNonNull(colHeaders, "The column headers list was null");
        return tableHeader(colHeaders, allSame(colHeaders.size(), format));
    }

    static public String tableHeader(List<String> colHeaders, List<ColFmt> formats) {
        Objects.requireNonNull(colHeaders, "The column headers list was null");
        Objects.requireNonNull(formats, "The column formats list was null");
        if (colHeaders.isEmpty()) {
            throw new IllegalArgumentException("The column headers list was empty");
        }
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("The column formats list was empty");
        }
        if (colHeaders.size() != formats.size()) {
            throw new IllegalArgumentException("The size of the header and format lists do not match");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        StringJoiner h1 = new StringJoiner("| ", "|", "|");
        for (String col : colHeaders) {
            h1.add(col);
        }
        sb.append(h1);
        sb.append(System.lineSeparator());
        StringJoiner h2 = new StringJoiner("| ", "|", "|");
        for (ColFmt col : formats) {
            h2.add(col.fmt);
        }
        sb.append(h2);
        return sb.toString();
    }

    public static String tableRow(List<String> elements) {
        Objects.requireNonNull(elements, "The row list was null");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("The row elements list was empty");
        }
        StringBuilder sb = new StringBuilder();
        StringJoiner h1 = new StringJoiner("| ", "|", "|");
        for (String col : elements) {
            h1.add(col);
        }
        sb.append(h1);
        return sb.toString();
    }

    public static String tableRow(double[] array) {
        return tableRow(null, array, D3FORMAT);
    }

    public static String tableRow(String rowLabel, double[] array) {
        return tableRow(rowLabel, array, D3FORMAT);
    }

    public static String tableRow(String rowLabel, double[] array, DecimalFormat df) {
        if (rowLabel == null) {
            return tableRow(Arrays.asList(JSLArrayUtil.toString(array, df)));
        } else {
            String[] data = JSLArrayUtil.toString(array, df);
            List<String> list = new ArrayList<>();
            list.add(rowLabel);
            list.addAll(Arrays.asList(data));
            return tableRow(list);
        }
    }

    public static class Table {

        private final StringBuilder sbTable = new StringBuilder();
        private final int numCols;

        public Table(List<String> colHeaders) {
            this(colHeaders, allSame(colHeaders.size(), ColFmt.LEFT));
        }

        public Table(List<String> colHeaders, ColFmt format) {
            this(colHeaders, allSame(colHeaders.size(), format));
        }

        public Table(List<String> colHeaders, List<ColFmt> formats) {
            Objects.requireNonNull(colHeaders, "The column headers list was null");
            Objects.requireNonNull(formats, "The column formats list was null");
            if (colHeaders.isEmpty()) {
                throw new IllegalArgumentException("The column headers list was empty");
            }
            if (formats.isEmpty()) {
                throw new IllegalArgumentException("The column formats list was empty");
            }
            if (colHeaders.size() != formats.size()) {
                throw new IllegalArgumentException("The size of the header and format lists do not match");
            }
            numCols = colHeaders.size();
            sbTable.append(tableHeader(colHeaders, formats));
            sbTable.append(System.lineSeparator());
        }

        public Table addRow(double[] data) {
            return addRow(null, data, D3FORMAT);
        }

        public Table addRow(String rowLabel, double[] data, DecimalFormat df) {
            Objects.requireNonNull(data, "The data for the row was null");
            if (rowLabel == null) {
                if (data.length != numCols) {
                    throw new IllegalArgumentException("The size of the array does not match the number of columns");
                }
            } else {
                if (data.length != (numCols - 1)) {
                    throw new IllegalArgumentException("The size of the array does not match the number of columns");
                }
            }

            sbTable.append(tableRow(rowLabel, data, df));
            sbTable.append(System.lineSeparator());
            return this;
        }

        public void addRows(double[][] data) {
            addRows(data, D3FORMAT);
        }

        public void addRows(double[][] data, DecimalFormat df) {
            Objects.requireNonNull(data, "The data for the row was null");
            if (data.length == 0) {
                return;
            }
            for (double[] array : data) {
                addRow(null, array, df);
            }
        }

        public void addRows(List<double[]> rows) {
            addRows(rows, D3FORMAT);
        }

        public void addRows(List<double[]> rows, DecimalFormat df) {
            Objects.requireNonNull(rows, "The data for the row was null");
            if (rows.isEmpty()) {
                return;
            }
            for (double[] array : rows) {
                addRow(null, array, df);
            }
        }

        @Override
        public String toString() {
            return sbTable.toString();
        }
    }

    public static void main(String[] args) {
        String s = header("manuel", 2);
        System.out.println(s);
        String b = bold("manuel");
        System.out.println(b);

        List<String> header = Arrays.asList("x", "y", "z");
        String h = tableHeader(header);
        System.out.println(h);

        NormalRV n = new NormalRV();
        Table t = new Table(header);

        for (int i = 1; i <= 10; i++) {
            t.addRow(n.sample(3));
        }

        System.out.println(t);

    }
}

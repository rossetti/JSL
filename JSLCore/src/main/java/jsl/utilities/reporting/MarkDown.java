package jsl.utilities.reporting;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.NormalRV;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A class to facilitate construction of markdown elements
 */
public class MarkDown {

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

    public static String tableRow(double[] array){
        return tableRow(array, null);
    }

    public static String tableRow(double[] array, DecimalFormat df){
        return tableRow(Arrays.asList(JSLArrayUtil.toString(array, df )));
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
        double[] z1 = n.sample(3);
        double[] z2 = n.sample(3);
        System.out.println(tableRow(z1));
        System.out.println(tableRow(z2));
    }
}

package jsl.utilities.reporting;

import jsl.utilities.JSLArrayUtil;

import static java.lang.String.format;

public final class TableFormatter {

    private static final char BORDER_KNOT = '+';
    private static final char HORIZONTAL_BORDER = '-';
    private static final char VERTICAL_BORDER = '|';

    private static final CellFormatter<Object> DEFAULT = new CellFormatter<Object>() {
        @Override
        public String asString(Object obj) {
            return obj.toString();
        }
    };

    private static final String DEFAULT_AS_NULL = "(NULL)";

    public static String asString(double[][] table){
        return asString(JSLArrayUtil.toDouble(table));
    }

    public static String asString(int[][] table){
        return asString(JSLArrayUtil.toInteger(table));
    }

    public static String asString(Object[][] table) {
        return asString(table, DEFAULT);
    }

    public static <T> String asString(T[][] table, CellFormatter<T> cellFormatter) {
        if ( table == null ) {
            throw new IllegalArgumentException("No tabular data provided");
        }
        if ( table.length == 0 ) {
            return "";
        }
        if( cellFormatter == null ) {
            throw new IllegalArgumentException("No instance of Printer provided");
        }
        final int[] widths = new int[getMaxColumns(table)];
        adjustColumnWidths(table, widths, cellFormatter);
        return convertTable(table, widths, getHorizontalBorder(widths), cellFormatter);
    }

    private static <T> String convertTable(T[][] table, int[] widths, String horizontalBorder, CellFormatter<T> cellFormatter) {
        final int lineLength = horizontalBorder.length();
        StringBuilder sb = new StringBuilder();
        sb.append(horizontalBorder);
        sb.append(System.lineSeparator());
        for ( final T[] row : table ) {
            if ( row != null ) {
                sb.append(getRow(row, widths, lineLength, cellFormatter));
                sb.append(System.lineSeparator());
                sb.append(horizontalBorder);
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private static <T> String getRow(T[] row, int[] widths, int lineLength, CellFormatter<T> cellFormatter) {
        final StringBuilder builder = new StringBuilder(lineLength).append(VERTICAL_BORDER);
        final int maxWidths = widths.length;
        for ( int i = 0; i < maxWidths; i++ ) {
            builder.append(padRight(getCellValue(safeGet(row, i, cellFormatter), cellFormatter), widths[i])).append(VERTICAL_BORDER);
        }
        return builder.toString();
    }

    private static String getHorizontalBorder(int[] widths) {
        final StringBuilder builder = new StringBuilder(256);
        builder.append(BORDER_KNOT);
        for ( final int w : widths ) {
            for ( int i = 0; i < w; i++ ) {
                builder.append(HORIZONTAL_BORDER);
            }
            builder.append(BORDER_KNOT);
        }
        return builder.toString();
    }

    private static int getMaxColumns(Object[][] rows) {
        int max = 0;
        for ( final Object[] row : rows ) {
            if ( row != null && row.length > max ) {
                max = row.length;
            }
        }
        return max;
    }

    private static <T> void adjustColumnWidths(T[][] rows, int[] widths, CellFormatter<T> cellFormatter) {
        for ( final T[] row : rows ) {
            if ( row != null ) {
                for ( int c = 0; c < widths.length; c++ ) {
                    final String cv = getCellValue(safeGet(row, c, cellFormatter), cellFormatter);
                    final int l = cv.length();
                    if ( widths[c] < l ) {
                        widths[c] = l;
                    }
                }
            }
        }
    }

    private static <T> String padRight(String s, int n) {
        return format("%1$-" + n + "s", s);
    }

    private static <T> T safeGet(T[] array, int index, CellFormatter<T> cellFormatter) {
        return index < array.length ? array[index] : null;
    }

    private static <T> String getCellValue(T value, CellFormatter<T> cellFormatter) {
        return value == null ? DEFAULT_AS_NULL : cellFormatter.asString(value);
    }

}

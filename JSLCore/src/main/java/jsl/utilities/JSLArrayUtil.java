package jsl.utilities;

import jsl.utilities.reporting.JSL;
import jsl.utilities.statistic.Statistic;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class has some array manipulation methods that I have found useful over the years.
 * Other libraries (e.g. guava and apache commons) also have array utilities which you might find useful.
 */
public class JSLArrayUtil {

    private JSLArrayUtil() {
    }

    /**
     * Returns the index associated with the minimum element in the array For
     * ties, this returns the first found.
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the minimum element
     */
    public static int getIndexOfMin(double[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the minimum value in the array
     */
    public static double getMin(double[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMin(x)];
    }

    /**
     * Returns the index associated with the maximum element in the array For
     * ties, this returns the first found.
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the maximum element
     */
    public static int getIndexOfMax(double[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the maximum value in the array
     */
    public static double getMax(double[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMax(x)];
    }

    /**
     * Returns the index associated with the minimum element in the array For
     * ties, this returns the first found.
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the minimum element
     */
    public static int getIndexOfMin(int[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the minimum value in the array
     */
    public static int getMin(int[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMin(x)];
    }

    /**
     * Returns the index associated with the maximum element in the array For
     * ties, this returns the first found
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the maximum element
     */
    public static int getIndexOfMax(int[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the maximum value in the array
     */
    public static int getMax(int[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMax(x)];
    }

    /**
     * Returns the index associated with the minimum element in the array For
     * ties, this returns the first found
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the minimum element
     */
    public static int getIndexOfMin(long[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the minimum value in the array
     */
    public static long getMin(long[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMin(x)];
    }

    /**
     * Returns the index associated with the maximum element in the array For
     * ties, this returns the first found
     *
     * @param x the array to search, must not be null or empty
     * @return the index associated with the maximum element
     */
    public static int getIndexOfMax(long[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        int index = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array to search, must not be null or empty
     * @return the maximum value in the array
     */
    public static long getMax(long[] x) {
        Objects.requireNonNull(x, "The array was null");
        if (x.length == 0) {
            throw new IllegalArgumentException("The array was empty");
        }
        return x[getIndexOfMax(x)];
    }

    /**
     * @param array the array to operate on
     * @return getMax() - getMin()
     */
    public static double getRange(double[] array) {
        double max = getMax(array);
        double min = getMin(array);
        return max - min;
    }

    /**
     * If the array is empty or null, -1 is returned.
     *
     * @param element the element to search for
     * @param array   the array to search in
     * @return the index of the first occurrence of the element
     */
    public static int findIndex(int element, int[] array) {
        // if array is Null
        if (array == null) {
            return -1;
        }
        // find length of array
        int len = array.length;
        int i = 0;
        // traverse in the array
        while (i < len) {
            // if the i-th element is t
            // then return the index
            if (array[i] == element) {
                return i;
            } else {
                i = i + 1;
            }
        }
        return -1;
    }

    /**
     * @param array the array to check
     * @return true if the array as at least one zero element
     */
    public static boolean hasZero(int[] array) {
        return findIndex(0, array) >= 0;
    }

    /**
     * If the array is empty or null, -1 is returned.
     *
     * @param element the element to search for
     * @param array   the array to search in
     * @return the index of the first occurrence of the element
     */
    public static int findIndex(double element, double[] array) {
        // if array is Null
        if (array == null) {
            return -1;
        }
        // find length of array
        int len = array.length;
        int i = 0;
        // traverse in the array
        while (i < len) {
            // if the i-th element is t
            // then return the index
            if (array[i] == element) {
                return i;
            } else {
                i = i + 1;
            }
        }
        return -1;
    }

    /**
     * @param array the array to check
     * @return true if the array as at least one zero element
     */
    public static boolean hasZero(double[] array) {
        return findIndex(0.0, array) >= 0;
    }

    /**
     * If the array is empty or null, -1 is returned.
     *
     * @param element the element to search for
     * @param array   the array to search in
     * @return the index of the first occurrence of the element
     */
    public static int findIndex(String element, String[] array) {
        // if array is Null
        if (array == null) {
            return -1;
        }
        // find length of array
        int len = array.length;
        int i = 0;
        // traverse in the array
        while (i < len) {
            // if the i-th element is t
            // then return the index
            if (array[i].equals(element)) {
                return i;
            } else {
                i = i + 1;
            }
        }
        return -1;
    }

    /**
     * Returns a new array that has been scaled so that the values are between
     * the minimum and maximum values of the supplied array
     *
     * @param array the array to scale, must not be null
     * @return the scaled array
     */
    public static double[] getMinMaxScaledArray(double[] array) {
        Objects.requireNonNull(array, "The array cannot be null");
        double max = getMax(array);
        double min = getMin(array);
        double range = max - min;
        if (range == 0.0){
            throw new IllegalArgumentException("The array cannot be scaled because the min == max!");
        }
        double[] x = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            x[i] = (array[i] - min) / range;
        }
        return x;
    }

    /**
     * Returns a new array that has been scaled so that the values are
     * the (x - avg)/sd values of the supplied array
     *
     * @param array the array to scale, must not be null
     * @return the scaled array
     */
    public static double[] getNormScaledArray(double[] array) {
        Objects.requireNonNull(array, "The array cannot be null");
        Statistic s = new Statistic(array);
        double avg = s.getAverage();
        double sd = s.getStandardDeviation();
        if (sd == 0.0){
            throw new IllegalArgumentException("The array cannot be scaled because std dev == 0.0");
        }
        double[] x = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            x[i] = (array[i] - avg) / sd;
        }
        return x;
    }

    /**
     * Copies all but element index of array fromA into array toB
     *
     * @param index index of element to leave out, must be 0 to fromA.length-1
     * @param fromA array to copy from, must not be null
     * @return a reference to the array toB
     */
    public static double[] copyWithout(int index, double[] fromA) {
        return copyWithout(index, fromA, new double[fromA.length - 1]);
    }

    /**
     * Copies all but element index of array fromA into array toB
     *
     * @param index index of element to leave out, must be 0 to fromA.length-1
     * @param fromA array to copy from, must not be null
     * @param toB   array to copy to, must be length fromA.length - 1
     * @return a reference to the array toB
     */
    public static double[] copyWithout(int index, double[] fromA, double[] toB) {
        if (index < 0) {
            throw new IllegalArgumentException("The index must be >= 0");
        }
        if (fromA == null) {
            throw new IllegalArgumentException("The fromA array was null.");
        }
        if (index > fromA.length - 1) {
            throw new IllegalArgumentException("The index must be <= fromA.length-1");
        }
        if (toB == null) {
            throw new IllegalArgumentException("The toB array was null.");
        }
        if (toB.length != fromA.length - 1) {
            throw new IllegalArgumentException("The length of toB was not fromA.length - 1");
        }
        if (fromA.length == 1) {
            return toB;
        }
        int k = 0;
        for (int j = 0; j < fromA.length; j++) {
            if (j != index) {
                toB[k] = fromA[j];
                k++;
            }
        }
        return toB;
    }

    /**
     * @param a the array to add the constant to
     * @param c the constant to add to each element
     * @return the transformed array
     */
    public static double[] addConstant(double[] a, double c) {
        if (a == null) {
            throw new IllegalArgumentException("The array was null.");
        }
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i] + c;
        }
        return a;
    }

    /**
     * @param a the array to add the constant to
     * @param c the constant to subtract from each element
     * @return the transformed array
     */
    public static double[] subtractConstant(double[] a, double c) {
        return addConstant(a, -c);
    }

    /**
     * @param a the array to multiply the constant by
     * @param c the constant to multiply against each element
     * @return the transformed array
     */
    public static double[] multiplyConstant(double[] a, double c) {
        if (a == null) {
            throw new IllegalArgumentException("The array was null.");
        }
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i] * c;
        }
        return a;
    }

    /**
     * @param a the array to divide the constant by
     * @param c the constant to divide each element, cannot be zero
     * @return the transformed array
     */
    public static double[] divideConstant(double[] a, double c) {
        if (c == 0.0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return multiplyConstant(a, 1.0 / c);
    }

    /**
     * Multiplies the arrays element by element. Arrays must have same length and must not be null.
     *
     * @param a the first array
     * @param b the second array
     * @return the array containing a[i]*b[i]
     */
    public static double[] multiplyElements(double[] a, double[] b) {
        Objects.requireNonNull(a, "Array a was null");
        Objects.requireNonNull(b, "Array b was null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("The array lengths must match");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] * b[i];
        }
        return c;
    }

    /**
     * Divides the arrays element by element. Arrays must have same length and must not be null.
     *
     * @param a the first array
     * @param b the second array, must not have any zero elements
     * @return the array containing a[i]/b[i]
     */
    public static double[] divideElements(double[] a, double[] b) {
        Objects.requireNonNull(a, "Array a was null");
        Objects.requireNonNull(b, "Array b was null");
        if (hasZero(b)) {
            throw new IllegalArgumentException("The divisor array has at least one element that is 0.0");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("The array lengths must match");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] / b[i];
        }
        return c;
    }


    /**
     * Assumes that the array can be ragged. Returns the number of columns
     * necessary that would cause the array to not be ragged. In other words,
     * the minimum number of columns to make the array a unragged array (matrix) where
     * all row arrays have the same number of elements.
     *
     * @param array the array to check, must not be null
     * @return the minimum number of columns in the array
     */
    public static int getMinNumColumns(double[][] array) {
        Objects.requireNonNull(array, "The array was null");
        int min = Integer.MAX_VALUE;
        for (int row = 0; row < array.length; row++) {
            if (array[row].length < min) {
                min = array[row].length;
            }
        }
        return min;
    }

    /**
     * Copies the supplied array by trimming to the minimum number of columns of the
     * supplied (potentially ragged) array so that the returned array is rectangular,
     * where all row arrays have the same number of elements (columns)
     *
     * @param array the array to copy
     * @return the copy
     */
    public static double[][] trimToRectangular(double[][] array) {
        Objects.requireNonNull(array, "The array was null");
        int rows = array.length;
        int cols = getMinNumColumns(array);
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = array[i][j];
            }
        }
        return matrix;
    }

    /**
     * Copies the supplied array by expanding to the maximum number of columns of the
     * supplied (potentially ragged) array so that the returned array is rectangular,
     * where all row arrays have the same number of elements (columns).
     * <p>
     * The expanded elements will be filled with 0.0
     *
     * @param array the array to copy
     * @return the copy
     */
    public static double[][] expandToRectangular(double[][] array) {
        return expandToRectangular(array, 0.0);
    }

    /**
     * Copies the supplied array by expanding to the maximum number of columns of the
     * supplied (ragged) array so that the returned array is rectangular,
     * where all row arrays have the same number of elements (columns).
     * <p>
     * The expanded elements will be filled with the supplied fill value
     *
     * @param array the array to copy
     * @return the copy
     */
    public static double[][] expandToRectangular(double[][] array, double fillValue) {
        Objects.requireNonNull(array, "The array was null");
        int rows = array.length;
        int cols = getMaxNumColumns(array);
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j > array[i].length) {
                    matrix[i][j] = fillValue;
                } else {
                    matrix[i][j] = array[i][j];
                }
            }
        }
        return matrix;
    }

    /**
     * An array is rectangular if all rows have the same number of elements (columns).
     *
     * @param array the array to check
     * @return true if the array is rectangular
     */
    public static <T> boolean isRectangular(T[][] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false; // no rows can't be rectangular
        }
        int nc = array[0].length; // number of columns in first row, all rows must have this
        for (int i = 0; i < array.length; i++) {
            if (array[i].length != nc) {
                return false;
            }
        }
        return true;
    }

    /**
     * An array is rectangular if all rows have the same number of elements (columns).
     *
     * @param array the array to check
     * @return true if the array is rectangular
     */
    public static boolean isRectangular(double[][] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false; // no rows can't be rectangular
        }
        int nc = array[0].length; // number of columns in first row, all rows must have this
        for (int i = 0; i < array.length; i++) {
            if (array[i].length != nc) {
                return false;
            }
        }
        return true;
    }

    /**
     * An array is rectangular if all rows have the same number of elements (columns).
     *
     * @param array the array to check
     * @return true if the array is rectangular
     */
    public static boolean isRectangular(int[][] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false; // no rows can't be rectangular
        }
        int nc = array[0].length; // number of columns in first row, all rows must have this
        for (int i = 0; i < array.length; i++) {
            if (array[i].length != nc) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assumes that the array can be ragged. Returns the number of elements in
     * the row array that has the most elements.
     *
     * @param array the array to check, must not be null
     * @return the minimum number of columns in the array
     */
    public static int getMaxNumColumns(double[][] array) {
        Objects.requireNonNull(array, "The array was null");
        int max = Integer.MIN_VALUE;
        for (int row = 0; row < array.length; row++) {
            if (array[row].length > max) {
                max = array[row].length;
            }
        }
        return max;
    }

    /**
     * @param k      the kth column to be extracted (zero based indexing)
     * @param matrix must not be null, assumed 2D rectangular array (i.e. all rows have the same number of columns)
     * @return a copy of the extracted column
     */
    public static double[] getColumn(int k, double[][] matrix) {
        Objects.requireNonNull(matrix, "The matrix was null");
        if (!isRectangular(matrix)) {
            throw new IllegalArgumentException("The matrix was not rectangular");
        }
        double[] column = new double[matrix.length]; // Here I assume a rectangular 2D array!
        for (int i = 0; i < column.length; i++) {
            column[i] = matrix[i][k];
        }
        return column;
    }

    /**
     * @param k      the kth column to be extracted (zero based indexing)
     * @param matrix must not be null, assumed 2D rectangular array (i.e. all rows have the same number of columns)
     * @return a copy of the extracted column
     */
    public static int[] getColumn(int k, int[][] matrix) {
        Objects.requireNonNull(matrix, "The matrix was null");
        if (!isRectangular(matrix)) {
            throw new IllegalArgumentException("The matrix was not rectangular");
        }
        int[] column = new int[matrix.length]; // Here I assume a rectangular 2D array!
        for (int i = 0; i < column.length; i++) {
            column[i] = matrix[i][k];
        }
        return column;
    }

    /**
     * @param index  the column to be extracted (zero based indexing)
     * @param matrix must not be null, assumed 2D rectangular array (i.e. all rows have the same number of columns)
     * @return a copy of the extracted column
     */
    public static Object[] getColumn(int index, Object[][] matrix) {
        Objects.requireNonNull(matrix, "The matrix was null");
        if (!isRectangular(matrix)) {
            throw new IllegalArgumentException("The matrix was not rectangular");
        }
        Object[] column = new Object[matrix.length]; // Here I assume a rectangular 2D array!
        for (int i = 0; i < column.length; i++) {
            column[i] = matrix[i][index];
        }
        return column;
    }

    /**
     * A convenience method for copying the entire array
     *
     * @param src the array to copy
     * @return the copy
     */
    public static double[] copyOf(double[] src) {
        Objects.requireNonNull(src, "The source array was null");
        if (src.length == 0) {
            return new double[0];
        }
        return Arrays.copyOf(src, src.length);
    }

    /**
     * A convenience method for copying the entire array
     *
     * @param src the array to copy
     * @return the copy
     */
    public static int[] copyOf(int[] src) {
        Objects.requireNonNull(src, "The source array was null");
        if (src.length == 0) {
            return new int[0];
        }
        return Arrays.copyOf(src, src.length);
    }

    /**
     * @param src the source array to copy
     * @return a copy of the array
     */
    public static double[][] copy2DArray(double[][] src) {
        Objects.requireNonNull(src, "The source array was null");
        if (src.length == 0) {
            return new double[0][0];
        }
        double[][] target = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            target[i] = copyOf(src[i]);
        }
        return target;
    }

    /**
     * @param src the source array to copy
     * @return a copy of the array
     */
    public static int[][] copy2DArray(int[][] src) {
        Objects.requireNonNull(src, "The source array was null");
        if (src.length == 0) {
            return new int[0][0];
        }
        int[][] target = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            target[i] = copyOf(src[i]);
        }
        return target;
    }

    /**
     *
     * @param array the array to fill, must not be null
     * @param value the supplier of the value, must not be null
     */
    public static void fill(double[] array, GetValueIfc value){
        Objects.requireNonNull(array, "The array was null");
        Objects.requireNonNull(value, "The value source was null");
        for (int i = 0; i < array.length; i++) {
            array[i] = value.getValue();
        }
    }

    /**
     *
     * @param array the array to fill, must not be null
     * @param value the supplier of the value, must not be null
     */
    public static void fill(double[][] array, GetValueIfc value){
        Objects.requireNonNull(array, "The array was null");
        Objects.requireNonNull(value, "The value source was null");
        for (double[] doubles : array) {
            fill(doubles, value);
        }
    }

    /**
     * The destination array is mutated by this method
     *
     * @param col  the column in the destination to fill
     * @param src  the source for filling the column, must not be null
     * @param dest the destination array, assumed to be rectangular, must not be null
     */
    public static void fillColumn(int col, double[] src, double[][] dest) {
        Objects.requireNonNull(src, "The source array was null");
        Objects.requireNonNull(dest, "The destination array was null");
        if (dest.length != src.length) {
            throw new IllegalArgumentException("The source array length and destination array must have the same number of rows");
        }
        if (!isRectangular(dest)) {
            throw new IllegalArgumentException("The matrix was not rectangular");
        }
        for (int i = 0; i < src.length; i++) {
            dest[i][col] = src[i];
        }
    }

    /**
     * The array must not be null
     *
     * @param array the input array
     * @return the sum of the squares of the elements of the array
     */
    public static double getSumSquares(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        double sum = 0.0;
        for (double v : array) {
            sum = sum + v * v;
        }
        return sum;
    }

    /**
     * The array must have non-negative elements and not be null
     *
     * @param array the input array
     * @return the sum of the square roots of the elements of the array
     */
    public static double getSumSquareRoots(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        double sum = 0.0;
        for (double v : array) {
            sum = sum + Math.sqrt(v);
        }
        return sum;
    }

    /**
     * Adds the arrays element by element. Arrays must have same length and must not be null.
     *
     * @param a the first array
     * @param b the second array
     * @return the array containing a[i]+b[i]
     */
    public static double[] addElements(double[] a, double[] b) {
        Objects.requireNonNull(a, "Array a was null");
        Objects.requireNonNull(b, "Array b was null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("The array lengths must match");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    /**
     * Adds the arrays element by element. Arrays must have same length and must not be null.
     *
     * @param a the first array
     * @param b the second array
     * @return the array containing a[i]-b[i]
     */
    public static double[] subtractElements(double[] a, double[] b) {
        Objects.requireNonNull(a, "Array a was null");
        Objects.requireNonNull(b, "Array b was null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("The array lengths must match");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] - b[i];
        }
        return c;
    }

    /**
     * Returns a list of the elements that are of the same type as the target
     * class.
     * Usage: getElements(objects, String.class);
     *
     * @param <T>         the type of the element to search for
     * @param objects     the list that can hold anything
     * @param targetClass the class type to find in the list, should be same as
     *                    T
     * @return a list that holds the items of the targetClass
     */
    public static <T> List<T> getElements(List objects, Class<T> targetClass) {

        List<T> stuff = new ArrayList<>();

        for (Object obj : objects) {
            if (targetClass.isInstance(obj)) {
                @SuppressWarnings("unchecked")
                T temp = (T) obj;
                stuff.add(temp);
            }
        }
        return stuff;
    }

    /**
     * Returns a count of the elements that are of the same type as the target
     * class.
     *
     * @param objects     the list that can hold anything
     * @param targetClass the class type to find in the list, should be same as
     *                    T
     * @return a list that holds the items of the targetClass
     */
    public static int countElements(List objects, Class targetClass) {
        int n = 0;
        for (Object obj : objects) {
            if (targetClass.isInstance(obj)) {
                n++;
            }
        }
        return n;
    }

    /**
     * @param first  the first array
     * @param second the second array
     * @return true if all elements are equal
     */
    public static boolean compareArrays(double[] first, double[] second) {
        Objects.requireNonNull(first, "The first array was null");
        Objects.requireNonNull(second, "The second array was null");
        if (first.length != second.length) {
            return false;
        }
        boolean flag = true;
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }
        return flag;
    }

    /**
     * Converts any null values to zero
     *
     * @param array the array to copy
     * @return the primitive array
     */
    public static double[] toPrimitive(Double[] array) {
        if (array == null) {
            return new double[0];
        }
        if (array.length == 0) {
            return new double[0];
        }
        double[] target = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = Optional.ofNullable(array[i].doubleValue()).orElse(0.0);
        }
        return target;
    }

    /**
     * Converts any nulls to zero
     *
     * @param doubleList the list to convert
     * @return the primitive array
     */
    public static double[] toPrimitiveDouble(List<Double> doubleList) {
        if (doubleList == null) {
            return new double[0];
        }
        if (doubleList.isEmpty()) {
            return new double[0];
        }
        Double[] da = new Double[doubleList.size()];
        doubleList.toArray(da);
        return toPrimitive(doubleList.toArray(da));
    }

    /**
     * Converts any null values to zero
     *
     * @param array the array to copy
     * @return the primitive array
     */
    public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return new int[0];
        }
        if (array.length == 0) {
            return new int[0];
        }
        int[] target = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = Optional.ofNullable(array[i].intValue()).orElse(0);
        }
        return target;
    }

    /**
     * Converts any nulls to zero
     *
     * @param List the list to convert
     * @return the primitive array
     */
    public static int[] toPrimitiveInteger(List<Integer> List) {
        if (List == null) {
            return new int[0];
        }
        if (List.isEmpty()) {
            return new int[0];
        }
        Integer[] da = new Integer[List.size()];
        List.toArray(da);
        return toPrimitive(List.toArray(da));
    }

    /**
     * Converts any null values to zero
     *
     * @param array the array to copy
     * @return the primitive array
     */
    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return new long[0];
        }
        if (array.length == 0) {
            return new long[0];
        }
        long[] target = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = Optional.ofNullable(array[i].longValue()).orElse(0L);
        }
        return target;
    }

    /**
     * Converts any nulls to zero
     *
     * @param List the list to convert
     * @return the primitive array
     */
    public static long[] toPrimitiveLong(List<Long> List) {
        if (List == null) {
            return new long[0];
        }
        if (List.isEmpty()) {
            return new long[0];
        }
        Long[] da = new Long[List.size()];
        List.toArray(da);
        return toPrimitive(List.toArray(da));
    }

    /**
     * Convert the array of double to an array of strings with each element the
     * corresponding value
     *
     * @param array the array of doubles
     * @return the array of strings representing the values of the doubles
     */
    public static String[] toString(double[] array) {
        if (array == null) {
            return new String[0];
        }
        if (array.length == 0) {
            return new String[0];
        }
        String[] target = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = String.valueOf(array[i]);
        }
        return target;
    }

    /**
     * @param array the array to convert
     * @return a comma delimited string of the array, if empty or null, returns the empty string
     */
    public static String toCSVString(String[] array) {
        if (array == null) {
            return "";
        }
        if (array.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < array.length; i++) {
            joiner.add(array[i]);
        }
        return joiner.toString();
    }

    /**
     * @param array the array to convert
     * @return a comma delimited string of the array, if empty or null, returns the empty string
     */
    public static String toCSVString(double[] array) {
        return toCSVString(array, null);
    }

    /**
     * @param format a format to apply to the values of the array when writing the strings
     * @param array the array to convert
     * @return a comma delimited string of the array, if empty or null, returns the empty string
     */
    public static String toCSVString(double[] array, DecimalFormat format) {
        if (array == null) {
            return "";
        }
        if (array.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < array.length; i++) {
            if (format == null){
                joiner.add(Double.toString(array[i]));
            } else {
                joiner.add(format.format(array[i]));
            }
        }
        return joiner.toString();
    }

    /**
     * @param array the array to convert
     * @return a comma delimited string of the array, if empty or null, returns the empty string
     */
    public static String toCSVString(int[] array) {
        if (array == null) {
            return "";
        }
        if (array.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < array.length; i++) {
            joiner.add(Integer.toString(array[i]));
        }
        return joiner.toString();
    }

    /**
     * @param array the array to convert
     * @return a comma delimited string of the array, if empty or null, returns the empty string
     */
    public static String toCSVString(long[] array) {
        if (array == null) {
            return "";
        }
        if (array.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < array.length; i++) {
            joiner.add(Long.toString(array[i]));
        }
        return joiner.toString();
    }

    /**
     * Convert the array of double to an array of Double with each element the
     * corresponding value
     *
     * @param array the array of doubles
     * @return the array of Doubles representing the values of the doubles
     */
    public static Double[] toDouble(double[] array) {
        if (array == null) {
            return new Double[0];
        }
        if (array.length == 0) {
            return new Double[0];
        }
        Double[] target = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i];
        }
        return target;
    }

    /**
     * Convert the array of int to an array of double with each element the
     * corresponding value
     *
     * @param array the array of ints
     * @return the array of doubles representing the values of the ints
     */
    public static double[] toDouble(int[] array) {
        if (array == null) {
            return new double[0];
        }
        if (array.length == 0) {
            return new double[0];
        }
        double[] target = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i];
        }
        return target;
    }

    /**
     * Convert the array of int to an array of double with each element the
     * corresponding value
     *
     * @param array the array of ints
     * @return the array of doubles representing the values of the ints
     */
    public static double[] toDouble(Integer[] array) {
        if (array == null) {
            return new double[0];
        }
        if (array.length == 0) {
            return new double[0];
        }
        double[] target = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i].doubleValue();
        }
        return target;
    }

    /**
     * Convert the array of long to an array of double with each element the
     * corresponding value
     *
     * @param array the array of longs
     * @return the array of doubles representing the values of the longs
     */
    public static double[] toDouble(long[] array) {
        if (array == null) {
            return new double[0];
        }
        if (array.length == 0) {
            return new double[0];
        }
        double[] target = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i];
        }
        return target;
    }

    /**
     * Convert the array of long to an array of double with each element the
     * corresponding value
     *
     * @param array the array of longs
     * @return the array of doubles representing the values of the longs
     */
    public static double[] toDouble(Long[] array) {
        if (array == null) {
            return new double[0];
        }
        if (array.length == 0) {
            return new double[0];
        }
        double[] target = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i].doubleValue();
        }
        return target;
    }

    /**
     * Convert the 2D array of double to a 2D array of Double with each element the
     * corresponding value
     *
     * @param array the array of doubles
     * @return the array of Doubles representing the values of the doubles
     */
    public static Double[][] toDouble(double[][] array) {
        if (array == null) {
            return new Double[0][0];
        }
        if (array.length == 0) {
            return new Double[0][0];
        }
        Double[][] target = new Double[array.length][];
        for (int i = 0; i < array.length; i++) {
            target[i] = toDouble(array[i]);
        }
        return target;
    }

    /**
     * Convert the array of int to an array of Intger with each element the
     * corresponding value
     *
     * @param array the array of ints
     * @return the array of Integers representing the values of the ints
     */
    public static Integer[] toInteger(int[] array) {
        if (array == null) {
            return new Integer[0];
        }
        if (array.length == 0) {
            return new Integer[0];
        }
        Integer[] target = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            target[i] = array[i];
        }
        return target;
    }

    /**
     * Convert the 2D array of int to a 2D array of Integer with each element the
     * corresponding value
     *
     * @param array the array of int
     * @return the array of Integer representing the values of the int
     */
    public static Integer[][] toInteger(int[][] array) {
        if (array == null) {
            return new Integer[0][0];
        }
        if (array.length == 0) {
            return new Integer[0][0];
        }
        Integer[][] target = new Integer[array.length][];
        for (int i = 0; i < array.length; i++) {
            target[i] = toInteger(array[i]);
        }
        return target;
    }

    /**
     * If the string cannot be converted to a number then the array value is assigned Double.NaN
     *
     * @param dblStrings an array of strings that represent doubles
     * @return the parsed doubles as an array
     */
    public static double[] parseDouble(String[] dblStrings) {
        if (dblStrings == null) {
            return new double[0];
        }
        if (dblStrings.length == 0) {
            return new double[0];
        }
        double[] target = new double[dblStrings.length];
        for (int i = 0; i < dblStrings.length; i++) {
            if (dblStrings[i] == null) {
                target[i] = Double.NaN;
            } else {
                try {
                    target[i] = Double.parseDouble(dblStrings[i]);
                } catch (NumberFormatException e) {
                    target[i] = Double.NaN;
                }
            }
        }
        return target;
    }

    /**
     * Transposes the array returned transpose[x][y] = array[y][x]
     *
     * @param array an array with m rows and n columns
     * @return an array with n columns and m rows
     */
    public static int[][] transpose2DArray(int[][] array) {
        Objects.requireNonNull(array, "The array was null");
        if (!isRectangular(array)) {
            throw new IllegalArgumentException("The array was not rectangular");
        }
        int m = array.length;
        int n = array[0].length;
        int[][] transpose = new int[n][m];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                transpose[x][y] = array[y][x];
            }
        }
        return transpose;
    }

    /**
     * Transposes the array returned transpose[x][y] = array[y][x]
     *
     * @param array an array with m rows and n columns
     * @return an array with n columns and m rows
     */
    public static double[][] transpose2DArray(double[][] array) {
        Objects.requireNonNull(array, "The array was null");
        if (!isRectangular(array)) {
            throw new IllegalArgumentException("The array was not rectangular");
        }
        int m = array.length;
        int n = array[0].length;
        double[][] transpose = new double[n][m];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                transpose[x][y] = array[y][x];
            }
        }
        return transpose;
    }

    /**
     * Each labeled array in the map becomes a row in the returned array, which may be ragged because
     * each row in the array have a different length.
     *
     * @param labeledRows a map holding named rows of data
     * @return a 2D array, where rows of the array hold the data in the order returned
     * from the string labels.
     */
    public static double[][] copyToRows(LinkedHashMap<String, double[]> labeledRows) {
        Objects.requireNonNull(labeledRows, "The source map was null");
        Set<String> keySet = labeledRows.keySet();
        int j = 0;
        double[][] data = new double[keySet.size()][];
        for (String name : keySet) {
            data[j] = copyOf(labeledRows.get(name));
            j++;
        }
        return data;
    }

    /**
     * Each labeled array in the map becomes a column in the returned array. Each array in
     * the map must have the same number of elements.
     *
     * @param labeledColumns a map holding named columns of data
     * @return a 2D array, where columns of the array hold the data in the order returned
     * from the string labels.
     */
    public static double[][] copyToColumns(LinkedHashMap<String, double[]> labeledColumns) {
        Objects.requireNonNull(labeledColumns, "The source map was null");
        if (labeledColumns.isEmpty()) {
            return new double[0][0];
        }
        double[][] data = copyToRows(labeledColumns);
        if (!isRectangular(data)) {
            throw new IllegalArgumentException("The stored arrays do not have the same number of elements");
        }
        return transpose2DArray(data);
    }

    /**
     * Assumes that the entries in the list are string representations of double values.
     * Each String[] can have a different number of elements.  Thus, the returned
     * array may be ragged.
     *
     * @param entries the list of data entries
     * @return the 2D array
     */
    public static double[][] parseTo2DArray(List<String[]> entries) {
        Objects.requireNonNull(entries, "The list was null");
        // read as 2-D array
        double[][] data = new double[entries.size()][];
        Iterator<String[]> iterator = entries.iterator();
        int row = 0;
        while (iterator.hasNext()) {
            String[] strings = iterator.next();
            double[] rowData = JSLArrayUtil.parseDouble(strings);
            data[row] = rowData;
            row++;
        }
        return data;
    }

    /**
     * Assumes that the file holds doubles with each value on a different line
     * 1.0
     * 4.0
     * 2.0
     * etc.
     *
     * @param pathToFile the path to a file holding the data
     * @return the data as an array
     */
    public static double[] scanToArray(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        try (Scanner scanner = new Scanner(pathToFile.toFile())) {
            ArrayList<Double> list = new ArrayList<>();
            while (scanner.hasNextDouble()) {
                list.add(scanner.nextDouble());
            }
            return JSLArrayUtil.toPrimitive(list.toArray(new Double[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new double[0];
    }

    /**
     * Writes the data in the array to rows in the file, each row with one data point
     *
     * @param array    the array to write, must not be null
     * @param fileName the name of the file, must not be null, file will appear in JSL.getInstance().getOutDir()
     */
    public static void writeToFile(double[] array, String fileName) {
        writeToFile(array, null, fileName);
    }

    /**
     * Writes the data in the array to rows in the file, each row with one data point
     *
     * @param df the format to write the array values, may be null
     * @param array    the array to write, must not be null
     * @param fileName the name of the file, must not be null, file will appear in JSL.getInstance().getOutDir()
     */
    public static void writeToFile(double[] array, DecimalFormat df, String fileName) {
        Objects.requireNonNull(array, "The array must not be null");
        Objects.requireNonNull(fileName, "The path to the file must not be null");
        Path pathToFile = JSL.getInstance().getOutDir().resolve(fileName);
        writeToFile(array, df, pathToFile);
    }

    /**
     * Writes the data in the array to rows in the file, each row with one data point
     *
     * @param array      the array to write, must not be null
     * @param pathToFile the path to the file, must not be null
     */
    public static void writeToFile(double[] array, Path pathToFile) {
        writeToFile(array, null, pathToFile);
    }

    /**
     * Writes the data in the array to rows in the file, each row with one data point
     *
     * @param df the format to write the array values, may be null
     * @param array      the array to write, must not be null
     * @param pathToFile the path to the file, must not be null
     */
    public static void writeToFile(double[] array, DecimalFormat df, Path pathToFile) {
        Objects.requireNonNull(array, "The array must not be null");
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        for (double x : array) {
            if (df == null){
                out.println(x);
            } else{
                out.println(df.format(x));
            }
        }
        out.flush();
    }

    /**
     * Writes the data in the array to rows in the file, each element in a row is
     * separated by a comma
     *
     * @param array    the array to write, must not be null
     * @param fileName the name of the file, must not be null, file will appear in JSL.getInstance().getOutDir()
     */
    public static void writeToFile(double[][] array, String fileName) {
        writeToFile(array, null, fileName);
    }

    /**
     * Writes the data in the array to rows in the file, each element in a row is
     * separated by a comma
     *
     * @param df the format to write the array values, may be null
     * @param array    the array to write, must not be null
     * @param fileName the name of the file, must not be null, file will appear in JSL.getInstance().getOutDir()
     */
    public static void writeToFile(double[][] array, DecimalFormat df, String fileName) {
        Objects.requireNonNull(array, "The array must not be null");
        Objects.requireNonNull(fileName, "The path to the file must not be null");
        Path pathToFile = JSL.getInstance().getOutDir().resolve(fileName);
        writeToFile(array, df, pathToFile);
    }

    /**
     * Writes the data in the array to rows in the file, each element in a row is
     * separated by a comma
     * @param array      the array to write, must not be null
     * @param pathToFile the path to the file, must not be null
     */
    public static void writeToFile(double[][] array, Path pathToFile) {
        writeToFile(array, null, pathToFile);
    }

    /**
     * Writes the data in the array to rows in the file, each element in a row is
     * separated by a comma
     * @param df the format to write the array values, may be null
     * @param array      the array to write, must not be null
     * @param pathToFile the path to the file, must not be null
     */
    public static void writeToFile(double[][] array, DecimalFormat df, Path pathToFile) {
        Objects.requireNonNull(array, "The array must not be null");
        Objects.requireNonNull(pathToFile, "The path to the file must not be null");
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        write(array, df, out);
    }

    /**  Allows writing directly to a known PrintWriter.  Facilitates writing
     *  to the file before or after the array is written
     * @param array the array to write, must not be null
     * @param out the PrintWriter to write to, must not be null
     */
    public static void write(double[][] array, PrintWriter out){
        write(array, null, out);
    }

    /**  Allows writing directly to a known PrintWriter.  Facilitates writing
     *  to the file before or after the array is written
     * @param df the format to write the array values, may be null
     * @param array the array to write, must not be null
     * @param out the PrintWriter to write to, must not be null
     */
    public static void write(double[][] array, DecimalFormat df, PrintWriter out){
        Objects.requireNonNull(array, "The array must not be null");
        Objects.requireNonNull(out, "The PrintWrite must not be null");
        for (double[] doubles : array) {
            out.println(toCSVString(doubles, df));
        }
        out.flush();
    }

    /**
     * @param array the array of objects
     * @param <T>   the type of the objects
     * @return a String array holding the string value of the elements of the array
     */
    public static <T> String[] asStringArray(T[] array) {
        if (array == null) {
            return new String[0];
        }
        String[] sArray = new String[array.length];
        for (int i = 0; i < sArray.length; i++)
            sArray[i] = String.valueOf(array[i]);

        return sArray;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are strictly increasing a_0 lt a_1 lt a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are strictly increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isStrictlyIncreasing(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] >= array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are strictly decreasing a_0 gt a_1 gt a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are strictly increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isStrictlyDecreasing(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] <= array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are increasing a_0 lte a_1 lte a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isIncreasing(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are decreasing a_0 gte a_1 gte a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are decreasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isDecreasing(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] < array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are equal a_0 = a_1 = a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are equal, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isAllEqual(double[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] != array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are equal a_0 = a_1 = a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are equal, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isAllEqual(int[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] != array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are strictly increasing a_0 lt a_1 lt a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are strictly increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isStrictlyIncreasing(int[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] >= array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are strictly decreasing a_0 gt a_1 gt a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are strictly increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isStrictlyDecreasing(int[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] <= array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are increasing a_0 lte a_1 lte a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are increasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isIncreasing(int[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examines each element, a_i starting at 0, and determines if all
     * the elements are decreasing a_0 gte a_1 gte a_2, etc.
     *
     * @param array the array to check, must not be null
     * @return true if all elements are decreasing, if there
     * are 0 elements then it returns false, 1 element returns true
     */
    public static boolean isDecreasing(int[] array) {
        Objects.requireNonNull(array, "The array was null");
        if (array.length == 0) {
            return false;
        }
        if (array.length == 1) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] < array[i]) {
                return false;
            }
        }
        return true;
    }
}

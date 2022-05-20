package jsl.utilities.rootfinding;

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;

import java.util.*;

public class GridEnumerator {

    /**
     * Function that will be evaluated
     */
    protected final FunctionIfc function;
    protected final List<Evaluation> points;

    public GridEnumerator(FunctionIfc theFunction) {
        Objects.requireNonNull(theFunction, "The function was null");
        function = theFunction;
        points = new ArrayList<>();
    }

    /** Makes a set of points for evaluation over the interval.  If n = 0, then 2 evaluations occur
     *  at both end points. If n = 1, then 3 evaluations occur
     *  at mid-point and both end points. The grid division is determined
     *  by the interval width divided by (n+1).
     *
     * @param theInterval the interval for evaluations, must not be null
     * @param numPoints the number of points within the interval to evaluate, must be greater than zero
     */
    public static double[] makePoints(Interval theInterval, int numPoints){
        Objects.requireNonNull(theInterval, "The interval was null");
        if (numPoints < 0) {
            throw new IllegalArgumentException("The number of points must be greater than zero");
        }
        double[] x = new double[numPoints + 2];
        double dx = theInterval.getWidth()/ (numPoints + 1);
        x[0] = theInterval.getLowerLimit();
        for (int i = 1; i < x.length; i++) {
            x[i] = x[i-1] + dx;
        }
        return x;
    }

    /** A set of points starting a lower limit and incrementing by delta
     *
     * @param lowerLimit the lower limit
     * @param delta the delta increment
     * @param numPoints the number of points after the lower limit, resulting in an upper limit
     * @return the points, including the lower limit at index 0
     */
    public static double[] makePoints(double lowerLimit, double delta, int numPoints){
        if (numPoints <= 0) {
            throw new IllegalArgumentException("The number of points must be greater than zero");
        }
        if (delta <= 0) {
            throw new IllegalArgumentException("The interval delta must be greater than zero");
        }
        double[] x = new double[numPoints + 1];
        x[0] = lowerLimit;
        for (int i = 1; i < x.length; i++) {
            x[i] = x[i-1] + delta;
        }
        return x;
    }

    /**
     *  Evaluates the supplied function at the points in the array
     *
     * @param pointsArray the array of points to evaluate
     */
    public void evaluate(double[] pointsArray){
        Objects.requireNonNull(points, "The points array was null");
        if (pointsArray.length == 0){
            throw new IllegalArgumentException("The points array was empty");
        }
        points.clear();
        for (double x: pointsArray) {
            points.add(new Evaluation(x, function.fx(x)));
        }
    }

    /** Evaluates the function at the end points and at n points equally
     *  spaced within the interval.  If n = 0, then 2 evaluations occur
     *  at both end points. If n = 1, then 3 evaluations occur
     *  at mid-point and both end points. The grid division is determined
     *  by the interval width divided by (n+1).
     *
     * @param theInterval the interval for evaluations, must not be null
     * @param numPoints the number of points within the interval to evaluate, must be greater than zero
     * @return the points that were evaluated
     */
    public double[] evaluate(Interval theInterval, int numPoints) {
        Objects.requireNonNull(theInterval, "The interval was null");
        if (numPoints < 0) {
            throw new IllegalArgumentException("The number of points must be greater than zero");
        }
        double[] x = makePoints(theInterval, numPoints);
        evaluate(x);
        return x;
    }

    /** Evaluates A set of points starting a lower limit and incrementing by delta
     *
     * @param lowerLimit the lower limit
     * @param delta the delta increment
     * @param numPoints the number of points after the lower limit, resulting in an upper limit
     * @return the points, including the lower limit at index 0
     */
    public double[] evaluate(double lowerLimit, double delta, int numPoints){
        double[] x = makePoints(lowerLimit, delta, numPoints);
        evaluate(x);
        return x;
    }

    /**
     *
     * @return an unmodifiable list of the evaluations in the order evaluated
     */
    public List<Evaluation> getEvaluations(){
        return Collections.unmodifiableList(points);
    }

    /**
     *
     * @return a list of the evaluations sorted from smallest to largest
     */
    public List<Evaluation> getSortedEvaluations(){
        List<Evaluation> list = new ArrayList<>();
        for(Evaluation e: points){
            list.add(e.newInstance());
        }
        Collections.sort(list);
        return list;
    }

    /**
     *
     * @return the minimum evaluation
     */
    public Evaluation getMinimum(){
        return Collections.min(points).newInstance();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GridEnumerator Evaluations");
        sb.append(System.lineSeparator());
        for(Evaluation e: points){
            sb.append(e.toString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     *
     * @return the maximum evaluation
     */
    public Evaluation getMaximum(){
        return Collections.max(points).newInstance();
    }

    /**
     *  An evaluation of the function at specific point
     */
    public static class Evaluation implements Comparable<Evaluation>{
        final double x;
        final double f;

        public Evaluation(double x, double f) {
            this.x = x;
            this.f = f;
        }

        public Evaluation newInstance(){
            return new Evaluation(x, f);
        }

        @Override
        public int compareTo(Evaluation that) {
            return Double.compare(f, that.f);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            sb.append("x = ").append(x);
            sb.append(", f(x) = ").append(f);
            sb.append(']');
            return sb.toString();
        }
    }

}

package jsl.utilities.statistic;

public class HistogramBin {

    final double lowerLimit;

    final double upperLimit;

    private int count;

    private String binLabel;

    private final int binNumber;

    public HistogramBin(int binNumber, double lowerLimit, double upperLimit) {
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("The lower limit of the bin must be < the upper limit");
        }
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.binNumber = binNumber;
        setBinLabel(String.format("%3d [%5.2f,%5.2f) ", binNumber, lowerLimit, upperLimit));
    }

    /**
     * @return an copy of this bin
     */
    public final HistogramBin newInstance() {
        HistogramBin bin = new HistogramBin(this.binNumber, this.lowerLimit, this.upperLimit);
        bin.count = this.count;
        return bin;
    }

    /**
     * Gets the number of the bin 1 = first bin, 2 = 2nd bin, etc
     *
     * @return the number of the bin
     */
    public final int getBinNumber() {
        return binNumber;
    }

    /**
     * @return the label for the bin
     */
    public final String getBinLabel() {
        return binLabel;
    }

    /**
     * @param label The label for the bin
     */
    public final void setBinLabel(String label) {
        binLabel = label;
    }

    /**
     * Increments the bin count by 1.0
     */
    public final void increment() {
        count = count + 1;
    }

    /**
     * Resets the bin count to 0.0
     */
    public final void reset() {
        count = 0;
    }

    public final double getLowerLimit() {
        return lowerLimit;
    }

    public final double getUpperLimit() {
        return upperLimit;
    }

    public final double getCount() {
        return count;
    }

    @Override
    public String toString() {
        String s = String.format("%s = %d", getBinLabel(), count);
        // String s = "[" + lowerLimit + "," + upperLimit + ") = " + count;
        return (s);
    }
}

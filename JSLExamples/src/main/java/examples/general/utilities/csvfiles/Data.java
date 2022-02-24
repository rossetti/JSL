package examples.general.utilities.csvfiles;

import com.opencsv.bean.CsvBindByName;

public class Data {

    @CsvBindByName
    private double x;

    @CsvBindByName
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        return sb.toString();
    }
}

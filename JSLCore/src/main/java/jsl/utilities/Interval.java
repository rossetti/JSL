/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * Created on Nov 4, 2006
 *
 */
package jsl.utilities;

/** Can be used to represent confidence intervals.  Intervals between two real
 *  numbers where the lower limit must be less than or equal to the upper limit.
 * 
 * @author rossetti
 *
 */
public class Interval {

    protected double myLower;

    protected double myUpper;

    /**
     * 
     * @param xLower the lower limit
     * @param xUpper  the upper limit
     */
    public Interval(double xLower, double xUpper) {
        setInterval(xLower, xUpper);
    }

    /** Sets the interval 
     *  Throws IllegalArgumentExceptons if the lower limit is &gt;= upper limit
     * 
     * @param xLower the lower limit
     * @param xUpper the upper limit
     */
    public final void setInterval(double xLower, double xUpper) {

        if (xLower > xUpper) {
            throw new IllegalArgumentException("The lower limit must be <= the upper limit");
        }

        myLower = xLower;
        myUpper = xUpper;
    }

    /** A new instance with the same interval settings.
     * 
     * @return  A new instance with the same interval settings.
     */
    public final Interval newInstance(){
        return new Interval(this.getLowerLimit(), this.getUpperLimit());
    }
    
    @Override
    public String toString() {
        return ("[" + myLower + ", " + myUpper + "]");
    }

    /**
     * 
     * @return the lower limit of the interval
     */
    public final double getLowerLimit() {
        return myLower;
    }

    /**
     * 
     * @return The upper limit of the interval
     */
    public final double getUpperLimit() {
        return myUpper;
    }
    
    /** The width of the interval
     * 
     * @return  The width of the interval
     */
    public final double getWidth(){
        return myUpper - myLower;
    }
    
    /** Half of the width of the interval
     * 
     * @return  Half of the width of the interval
     */
    public final double getHalfWidth(){
        return getWidth()/2.0;
    }

    /**
     * 
     * @param x the value to check
     * @return true if x is in the interval (includes end points)
     */
    public final boolean contains(double x) {
        return ((myLower <= x) && (x <= myUpper));
    }
    
    /** Checks if the supplied interval is contained within
     *  this interval
     * 
     * @param interval the interval to check
     * @return true only if both lower and upper limits of supplied interval 
     *  are within this interval
     */
    public final boolean contains(Interval interval){
        if (interval == null){
            return false;
        }
        return contains(interval.getLowerLimit()) && contains(interval.getUpperLimit());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;

        if (Double.compare(interval.myLower, myLower) != 0) return false;
        return Double.compare(interval.myUpper, myUpper) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(myLower);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(myUpper);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

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
package jsl.utilities.random.rng;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.AR1NormalRV;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.Objects;

/**
 * Uses the auto-regressive to anything algorithm
 * to generate correlated uniform variates.
 * The user supplies the correlation of the underlying
 * AR(1) process.  The resulting correlation in the u's
 * may not necessarily meet this correlation, due to
 * the correlation matching problem.
 */
public class AR1CorrelatedRNStream implements RNStreamIfc, SetRandomNumberStreamIfc, GetRandomNumberStreamIfc {

    private AR1NormalRV myAR1;

    private double myPrevU;

    private RNStreamIfc myStream;

    /**
     *
     */
    public AR1CorrelatedRNStream() {
        this(0.0, JSLRandom.nextRNStream());
    }

    /**
     * @param correlation the correlation, must be within [-1,1]
     */
    public AR1CorrelatedRNStream(double correlation) {
        this(correlation, JSLRandom.nextRNStream());
    }

    /**
     * @param correlation the correlation, must be within [-1,1]
     * @param rng the underlying source of randomness
     */
    public AR1CorrelatedRNStream(double correlation, RNStreamIfc rng) {
        Objects.requireNonNull(rng, "The supplied RngIfc was null");
        myAR1 = new AR1NormalRV(0.0, 1.0, correlation, rng);
        myStream = rng;
    }

    @Override
    public String getName() {
        return myStream.getName();
    }

    @Override
    public int getId() {
        return myStream.getId();
    }

    @Override
    public double randU01() {
        // generate the correlated normal
        double z = myAR1.getValue();
        // invert to get the correlated uniforms
        double u = Normal.stdNormalCDF(z);
        myPrevU = u;
        return u;
    }

    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return myStream;
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myStream = stream;
    }

    @Override
    public final int randInt(int i, int j) {
        return (i + (int) (randU01() * (j - i + 1)));
    }

    @Override
    public final void advanceToNextSubStream() {
        myAR1.advanceToNextSubStream();
    }

    @Override
    public final void resetStartStream() {
        myAR1.resetStartStream();
    }

    @Override
    public final void resetStartSubStream() {
        myAR1.resetStartSubStream();
    }

    @Override
    public final void setAntitheticOption(boolean flag) {
        myAR1.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myAR1.getAntitheticOption();
    }

    /**
     *
     * @return the lag 1 correlation
     */
    public final double getLag1Correlation() {
        return myAR1.getLag1Correlation();
    }

    /**
     *
     * @param correlation the correlation, must be within [-1,1]
     */
    public final void setLag1Correlation(double correlation){
        myAR1 = new AR1NormalRV(0.0, 1.0, correlation, myStream);
    }

    @Override
    public final double getPrevU01() {
        return myPrevU;
    }

    @Override
    public final double getAntitheticValue() {
        return 1.0 - myPrevU;
    }

    @Override
    public RNStreamIfc newInstance() {
        return newInstance(null);
    }

    @Override
    public RNStreamIfc newInstance(String name) {
        RNStreamIfc c = myStream.newInstance(name);
        double r = getLag1Correlation();
        return new AR1CorrelatedRNStream(r, c);
    }

    @Override
    public RNStreamIfc newAntitheticInstance(String name) {
        RNStreamIfc c = myStream.newAntitheticInstance(name);
        double r = getLag1Correlation();
        return new AR1CorrelatedRNStream(r, c);
    }

    @Override
    public RNStreamIfc newAntitheticInstance() {
        return newAntitheticInstance(null);
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myStream.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myStream.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myStream.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myStream.setResetStartStreamOption(b);
    }
}

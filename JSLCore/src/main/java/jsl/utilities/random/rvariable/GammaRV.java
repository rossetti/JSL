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

package jsl.utilities.random.rvariable;

import jsl.utilities.distributions.Gamma;
import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Gamma(shape, scale) random variable
 */
public final class GammaRV extends AbstractRVariable {

    private final Gamma myGamma;

    public GammaRV(double shape, double scale) {
        this(shape, scale, JSLRandom.nextRNStream());
    }

    public GammaRV(double shape, double scale, int streamNum) {
        this(shape, scale, JSLRandom.rnStream(streamNum));
    }

    public GammaRV(double shape, double scale, RNStreamIfc rng) {
        super(rng);
        myGamma = new Gamma(shape, scale);
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public GammaRV newInstance(RNStreamIfc rng) {
        return new GammaRV(this.getShape(), this.getScale(), rng);
    }

    @Override
    public String toString() {
        return "GammaRV{" +
                "shape=" + myGamma.getShape() +
                ", scale=" + myGamma.getScale() +
                '}';
    }

    /**
     * Gets the shape
     *
     * @return The shape parameter as a double
     */
    public double getShape() {
        return myGamma.getShape();
    }

    /**
     * Gets the scale parameter
     *
     * @return The scale parameter as a double
     */
    public double getScale() {
        return myGamma.getScale();
    }

    @Override
    protected double generate() {
        double v = myGamma.invCDF(myRNStream.randU01());
        return v;
    }

    /**
     * The keys are "shape" with default value 1.0 and "scale" with
     * default value 1.0
     *
     * @return a control for Gamma random variables
     */
    public static RVControls makeControls() {
        return new RVControls() {
            @Override
            protected final void fillControls() {
                addDoubleControl("shape", 1.0);
                addDoubleControl("scale", 1.0);
                setName(RVType.Gamma.name());
                setRVType(RVType.Gamma);
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double scale = getDoubleControl("scale");
                double shape = getDoubleControl("shape");
                return new GammaRV(shape, scale, rnStream);
            }
        };
    }

    /**
     * Provides a random number via the standard acceptance rejection technique
     * see Law and Kelton for the algorithm
     *
     * @return double a random number distributed according to the receiver.
     */
    public double randomViaAcceptanceRejection() {
        double r;

        if (getShape() > 1) {
            r = randomForAlphaGreaterThan1();
        } else if (getShape() < 1) {
            r = randomForAlphaLessThan1();
        } else {
            r = randomForAlphaEqual1();
        }

        return r * getShape();
    }

    /**
     * @return double
     */
    private double randomForAlphaEqual1() {
        return -Math.log(1 - myRNStream.randU01());
    }

    /**
     * @return double
     */
    private double randomForAlphaGreaterThan1() {
        double u1, u2, v, y, z, w;
        double a = Math.sqrt(2 * getShape() - 1);
        double b = getShape() - Math.log(4.0);
        double q = getShape() + 1 / a;
        double d = 1 + Math.log(4.5);
        while (true) {
            u1 = myRNStream.randU01();
            u2 = myRNStream.randU01();
            v = a * Math.log(u1 / (1 - u1));
            y = getShape() * Math.exp(v);
            z = u1 * u1 * u2;
            w = b + q * v - y;
            if (w + d - 4.5 * z >= 0 || w >= Math.log(z)) {
                return y;
            }
        }
    }

    /**
     * @return double
     */
    private double randomForAlphaLessThan1() {
        double p, y;
        double b = (Math.E + getShape()) / Math.E;

        while (true) {
            p = myRNStream.randU01() * b;
            if (p > 1) {
                y = -Math.log((b - p) / getShape());
                if (myRNStream.randU01() <= Math.pow(y, getShape() - 1)) {
                    return y;
                }
            }
            y = Math.pow(p, 1 / getShape());
            if (myRNStream.randU01() <= Math.exp(-y)) {
                return y;
            }
        }
    }

}

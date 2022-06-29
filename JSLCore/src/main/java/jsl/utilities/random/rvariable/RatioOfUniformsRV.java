package jsl.utilities.random.rvariable;

import jsl.utilities.distributions.PDFIfc;
import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Objects;

/**
 *  Provides a framework for generating random variates using the
 *  ratio of uniforms method.
 */
public class RatioOfUniformsRV extends RVariable {

    private double myPrevValue;

    protected UniformRV uCDF;

    protected UniformRV vCDF;

    protected PDFIfc pdf;

    /** Specifies the pair (u, v), with ratio v/u
     *
     * @param umax the maximum bound in the "u" variate
     * @param vmin the minimum bound for the "v" variate
     * @param vmax the maximum bound in the "v" variate
     * @param f the desired PDF
     */
    public RatioOfUniformsRV(double umax, double vmin, double vmax, PDFIfc f) {
        this(umax, vmin, vmax, f, JSLRandom.nextRNStream());
    }

    /** Specifies the pair (u, v), with ratio v/u
     *
     * @param umax the maximum bound in the "u" variate
     * @param vmin the minimum bound for the "v" variate
     * @param vmax the maximum bound in the "v" variate
     * @param f the desired PDF
     * @param rnStream the random number stream to use
     */
    public RatioOfUniformsRV(double umax, double vmin, double vmax, PDFIfc f, RNStreamIfc rnStream) {
        super(rnStream);
        Objects.requireNonNull(f, "The supplied PDF was null");
        uCDF = new UniformRV(0.0, umax, rnStream);
        vCDF = new UniformRV(vmin, vmax, rnStream);
        pdf = f;
        myPrevValue = Double.NaN;
    }

    protected double generate() {
        while (true) {
            double u = uCDF.getValue();
            double v = vCDF.getValue();
            double z = v / u;
            if (u * u < pdf.pdf(z)) {
                return z;
            }
        }
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new RatioOfUniformsRV(uCDF.getMaximum(), vCDF.getMinimum(), vCDF.getMaximum(), pdf, rng);
    }

}

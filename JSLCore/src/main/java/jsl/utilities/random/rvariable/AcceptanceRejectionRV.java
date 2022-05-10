package jsl.utilities.random.rvariable;

import jsl.utilities.distributions.ContinuousDistributionIfc;
import jsl.utilities.distributions.PDFIfc;
import jsl.utilities.random.rng.RNStreamIfc;

import java.util.Objects;

public class AcceptanceRejectionRV extends AbstractRVariable {

    private final ContinuousDistributionIfc distribution;

    private final PDFIfc pdf;

    private final RVariableIfc rVariable;

    public AcceptanceRejectionRV(ContinuousDistributionIfc proposalDistribution, PDFIfc pdf) {
        this(proposalDistribution, pdf, JSLRandom.nextRNStream());
    }

    public AcceptanceRejectionRV(ContinuousDistributionIfc proposalDistribution, PDFIfc pdf, RNStreamIfc rnStream) {
        super(rnStream);
        Objects.requireNonNull(proposalDistribution, "The supplied distribution was null");
        Objects.requireNonNull(pdf, "The supplied distribution was null");
        Objects.requireNonNull(rnStream, "The supplied distribution was null");
        if (!proposalDistribution.getDomain().equals(pdf.getDomain())) {
            throw new IllegalArgumentException("The domains of the two distributions are not equal");
        }
        this.distribution = proposalDistribution;
        this.pdf = pdf;
        rVariable = proposalDistribution.getRandomVariable(rnStream);
    }

    @Override
    protected double generate() {
        double w;
        double u;
        do {
            w = rVariable.getValue();
            u = rVariable.getRandomNumberStream().randU01();
        } while (u * distribution.pdf(w) > pdf.pdf(w));
        return w;
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new AcceptanceRejectionRV(distribution, pdf, rng);
    }
}

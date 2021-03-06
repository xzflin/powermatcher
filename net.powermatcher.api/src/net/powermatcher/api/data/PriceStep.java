package net.powermatcher.api.data;

/**
 * This immutable data object represents a priceStep and its {@link MarketBasis}. A priceStep is the index in the demand
 * array. This is an integer and can be any value between 0 and marketbasis.priceSteps. The value 0 corresponds to the
 * minimal price and marketbasis.priceStep - 1 corresponds to the the maximal price.
 *
 * @author FAN
 * @version 2.0
 */
public class PriceStep {

    /**
     * The marketbasis of this cluster.
     */
    private final MarketBasis marketBasis;

    /**
     * The value of the priceStep.
     */
    private final int priceStep;

    /**
     * A constructor that creates a new {@link PricePoint} instance.
     *
     * @param marketBasis
     *            the marketbasis of this cluster.
     * @param priceStep
     *            the value of the priceStep.
     */
    public PriceStep(MarketBasis marketBasis, int priceStep) {
        if (marketBasis == null) {
            throw new NullPointerException("marketBasis can not be null");
        } else if (priceStep < 0 || priceStep >= marketBasis.getPriceSteps()) {
            throw new IllegalArgumentException("PriceStep " + priceStep + " is out of bounds [0, "
                                               + marketBasis.getPriceSteps() + ")");
        }
        this.marketBasis = marketBasis;
        this.priceStep = priceStep;
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    /**
     * @return the current value of priceStep.
     */
    public int getPriceStep() {
        return priceStep;
    }

    /**
     * @return true when the {@link PriceStep} is at its maximum (e.g. marketBasis.getPriceSteps() - 1)
     */
    public boolean isMaximum() {
        return priceStep == marketBasis.getPriceSteps() - 1;
    }

    /**
     * @return true when the {@link PriceStep} is at its minimum (e.g. 0)
     */
    public boolean isMinimum() {
        return priceStep == 0;
    }

    /**
     * @return The {@link PriceStep} that is exactly 1 step above the current.
     * @throws IllegalStateException
     *             when it is already at its maximum level.
     */
    public PriceStep increment() {
        if (isMaximum()) {
            throw new IllegalStateException("The priceStep is already at its maximum");
        }
        return new PriceStep(marketBasis, priceStep + 1);
    }

    /**
     * @return The {@link PriceStep} that is exactly 1 step below the current.
     * @throws IllegalStateException
     *             when it is already at its minimum level.
     */
    public PriceStep decrement() {
        if (isMinimum()) {
            throw new IllegalStateException("The priceStep is already at its minimum");
        }
        return new PriceStep(marketBasis, priceStep - 1);
    }

    /**
     * Creates a {@link Price}, based on the {@link MarketBasis} as the priceStep
     *
     * @return the new {@link Price}
     */
    public Price toPrice() {
        return new Price(marketBasis, marketBasis.getMinimumPrice() + priceStep * marketBasis.getPriceIncrement());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 16333 * marketBasis.hashCode() + 3557 * priceStep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            PriceStep other = (PriceStep) obj;
            return marketBasis.equals(other.marketBasis) && priceStep == other.priceStep;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PriceStep " + Integer.toString(priceStep);
    }
}

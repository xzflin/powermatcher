package net.powermatcher.examples;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.measure.Measure;
import javax.measure.unit.SI;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.core.BaseAgentEndpoint;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link PVPanelAgent} is a implementation of a {@link BaseAgentEndpoint}. It represents a dummy freezer.
 * {@link PVPanelAgent} creates a {@link PointBid} with random {@link PricePoint}s at a set interval. It does nothing
 * with the returned {@link Price}.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = PVPanelAgent.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, AgentEndpoint.class })
public class PVPanelAgent
    extends BaseAgentEndpoint
    implements AgentEndpoint {
    private static final Logger LOGGER = LoggerFactory
                                                      .getLogger(PVPanelAgent.class);

    private static Random generator = new Random();

    public static interface Config {
        @Meta.AD(deflt = "concentrator")
        String desiredParentId();

        @Meta.AD(deflt = "pvpanel")
        String agentId();

        @Meta.AD(deflt = "30", description = "Number of seconds between bid updates")
        long bidUpdateRate();

        @Meta.AD(deflt = "-700", description = "The mimimum value of the random demand.")
        double minimumDemand();

        @Meta.AD(deflt = "-600", description = "The maximum value the random demand.")
        double maximumDemand();
    }

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * The mimimum value of the random demand.
     */
    private double minimumDemand;

    /**
     * The maximum value the random demand.
     */
    private double maximumDemand;

    private Config config;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        activate(config.agentId(), config.desiredParentId());

        minimumDemand = config.minimumDemand();
        maximumDemand = config.maximumDemand();

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
        scheduledFuture.cancel(false);
        LOGGER.info("Agent [{}], deactivated", getAgentId());
    }

    /**
     * {@inheritDoc}
     */
    void doBidUpdate() {
        if (isInitialized()) {
            double demand = minimumDemand + (maximumDemand - minimumDemand)
                            * generator.nextDouble();
            publishBid(Bid.flatDemand(getMarketBasis(), demand));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void handlePriceUpdate(PriceUpdate priceUpdate) {
        LOGGER.debug("Received price update [{}], current bidNr = {}",
                     priceUpdate, getLastBidUpdate().getBidNumber());
        publishEvent(new IncomingPriceUpdateEvent(getClusterId(),
                                                  getAgentId(),
                                                  getSession().getSessionId(),
                                                  now(),
                                                  priceUpdate));
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} value.
     */
    @Override
    public void setContext(FlexiblePowerContext context) {
        super.setContext(context);
        scheduledFuture = context.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, Measure.valueOf(0, SI.SECOND), Measure.valueOf(config.bidUpdateRate(), SI.SECOND));
    }
}

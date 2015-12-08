package ar.edu.itba.pod.simul;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.simul.local.LocalMarketManager;
import ar.edu.itba.pod.simul.local.LocalSimulationManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;
import ar.edu.itba.pod.simul.ui.FeedbackSimulationManager;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

/**
 * Simulation main program
 */
public class SimulationApp implements Runnable {

	public static void main(String[] args) {
		new SimulationApp().run();
	}

	@Override
	public void run() {
		FeedbackCallback callback = new ConsoleFeedbackCallback();
		TimeMapper timeMapper = TimeMappers.oneSecondEach(6, TimeUnit.HOURS);
		
		MarketManager marketManager = new LocalMarketManager();
		marketManager = new FeedbackMarketManager(callback, marketManager);
		
		marketManager.start();
		
		Market market = marketManager.market();
		
		// Define simulation agents
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");
		
		Agent mine1 = SimpleProducer.named("pig iron mine")
									.producing(2).of(pigIron)
									.every(12, TimeUnit.HOURS)
									.build();
		Agent mine2 = SimpleProducer.named("copper mine")
									.producing(4).of(copper)
									.every(1, TimeUnit.DAYS)
									.build();
		Agent refinery = Factory.named("steel refinery")
									.using(5, pigIron).and(2, copper)
									.producing(6, steel)
									.every(1, TimeUnit.DAYS)
									.build();
		Agent factory = SimpleConsumer.named("factory")
									.consuming(10).of(steel)
									.every(2, TimeUnit.DAYS)
									.build();

		SimulationManager sim = new LocalSimulationManager(timeMapper);
		sim = new FeedbackSimulationManager(callback, sim);
		sim.register(Market.class, market);
		sim.addAgent(mine1);
		sim.addAgent(mine2);
		sim.addAgent(refinery);
		sim.addAgent(factory);
		
		sim.start();
		try {
			Thread.sleep(1000 * 20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sim.shutdown();
		
		marketManager.shutdown();
	}
}

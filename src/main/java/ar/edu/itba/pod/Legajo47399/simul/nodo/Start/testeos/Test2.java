package ar.edu.itba.pod.Legajo47399.simul.nodo.Start.testeos;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.Legajo47399.simul.ObjectFactoryImpl;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
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

public class Test2 implements Runnable {

	private String localIp, remoteIp;
	private boolean addAgent = false;

	public static void main(String[] args) {

		if (args.length == 1) {
			new Test2(args[0]).run();
		} else if (args.length == 2) {
			new Test2(args[0], args[1]).run();
		} else {
			new Test2(args[0], args[1], args[0]).run();
		}
	}

	public Test2(String localIp, String remoteIp, String add) {
		this.localIp = localIp;
		this.remoteIp = remoteIp;
		this.addAgent = true;
	}

	public Test2(String localIp, String remoteIp) {
		this.localIp = localIp;
		this.remoteIp = remoteIp;
	}

	public Test2(String localIp) {
		this.localIp = localIp;
	}

	@Override
	public void run() {
		TimeMapper timeMapper = TimeMappers.oneSecondEach(6, TimeUnit.HOURS);
		System.out.println("Creating transaction contex ...");
		ObjectFactory context = new ObjectFactoryImpl();
		FeedbackCallback callback = new ConsoleFeedbackCallback();
		ConnectionManager connectionManager;
		if (this.remoteIp != null) {
			connectionManager = context.createConnectionManager(localIp,
					remoteIp);
		} else {
			connectionManager = context.createConnectionManager(localIp);
		}
		MarketManager marketManager = context
				.getMarketManager(connectionManager);
		marketManager = new FeedbackMarketManager(callback, marketManager);
		marketManager.start();
		Market market = marketManager.market();
		Resource copper = new Resource("Mineral", "Copper");
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		Resource steel = new Resource("Alloy", "Steel");
		SimulationManager sim = context.getSimulationManager(connectionManager,
				timeMapper);
		try {
			sim = new FeedbackSimulationManager(callback, sim);
			sim.register(Market.class, market);
			sim.start();
			if (this.remoteIp == null) {
				for (int i = 1; i <= 1; i++) {
					sim.addAgent(SimpleProducer
                            .named("-PigIron Producer A-").producing(3).of(pigIron)
                            .every(1, TimeUnit.DAYS).build());
                    System.out.println("Producer 1 Creado");
                   
                    sim.addAgent(SimpleProducer
                            .named("-Copper Producer A-").producing(3).of(copper)
                            .every(1, TimeUnit.DAYS).build());
                    System.out.println("Producer 2 Creado");

                   
                    sim.addAgent(Factory
                            .named("-Factory Steel A-")
                            .using(1, pigIron).and(1, copper)
                            .producing(1, steel).every(1, TimeUnit.DAYS)
                            .build());
                    System.out.println("Factory Creado: 1");
                   
                    sim.addAgent(Factory
                            .named("-Factory Steel B-")
                            .using(1, pigIron).and(1, copper)
                            .producing(1, steel).every(1, TimeUnit.DAYS)
                            .build());
                    System.out.println("Factory Creado: 2");
                   
                    sim.addAgent(SimpleConsumer
                            .named("-Consumer Steel A-")
                            .consuming(1).of(steel)
                            .every(2, TimeUnit.DAYS).build());
                    System.out.println("Consumer 1 Creado");
                   
                    sim.addAgent(SimpleConsumer
                            .named("-Consumer Steel B-")
                            .consuming(1).of(steel)
                            .every(2, TimeUnit.DAYS).build());
                    System.out.println("Consumer Creado");
				}
				Thread.sleep(1000 * 60 * 15);
			} else if (addAgent) {
				System.out.println("Agregando agente nuevo");
				Agent mine3 = SimpleProducer.named("producer3").producing(2)
						.of(pigIron).every(12, TimeUnit.HOURS).build();
				sim.addAgent(mine3);
				Thread.sleep(1000 * 60 * 5);
			} else {
				Thread.sleep(1000 * 60 * 5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Disconnecting context from cluster ...");
		try {

			connectionManager.getClusterAdmimnistration().disconnectFromGroup(
					localIp + ":1099");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(1000 * 15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sim.shutdown();
		marketManager.shutdown();
		System.exit(0);
	}
}

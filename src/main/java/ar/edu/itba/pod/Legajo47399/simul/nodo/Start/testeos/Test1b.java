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
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class Test1b implements Runnable {

	private String localIp, remoteIp;
	private boolean addAgent = false;

	public static void main(String[] args) {

		/*if(System.getSecurityManager()==null){
			System.setSecurityManager(new SecurityManager());
		}*/
		
		if (args.length == 1) {
			new Test1b(args[0]).run();
		} else if (args.length == 2) {
			new Test1b(args[0], args[1]).run();
		} else {
			new Test1b(args[0], args[1], args[0]).run();
		}
	}

	public Test1b(String localIp, String remoteIp, String add) {
		this.localIp = localIp;
		this.remoteIp = remoteIp;
		this.addAgent = true;
	}

	public Test1b(String localIp, String remoteIp) {
		this.localIp = localIp;
		this.remoteIp = remoteIp;
	}

	public Test1b(String localIp) {
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

		Resource pigIron = new Resource("Mineral", "Pig Iron");
		SimulationManager sim = context.getSimulationManager(connectionManager,
				timeMapper);

		try {
			sim = new FeedbackSimulationManager(callback, sim);

			Market market = marketManager.market();
			sim.register(Market.class, market);
			sim.start();

			if (this.remoteIp == null) {
				for (int i = 1; i <= 1; i++) {
					sim.addAgent(SimpleProducer.named("producer" + i)
							.producing(3).of(pigIron).every(1, TimeUnit.DAYS)
							.build());
					sim.addAgent(SimpleConsumer.named("consumerA" + i)
							.consuming(1).of(pigIron).every(1, TimeUnit.DAYS)
							.build());
					sim.addAgent(SimpleConsumer.named("consumerB" + i)
							.consuming(1).of(pigIron).every(1, TimeUnit.DAYS)
							.build());
					sim.addAgent(SimpleConsumer.named("consumerC" + i)
							.consuming(1).of(pigIron).every(1, TimeUnit.DAYS)
							.build());
				}
				Thread.sleep(1000 * 60 * 15);
			} else if (addAgent) {
				System.out.println("Agregando agente nuevo");
				for (int i = 0; i < 4; i++) {
					Agent mine3 = SimpleProducer.named("producer3"+i)
							.producing(2).of(pigIron).every(12, TimeUnit.HOURS)
							.build();
					sim.addAgent(mine3);
				}
				Thread.sleep(1000 * 60 * 5);
			} else {
				Thread.sleep(1000 * 60 * 5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Disconnecting context from cluster ...");
		try {
			// TODO: Cambiar el parametro para que efectivamente reciba el
			// nodeId
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
package ar.edu.itba.pod.Legajo47399.simul.nodo.Start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.Legajo47399.simul.ObjectFactoryImpl;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;
import ar.edu.itba.pod.simul.ui.FeedbackSimulationManager;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class Simulacion1a {

	private static ObjectFactory myOF;

	public static void main(String[] args) {
		String myIp = null;

		FeedbackCallback callback = new ConsoleFeedbackCallback();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		myOF = new ObjectFactoryImpl();
		ConnectionManager cm = null;

		String userOption = null;

		myIp = leerStringEntradaStandar("Ingrese el IP local");
		cm = myOF.createConnectionManager(myIp);

		MarketManager market = myOF.getMarketManager(cm);
		market = new FeedbackMarketManager(callback, market);

		market.start();
		SimulationManager sim = myOF.getSimulationManager(cm,
				TimeMappers.oneSecondEach(3, TimeUnit.HOURS));
		sim = new FeedbackSimulationManager(callback, sim);

		sim.register(Market.class, market.market());
		System.out.println("Iniciando simulacion...");
		sim.start();
		System.out.println("Simulacion iniciada.");

		Resource recursoA = new Resource("Mineral", "RA");
		Resource recursoB = new Resource("Mineral", "RB");
		Resource recursoC = new Resource("Mineral", "RC");
		Resource recursoD = new Resource("Mineral", "RD");
		Resource recursoE = new Resource("Mineral", "RE");

		sim.addAgent(SimpleProducer.named("RA Producer").producing(2)
				.of(recursoA).every(1, TimeUnit.HOURS).build());

		sim.addAgent(SimpleProducer.named("RB Producer").producing(2)
				.of(recursoB).every(1, TimeUnit.HOURS).build());

		sim.addAgent(SimpleProducer.named("RC Producer").producing(2)
				.of(recursoC).every(1, TimeUnit.HOURS).build());
		
		sim.addAgent(Factory.named("RED Factory").using(15, recursoA)
				.and(22, recursoB).and(10, recursoC).producing(10, recursoE).and(10, recursoD).every(1, TimeUnit.HOURS).build());

		sim.addAgent(SimpleConsumer.named("RE Consumer").consuming(10)
				.of(recursoE).every(1, TimeUnit.HOURS).build());
		

		try {
			Thread.sleep(1000 * 20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sim.shutdown();

		market.shutdown();

		System.out.println("Las transacciones por segundo fueron: "
				+ market.market().marketData().getHistory()
						.getTransactionsPerSecond());

	}

	private static String leerStringEntradaStandar(String mensaje) {
		String resp = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(mensaje);
		try {
			resp = br.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read the option!");
			System.exit(1);
		}
		return resp;
	}

}
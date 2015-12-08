package ar.edu.itba.pod.Legajo47399.simul.nodo.Start;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class Simulacion2main {
	private static ObjectFactory myOF;

	public static void main(String[] args) {
		String myIp = null;

		FeedbackCallback callback = new ConsoleFeedbackCallback();

		myOF = new ObjectFactoryImpl();
		ConnectionManager cm = null;

		myIp = leerStringEntradaStandar("Ingrese el IP local");
		cm = myOF.createConnectionManager(myIp);

		MarketManager market = myOF.getMarketManager(cm);
		market = new FeedbackMarketManager(callback, market);

		market.start();
		SimulationManager sim = myOF.getSimulationManager(cm,
				TimeMappers.oneSecondEach(10, TimeUnit.HOURS));
		sim = new FeedbackSimulationManager(callback, sim);

		sim.register(Market.class, market.market());
		System.out.println("Iniciando simulacion...");
		sim.start();
		System.out.println("Simulacion iniciada.");

		Resource recursoA = new Resource("Mineral", "RA");

		Boolean salir = false;
		Integer contador = 0;

		while (true && !salir) {

			System.out.println("Agrego un producer"+contador);

			sim.addAgent(SimpleProducer.named("RA Producer" + contador)
					.producing(2).of(recursoA).every(1, TimeUnit.HOURS).build());

			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Las transacciones por segundo fueron: "
					+ market.market().marketData().getHistory()
							.getTransactionsPerSecond());

			System.out.println("Agrego un consumer"+contador);
			sim.addAgent(SimpleConsumer.named("RA Consumer" + contador)
					.consuming(10).of(recursoA).every(1, TimeUnit.HOURS)
					.build());

		/*	try {
				bw.write("Las transacciones por segundo fueron:"
						+ market.market().marketData().getHistory()
								.getTransactionsPerSecond() + "\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			
			
			try {
			    FileWriter fw = new FileWriter("archivoplano.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter salida = new PrintWriter(bw);
			    salida.println(market.market().marketData().getHistory()
							.getTransactionsPerSecond()+" - "+contador+" - fecha:"+System.currentTimeMillis());
			    salida.close();
			}
			catch(java.io.IOException ioex) {
			  System.out.println("se presento el error: "+ioex.toString());
			}
			
			
			
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			contador++;
		}

		sim.shutdown();

		market.shutdown();

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

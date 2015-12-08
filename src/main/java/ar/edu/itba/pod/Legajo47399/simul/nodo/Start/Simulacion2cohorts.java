package ar.edu.itba.pod.Legajo47399.simul.nodo.Start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.Legajo47399.simul.ObjectFactoryImpl;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.TransferHistory;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;
import ar.edu.itba.pod.simul.ui.FeedbackSimulationManager;

public class Simulacion2cohorts {
	private static ObjectFactory myOF;

	public static void main(String[] args) {
		String myIp = null;
		String otherIp = null;

		FeedbackCallback callback = new ConsoleFeedbackCallback();

		myOF = new ObjectFactoryImpl();
		ConnectionManager cm = null;

		myIp = leerStringEntradaStandar("Ingrese el IP local");
		otherIp = leerStringEntradaStandar("Ingrese el IP del nodo que se quiere conectar");
		cm = myOF.createConnectionManager(myIp, otherIp);

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


		int optionNumber;
		do {
			printMenu2();
			optionNumber = leerNumeroEntradaStandar("Ingrese una opcion", 1, 2);

			switch (optionNumber) {

			case 1:
				MarketData mData = market.market().marketData();

				TransferHistory aux = mData.getHistory();
				System.out.println("Las transacciones por segundo son:"
						+ aux.getTransactionsPerSecond());
				break;

			case 2:
				try {
					cm.getClusterAdmimnistration().disconnectFromGroup(
							myIp + ":1099");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		} while (optionNumber != 2);

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

	private static void printMenu2() {

		System.out.println("Bienvenido al nodo de simulacion:-");
		System.out.println("1.Visualizar la informacion de la estadistica.");
		System.out.println("2.Desconectar el nodo.");
		System.out.println("Por favor seleccione una opcion:");
	}

	private static Integer leerNumeroEntradaStandar(String mensaje,
			Integer limiteInferior, Integer limiteSuperior) {
		String opcion = null;
		Integer resp = null;
		boolean error = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do {
			error = false;
			System.out.println(mensaje);
			try {
				opcion = br.readLine();
			} catch (IOException ioe) {
				System.out.println("IO error trying to read the option!");
				error = true;
			}
			try {
				resp = Integer.valueOf(opcion);
			} catch (NumberFormatException e) {
				System.out.println("Por favor ingresar un numero valido.");
				error = true;
			}
		} while (error || (resp < limiteInferior) || (resp > limiteSuperior));

		return resp;

	}

}

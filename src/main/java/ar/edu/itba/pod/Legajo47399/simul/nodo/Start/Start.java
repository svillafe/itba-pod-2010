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

public class Start {

	private static ObjectFactory myOF;

	public static void main(String[] args) throws RemoteException {
		
		String myIp = null;
		String otherIp = null;
		Boolean salir = false;
		int optionNumber = 0;

		FeedbackCallback callback = new ConsoleFeedbackCallback();
		
		
		//if(System.getSecurityManager()==null){
		//	System.setSecurityManager(new SecurityManager());
		//}

		myOF = new ObjectFactoryImpl();
		ConnectionManager cm = null;

		do {
			printMenu();

			

			optionNumber = leerNumeroEntradaStandar("Ingrese una opcion", 1, 2);

			switch (optionNumber) {
			case 1:
				myIp = leerStringEntradaStandar("Ingrese el IP local");
				cm = myOF.createConnectionManager(myIp);
				break;

			case 2:
				myIp = leerStringEntradaStandar("Ingrese el IP local");
				otherIp = leerStringEntradaStandar("Ingrese el IP del nodo que se quiere conectar");
				cm = myOF.createConnectionManager(myIp, otherIp);

				break;
			case 3:
				salir = true;
				break;
			default:
				break;
			}
			if (!salir) {
				MarketManager market = myOF.getMarketManager(cm);
				market = new FeedbackMarketManager(callback, market);

				market.start();
				
				SimulationManager sim = myOF.getSimulationManager(cm,
						TimeMappers.oneSecondEach(6, TimeUnit.HOURS));
				sim = new FeedbackSimulationManager(callback, sim);

				sim.register(Market.class, market.market());
				System.out.println("Iniciando simulacion...");
				sim.start();
				System.out.println("Simulacion iniciada.");

				Resource copper = new Resource("Mineral", "Copper");
				Resource pigIron = new Resource("Mineral", "Pig Iron");
				Resource steel = new Resource("Alloy", "Steel");

				Integer numCooperProducer = 0;
				Integer numPIProducer = 0;
				Integer numSteelfactory = 0;
				Integer numCooperConsumer = 0;
				Integer numSteelConsumer = 0;
				Integer numPigIronConsumer = 0;

				do {
					printMenu2();
					optionNumber = leerNumeroEntradaStandar(
							"Ingrese una opcion", 1, 12);

					switch (optionNumber) {

					case 1:
						// Agregar un agente simple producer. (Cooper)
						sim.addAgent(SimpleProducer
								.named("-Cooper Producer:" + numCooperProducer
										+ "-").producing(1).of(copper)
								.every(1, TimeUnit.DAYS).build());
						System.out.println("Producer Agregado");
						numCooperProducer = numCooperProducer + 1;

						break;
					case 2:
						// Agregar un agente simple producer. (Pig iron)
						sim.addAgent(SimpleProducer
								.named("-PigIron Producer:+" + numPIProducer
										+ "-").producing(2).of(pigIron)
								.every(1, TimeUnit.DAYS).build());
						System.out.println("Producer Creado");
						numPIProducer = numPIProducer + 1;

						break;

					case 3:
						// Agregar un agente simple factory. (Steel)
						sim.addAgent(Factory
								.named("-Factory Steel, (pigIron,cooper)->Steel:"
										+ numSteelfactory + "-")
								.using(1, pigIron).and(1, copper)
								.producing(6, steel).every(1, TimeUnit.DAYS)
								.build());
						System.out.println("Factory Creado");
						numSteelfactory = numSteelfactory + 1;

						break;

					case 4:
						// 4.Agregar un agente simple consumer.(Cooper)
						sim.addAgent(SimpleConsumer
								.named("-Consumer Copper: "
										+ numCooperConsumer + "-")
								.consuming(1).of(copper)
								.every(1, TimeUnit.DAYS).build());
						System.out.println("Consumer Creado");
						numCooperConsumer = numCooperConsumer + 1;

					break;

					case 5:
						// Agregar un agente simple consumer.(Steel)
						sim.addAgent(SimpleConsumer
								.named("-Consumer Steel: " + numSteelConsumer
										+ "-").consuming(1).of(steel)
								.every(1, TimeUnit.DAYS).build());
						System.out.println("Consumer Creado");
						numSteelConsumer = numSteelConsumer + 1;

						break;

					case 6:
						// Agregar un agente simple consumer.(Pig Iron)
						sim.addAgent(SimpleConsumer
								.named("-Consumer Pig Iron: "
										+ numPigIronConsumer + "-")
								.consuming(1).of(pigIron)
								.every(1, TimeUnit.DAYS).build());
						System.out.println("Consumer Creado");
						numPigIronConsumer = numPigIronConsumer + 1;
						break;

					case 7:
						MarketData mData = market.market().marketData();
						TransferHistory aux = mData.getHistory();
						System.out.println("Las transacciones por segundo son:"
								+ aux.getTransactionsPerSecond());

						break;
					case 8:
						// Crear 200 agentes iguales
						for (int i = 1; i <= 35; i++) {
							sim.addAgent(SimpleProducer
		                            .named("-PigIron Producer A:"+i).producing(1).of(pigIron)
		                            .every(1, TimeUnit.DAYS).build());
		                    System.out.println("Producer 1 Creado");
		                   
		                    sim.addAgent(SimpleProducer
		                            .named("-Copper Producer A:"+i).producing(1).of(copper)
		                            .every(1, TimeUnit.DAYS).build());
		                    System.out.println("Producer 2 Creado");

		                   
		                    sim.addAgent(Factory
		                            .named("-Factory Steel A:"+i)
		                            .using(1, pigIron).and(1, copper)
		                            .producing(1, steel).every(1, TimeUnit.DAYS)
		                            .build());
		                    System.out.println("Factory Creado: 1");
		                   
		                   
		                    sim.addAgent(SimpleConsumer
		                            .named("-Consumer Steel A:"+i)
		                            .consuming(1).of(steel)
		                            .every(2, TimeUnit.DAYS).build());
		                    System.out.println("Consumer 1 Creado");
		                   
		                 }
						break;
					case 9:
						System.out
								.println("La cantidad de agentes en el nodo es:"
										+ sim.getAgents().size());
						System.out
								.println("--------------------------------------------------------");
						break;
					case 10:
						System.out.println("Mis vecinos son:");
						System.out.println(((ObjectFactoryImpl) myOF)
								.getVecinos());
						System.out
								.println("--------------------------------------------------------");
						break;

					case 11:

						sim.addAgent(SimpleProducer
								.named("-PigIron Producer 1").producing(10)
								.of(pigIron).every(5, TimeUnit.HOURS).build());
						System.out.println("Producer 1 Creado");

						sim.addAgent(SimpleProducer.named("-Copper Producer 1")
								.producing(4).of(copper)
								.every(5, TimeUnit.HOURS).build());
						System.out.println("Producer 2 Creado");

						sim.addAgent(Factory
								.named("-Factory Steel, (pigIron,cooper)->Steel:1-")
								.using(5, pigIron).and(2, copper)
								.producing(6, steel).every(15, TimeUnit.HOURS)
								.build());
						System.out.println("Factory Creado: 1");

						sim.addAgent(Factory
								.named("-Factory Steel, (pigIron,cooper)->Steel:2-")
								.using(5, pigIron).and(2, copper)
								.producing(6, steel).every(15, TimeUnit.DAYS)
								.build());
						System.out.println("Factory Creado: 2");

						sim.addAgent(SimpleConsumer
								.named("-Consumer Steel: 1-").consuming(2)
								.of(steel).every(1, TimeUnit.DAYS).build());
						System.out.println("Consumer 1 Creado");

						sim.addAgent(SimpleConsumer
								.named("-Consumer Steel: 2-").consuming(2)
								.of(steel).every(1, TimeUnit.DAYS).build());
						System.out.println("Consumer Creado");

						break;

					case 12:
						cm.getClusterAdmimnistration().disconnectFromGroup(
								myIp + ":1099");
						salir = true;
						break;

					default:
						break;
					}
				} while (optionNumber != 12);
			}
		} while (!salir);

	}

	private static void printMenu() {

		System.out.println("Bienvenido al nodo");
		System.out.println("1.Crear un Cluster.");
		System.out.println("2.Conectarse a un Cluster.");
		System.out.println("3.Salir.");
		System.out.println("Por favor seleccione una opcion:");
	}

	private static void printMenu2() {

		System.out.println("Bienvenido al nodo:-");
		System.out.println("1.Agregar un agente simple producer. (Cooper)");
		System.out.println("2.Agregar un agente simple producer. (Pig iron)");
		System.out.println("3.Agregar un agente simple factory. (Steel)");
		System.out.println("4.Agregar un agente simple consumer.(Cooper)");
		System.out.println("5.Agregar un agente simple consumer.(Steel)");
		System.out.println("6.Agregar un agente simple consumer.(Pig Iron)");
		System.out.println("7.Visualizar la informacion de la estadistica.");
		System.out.println("8.Crear 140 agentes.(50 producer Cooper,50Producer Puig, 50 Factory,50 Consumer)");
		System.out.println("9.Cantidad de Agentes en el nodo.");
		System.out.println("10.Vecinos Conocidos.");
		System.out.println("11.Caso testeo Gustavo.");
		System.out.println("12.Desconectar el nodo.");
		System.out.println("Por favor seleccione una opcion:");
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

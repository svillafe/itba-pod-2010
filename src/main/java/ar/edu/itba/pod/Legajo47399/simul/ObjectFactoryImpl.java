package ar.edu.itba.pod.Legajo47399.simul;

import java.rmi.RemoteException;
import java.util.Set;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class ObjectFactoryImpl implements ObjectFactory {

	private Node myNode;
	
	
	
	public ObjectFactoryImpl(){
		super();
	}
	
	@Override
	public ConnectionManager createConnectionManager(String localIp) {
		this.myNode=new Node(localIp);
		this.myNode.bindService();
		try {
			this.myNode.createCluster();
		} catch (RemoteException e) {
			throw new IllegalStateException("Error mientras se queria crear un cluster:"+e.getMessage());
		}
		return myNode.getMyConnectionManager();
	}

	@Override
	public ConnectionManager createConnectionManager(String localIp,
			String groupIp) {
		this.myNode = new Node(localIp);
		this.myNode.bindService();
		try{
			this.myNode.connectToaCluster(groupIp);
		}catch(RemoteException e){
			throw new IllegalStateException("Error mientras se queria conectar a un cluster:"+e.getMessage());
		}
		
		return myNode.getMyConnectionManager();
	}

	@Override
	public MarketManager getMarketManager(ConnectionManager mgr) {
		return this.myNode.getMyMarketManager(mgr);
	}

	@Override
	public SimulationManager getSimulationManager(ConnectionManager mgr,
			TimeMapper timeMappers) {

		return this.myNode.getMySimulationManager(mgr,timeMappers);
	}

	/*Borrar este metodo*/
	public Set<String> getVecinos() {
		return myNode.getClusterNodes();
		
		
	}

}

package ar.edu.itba.pod.Legajo47399.simul.comunication.payload.handler;

import java.rmi.RemoteException;

import ar.edu.itba.pod.Legajo47399.simul.nodo.Node;
import ar.edu.itba.pod.simul.communication.Message;

public class HandlerResourceTransferCanceled implements HandlerPayload {
	private static HandlerPayload instance;
	private Node myNode;
	
	
	private HandlerResourceTransferCanceled(Node myNode) {
		super();
		this.myNode = myNode;
	}
	@Override
	public void execute(Message m) {
		
		try {
			myNode.getMyConnectionManager().getThreePhaseCommit().abort();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static HandlerPayload getInstance(Node myNode) {
		if(instance==null){
			instance=new HandlerResourceTransferCanceled(myNode);
		}
		return instance;
	}

}

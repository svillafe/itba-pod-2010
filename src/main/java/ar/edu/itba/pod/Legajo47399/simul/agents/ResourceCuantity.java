package ar.edu.itba.pod.Legajo47399.simul.agents;

import ar.edu.itba.pod.simul.market.Resource;

public class ResourceCuantity {
	Integer cant;
	Resource recurso;
	public ResourceCuantity(Integer cant, Resource recurso) {
		super();
		this.cant = cant;
		this.recurso = recurso;
	}
	public Integer getCant() {
		return cant;
	}
	public void setCant(Integer cant) {
		this.cant = cant;
	}
	public Resource getRecurso() {
		return recurso;
	}
	public void setRecurso(Resource recurso) {
		this.recurso = recurso;
	}
	
	
}

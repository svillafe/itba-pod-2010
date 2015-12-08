package ar.edu.itba.pod.simul.market;

import java.io.Serializable;

import ar.edu.itba.pod.thread.doc.Immutable;


/**
 * Resource type 
 */
@Immutable
public class Resource implements Serializable {
	private final String category;
	private final String name;

	public Resource(String category, String name) {
		super();
		this.category = category;
		this.name = name;
	}
	
	public String category() {
		return category;
	}
	
	public String name() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (category != null) {
			return String.format("%s (%s)", name, category); 
		}
		return name;
	}
}

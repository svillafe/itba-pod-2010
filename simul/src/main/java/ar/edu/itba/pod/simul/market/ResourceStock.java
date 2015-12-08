package ar.edu.itba.pod.simul.market;

/**
 * Representation of a resource stock
 */
public interface ResourceStock {
	/**
	 * Stock name, should identify the stock location
	 */
	public String name(); 
	
	/**
	 * Returns the type of resource being stocked
	 * @return
	 */
	public Resource resource();
	
	/**
	 * Returns the current amount of stock
	 * @return The current amount of stock
	 */
	public int current();

	/**
	 * Adds <code>size</code> units to the stock
	 * @param size units to add to the stock
	 */
	public void add(int size);
	
	/**
	 * Removes <code>size</code> units from the stock
	 * @param size units to remove
	 * @throws IllegalArgumentException if there are not enough items in the stock 
	 */
	public void remove(int size);
}

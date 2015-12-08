package ar.edu.itba.pod.simul.time;

import java.util.concurrent.TimeUnit;

/**
 * A Time Mapper converts between simulated time and real time.
 */
public interface TimeMapper {
	/**
	 * Converts the given amount simulated time of time to realtime miliseconds 
	 * @param amount amount of simulated time
	 * @param unit unit of simulated time
	 * @return realtime equivalent in miliseconds
	 */
	public long toMillis(int amount, TimeUnit unit);
}

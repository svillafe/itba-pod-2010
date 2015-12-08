package ar.edu.itba.pod.simul.time;

import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.thread.doc.Immutable;

import com.google.common.base.Preconditions;

/**
 * Static factory for Tiume mappers.
 */
public class TimeMappers {
	private static final TimeMapper REALTIME = new ScaledMapper(1L, 1L);
	
	public static TimeMapper realtime() {
		return REALTIME;
	}
	
	/**
	 * Returns a time mapper that transforms each given <code> amount </code> units of
	 * <code> unit </code> to one second in real time
	 * <p>
	 * calling <code>oneSecondEach(1, TimeUnit.SECONDS) </code> means realtime,
	 * while calling <code>oneSecondEach(1, TimeUnit.MINUTE) </code> will mean that
	 * each minute will be mapped to one second (i.e.: faster than real time)
	 * </p>
	 * @param amount the amount of time  
	 * @param unit The unit of time
	 * @return A time provider that maps time based on the parameters
	 */
	public static TimeMapper oneSecondEach(int amount, TimeUnit unit) {
		long milis = unit.toMillis(amount);
		Preconditions.checkArgument(milis > 0, "Clock too fast!");
		
		long gcd = gcd(milis, 1000);
		
		return new ScaledMapper(milis / gcd, 1000 / gcd);
	}
	
   private static long gcd(long x, long y) {
        if (x < 0) {
        	x = -x;
        }
        if (y < 0) {
        	y = -y;
        }
        return (y == 0) ? x : gcd(y, x % y);
    }	

	@Immutable
	private static class ScaledMapper implements TimeMapper {
		private final long numerator;
		private final long denominator;
		
		public ScaledMapper(long numerator, long denominator) {
			super();
			Preconditions.checkArgument(denominator > 0, "invalid denominator");
			this.numerator = numerator;
			this.denominator = denominator;
		}

		@Override
		public long toMillis(int amount, TimeUnit unit) {
			// this is toMilis(...) / fraction
			return unit.toMillis(amount) * denominator / numerator;
		}
	}
	
}

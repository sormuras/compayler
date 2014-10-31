package databench;

import java.util.concurrent.TimeUnit;

public class Watch {

	private final long t0 = System.nanoTime();

	static public Watch start() {
		return new Watch();
	}

	public long nanosEllapsed() {
		return System.nanoTime() - t0;
	}

	public double secondsEllapsed() {
		return TimeUnit.SECONDS.convert(nanosEllapsed(), TimeUnit.NANOSECONDS);
	}

	private Watch() {
	}
}
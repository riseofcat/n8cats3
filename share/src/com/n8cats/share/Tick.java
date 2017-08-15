package com.n8cats.share;

public class Tick extends IntHashStrEquals {
public int tick;
protected int getInt() {
	return tick;
}

public static class Add extends IntHashStrEquals {
	public int add;
	protected int getInt() {
		return add;
	}
}
}

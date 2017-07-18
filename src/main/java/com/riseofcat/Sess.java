package com.riseofcat;

abstract public class Sess {
public final long startTimeMs;
public final int id;
public Sess(int id) {
	startTimeMs = System.currentTimeMillis();
	this.id = id;
}
public abstract void send(String message);
public abstract void stop();
}

package com.riseofcat.session;

abstract public class AbstSesServ<C, S, Extra> {
abstract public void start(Ses session);
abstract public void close(Ses session);
abstract public void message(Ses ses, C code);

public abstract class Ses {
	public final long startTimeMs;
	public final int id;
	public Ses(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	abstract public void stop();
	abstract public void send(S message);
	abstract public Extra getExtra();
}

}

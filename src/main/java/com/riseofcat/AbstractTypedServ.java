package com.riseofcat;

abstract public class AbstractTypedServ<C,S> {
abstract public void start(Session<S> s);
abstract public void close(Session<S> session);
abstract public void message(AbstractTypedServ.Session<S> session, C data);

public static abstract class Session<S> {
	public final int id;
	public Session(int id) {
		this.id = id;
	}
	abstract public void send(S data);
	abstract public void stop();
}

}

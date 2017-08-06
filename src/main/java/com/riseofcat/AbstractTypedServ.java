package com.riseofcat;

abstract public class AbstractTypedServ<C, S> {
abstract public void abstractStart(Ses<S> s);
abstract public void abstractClose(Ses<S> session);
abstract public void abstractMessage(Ses<S> session, C data);

public static abstract class Ses<S> {
	public final int id;
	public Ses(int id) {
		this.id = id;
	}
	abstract public void send(S data);
	abstract public void stop();
}

}

package com.riseofcat;
abstract public class AbstractPayloadServer<C, S> {
public AbstractPayloadServer() {
}
abstract public void starts(Session<C,S> session);
abstract public void payloadMessage(Session<C,S> session, C payload);
abstract public void closed(Session<C,S> session);

public static abstract class Session<C, S> {
	public final int id;
	public Session(int id) {
		this.id = id;
	}
	abstract public void send(S payload);
	abstract public void stop();
	abstract public Integer getLatency();
}
}

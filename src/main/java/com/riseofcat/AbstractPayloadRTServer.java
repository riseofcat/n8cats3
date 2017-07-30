package com.riseofcat;
abstract public class AbstractPayloadRTServer<C, S> {
public AbstractPayloadRTServer() {
}
abstract public void start(Session<S> session);
abstract public void payloadMessage(Session<S> session, C payload);
abstract public void close(Session<S> session);

public static abstract class Session<S> {
	public final int id;
	public Session(int id) {
		this.id = id;
	}
	abstract public void send(S payload);
	abstract public void stop();
	abstract public Integer getLatency();
}
}

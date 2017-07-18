package com.riseofcat;
import java.io.Reader;
abstract public class IRealTimeServer {
final public void message(Sess ses, Reader reader) {
	ses.incomeCalls++;
	message2(ses, reader);
}
protected abstract void message2(Sess ses, Reader reader);
final public void message(Sess ses, String message) {
	ses.incomeCalls++;
	message2(ses, message);
}
protected abstract void message2(Sess ses, String message);
abstract public void starts(Sess session);
abstract public void closed(Sess session);

public static abstract class Sess {
	public final long startTimeMs;
	public final int id;
	public int incomeCalls;
	public int outCalls;
	public Sess(int id) {
		startTimeMs = System.currentTimeMillis();
		this.id = id;
	}
	public void send(String message) {
		outCalls++;
	}
	public abstract void stop();
}
}

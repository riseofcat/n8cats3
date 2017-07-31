package com.riseofcat;
import java.io.Reader;
abstract public class AbstractStringRTServer extends AbstractRTServer {
final public void message(Session ses, Reader reader) {
	ses.incomeCalls++;
	abstractMessage(ses, reader);
}
final public void message(Session ses, String message) {
	ses.incomeCalls++;
	abstractMessage(ses, message);
}
abstract protected void abstractMessage(Session ses, Reader reader);
abstract protected void abstractMessage(Session ses, String message);

public static abstract class Session extends AbstractRTServer.Session{
	private int incomeCalls;
	private int outCalls;
	public Session(int id) {
		super(id);
	}
	public void send(String message) {
		outCalls++;
	}
	@Override
	public int getIncomeCalls() {
		return incomeCalls;
	}
	@Override
	public int getOutCalls() {
		return outCalls;
	}
}

}

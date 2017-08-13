package com.riseofcat;
abstract public class AbstSesServ<C, S, E> {
abstract public void start(Ses session);
abstract public void close(Ses session);
abstract public void message(Ses ses, C code);

public abstract class Ses {
	private TypeMap typeMapCache;
	abstract public int getId();
	abstract public void stop();
	abstract public void send(S message);
	abstract public E getExtra();
	abstract protected TypeMap getTypeMap();
	public <T> void put(T value) {
		if(typeMapCache == null) {
			typeMapCache = getTypeMap();
		}
		typeMapCache.put(value);
	}
	public <T> T get(Class<T> type) {
		return getTypeMap().get(type);
	}
}

}

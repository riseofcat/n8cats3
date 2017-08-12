package com.riseofcat;
abstract public class AbstSesServ<C, S, E> {
abstract public void start(Ses session);
abstract public void close(Ses session);
abstract public void message(Ses ses, C code);

public abstract class Ses {
	abstract public int getId();
	abstract public void stop();
	abstract public void send(S message);
	abstract public E getExtra();
}

}

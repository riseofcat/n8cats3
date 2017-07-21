package com.n8cats.share;

public class ServerPayload {
public String message;
public Object welcome;//x,y surround[type,x,y,speed]
public Object actions;//[time,action]
public State state;
public static class State {
	public Logic.Car[] cars;
}
}

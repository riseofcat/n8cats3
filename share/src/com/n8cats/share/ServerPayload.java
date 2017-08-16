package com.n8cats.share;

import java.util.ArrayList;
import java.util.HashSet;

public class ServerPayload {
public Welcome welcome;
public int tick;
public State state;
public ArrayList<PlayerActions> actions;
public HashSet<Integer> canceledActions;
public ArrayList<DelayedActions> delayedActions;
public ServerError error;

public static class PlayerActions {
	public Logic.Player.Id id;
	public ArrayList<TickActions> ticks;
}

public static class TickActions {
	public int wait;
	public ArrayList<Logic.Action> actions;
}

public static class Welcome {
	public Logic.Player.Id id;
}

public static class DelayedActions {
	public int delay;
	HashSet<Integer> actions;
}

public static class ServerError {
	public int code;
	public String message;

}

public static class State {
	public int tick;
	public Logic.State state;
}
}

package com.n8cats.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class ServerPayload {
public Tick tick;
public Welcome welcome;
public TreeMap<Tick.Add, HashMap<Logic.Player.Id, ArrayList<Logic.Action>>> tickActions;
public Logic.State state;
public static class Welcome {
	public float x,y;
}
}

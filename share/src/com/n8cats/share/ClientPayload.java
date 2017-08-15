package com.n8cats.share;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientPayload {
public Tick tick;
public HashMap<Tick.Add, ArrayList<Logic.Action>> actions;
public Logic.Action action;
}

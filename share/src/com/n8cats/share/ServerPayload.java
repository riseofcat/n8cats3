package com.n8cats.share;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

public class ServerPayload {
public int tick;
@Nullable public Welcome welcome;
@Nullable public Stable stable;
@Nullable public ArrayList<TickActions> actions;
@Nullable public HashSet<Integer> canceled;
@Nullable public ArrayList<AppliedActions> apply;
@Nullable public ServerError error;

public static class TickActions {
	public int tick;
	public ArrayList<PlayerAction> list;//Порядок важен
}

public static class PlayerAction {//todo перенести в Logic и может extends от Action
	public Logic.Player.Id id;
	public Logic.Action action;
	public int actionVersion;//todo redundant for client
}

public static class Welcome {
	public Logic.Player.Id id;
}

public static class AppliedActions {
	@SuppressWarnings("unused")
	public AppliedActions() {
	}
	public AppliedActions(int aid, int delay) {
		this.aid = aid;
		this.delay = delay;
	}
	public int aid;
	public int delay;
}

public static class ServerError {
	public int code;
	public String message;

}

public static class Stable {
	public int tick;//все actions уже пришли и новых больше не будет. Если кто-то кого-то убил, то в этом кадре засчитывается фраг. Но само убийство и набор очков мог произойти в прошлом
	public Logic.State state;
}

}

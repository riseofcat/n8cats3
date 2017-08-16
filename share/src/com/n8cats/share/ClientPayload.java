package com.n8cats.share;

import java.util.ArrayList;

public class ClientPayload {
public int tick;
public ArrayList<TickActions> actions;

public static class TickActions {
	public int id;//Если действия будут отложены или не применимы то сервер сообщит по id-шнику. Этот id будет рецеркулировать, например от 0 до 255
	public int wait;
	public ArrayList<Logic.Action> actions;
}

}

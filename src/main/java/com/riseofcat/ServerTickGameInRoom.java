package com.riseofcat;
import com.n8cats.share.Logic;

import java.util.Timer;
import java.util.TimerTask;
public class ServerTickGameInRoom {
private final RoomsServer.Room room;
private final Logic logic;
public final int reconcilationTicks = 10;
public final int oldTicks = 20;
private int tick = 0;

public ServerTickGameInRoom(RoomsServer.Room room, Logic logic) {
	this.room = room;
	this.logic = logic;
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			App.log.info("timer tick");
		}
	}, 0, Logic.UPDATE_MS);
}

public static class Reconciliation {

}
}

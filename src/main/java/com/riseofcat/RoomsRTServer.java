package com.riseofcat;

import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.riseofcat.session.AbstSesServ;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class RoomsRTServer extends AbstSesServ<ClientPayload, ServerPayload, PingPongServ.ExtraLatency> {
public final static int MAXIMUM_ROOM_PLAYERS = 5;
public final Signal<Room> onRoomCreated = new Signal<>();
//todo onRoomDestroyed
private final Set<Room> rooms = new HashSet<>();
private final Map<Ses<ServerPayload, PingPongServ.ExtraLatency>, Room> sessions = new ConcurrentHashMap<>();
@Override
public void abstractStart(Ses<ServerPayload, PingPongServ.ExtraLatency> session) {
	Room room = null;
	synchronized(this) {
		for(Room r : rooms) {
			if(r.getPlayersCount() < MAXIMUM_ROOM_PLAYERS) {
				room = r;
				break;
			}
		}
		if(room == null) {
			App.log.info("new room created");
			room = new Room();
			rooms.add(room);
			onRoomCreated.dispatch(room);
		}
		room.add(session);
	}
	sessions.put(session, room);
}
@Override
public void abstractClose(Ses<ServerPayload, PingPongServ.ExtraLatency> session) {
	Room room = sessions.remove(session);
	room.remove(session);
}
@Override
public void abstractMessage(Ses<ServerPayload, PingPongServ.ExtraLatency> session, ClientPayload payload) {
	Room room = sessions.get(session);
	room.message(session, payload);
}
public class Room {
	final public Signal<Player> onPlayerAdded = new Signal<>();
	final public Signal<Player> onPlayerRemoved = new Signal<>();
	final public Signal<PlayerMessage> onMessage = new Signal<>();
	private final Map<Ses<ServerPayload, PingPongServ.ExtraLatency>, Player> players = new ConcurrentHashMap<>();
	public int getPlayersCount() {
		return players.size();
	}
	public Collection<Player> getPlayers() {
		return players.values();
	}
	private void add(Ses<ServerPayload, PingPongServ.ExtraLatency> session) {
		Player player = new Player(session);
		players.put(session, player);
		synchronized(this) {
			onPlayerAdded.dispatch(player);
		}
	}
	private void message(Ses<ServerPayload, PingPongServ.ExtraLatency> session, ClientPayload payload) {
		onMessage.dispatch(new PlayerMessage(players.get(session), payload));
	}
	private void remove(Ses<ServerPayload, PingPongServ.ExtraLatency> session) {
		Player remove = players.remove(session);
		synchronized(this) {
			onPlayerRemoved.dispatch(remove);
		}
	}
	public class Player extends Logic.Player {
		public final Ses<ServerPayload, PingPongServ.ExtraLatency> session;
		public Player(Ses<ServerPayload, PingPongServ.ExtraLatency> session) {
			this.session = session;
		}
		@Override
		public Id getId() {
			return new Id(session.id);
		}
	}
}
public class PlayerMessage {
	public final Room.Player player;
	public final ClientPayload payload;
	public PlayerMessage(Room.Player player, ClientPayload payload) {
		this.player = player;
		this.payload = payload;
	}
}
}

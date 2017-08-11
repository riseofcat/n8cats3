package com.riseofcat;

import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.riseofcat.session.AbstSesServ;
import com.riseofcat.session.CountSesServ;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class RoomsRTServer extends AbstSesServ<ClientPayload, ServerPayload, PingPongServ.ExtraLatency<CountSesServ.ExtraCount<Void>>> {
public final static int MAXIMUM_ROOM_PLAYERS = 5;
public final Signal<Room> onRoomCreated = new Signal<>();
//todo onRoomDestroyed
private final Set<Room> rooms = new HashSet<>();
private final Map<Ses, Room> map = new ConcurrentHashMap<>();
@Override
public void start(Ses session) {
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
	map.put(session, room);
}
@Override
public void close(Ses session) {
	Room room = map.remove(session);
	room.remove(session);
}
@Override
public void message(Ses session, ClientPayload payload) {
	Room room = map.get(session);
	room.message(session, payload);
}
public class Room {
	final public Signal<Player> onPlayerAdded = new Signal<>();
	final public Signal<Player> onPlayerRemoved = new Signal<>();
	final public Signal<PlayerMessage> onMessage = new Signal<>();
	private final Map<Ses, Player> players = new ConcurrentHashMap<>();
	public int getPlayersCount() {
		return players.size();
	}
	public Collection<Player> getPlayers() {
		return players.values();
	}
	private void add(Ses session) {
		Player player = new Player(session);
		players.put(session, player);
		synchronized(this) {
			onPlayerAdded.dispatch(player);
		}
	}
	private void message(Ses session, ClientPayload payload) {
		onMessage.dispatch(new PlayerMessage(players.get(session), payload));
	}
	private void remove(Ses session) {
		Player remove = players.remove(session);
		synchronized(this) {
			onPlayerRemoved.dispatch(remove);
		}
	}
	public class Player extends Logic.Player {
		public final Ses session;
		public Player(Ses session) {
			this.session = session;
		}
		@Override
		public Id getId() {
			return new Id(session.getId());
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

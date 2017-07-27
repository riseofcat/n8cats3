package com.riseofcat;

import com.n8cats.lib_gwt.Signal;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class RoomsServer extends AbstractPayloadServer<ClientPayload, ServerPayload> {
public final static int MAXIMUM_ROOM_PLAYERS = 5;
public final Signal<Room> onRoomCreated = new Signal<>();
//todo onRoomDestroyed
private final List<Room> rooms = new ArrayList<>();
private final Map<Session<ClientPayload, ServerPayload>, Room> sessions = new ConcurrentHashMap<>();
@Override
public void start(Session<ClientPayload, ServerPayload> session) {
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
public void payloadMessage(Session<ClientPayload, ServerPayload> session, ClientPayload payload) {
	sessions.get(session).message(session, payload);
}
@Override
public void close(Session<ClientPayload, ServerPayload> session) {
	Room room = sessions.remove(session);
	room.remove(session);
}
public class Room {
	final public Signal<Player> onPlayerAdded = new Signal<>();
	final public Signal<Player> onPlayerRemoved = new Signal<>();
	final public Signal<PlayerMessage> onMessage = new Signal<>();
	private final Map<Session<ClientPayload, ServerPayload>, Player> sessions = new ConcurrentHashMap<>();
	public int getPlayersCount() {
		return sessions.size();
	}
	public Collection<Player> getPlayers() {
		return sessions.values();//todo optimize
	}
	private void add(Session<ClientPayload, ServerPayload> session) {
		Player player = new Player(session);
		sessions.put(session, player);
		synchronized(this) {
			onPlayerAdded.dispatch(player);
		}
	}
	private void message(Session<ClientPayload, ServerPayload> session, ClientPayload payload) {
		onMessage.dispatch(new PlayerMessage(sessions.get(session), payload));
	}
	private void remove(Session<ClientPayload, ServerPayload> session) {
		Player remove = sessions.remove(session);
		synchronized(this) {
			onPlayerRemoved.dispatch(remove);
		}
	}
	public class Player extends Logic.Player {
		public final Session<ClientPayload, ServerPayload> session;
		public Player(Session<ClientPayload, ServerPayload> session) {
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

package com.riseofcat;

import com.n8cats.share.ClientPayload;
import com.n8cats.share.ServerPayload;
public class GameRealtimeServer extends AbstractGameRealtimeServer<ClientPayload, ServerPayload> {
@Override
public void starts(JsonRealtimeServer<ClientPayload, ServerPayload>.Sess2 sess2) {
	App.log.info("start");
	ServerPayload payload = new ServerPayload();
	payload.message = "from server";
	sess2.send(payload);
}
@Override
public void payloadMessage(JsonRealtimeServer<ClientPayload, ServerPayload>.Sess2 sess2, ClientPayload payload) {
	App.log.info(payload.message);
}
@Override
public void closed(AbstractRealTimeServer.Session sess) {
	App.log.info("close");
}
}

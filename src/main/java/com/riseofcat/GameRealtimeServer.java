package com.riseofcat;

import com.n8cats.share.ClientPayload;
import com.n8cats.share.ServerPayload;
public class GameRealtimeServer extends AbstractPayloadServer<ClientPayload, ServerPayload> {
@Override
public void starts(Session<ClientPayload, ServerPayload> session) {
	App.log.info("start");
	ServerPayload payload = new ServerPayload();
	payload.message = "from server";
	session.send(payload);
}
@Override
public void payloadMessage(Session<ClientPayload, ServerPayload> session, ClientPayload payload) {
	App.log.info(payload.message);
}
@Override
public void closed(Session<ClientPayload, ServerPayload> session) {
	App.log.info("close");
}
}

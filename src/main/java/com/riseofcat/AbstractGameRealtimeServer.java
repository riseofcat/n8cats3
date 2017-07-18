package com.riseofcat;
abstract public class AbstractGameRealtimeServer<C, S> {
abstract public void starts(JsonRealtimeServer<C, S>.Sess2 sess2);
abstract public void payloadMessage(JsonRealtimeServer<C, S>.Sess2 sess2, C payload);
abstract public void closed(Sess sess);
}

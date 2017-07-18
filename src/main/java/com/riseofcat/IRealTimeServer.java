package com.riseofcat;
import java.io.Reader;
public interface IRealTimeServer {
void starts(Sess session);
void closed(Sess session);
void message(Sess sparkSess, Reader reader);
void message(Sess sparkSess, String message);
}

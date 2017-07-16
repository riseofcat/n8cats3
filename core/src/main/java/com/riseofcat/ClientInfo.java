package com.riseofcat;
import com.n8cats.lib_gwt.IAction;
public interface ClientInfo {
int sinceLaunchTimeMs();
//int launchUnixTimeSec();
//int totalRenderTimeMs();
//float fps();
//String deviceName();
//int unixTimeSec();
//int timeZoneSec();
void wait(int ms, IAction action);
}

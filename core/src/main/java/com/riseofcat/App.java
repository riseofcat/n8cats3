package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.n8cats.lib_gwt.ILog;

import java.util.List;

public class App {
private static Long createMs;
public static Context context;
static {

}
public static final ILog log = new ILog() {
	{

	}
	private static final String TAG = "n8cats";
	public String info(String s) {
		Gdx.app.log(TAG, s);
		return s;
	}
	public void error(String s) {
		Gdx.app.error(TAG, s);
	}
	public void warning(String s) {
		Gdx.app.error(TAG, s);
	}
	public void debug(String s) {
		info("debug " + s);//todo
		Gdx.app.debug(TAG, s);
	}
};
public static void create() {//todo вынести
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
}
public static long timeMs() {//todo предусмотреть перевод времени
	if(false) System.currentTimeMillis();
	long result = TimeUtils.millis();
	if(createMs == null) createMs = result;
	return result;
}
public static void breakpoint() {
	int a = 1+1;
}
public interface Context {
	<T>List<T> createConcurrentList();
}
}

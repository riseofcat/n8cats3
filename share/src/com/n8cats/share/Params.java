package com.n8cats.share;

public class Params {
public static final String TITLE = "mass-power.io";
public static final int DEFAULT_LATENCY_MS = 100;
public static final int DELAY_TICKS = Params.DEFAULT_LATENCY_MS * 3 / Logic.UPDATE_MS + 1;//количество тиков для хранения действий //bigger delayed
public static final int REMOVE_TICKS = DELAY_TICKS * 2;//bigger removed
public static final int FUTURE_TICKS = DELAY_TICKS * 2;

}

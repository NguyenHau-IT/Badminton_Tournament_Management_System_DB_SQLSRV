package com.example.btms.service.scoreboard;

import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.util.log.Log;

public final class ScoreboardRemote {
	private static final ScoreboardRemote INSTANCE = new ScoreboardRemote();

	public static ScoreboardRemote get() {
		return INSTANCE;
	}

	private final Object LOCK = new Object();
	private final BadmintonMatch sharedMatch = new BadmintonMatch();
	private final Log sharedLog = new Log();

	private ScoreboardRemote() {
	}

	public BadmintonMatch match() {
		return sharedMatch;
	}

	public Log log() {
		return sharedLog;
	}

	public Object lock() {
		return LOCK;
	}

	public <T> T withLock(java.util.function.Supplier<T> fn) {
		synchronized (LOCK) {
			return fn.get();
		}
	}

	public void withLock(Runnable r) {
		synchronized (LOCK) {
			runQuiet(r);
		}
	}

	private static void runQuiet(Runnable r) {
		if (r != null)
			r.run();
	}
}
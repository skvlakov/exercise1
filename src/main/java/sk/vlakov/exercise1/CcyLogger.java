package sk.vlakov.exercise1;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CcyLogger {
	private static Logger mLogger = null;
	private static ConsoleHandler mConsoleHandler = null;

	public static synchronized Logger getLogger() {
		if (mLogger == null) {
			mLogger = Logger.getLogger(CcyLogger.class.getName());
			mConsoleHandler = new ConsoleHandler();
			mLogger.addHandler(mConsoleHandler);
			mLogger.setLevel(Level.SEVERE);
			mConsoleHandler.setLevel(Level.SEVERE);
		}
		return mLogger;
	}

	public static synchronized void setLogger(Level aLevel) {
		mLogger.setLevel(aLevel);
		mConsoleHandler.setLevel(aLevel);
	}

}

package nohorjo.application;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Service;
import android.content.Context;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;
import static nohorjo.settings.SettingsManager.*;

public abstract class App {

	protected static final String LINES_IN_OUT = "50";
	public static final String TRACKING_ENABLED = "true";
	public static final String INTERVAL_SECONDS = "15";
	public static final String GPS_CHECK_INTERVAL = "60";
	public static final String GPS_TRACKING_ACCURACY = "500";
	public static final String SERVICE_HEART_BEAT = "60";
	public static final String RETRY_LIMIT = "5";
	public static final String MAX_FILE_SIZE = "1024";
	public static final String REMOTE_RETRY_DELAY = "60";
	private static Context context;

	public static String key() throws SettingsException {
		return getPhoneNumber().substring(1);
	}

	public static int port() throws SettingsException {
		return Integer.parseInt(getPhoneNumber().substring(1, 5));
	}

	public static String getPhoneNumber() throws SettingsException {
		return SettingsManager.getSetting(PHONE_NUMBER);
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		if (App.context == null || !(App.context instanceof Service)) {
			App.context = context;
		}
	}

	public static UncaughtExceptionHandler getExceptionHandler() {
		return new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				FileOut.printStackTrace(ex);
			}
		};
	}
}

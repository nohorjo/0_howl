package nohorjo.howl;

import nohorjo.application.App;
import nohorjo.howl.howlers.SMSHowler;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

/**
 * Controls the usage of {@link Howler}s especially in a multithreaded
 * environment
 * 
 * @author muhammed
 *
 */
public abstract class HowlController {
	private static boolean isHowling;

	/**
	 * The running thread
	 */
	static {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					if (isHowling) {
						SMSHowler.howl();
						try {
							Thread.sleep(Integer.parseInt(
									SettingsManager.getSetting(SettingsManager.INTERVAL_SECONDS, App.INTERVAL_SECONDS))
									* 1000);
						} catch (InterruptedException e) {
							FileOut.printStackTrace(e);
						}
					}
				}
			}
		}.start();
	}

	public static synchronized void stop() {
		FileOut.println("Howling stopped");
		SMSHowler.cancel();
		isHowling = false;
	}

	public static synchronized void start() {
		if (!isHowling) {
			try {
				App.getPhoneNumber();
				FileOut.println("Howling");
				isHowling = true;
			} catch (SettingsException e) {
				FileOut.println("Phone number not set");
			}
		}
	}

	public static synchronized void respond(String command) {
		SMSHowler.respond(command);
	}

	public static boolean isHowling() {
		return isHowling;
	}
}

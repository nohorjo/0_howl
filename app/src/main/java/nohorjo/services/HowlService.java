package nohorjo.services;

import static nohorjo.settings.SettingsManager.INTERVAL_SECONDS;
import static nohorjo.settings.SettingsManager.PHONE_NUMBER;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import nohorjo.application.App;
import nohorjo.output.FileOut;
import nohorjo.remote.howlserver.HowlComms;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public class HowlService extends Service {
	private static boolean running;

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(App.getExceptionHandler());
		new Thread() {
			@Override
			public void run() {
				startService();
			};
		}.start();
	}

	private void startService() {
		if (!running || !HowlComms.isAlive()) {
			FileOut.println("Starting service...");
			App.setContext(this);
			// wait to load phone number
			new Thread() {
				@Override
				public void run() {
					String phoneNumber;
					while (true) {
						try {
							try {
								phoneNumber = SettingsManager.getSetting(PHONE_NUMBER);
								if (phoneNumber != null) {
									initHowler();
									break;
								}
							} catch (SettingsException e) {
								FileOut.println("Phone number not set");
							}
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							FileOut.printStackTrace(e);
						}
					}
				};
			}.start();
		} else {
			FileOut.println("Service already running");
		}
	}

	protected synchronized void initHowler() {
		try {
			HowlComms.init();
			running = true;
			FileOut.println("Service started!");
			while (true)
				continue;
		} catch (Exception e) {
			FileOut.printStackTrace(e);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onCreate();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			FileOut.println("Destroying service");
			running = false;
			HowlBR.schedule(this);
		} catch (Throwable e) {
			FileOut.printStackTrace(e);
		}
	}
}

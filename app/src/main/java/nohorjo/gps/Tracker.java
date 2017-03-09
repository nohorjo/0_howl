package nohorjo.gps;

import static nohorjo.settings.SettingsManager.GPS_CHECK_INTERVAL;
import static nohorjo.settings.SettingsManager.GPS_TRACKING_ACCURACY;
import static nohorjo.settings.SettingsManager.TRACKING_ENABLED;

import java.io.IOException;

import nohorjo.application.App;
import nohorjo.output.FileOut;
import nohorjo.remote.howlserver.HowlComms;
import nohorjo.remote.wordpress.IPHandler;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;
import nohorjo.socket.SocketClient;

public abstract class Tracker {
	private static SocketClient client;
	private static boolean running = true;
	private static double[] oldLatLong = new double[] { -40, -140 };

	static {
		while (true) {
			try {
				client = new SocketClient(IPHandler.getIPAddress(), App.port());
				break;
			} catch (IOException e) {
				try {
					IPHandler.reloadIPAddress();
				} catch (Exception e1) {
					FileOut.printStackTrace(e1);
				}
			} catch (SettingsException e) {
				FileOut.printStackTrace(e);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void start() {
		new Thread() {
			@Override
			public void run() {
				while (running) {
					try {
						checkLocation(false);
						Thread.sleep(Integer
								.parseInt(SettingsManager.getSetting(GPS_CHECK_INTERVAL, App.GPS_CHECK_INTERVAL)));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public static void checkLocation(boolean force) {
		if (Boolean.parseBoolean(SettingsManager.getSetting(TRACKING_ENABLED, App.TRACKING_ENABLED))) {
			double[] newLatLong = GPSManager.getLatLong();
			if (force || distance(newLatLong) > Integer
					.parseInt(SettingsManager.getSetting(GPS_TRACKING_ACCURACY, App.GPS_TRACKING_ACCURACY))) {
				oldLatLong = newLatLong;
				postLocation();
			}
		}
	}

	private static void postLocation() {
		try {
			FileOut.println("Posting location update: " + oldLatLong[0] + " " + oldLatLong[1]);
			HowlComms.postLocation(oldLatLong);
		} catch (IOException e) {
			try {
				HowlComms.init();
				HowlComms.postLocation(oldLatLong);
			} catch (Exception e1) {
				FileOut.println("Failed to post location\n\t" + e1.getMessage());
				e1.printStackTrace();
			}
		}

	}

	private static double distance(double[] newLatLong) {
		double lat1 = oldLatLong[0];
		double lat2 = newLatLong[0];
		double a = Math.pow(Math.sin(Math.toRadians(lat2 - lat1) / 2), 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
						* Math.pow(Math.sin(Math.toRadians(newLatLong[1] - oldLatLong[1]) / 2), 2);
		return 6371e3 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	public static void destroy() {
		try {
			running = false;
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package nohorjo.gps;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import nohorjo.application.App;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsManager;

public abstract class GPSManager {

	public static String getLatLongString() {
		double[] latLong = getLatLong();
		return latLong[0] + " " + latLong[1];
	}

	public static double[] getLatLong() {
		// wait for permission
		while (!SettingsManager.isRequiredPermissionsGranted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Location last = getLastKnownLocation();
		if (last == null) {
			FileOut.println("Location unknown!");
			return new double[] { -40, -140 };
		}
		return new double[] { last.getLatitude(), last.getLongitude() };
	}

	private static Location getLastKnownLocation() {
		LocationManager mLocationManager = ((LocationManager) App.getContext()
				.getSystemService(Context.LOCATION_SERVICE));
		List<String> providers = mLocationManager.getProviders(true);
		Location bestLocation = null;
		for (String provider : providers) {
			Location l = mLocationManager.getLastKnownLocation(provider);
			if (l == null) {
				continue;
			}
			if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
				bestLocation = l;
			}
		}
		return bestLocation;
	}
}

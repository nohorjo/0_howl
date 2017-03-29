package nohorjo.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import nohorjo.application.App;
import nohorjo.common.CommonUtils;
import nohorjo.output.FileOut;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public abstract class SettingsManager {

	public static final String PHONE_NUMBER = "phone_number", INTERVAL_SECONDS = "interval_seconds",
			MAX_FILE_SIZE = "max_file_size", GPS_CHECK_INTERVAL = "gps_check_interval",
			GPS_TRACKING_ACCURACY = "gps_tracking_accuracy", SERVICE_HEART_BEAT = "service_heart_beat",
			LINES_IN_OUT = "lines_in_out", RETRY_LIMIT = "retry_limit", REMOTE_RETRY_DELAY = "remote_retry_delay",
			TRACKING_ENABLED = "tracking_enabled", DEFAULT_HOME = "default_home";

	private static final String settingsFile = "howl.settings";

	private static String[] requiredPermissions = { ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, SEND_SMS };
	private static String[] storagePermissions = { WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE };
	private static String[] otherPermissions = { READ_SMS, RECEIVE_SMS, INTERNET };

	static {
		requestPermissions();
	}

	private static void requestPermissions() {
		String[] permissions = CommonUtils.arrayConcat(requiredPermissions, storagePermissions, otherPermissions);

		if (App.getContext() instanceof Activity) {
			((Activity) App.getContext()).requestPermissions(permissions, 0);
		}
	}

	public static String getSetting(String setting, String defaultVal) {
		try {
			return getSetting(setting);
		} catch (SettingsException e) {
			setSetting(setting, defaultVal);
			return defaultVal;
		}
	}

	public static String getSetting(String setting) throws SettingsException {
		String val = App.getContext().getSharedPreferences(settingsFile, 0).getString(setting, null);
		if (val == null) {
			throw new SettingsException(setting);
		}
		return val;
	}

	public static boolean isSet(String setting){
		try {
			getSetting(setting);
			return true;
		}catch (SettingsException e){
			return false;
		}
	}

	public static void setSetting(String setting, String value) {
		SharedPreferences.Editor editor = App.getContext().getSharedPreferences(settingsFile, 0).edit();
		editor.putString(setting, value);
		editor.commit();
		FileOut.println("Saved setting: " + setting + "=" + value);
	}

	public static boolean isStoragePermissionsGranted() {
		return checkPermissions(storagePermissions);
	}

	public static boolean isRequiredPermissionsGranted() {
		return checkPermissions(requiredPermissions);
	}

	private static boolean checkPermissions(String[] permissions) {
		for (String permission : permissions) {
			if (App.getContext().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}

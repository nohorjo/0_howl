package nohorjo.application;

import static nohorjo.settings.SettingsManager.GPS_CHECK_INTERVAL;
import static nohorjo.settings.SettingsManager.GPS_TRACKING_ACCURACY;
import static nohorjo.settings.SettingsManager.INTERVAL_SECONDS;
import static nohorjo.settings.SettingsManager.LINES_IN_OUT;
import static nohorjo.settings.SettingsManager.MAX_FILE_SIZE;
import static nohorjo.settings.SettingsManager.PHONE_NUMBER;
import static nohorjo.settings.SettingsManager.REMOTE_RETRY_DELAY;
import static nohorjo.settings.SettingsManager.RETRY_LIMIT;
import static nohorjo.settings.SettingsManager.SERVICE_HEART_BEAT;
import static nohorjo.settings.SettingsManager.TRACKING_ENABLED;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import nohorjo.howl.HowlController;
import nohorjo.howl.R;
import nohorjo.output.FileOut;
import nohorjo.services.HowlBR;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public class MainActivity extends HomeSetter {

	private EditText phoneNumberText, intervalText, gpsCheckInterval, remoteRetryDelay, maxFileSize, retryLimit,
			linesInOut, serviceHeartBeat, trackingAccuracyMeters;
	private Switch toggleRunning, trackingEnabledSwitch;
	private LinearLayout content;
	private Button activityView, settingsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(App.getExceptionHandler());
		App.setContext(this);
		setContentView(R.layout.activity_main);

		try {
			HowlBR.schedule(this);
			setContent(R.layout.activity);
			HowlController.start();
		} catch (Exception e) {
			FileOut.printStackTrace(e);
		}
	}

	@SuppressLint("InflateParams")
	private void setContent(int id) {
		if (content == null) {
			content = (LinearLayout) findViewById(R.id.content);
		}
		content.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();
		View menuLayout = inflater.inflate(id, null);
		content.addView(menuLayout);
		prepareWidgetsAndFields();
	}

	private void prepareWidgetsAndFields() {
		activityView = (Button) findViewById(R.id.activityView);
		settingsView = (Button) findViewById(R.id.settingsView);
		content = (LinearLayout) findViewById(R.id.content);
		activityView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setContent(R.layout.activity);
			}
		});
		settingsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setContent(R.layout.settings);
			}
		});

		prepareSettings();
		prepareActivity();
	}

	private void prepareActivity() {
		try {
			{
				toggleRunning = (Switch) findViewById(R.id.howlSwitch);
				toggleRunning.setChecked(HowlController.isHowling());
				toggleRunning.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							HowlController.start();
						} else {
							HowlController.stop();
						}
					}
				});
			}
			{
				setOutput();
			}
		} catch (Exception e) {
		}
	}

	private void prepareSettings() {
		try {
			try {

				((TextView) findViewById(R.id.versionText))
						.setText("Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
			} catch (Exception e) {
				FileOut.printStackTrace(e);
			}
			{
				trackingEnabledSwitch = (Switch) findViewById(R.id.trackingEnabled);
				boolean isEnabled = Boolean
						.parseBoolean(SettingsManager.getSetting(TRACKING_ENABLED, App.TRACKING_ENABLED));
				trackingEnabledSwitch.setChecked(isEnabled);

				trackingEnabledSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						SettingsManager.setSetting(TRACKING_ENABLED, Boolean.toString(isChecked));
						String message = isChecked ? "Enabled tracking" : "Disabled tracking";
						FileOut.println(message);
						Toast.makeText(getParent(), message, Toast.LENGTH_LONG).show();
					}
				});
			}
			{
				phoneNumberText = (EditText) findViewById(R.id.phoneNumberText);
				try {
					phoneNumberText.setText(SettingsManager.getSetting(PHONE_NUMBER));
				} catch (SettingsException e) {
				}

			}
			{
				intervalText = (EditText) findViewById(R.id.intervalText);
				intervalText.setText(SettingsManager.getSetting(INTERVAL_SECONDS, App.INTERVAL_SECONDS));
			}
			{
				gpsCheckInterval = (EditText) findViewById(R.id.gpsCheckInterval);
				gpsCheckInterval.setText(SettingsManager.getSetting(GPS_CHECK_INTERVAL, App.GPS_CHECK_INTERVAL));
			}
			{
				trackingAccuracyMeters = (EditText) findViewById(R.id.gpsTrackingAccuracyMeters);
				trackingAccuracyMeters
						.setText(SettingsManager.getSetting(GPS_TRACKING_ACCURACY, App.GPS_TRACKING_ACCURACY));
			}
			{
				serviceHeartBeat = (EditText) findViewById(R.id.serviceHeartbeat);
				serviceHeartBeat.setText(SettingsManager.getSetting(SERVICE_HEART_BEAT, App.SERVICE_HEART_BEAT));
			}
			{
				linesInOut = (EditText) findViewById(R.id.linesInOut);
				linesInOut.setText(SettingsManager.getSetting(LINES_IN_OUT, App.LINES_IN_OUT));
			}
			{
				retryLimit = (EditText) findViewById(R.id.retryLimit);
				retryLimit.setText(SettingsManager.getSetting(RETRY_LIMIT, App.RETRY_LIMIT));
			}
			{
				maxFileSize = (EditText) findViewById(R.id.maxFileSize);
				maxFileSize.setText(SettingsManager.getSetting(MAX_FILE_SIZE, App.MAX_FILE_SIZE));
			}
			{
				remoteRetryDelay = (EditText) findViewById(R.id.remoteRetryDelaySeconds);
				remoteRetryDelay.setText(SettingsManager.getSetting(REMOTE_RETRY_DELAY, App.REMOTE_RETRY_DELAY));
			}
		} catch (Exception e) {
		}
	}

	public void clearHistory(View view) {
		FileOut.clear();
	}

	public void saveSettings(View view) {
		try {
			String phoneNumber = phoneNumberText.getText().toString();
			String intervalString = intervalText.getText().toString();
			String gpsCheckIntervalString = gpsCheckInterval.getText().toString();
			String trackingAccuracyMetersString = trackingAccuracyMeters.getText().toString();
			String serviceHeartBeatString = serviceHeartBeat.getText().toString();
			String linesInOutString = linesInOut.getText().toString();
			String retryLimitString = retryLimit.getText().toString();
			String maxFileSizeString = maxFileSize.getText().toString();
			String remoteRetryDelayString = remoteRetryDelay.getText().toString();

			if (isNullOrEmpty(phoneNumber, intervalString, gpsCheckIntervalString, trackingAccuracyMetersString,
					serviceHeartBeatString, linesInOutString, remoteRetryDelayString, maxFileSizeString,
					retryLimitString)) {
				FileOut.println("Invalid settings");
				Toast.makeText(this, "Invalid settings", Toast.LENGTH_LONG).show();
				return;
			}

			SettingsManager.setSetting(PHONE_NUMBER, phoneNumber);
			SettingsManager.setSetting(INTERVAL_SECONDS, intervalString);

			SettingsManager.setSetting(REMOTE_RETRY_DELAY, remoteRetryDelayString);
			SettingsManager.setSetting(GPS_CHECK_INTERVAL, gpsCheckIntervalString);
			SettingsManager.setSetting(MAX_FILE_SIZE, maxFileSizeString);
			SettingsManager.setSetting(SERVICE_HEART_BEAT, serviceHeartBeatString);
			SettingsManager.setSetting(GPS_TRACKING_ACCURACY, trackingAccuracyMetersString);
			SettingsManager.setSetting(RETRY_LIMIT, retryLimitString);
			SettingsManager.setSetting(LINES_IN_OUT, linesInOutString);
			Toast.makeText(this, "Saved settings", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			FileOut.printStackTrace(e);
		}
	}

	private boolean isNullOrEmpty(String... strings) {
		for (String string : strings) {
			if (string == null || string.equals("")) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		HowlController.start();
		try {
			toggleRunning.setChecked(HowlController.isHowling());
		} catch (Exception e) {
		}
	}

	public void setHome(View view) {
		setHome(false);
	}

}

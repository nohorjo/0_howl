package nohorjo.application;

import static nohorjo.settings.SettingsManager.LINES_IN_OUT;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import nohorjo.howl.R;
import nohorjo.output.FileOut;
import nohorjo.services.HowlBR;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public class HomeSetter extends Activity {
	private TextView out;
	private ScrollView oc;

	protected void setOutput() {
		oc = (ScrollView) findViewById(R.id.oc);
		out = (TextView) oc.getChildAt(0);
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									String outText = out.getText().toString();
									String updatedText = FileOut.getLastNLines(Integer
											.parseInt(SettingsManager.getSetting(LINES_IN_OUT, App.LINES_IN_OUT)));
									if (!updatedText.equals(outText)) {
										out.setText(updatedText);
										oc.scrollTo(0, oc.getBottom());
									}
								} catch (IOException e) {
									FileOut.printStackTrace(e);
								}
							}
						});
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
			};
		}.start();
	}

	protected void setHome(final boolean goHome) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Select default launcher");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);

		final Map<String, String> launchers = new HashMap<>();
		PackageManager pm = getPackageManager();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);
		for (ResolveInfo resolveInfo : lst) {
			String packageName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;
			ApplicationInfo ai;
			try {
				ai = pm.getApplicationInfo(packageName, 0);
			} catch (final NameNotFoundException e) {
				ai = null;
			}
			final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
			launchers.put(applicationName, packageName + "/" + className);
			arrayAdapter.add(applicationName);
		}

		alert.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String pn = launchers.get(arrayAdapter.getItem(which));
				SettingsManager.setSetting(SettingsManager.DEFAULT_HOME, pn);
			}
		});

		alert.setCancelable(false);
		alert.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (goHome) {
					scheduleBRAndCallHome();
				}
			}
		});
		alert.show();
	}

	protected void scheduleBRAndCallHome() {
		try {
			String[] defaultHome = SettingsManager.getSetting(SettingsManager.DEFAULT_HOME).split("/");
			HowlBR.schedule(this);
			FileOut.println("Calling home");
			Intent home = new Intent();
			home.setClassName(defaultHome[0], defaultHome[1]);
			home.addCategory(Intent.CATEGORY_LAUNCHER);
			home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(home);
		} catch (SettingsException e) {
			setHome(true);
		}
	}
}

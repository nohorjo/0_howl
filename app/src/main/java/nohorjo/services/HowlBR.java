package nohorjo.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import nohorjo.application.App;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsManager;

public class HowlBR extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		FileOut.println("Checking on service");
		App.setContext(context);
		context.startService(new Intent(context, HowlService.class));
	}

	public static void schedule(Context c) {
		FileOut.println("Scheduling service");
		Intent howlServiceIntent = new Intent(c, HowlBR.class);
		if (PendingIntent.getBroadcast(c, 0, howlServiceIntent, PendingIntent.FLAG_NO_CREATE) == null) {
			((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(
					AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000,
					Integer.parseInt(
							SettingsManager.getSetting(SettingsManager.SERVICE_HEART_BEAT, App.SERVICE_HEART_BEAT))
							* 1000,
					PendingIntent.getBroadcast(c, 0, howlServiceIntent, 0));
		}
	}
}

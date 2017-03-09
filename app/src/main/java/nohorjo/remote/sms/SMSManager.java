package nohorjo.remote.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import nohorjo.application.App;
import nohorjo.howl.howlers.SMSHowler;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;

public class SMSManager extends BroadcastReceiver {

	public boolean send(String message) {
		// wait for permission
		while (!SettingsManager.isRequiredPermissionsGranted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		SmsManager smsManager = SmsManager.getDefault();
		try {
			smsManager.sendTextMessage(App.getPhoneNumber(), null, message, null, null);
			FileOut.println("Sent: " + message);
			return true;
		} catch (SettingsException e) {
			FileOut.printStackTrace(e);
			return false;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);

		for (int i = 0; i < msgs.length; i++) {
			SmsMessage msg = msgs[i];
			SMSHowler.getReveiveAction().run(msg.getMessageBody());
		}

	}
}

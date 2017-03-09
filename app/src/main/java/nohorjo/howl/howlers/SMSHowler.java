package nohorjo.howl.howlers;

import nohorjo.delegation.Action;
import nohorjo.gps.GPSManager;
import nohorjo.output.FileOut;
import nohorjo.remote.sms.SMSManager;
import nohorjo.settings.SettingsManager;

public abstract class SMSHowler {
	private static SMSManager sms;

	protected static final String COMMAND_IDENTIFIER = "@#@";
	protected static int count;

	private static Action receiveAction = new Action() {
		@Override
		public Object run(Object... commands) {
			for (Object command : commands) {
				respond((String) command);
			}
			return null;
		}
	};

	static {
		sms = new SMSManager();
	}

	public static void respond(String command) {
		try {
			if (command.startsWith(COMMAND_IDENTIFIER)) {
				command = command.replace(COMMAND_IDENTIFIER, "");
				performCommand(command);
			}
		} catch (Exception e) {
			FileOut.printStackTrace(e);
		}
	}

	public static void cancel() {
		sms.send(GPSManager.getLatLongString() + "\nCancelled");
		FileOut.println("Howled " + ++count + " time" + (count > 1 ? "s" : ""));
	}

	public static void howl() {
		sms.send(GPSManager.getLatLongString());
		FileOut.println("Howled " + ++count + " time" + (count > 1 ? "s" : ""));
	}

	public static void performCommand(String command) {
		try {
			FileOut.println("Received command: " + command);
			String key = command.substring(0, 1);
			command = command.substring(1);
			switch (key) {
			case "i":
				SettingsManager.setSetting(SettingsManager.INTERVAL_SECONDS, command);
				FileOut.println("Changing interval to: " + command);
				break;
			default:
				howl();
				break;
			}
		} catch (IndexOutOfBoundsException e) {
		}
	}

	public static Action getReveiveAction() {
		return receiveAction;
	}

}

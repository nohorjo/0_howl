package nohorjo.settings;

public class SettingsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3743020438705395844L;

	public SettingsException(String setting) {
		super("Setting not set: " + setting);
	}

}

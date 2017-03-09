package nohorjo.remote.wordpress;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Base64;
import nohorjo.application.App;
import nohorjo.crypto.AESEncryptor;
import nohorjo.crypto.EncryptionException;
import nohorjo.http.HttpOperation;
import nohorjo.output.FileOut;
import nohorjo.settings.SettingsException;

public abstract class IPHandler {
	private static String IP_ADDRESS;
	private static final HttpOperation httpOperation = new HttpOperation();
	private static final AESEncryptor aes;

	static {
		aes = new AESEncryptor() {
			@Override
			protected String encodeB64(byte[] input) {
				return new String(Base64.encode(input, 0));
			}

			@Override
			protected byte[] decodeB64(byte[] input) {
				return Base64.decode(input, 0);
			}
		};
		while (true) {
			try {
				reloadIPAddress();
				break;
			} catch (IndexOutOfBoundsException e) {
				// phone number not set
			} catch (Exception e) {
				FileOut.printStackTrace(e);
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					FileOut.printStackTrace(e1);
				}
			}
		}
	}

	public static String getIPAddress() {
		return IP_ADDRESS.trim();
	}

	public static void reloadIPAddress() throws IOException, EncryptionException, SettingsException {
		String html = httpOperation.doGet("https://vermisa.wordpress.com/2016/12/30/piip/");
		Matcher m = Pattern.compile("£[^£]*£").matcher(html);
		if (m.find()) {
			String ipenc = m.group().replace("£", "");
			IP_ADDRESS = aes.decrypt(App.key(), ipenc);
		}
	}
}

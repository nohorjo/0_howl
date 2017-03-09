package nohorjo.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import nohorjo.application.App;
import nohorjo.file.FileUtils;
import nohorjo.settings.SettingsManager;

public abstract class FileOut {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM_HH.mm.ss");

	private static File outFile;
	private static String buffer = "";
	private static boolean initialised;

	static {
		initialised = init();
	}

	public static void println(String string) {
		System.out.println(string);
		String d = sdf.format(new Date());
		buffer += d + ">\t" + string + "\n";
		if (initialised || (initialised = init())) {
			if (outFile.length() > Integer
					.parseInt(SettingsManager.getSetting(SettingsManager.MAX_FILE_SIZE, App.MAX_FILE_SIZE)) * 1024) {
				outFile.renameTo(new File(outFile.getParentFile(), "logs-" + d + ".txt"));
			}
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)))) {
				out.print(buffer);
				buffer = "";
			} catch (IOException e) {
				printStackTrace(e);
			}
		}
	}

	private static boolean init() {
		if (SettingsManager.isStoragePermissionsGranted()) {
			File outFileRoot = new File(Environment.getExternalStorageDirectory(), "0_howl");
			outFileRoot.mkdirs();
			outFile = new File(outFileRoot, "log.txt");
			try {
				outFile.createNewFile();
				return true;
			} catch (IOException e) {
				printStackTrace(e);
			}
		}
		return false;
	}

	public static void printStackTrace(Throwable e) {
		println("ERR:" + e.getMessage());
		for (StackTraceElement el : e.getStackTrace()) {
			println("ERR:\t" + el.toString());
		}
	}

	public static String getLastNLines(int count) throws IOException {
		if (initialised || (initialised = init())) {
			return FileUtils.readLastNLines(outFile, count);
		}
		return buffer;
	}

	public static void clear() {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, false)))) {
			buffer = "";
			out.print(buffer);
		} catch (IOException e) {
			printStackTrace(e);
		}
	}
}

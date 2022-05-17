package me.mcblueparrot.client.installer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import me.mcblueparrot.client.installer.InstallStatusCallback;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author maks & kode
 */
public class Utils {

	public static JSONObject json(URL url) throws IOException {
		return new JSONObject(IOUtils.toString(url, StandardCharsets.UTF_8));
	}

	public static URL sneakyParse(String spec) {
		try {
			return new URL(spec);
		} catch (MalformedURLException error) {
			throw new IllegalArgumentException(spec);
		}
	}

	public static void downloadFileMonitored(File destination, URL url, InstallStatusCallback callback) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.connect();
		InputStream stream = conn.getInputStream();
		FileOutputStream output = new FileOutputStream(destination);
		long fileLength = conn.getContentLength();
		float filePercentDivider = 10000f / fileLength;
		if(fileLength == -1)
			callback.setProgressBarIndeterminate(true);
		int cnt;
		int ov_cnt = 0;
		byte[] buf = new byte[1024];
		while ((cnt = stream.read(buf)) != -1) {
			output.write(buf, 0, cnt);
			ov_cnt += cnt;
			callback.setProgressBarValues(10000, (int) (ov_cnt * filePercentDivider));
		}
		if(fileLength == -1)
			callback.setProgressBarIndeterminate(false);
	}

}

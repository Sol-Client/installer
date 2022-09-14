/*
 * MIT License
 *
 * Copyright (c) 2022 TheKodeToad, artDev & other contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.solclient.installer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.github.solclient.installer.InstallStatusCallback;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonObject;

public class Utils {

	public static final String USER_AGENT = "Mozilla/5.0";

	public static JsonObject json(URL url) throws IOException {
		try(InputStream in = url.openStream(); Reader reader = new InputStreamReader(in)) {
			return JsonDeserializer.read(reader).asObject();
		}
	}

	public static URL sneakyParse(String spec) {
		try {
			return new URL(spec);
		} catch (MalformedURLException error) {
			throw new IllegalArgumentException(spec);
		}
	}

	public static void downloadFileMonitored(File destination, URL url, InstallStatusCallback callback) throws IOException {
		callback.setProgressBarValues(1000, 0);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.connect();
		InputStream stream = conn.getInputStream();

		try(FileOutputStream output = new FileOutputStream(destination)) {
			long fileLength = conn.getContentLength();
			float filePercentDivider = 10000f / fileLength;

			if(fileLength == -1) {
				callback.setProgressBarIndeterminate(true);
			}

			int cnt;
			int ov_cnt = 0;
			byte[] buf = new byte[1024];
			while ((cnt = stream.read(buf)) != -1) {
				output.write(buf, 0, cnt);
				ov_cnt += cnt;
				callback.setProgressBarValues(10000, (int) (ov_cnt * filePercentDivider));
			}

			if(fileLength == -1) {
				callback.setProgressBarIndeterminate(false);
			}
		}
	}
	public static String downloadFileToString(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.connect();
		InputStream stream = conn.getInputStream();
		StringBuilder output = new StringBuilder();
		int cnt;
		byte[] buf = new byte[1024];

		while ((cnt = stream.read(buf)) != -1) {
			output.append(new String(buf,0,cnt));
		}

		return output.toString();
	}
}

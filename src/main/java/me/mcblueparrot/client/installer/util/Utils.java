package me.mcblueparrot.client.installer.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

	public static JSONObject json(URL url) throws IOException {
		return new JSONObject(IOUtils.toString(url, StandardCharsets.UTF_8));
	}

	public static URL sneakyParse(String spec) {
		try {
			return new URL(spec);
		}
		catch(MalformedURLException error) {
			throw new IllegalArgumentException(spec);
		}
	}

}

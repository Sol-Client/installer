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
package io.github.solclient.installer;

import io.github.solclient.installer.locale.Locale;
import io.github.solclient.installer.util.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MinecraftJsonPatcher {
	
	MessageDigest shaDigest;
	File gameJson;
	File targetJson;
	JSONObject gameJsonObject;
	File gameJar;
	File targetJar;
	String targetName;
	File libsFolder;

	public MinecraftJsonPatcher(File gamedir, String targetVid) throws NoSuchAlgorithmException {
		shaDigest = MessageDigest.getInstance("SHA-1");
		gameJson = new File(gamedir, "versions/1.8.9/1.8.9.json");
		gameJar = new File(gamedir, "versions/1.8.9/1.8.9.jar");
		targetName = targetVid;
		targetJson = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".json");
		targetJar = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".jar");
		targetJson.getParentFile().mkdirs();
		libsFolder = new File(gamedir, "libraries");
	}

	public boolean load(InstallStatusCallback cb) throws IOException {
		gameJsonObject = new JSONObject(FileUtils.readFileToString(gameJson, "UTF-8"));
		JSONObject downloads = gameJsonObject.getJSONObject("downloads");
		if(downloads != null) {
			JSONObject client = (JSONObject) downloads.get("client");
			if(!verify(gameJar, client.getString("sha1"))) {
				cb.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_GENERIC, gameJar.getName()));
				Utils.downloadFileMonitored(gameJar,new URL(client.getString("url")), cb);
			}else{
				cb.setTextStatus(Locale.getString(Locale.MSG_JAR_VERIFIED,gameJar.getName()));
			}
		}else {
			cb.setTextStatus(Locale.getString(Locale.MSG_DAMAGED_MC_JSON));
			return false;
		}
		gameJsonObject.put("id", targetName);
		return true;
	}
	
	public void save(String newMain, String appendArgs) throws IOException {
		gameJsonObject.put("mainClass", newMain);
		gameJsonObject.put("minecraftArguments", gameJsonObject.getString("minecraftArguments").concat(appendArgs));
		FileUtils.write(targetJson, gameJsonObject.toString(2), StandardCharsets.UTF_8);
	}
	
	public void putLibrary(File origin, String libName) throws IOException {
		JSONArray libraries = gameJsonObject.getJSONArray("libraries");
		JSONObject library = new JSONObject();
		library.put("name", libName);
		libraries.put(library);
		FileUtils.copyFile(origin, new File(libsFolder, mavenNameToPath(libName)));
	}
	
	public void removeLibrary(String mavenName) {
	   JSONArray libraries = gameJsonObject.getJSONArray("libraries");
	   for(int i = 0; i < libraries.length(); i++) {
		   if(libraries.getJSONObject(i).getString("name").equals(mavenName)) {
			   libraries.remove(i);
			   return;
		   }
	   }
	}
   public boolean putFullLibrary(String url, String mavenName, InstallStatusCallback cb) throws IOException{
	   String libLocalPath = mavenNameToPath(mavenName);
	   File libPath = new File(libsFolder, libLocalPath);
	   cb.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_GENERIC, mavenName));
	   if(!libPath.getParentFile().exists() && !libPath.getParentFile().mkdirs()) {
		   cb.setTextStatus(Locale.getString(Locale.MSG_CANT_CREATE_FOLDER, libPath.getAbsolutePath()));
           return false;
	   }
	   Utils.downloadFileMonitored(libPath, new URL(url), cb);
	   JSONArray libraries = gameJsonObject.getJSONArray("libraries");
	   JSONObject artifact = new JSONObject();
	   artifact.put("url", url);
	   artifact.put("path", libLocalPath);
	   artifact.put("size", libPath.length());
	   artifact.put("sha1", compute(libPath));
	   JSONObject library = new JSONObject();
	   library.put("name", mavenName);
	   library.put("downloads", new JSONObject().put("artifact", artifact));
	   libraries.put(library);
       return true;
   } 
	
	public File getSourceClient() {
		return gameJar;
	}

	public File getTargetClient() {
		return targetJar;
	}
	
	public void computeTargetClient() throws IOException {
		JSONObject downloads = gameJsonObject.getJSONObject("downloads");
		JSONObject newClient = new JSONObject();
		newClient.put("url", "");
		newClient.put("sha1", compute(targetJar));
		newClient.put("size", targetJar.length());
		downloads.put("client", newClient);
	}
	
	private String mavenNameToPath(String mavenName) {
		String[] mvnNameSplit = mavenName.split(":");
		return mvnNameSplit[0].replaceAll("\\.", "/") + "/" + mvnNameSplit[1] + "/" + mvnNameSplit[2] + "/" + mvnNameSplit[1] + "-" + mvnNameSplit[2] + ".jar";
	}
	
	private String compute(File input) throws IOException {
		return byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
	}
	
	private boolean verify(File input, String sha1) throws IOException {
		if(!input.exists())
			return false;
		if(sha1 == null)
			return true;
		String f_sha1 = byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
		return f_sha1.equals(sha1);
	}

	private static String byteToHex(final byte[] hash) {
        String result;
        try (Formatter formatter = new Formatter()) {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }   result = formatter.toString();
        }
		return result;
	}

}

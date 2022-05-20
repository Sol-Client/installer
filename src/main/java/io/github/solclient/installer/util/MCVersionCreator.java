/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.solclient.installer.util;

import io.github.solclient.installer.InstallStatusCallback;
import io.github.solclient.installer.Launchers;
import io.github.solclient.installer.locale.Locale;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author maks
 */
public class MCVersionCreator implements VersionCreator{
	
	File tempDir;
	File targetDir;
	File gameJson;
	File targetJson;
	JSONObject gameJsonObject;
	File gameJar;
	File targetJar;
	String targetName;
	File libsFolder;

	MCVersionCreator() {}
	
	public MCVersionCreator(File gamedir, File tempDir, String targetVid) {
		targetName = targetVid;
		this.tempDir = tempDir;
		gameJson = new File(gamedir, "versions/1.8.9/1.8.9.json");
		gameJar = new File(gamedir, "versions/1.8.9/1.8.9.jar");
		targetJson = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".json");
		targetJar = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".jar");
		targetJson.getParentFile().mkdirs();
		libsFolder = new File(gamedir, "libraries");
	}

	@Override
	public boolean load(InstallStatusCallback cb) throws IOException {
		if(gameJson != null && gameJson.canRead()) {
			gameJsonObject = new JSONObject(FileUtils.readFileToString(gameJson, "UTF-8"));
		}else{
			cb.setTextStatus(Locale.getString(Locale.MSG_SEARCHING_MINECRAFT, "1.8.9"));
			gameJsonObject = getMinecraftJson("1.8.9",cb);
		}
		if(gameJar == null) {
			gameJar = new File(tempDir, "1.8.9.jar");
		}
		JSONObject downloads = gameJsonObject.getJSONObject("downloads");
		if(downloads != null) {
			JSONObject client = (JSONObject) downloads.get("client");
			if(!VersionCreatorUtils.verify(gameJar, client.getString("sha1"))) {
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
		update();
		return true;
	}
	
	private JSONObject getMinecraftJson(String mcVersion, InstallStatusCallback cb) throws IOException{
		JSONObject versionManifest = new JSONObject(Utils.downloadFileToString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")));
		if(versionManifest.has("versions")) {
			for(Object o : versionManifest.getJSONArray("versions")) {
				JSONObject version = (JSONObject) o;
				if(version.has("id") && mcVersion.equals(version.getString("id")) && version.has("url")) {
					return new JSONObject(Utils.downloadFileToString(new URL(version.getString("url"))));
				}
			}
			cb.setTextStatus(Locale.getString(Locale.MSG_NO_MINECRAFT, mcVersion));
			return null;
		}else{
			cb.setTextStatus(Locale.getString(Locale.MSG_INVALID_MANIFEST));
			return null;
		}
	}

	void update() {
		String[] args = gameJsonObject.getString("minecraftArguments").split(" ");
		gameJsonObject.remove("minecraftArguments");
		if(!gameJsonObject.has("arguments")) {
			gameJsonObject.put("arguments", new JSONObject());
		}
		JSONObject arguments = gameJsonObject.getJSONObject("arguments");
		for(String arg : args) {
			arguments.append("game", arg);
		}
		arguments.append("game", new JSONObject(
				"{\"rules\":[{\"action\":\"allow\",\"features\":{\"is_demo_user\":true}}],\"value\":\"--demo\"}"));
		arguments.append("game", new JSONObject(
				"{\"rules\":[{\"action\":\"allow\",\"features\":{\"has_custom_resolution\":true}}],\"value\":[\"--width\",\"${resolution_width}\",\"--height\",\"${resolution_height}\"]}"));

		arguments.append("jvm", "-cp");
		arguments.append("jvm", "${classpath}");
		arguments.append("jvm", new JSONObject(
				"{\"rules\":[{\"action\":\"allow\",\"os\":{\"name\":\"windows\"}}],\"value\":\"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump\"}"));

		addProperty("java.library.path", "${natives_directory}");
	}

	@Override
	public void addProperty(String key, String value) {
		gameJsonObject.getJSONObject("arguments").append("jvm", "-D" + key + "=" + value);
	}

	
	
	@Override
	public void addGameArguments(String... arguments) {
		for(String argument : arguments) {
			gameJsonObject.getJSONObject("arguments").accumulate("game", argument);
		}
	}

	@Override
	public void save(String mainClass) throws IOException {
		gameJsonObject.put("mainClass", mainClass);
		FileUtils.write(targetJson, gameJsonObject.toString(2), StandardCharsets.UTF_8);
	}

	@Override
	public void putLibrary(File origin, String libName) throws IOException {
		JSONArray libraries = gameJsonObject.getJSONArray("libraries");
		String mavenPath = VersionCreatorUtils.mavenNameToPath(libName);
		File libPath = new File(libsFolder, mavenPath);
		JSONObject library = new JSONObject();
		library.put("name", libName);
		libraries.put(library);
		FileUtils.copyFile(origin, libPath);
	}

	@Override
	public void removeLibrary(String mavenName) {
		JSONArray libraries = gameJsonObject.getJSONArray("libraries");
		for(int i = 0; i < libraries.length(); i++) {
			if(libraries.getJSONObject(i).getString("name").equals(mavenName)) {
				libraries.remove(i);
				return;
			}
		}
	}

	@Override
	public boolean putFullLibrary(String url, String mavenName, InstallStatusCallback cb) throws IOException{
		String libLocalPath = VersionCreatorUtils.mavenNameToPath(mavenName);
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
		artifact.put("sha1", VersionCreatorUtils.compute(libPath));
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

	public String getTargetName() {
		return targetName;
	}

	@Override
	public void computeTargetClient() throws IOException {
		JSONObject downloads = gameJsonObject.getJSONObject("downloads");
		JSONObject newClient = new JSONObject();
		newClient.put("url", "");
		newClient.put("sha1", VersionCreatorUtils.compute(targetJar));
		newClient.put("size", targetJar.length());
		downloads.put("client", newClient);
	}

	@Override
	public void setTweakerClass(String tweakerClass) {
		addGameArguments("--tweakClass", tweakerClass);
	}



}

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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import io.github.solclient.installer.InstallStatusCallback;
import io.github.solclient.installer.locale.Locale;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonArray;
import io.toadlabs.jfgjds.data.JsonObject;
import io.toadlabs.jfgjds.data.JsonValue;

public class MCVersionCreator implements VersionCreator{

	File tempDir;
	File targetDir;
	File gameJson;
	File targetJson;
	JsonObject gameJsonObject;
	File gameJar;
	File targetJar;
	String targetName;
	File libsFolder;

	MCVersionCreator() {}

	public MCVersionCreator(File gamedir, File tempDir, String targetVid) {
		targetName = targetVid;
		this.tempDir = tempDir;
		gameJson = new File(gamedir, "versions/1.8.9/1.8.9.json");
		gameJson.getParentFile().mkdirs(); // In case of user hasn't downloaded vanilla version yet.
		gameJar = new File(gamedir, "versions/1.8.9/1.8.9.jar");
		targetJson = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".json");
		targetJar = new File(gamedir, "versions/"+targetVid+"/"+targetVid+".jar");
		targetJson.getParentFile().mkdirs();
		libsFolder = new File(gamedir, "libraries");
	}

	@Override
	public boolean load(InstallStatusCallback cb) throws IOException {
		if(gameJson != null && gameJson.canRead()) {
			gameJsonObject = JsonDeserializer.read(new FileInputStream(gameJson), StandardCharsets.UTF_8).asObject();
		}
		else {
			cb.setTextStatus(Locale.getString(Locale.MSG_SEARCHING_MINECRAFT, "1.8.9"));
			gameJsonObject = getMinecraftJson("1.8.9", cb);
		}
		if(gameJar == null) {
			gameJar = new File(tempDir, "1.8.9.jar");
		}
		JsonObject downloads = gameJsonObject.get("downloads").asObject();
		if(downloads != null && downloads.contains("client")) {
			JsonObject client = downloads.get("client").asObject();
			if(!VersionCreatorUtils.verify(gameJar, client.get("sha1").getStringValue())) {
				cb.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_GENERIC, gameJar.getName()));
				Utils.downloadFileMonitored(gameJar,new URL(client.get("url").getStringValue()), cb);
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

	private JsonObject getMinecraftJson(String mcVersion, InstallStatusCallback cb) throws IOException{
		JsonObject versionManifest = Utils.json(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"));
		if(versionManifest.contains("versions")) {
			for(JsonValue versionValue  : versionManifest.get("versions").asArray()) {
				JsonObject version = versionValue.asObject();
				if(mcVersion.equals(version.getOpt("id").map(JsonValue::getStringValue).orElse(null)) && version.contains("url")) {
					return Utils.json(new URL(version.get("url").getStringValue()));
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
		String[] args = gameJsonObject.get("minecraftArguments").getStringValue().split(" ");
		gameJsonObject.remove("minecraftArguments");
		JsonObject arguments = gameJsonObject.computeIfAbsent("arguments", JsonObject.DEFAULT_COMPUTION).asObject();

		JsonArray game = arguments.computeIfAbsent("game", JsonArray.DEFAULT_COMPUTION).asArray();

		for(String arg : args) {
			game.add(arg);
		}

		game.add(JsonObject.of(
			"rules", JsonArray.of(
				JsonObject.of("action", "allow", "features", JsonObject.of("is_demo_user", true))
			),
			"value", "--demo"
		));

		game.add(JsonObject.of(
			"rules", JsonArray.of(
				JsonObject.of("action", "allow", "features", JsonObject.of("has_custom_resolution", true))
			),
			"value", JsonArray.of(
				"--width",
				"${resolution_width}",
				"--height",
				"${resolution_height}"
			)
		));

		JsonArray jvm = arguments.computeIfAbsent("jvm", JsonArray.DEFAULT_COMPUTION).asArray();
		jvm.add("-cp").add("${classpath}");
		jvm.add(JsonObject.of(
			"rules", JsonArray.of(JsonObject.of("action", "allow", "os", JsonObject.of("name", "windows"))),
			"value", "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
		));

		addProperty("java.library.path", "${natives_directory}");
	}

	@Override
	public void addProperty(String key, String value) {
		gameJsonObject.get("arguments").asObject().get("jvm").asArray().add("-D" + key + "=" + value);
	}

	@Override
	public void addGameArguments(String... arguments) {
		for(String argument : arguments) {
			gameJsonObject.get("arguments").asObject().get("game").asArray().add(argument);
		}
	}

	@Override
	public void save(String mainClass) throws IOException {
		gameJsonObject.put("mainClass", mainClass);
		FileUtils.write(targetJson, gameJsonObject.toString(), StandardCharsets.UTF_8);
	}

	@Override
	public void putLibrary(File origin, String libName) throws IOException {
		JsonArray libraries = gameJsonObject.get("libraries").asArray();
		String mavenPath = VersionCreatorUtils.mavenNameToPath(libName);
		File libPath = new File(libsFolder, mavenPath);
		libraries.add(JsonObject.of("name", libName));
		FileUtils.copyFile(origin, libPath);
	}

	@Override
	public void removeLibrary(String mavenName) {
		JsonArray libraries = gameJsonObject.get("libraries").asArray();
		for(int i = 0; i < libraries.size(); i++) {
			if(libraries.get(i).asObject().get("name").getStringValue().equals(mavenName)) {
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
		JsonArray libraries = gameJsonObject.get("libraries").asArray();
		JsonObject artifact = new JsonObject();
		artifact.put("url", url);
		artifact.put("path", libLocalPath);
		artifact.put("size", libPath.length());
		artifact.put("sha1", VersionCreatorUtils.compute(libPath));
		JsonObject library = new JsonObject();
		library.put("name", mavenName);
		library.put("downloads", JsonObject.of("artifact", artifact));
		libraries.add(library);
		return true;
	}

	@Override
	public File getSourceClient() {
		return gameJar;
	}

	@Override
	public File getTargetClient() {
		return targetJar;
	}

	@Override
	public String getTargetName() {
		return targetName;
	}

	@Override
	public void computeTargetClient() throws IOException {
		JsonObject downloads = gameJsonObject.get("downloads").asObject();
		JsonObject newClient = new JsonObject();
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

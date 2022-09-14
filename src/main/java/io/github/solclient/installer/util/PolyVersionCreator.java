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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import io.github.solclient.installer.InstallStatusCallback;
import io.toadlabs.jfgjds.data.JsonArray;
import io.toadlabs.jfgjds.data.JsonObject;

public class PolyVersionCreator extends MCVersionCreator {
	File instanceHome;
	File polyRoot;
	String version;
	private static final String MMC_PACK = "{\n" +
"    \"components\": [\n" +
"        {\n" +
"            \"important\": true,\n" +
"            \"uid\": \"net.minecraft\",\n" +
"            \"version\": \"1.8.9\"\n" +
"        }\n" +
"    ],\n" +
"    \"formatVersion\": 1\n" +
"}";
	Properties instanceProperties = new Properties();

	public PolyVersionCreator(File gamedir, File tmpDir, String targetName) {
		this.polyRoot = gamedir;
		super.tempDir = tmpDir;
		super.targetName = targetName;
		super.gameJar = null;
		super.gameJson = null;
		String instancesFldr = "instances";
		Properties polyprops = new Properties();
		try {
			FileInputStream props = new FileInputStream(new File(gamedir, "polymc.cfg"));
			polyprops.load(props);
			props.close();
			if (polyprops.containsKey("InstanceDir")) {
				instancesFldr = polyprops.getProperty("InstanceDir");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		instanceHome = new File(gamedir, instancesFldr + "/" + targetName);
		instanceHome.mkdirs();
		targetJson = new File(instanceHome, "patches/net.minecraft.json");
		targetJson.getParentFile().mkdirs();
		libsFolder = new File(instanceHome, "libraries");
		libsFolder.mkdirs();
		targetJar = new File(libsFolder, "transformed.jar");

	}
	@Override
	public boolean load(InstallStatusCallback cb) throws IOException{
		if(!super.load(cb)) {
			return false;
		}
		if(gameJsonObject.contains("javaVersion")) {
			int javaMajor = gameJsonObject.get("javaVersion").asObject().get("majorVersion").getIntNumberValue();
			gameJsonObject.put("compatibleJavaMajors", javaMajor);
			gameJsonObject.remove("javaVersion");
		}
		gameJsonObject.remove("downloads");
		gameJsonObject.put("name", targetName);
		return true;
	}

	@Override
	void update() {
		//Do nothing: the original changes formats for MC arguments
	}

	@Override
	public void save(String main) throws IOException{
		super.save(main);
		instanceProperties.put("name", targetName);
		instanceProperties.put("OverrideJavaArgs", "true");
		instanceProperties.put("iconKey", "solclient");
		FileOutputStream fInstanceProperties = new FileOutputStream(new File(instanceHome, "instance.cfg"));
		instanceProperties.store(fInstanceProperties, "");
		fInstanceProperties.close();
		FileOutputStream fMMCPack = new FileOutputStream(new File(instanceHome, "mmc-pack.json"));
		fMMCPack.write(MMC_PACK.getBytes(StandardCharsets.UTF_8));
		fMMCPack.close();
		File solIcon = new File(polyRoot,"icons/solclient.png");
		solIcon.getParentFile().mkdirs();
		InputStream iconRes = PolyVersionCreator.class.getResourceAsStream("/logo_128x.png");
		FileUtils.copyInputStreamToFile(iconRes, solIcon);
	}

	@Override
	public void addGameArguments(String... arguments) {
		String args = gameJsonObject.get("minecraftArguments").getStringValue();
		for(String arg : arguments) {
			args += ' ' + arg;
		}
		gameJsonObject.put("minecraftArguments", args);
	}

	@Override
	public void addProperty(String property, String value) {
		String args;
		if(instanceProperties.containsKey("JvmArgs")) {
			args = instanceProperties.getProperty("JvmArgs");
			args += " -D"+property+"="+value;
			instanceProperties.replace("JvmArgs", args);
		}else{
			args = "-D"+property+"="+value;
			instanceProperties.put("JvmArgs", args);
		}
	}

	@Override
	public void computeTargetClient() throws IOException{
		JsonObject mainJar = new JsonObject();
		mainJar.put("name", "io.github.solclient:transformed:1.8.9");
		mainJar.put("MMC-hint", "local");
		mainJar.put("MMC-filename", "transformed.jar");
		gameJsonObject.put("mainJar", mainJar);
	}

	@Override
	public void putLibrary(File origin, String libName) throws IOException{
		JsonArray libraries = gameJsonObject.get("libraries").asArray();
		String mavenPath = VersionCreatorUtils.mavenNameToPath(libName);
		mavenPath = mavenPath.substring(mavenPath.lastIndexOf("/"));
		File libPath = new File(libsFolder, mavenPath);
		JsonObject library = new JsonObject();
		library.put("name", libName);
		library.put("MMC-hint", "local");
		libraries.add(library);
		FileUtils.copyFile(origin, libPath);
	}

	@Override
	public void setTweakerClass(String tweaker) {
		gameJsonObject.put("+tweakers", JsonArray.of(tweaker));
	}

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.solclient.installer.util;

import io.github.solclient.installer.InstallStatusCallback;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author maks
 */
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
		this.version = version;
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
		if(gameJsonObject.has("javaVersion")) {
			int javaMajor = gameJsonObject.getJSONObject("javaVersion").getInt("majorVersion");
			gameJsonObject.append("compatibleJavaMajors", javaMajor);
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
		String args = gameJsonObject.getString("minecraftArguments");
		for(String arg : arguments) {
			args += " "+arg;
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
		JSONObject mainJar = new JSONObject();
		mainJar.put("name", "io.github.solclient:transformed:1.8.9");
		mainJar.put("MMC-hint", "local");
		mainJar.put("MMC-filename", "transformed.jar");
		gameJsonObject.put("mainJar", mainJar);
	}
	
	@Override
	public void putLibrary(File origin, String libName) throws IOException{
		JSONArray libraries = gameJsonObject.getJSONArray("libraries");
		String mavenPath = VersionCreatorUtils.mavenNameToPath(libName);
		mavenPath = mavenPath.substring(mavenPath.lastIndexOf("/"));
		File libPath = new File(libsFolder, mavenPath);
		JSONObject library = new JSONObject();
		library.put("name", libName);
		library.put("MMC-hint", "local");
		libraries.put(library);
		FileUtils.copyFile(origin, libPath);
	}
	
	@Override
	public void setTweakerClass(String tweaker) {
		gameJsonObject.put("+tweakers", new JSONArray().put(tweaker));
	}
}

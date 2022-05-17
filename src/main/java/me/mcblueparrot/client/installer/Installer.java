/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.mcblueparrot.client.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import static me.mcblueparrot.client.installer.DefaultPaths.LAUNCHER_TYPE_MINECRAFT;
import static me.mcblueparrot.client.installer.DefaultPaths.LAUNCHER_TYPE_POLYMC;
import me.mcblueparrot.client.installer.util.ClientRelease;
import me.mcblueparrot.client.installer.util.OperatingSystem;
import me.mcblueparrot.client.installer.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 *
 * @author maks & kode
 */
public class Installer {
	private static final String MAPPINGS_URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip";
	private int launcherType = -1;
	private List<File> locations;
	private File data;
	private InstallStatusCallback callback;

	public void setPath(File f) {
		this.data = f;
	}

	public void install(InstallStatusCallback callback) {
		this.callback = callback;
		new Thread(this::installAsync).start();
	}

	private void installAsync() {
		File mcJar = new File(data, "versions/1.8.9/1.8.9.jar");
		if(!mcJar.exists()) {
			callback.setTextStatus("Unable to find Minecraft 1.8.9");
			callback.onDone(false);
			return;
		}
		File solClientDestination = new File(data, "versions/sol-client");
		solClientDestination.mkdirs();
		callback.setProgressBarIndeterminate(true);
		callback.setTextStatus("Getting version info...");
		ClientRelease latest;
		try {
			latest = ClientRelease.latest();
		}catch(Throwable e) {
			callback.setTextStatus("Failed to get version info", e);
			callback.onDone(false);
			return;
		}
		callback.setTextStatus("Using version " + latest.getId());
		File cacheFolder = new File(System.getProperty("java.io.tmpdir"), "sol-installer-cache");
		if(!cacheFolder.exists()) {
			if(!cacheFolder.mkdirs()) {
				callback.setTextStatus("Failed to create installation folder");
				callback.onDone(false);
				return;
			}
		}
		cacheFolder.deleteOnExit();
		String gameJarUrl = latest.getGameJar();
		File gameJar = new File(cacheFolder, "game.jar");
		File optifineJar = new File(cacheFolder, "optifine.jar");
		File mappings = new File(cacheFolder, "mappings.zip");
		File joinedSrg = new File(cacheFolder, "joined.srg");
		try {
			callback.setProgressBarIndeterminate(false);
			callback.setTextStatus("Downloading client...");
			Utils.downloadFileMonitored(gameJar, new URL(gameJarUrl), callback);
			callback.setTextStatus("Downloading OptiFine...");
			Utils.downloadFileMonitored(optifineJar, getOptifineUrl(), callback);
			callback.setTextStatus("Downloading mappings...");
			Utils.downloadFileMonitored(mappings, new URL(MAPPINGS_URL), callback);
		}catch(Exception e) {
			callback.setTextStatus("Download failed", e);
			callback.onDone(false);
			return;
		}
		try {
			callback.setProgressBarIndeterminate(true);
			callback.setTextStatus("Unpacking mappings...");
			ZipFile mappingsFile = new ZipFile(mappings);
			ZipEntry joinedSrgEntry = mappingsFile.getEntry("joined.srg");
			if(joinedSrgEntry == null) {
				callback.setTextStatus("Unable to find mappings!");
				callback.onDone(false);
				mappingsFile.close();
				return;
			}
			FileOutputStream srg = new FileOutputStream(joinedSrg);
			IOUtils.copy(mappingsFile.getInputStream(joinedSrgEntry), srg);
			mappingsFile.close();
			callback.setTextStatus("Remapping...");
			net.md_5.specialsource.SpecialSource.main(new String[]{
				"--in-jar", mcJar.getAbsolutePath(),
				"--out-jar", new File(solClientDestination,"sol-client.jar").getAbsolutePath(),
				"--srg-in", joinedSrg.getAbsolutePath()
			});
			callback.setTextStatus("Done!");
			callback.onDone(true);
		}catch(Exception e) {
			callback.setTextStatus("Unable to remap due to "+e.toString());
			callback.onDone(false);
			return;
		}
	}

	private URL getOptifineUrl() throws IOException{
		String downloadPage = IOUtils.toString(new URL("https://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_M5.jar"), StandardCharsets.UTF_8);
		String link = downloadPage.substring(downloadPage.indexOf("downloadx"));
		link = link.substring(0, link.indexOf("'"));
		link = "https://optifine.net/" + link;
		return new URL(link);
	}

	private boolean addProfile() throws IOException{
		switch (launcherType) {
			default:
			case LAUNCHER_TYPE_MINECRAFT:
				File launcherProfiles = new File(data, "launcher_profiles.json");
				if (!launcherProfiles.exists()) {
					launcherProfiles = new File(data, "launcher_profiles_microsoft_store.json");
					if (!launcherProfiles.exists()) {
						return false;
					}
				}

				JSONObject profiles = new JSONObject(
						FileUtils.readFileToString(launcherProfiles, StandardCharsets.UTF_8)).getJSONObject("profiles");

				JSONObject newProfile = new JSONObject();

				String now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString();

				newProfile.put("created", now);
				newProfile.put("lastUsed", now);

				profiles.put("sol-client", newProfile);

				return true;
			case LAUNCHER_TYPE_POLYMC:
				return false;
		}
	}
}

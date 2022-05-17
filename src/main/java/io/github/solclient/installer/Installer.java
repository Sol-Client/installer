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
import static io.github.solclient.installer.Launchers.LAUNCHER_TYPE_MINECRAFT;
import static io.github.solclient.installer.Launchers.LAUNCHER_TYPE_POLYMC;
import io.github.solclient.installer.util.ClientRelease;
import io.github.solclient.installer.util.Utils;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class Installer {
	private static final String MAPPINGS_URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip";
	private File data;
	private volatile int launcherType = -1;
	private volatile InstallStatusCallback callback;

	public void setPath(File f) {
		this.data = f;
	}

	public void install(int launcherType, InstallStatusCallback callback) {
		this.launcherType = launcherType;
		this.callback = callback;
		new Thread(this::installAsync).start();
	}

	private void installAsync() {
        MinecraJsonPatcher jsonPatcher;
        try {
            jsonPatcher = new MinecraJsonPatcher(data, "sol-client");
            if(!jsonPatcher.load(callback)) {
                callback.onDone(false);
                return;
            }
            jsonPatcher.removeLibrary("org.apache.logging.log4j:log4j-api:2.0-beta9");
            jsonPatcher.removeLibrary("org.apache.logging.log4j:log4j-core:2.0-beta9");
            jsonPatcher.removeLibrary("com.google.code.gson:gson:2.2.4");
        } catch (Exception ex) {
            callback.setTextStatus("Failed to initialize!", ex);
            callback.onDone(false);
            return;
        }
		callback.setProgressBarIndeterminate(true);
		callback.setTextStatus("Getting version info...");
		ClientRelease latest;
		try {
			latest = ClientRelease.latest();
		}
		catch(Throwable e) {
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
            jsonPatcher.putLibrary(gameJar, "io.github.solclient:gamejar:"+latest.getId());
			callback.setTextStatus("Downloading OptiFine...");
			Utils.downloadFileMonitored(optifineJar, getOptifineUrl(), callback);
            jsonPatcher.putLibrary(optifineJar, "io.github.solclient:optifine:1.8.9");
			callback.setTextStatus("Downloading mappings...");
			Utils.downloadFileMonitored(mappings, new URL(MAPPINGS_URL), callback);
            jsonPatcher.putFullLibrary("https://repo.maven.apache.org/maven2/org/slick2d/slick2d-core/1.0.2/slick2d-core-1.0.2.jar", 
                    "org.slick2d:slick2d-core:1.0.2", callback);
            jsonPatcher.putFullLibrary("https://repo.codemc.io/repository/maven-public/com/logisticscraft/occlusionculling/0.0.5-SNAPSHOT/occlusionculling-0.0.5-20210620.172315-1.jar", 
                    "com.logisticscraft:occlusionculling:0.0.5-SNAPSHOT", callback);
            jsonPatcher.putFullLibrary("https://repo.hypixel.net/repository/Hypixel/net/hypixel/hypixel-api-core/4.0/hypixel-api-core-4.0.jar",
                    "net.hypixel:hypixel-api-core:4.0", callback);
            jsonPatcher.putFullLibrary("https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar", 
                    "org.spongepowered:mixin:0.7.11-SNAPSHOT", callback);
            jsonPatcher.putFullLibrary("https://libraries.minecraft.net/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar", 
                    "net.minecraft:launchwrapper:1.12", callback);
            jsonPatcher.putFullLibrary("https://repo.maven.apache.org/maven2/org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar", 
                    "org.ow2.asm:asm-debug-all:5.2", callback);
            jsonPatcher.putFullLibrary("https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-core/2.17.1/log4j-core-2.17.1.jar", 
                    "org.apache.logging.log4j:log4j-core:2.17.1", callback);
            jsonPatcher.putFullLibrary("https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-api/2.17.1/log4j-api-2.17.1.jar", 
                    "org.apache.logging.log4j:log4j-api:2.17.1", callback);
            jsonPatcher.putFullLibrary("https://libraries.minecraft.net/com/google/code/gson/gson/2.8.8/gson-2.8.8.jar", 
                    "com.google.code.gson:gson:2.8.8", callback);
		}
		catch(Throwable e) {
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
			net.md_5.specialsource.SpecialSource.main(new String[] {
				"--in-jar", jsonPatcher.getSourceClient().getAbsolutePath(),
				"--out-jar", jsonPatcher.getTargetClient().getAbsolutePath(),
				"--srg-in", joinedSrg.getAbsolutePath()
			});
			callback.setTextStatus("Saving...");
            jsonPatcher.computeTargetClient();
            jsonPatcher.save("net.minecraft.launchwrapper.Launch", " --tweakClass me.mcblueparrot.client.tweak.Tweaker");
			callback.onDone(true);
		}
		catch(Throwable e) {
			callback.setTextStatus("Unable to remap", e);
			callback.onDone(false);
		}
	}

	private URL getOptifineUrl() throws IOException{
		URLConnection connection = new URL("https://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_M5.jar").openConnection();
		connection.setRequestProperty("User-Agent", Utils.USER_AGENT);
		String downloadPage = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
		String link = downloadPage.substring(downloadPage.indexOf("downloadx"));
		link = link.substring(0, link.indexOf("'"));
		link = "https://optifine.net/" + link;
		return new URL(link);
	}

	private boolean addProfile() throws IOException{
		switch(launcherType) {
			default:
			case LAUNCHER_TYPE_MINECRAFT:
				File launcherProfiles = new File(data, "launcher_profiles.json");
				if(!launcherProfiles.exists()) {
					launcherProfiles = new File(data, "launcher_profiles_microsoft_store.json");
					if(!launcherProfiles.exists()) {
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

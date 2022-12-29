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

import static io.github.solclient.installer.Launcher.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.zip.*;

import org.apache.commons.io.*;

import io.github.solclient.installer.locale.Locale;
import io.github.solclient.installer.util.*;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonObject;

public final class Installer {

	private static final String MAPPINGS_URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip";
	private File data;
	private boolean enableOptifine = true;
	private int launcherType = -1;
	private InstallStatusCallback callback;

	public void setPath(File f) {
		this.data = f;
	}

	public void setOptifineEnabled(boolean enabled) {
		enableOptifine = enabled;
	}

	public void install(int launcherType, InstallStatusCallback callback) {
		this.launcherType = launcherType;
		this.callback = callback;
		new Thread(this::installAsync).start();
	}

	private void installAsync() {
		callback.setProgressBarIndeterminate(true);
		callback.setTextStatus(Locale.get(Locale.MSG_GETTING_VERSION_INFO));
		ClientRelease latest;
		try {
			latest = ClientRelease.latest();
		} catch (Throwable error) {
			callback.setTextStatus(Locale.get(Locale.MSG_GETTING_VERSION_INFO_FAILED), error);
			callback.onDone(false);
			return;
		}
		File cacheFolder = new File(System.getProperty("java.io.tmpdir"), "sol-installer-cache");
		if (!cacheFolder.exists()) {
			if (!cacheFolder.mkdirs()) {
				callback.setTextStatus(Locale.get(Locale.MSG_CACHE_FAILED));
				callback.onDone(false);
				return;
			}
		}
		VersionCreator creator;
		try {
			String name = "Sol Client " + latest.getId();
			switch (launcherType) {
				default:
				case Launcher.MOJANG:
					creator = new MCVersionCreator(data, cacheFolder, name);
					break;
				case Launcher.MULTIMC:
					creator = new MultiMCVersionCreator(data, cacheFolder, name, "sol-client");
					break;
				case Launcher.PRISM:
					creator = new PrismVersionCreator(data, cacheFolder, name, "sol-client");
					break;
			}

			if (!creator.load(callback)) {
				callback.onDone(false);
				return;
			}
			creator.removeLibrary("org.apache.logging.log4j:log4j-api:2.0-beta9");
			creator.removeLibrary("org.apache.logging.log4j:log4j-core:2.0-beta9");
			creator.removeLibrary("com.google.code.gson:gson:2.2.4");
		} catch (Exception error) {
			callback.setTextStatus(Locale.get(Locale.MSG_INITIALIZATION_FAILED), error);
			callback.onDone(false);
			return;
		}
		callback.setTextStatus(Locale.get(Locale.MSG_INSTALLING_VERSION, latest.getId()));

		cacheFolder.deleteOnExit();

		String gameJarUrl = latest.getGameJar();
		File clientJar = new File(cacheFolder, "sol-client.jar");
		File optifineJar = new File(cacheFolder, "optifine.jar");
		File optifineJarMod = new File(cacheFolder, "optifine-mod.jar");
		File patchedJar = new File(cacheFolder, "patched.jar");
		File mappings = new File(cacheFolder, "mappings.zip");
		File joinedSrg = new File(cacheFolder, "joined.srg");

		try {
			callback.setProgressBarIndeterminate(false);
			callback.setTextStatus(Locale.get(Locale.MSG_DOWNLOADING_CLIENT));

			Utils.downloadFileMonitored(clientJar, new URL(gameJarUrl), callback);
			creator.putLibrary(clientJar, "io.github.solclient:client:" + latest.getId());

			if (enableOptifine) {
				callback.setTextStatus(Locale.get(Locale.MSG_DOWNLOADING_GENERIC, "OptiFine"));
				Utils.downloadFileMonitored(optifineJar, getOptiFineUrl(), callback);
			}

			callback.setTextStatus(Locale.get(Locale.MSG_DOWNLOADING_MAPPINGS));
			Utils.downloadFileMonitored(mappings, new URL(MAPPINGS_URL), callback);

			boolean libs = true;

			libs = libs && creator.putFullLibrary(
					"https://repo.maven.apache.org/maven2/org/slick2d/slick2d-core/1.0.2/slick2d-core-1.0.2.jar",
					"org.slick2d:slick2d-core:1.0.2", callback);
			libs = libs && creator.putFullLibrary(
					"https://repo.hypixel.net/repository/Hypixel/net/hypixel/hypixel-api-core/4.0/hypixel-api-core-4.0.jar",
					"net.hypixel:hypixel-api-core:4.0", callback);
			libs = libs && creator.putFullLibrary(
					"https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar",
					"org.spongepowered:mixin:0.7.11-SNAPSHOT", callback);
			libs = libs && creator.putFullLibrary(
					"https://libraries.minecraft.net/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar",
					"net.minecraft:launchwrapper:1.12", callback);
			libs = libs && creator.putFullLibrary(
					"https://repo.maven.apache.org/maven2/org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar",
					"org.ow2.asm:asm-debug-all:5.2", callback);
			libs = libs && creator.putFullLibrary(
					"https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-core/2.17.1/log4j-core-2.17.1.jar",
					"org.apache.logging.log4j:log4j-core:2.17.1", callback);
			libs = libs && creator.putFullLibrary(
					"https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-api/2.17.1/log4j-api-2.17.1.jar",
					"org.apache.logging.log4j:log4j-api:2.17.1", callback);
			libs = libs && creator.putFullLibrary(
					"https://libraries.minecraft.net/com/google/code/gson/gson/2.8.8/gson-2.8.8.jar",
					"com.google.code.gson:gson:2.8.8", callback);

			if (!libs) {
				callback.onDone(false);
				return;
			}
		} catch (Throwable error) {
			callback.setTextStatus(Locale.get(Locale.MSG_DOWNLOAD_ERROR), error);
			callback.onDone(false);
			return;
		}
		try {
			if (enableOptifine) {
				callback.setProgressBarIndeterminate(false);
				callback.setTextStatus(Locale.get(Locale.MSG_EXTRACTING_OPTIFINE));
				URLClassLoader classLoader = new URLClassLoader(new URL[] { optifineJar.toURI().toURL() }, null);
				Class<?> patcher = Class.forName("optifine.Patcher", false, classLoader);
				Method processMethod = patcher.getMethod("process", File.class, File.class, File.class);
				processMethod.invoke(processMethod, creator.getSourceClient(), optifineJar, optifineJarMod);
				callback.setTextStatus(Locale.get(Locale.MSG_INSTALLING_OPTIFINE));
				try (ZipFile optifinePatches = new ZipFile(optifineJarMod);
						ZipFile srcZip = new ZipFile(creator.getSourceClient());
						ZipOutputStream patchedOut = new ZipOutputStream(new FileOutputStream(patchedJar))) {
					Enumeration<? extends ZipEntry> srcEntries = srcZip.entries();
					int ctr = 0;
					int max = srcZip.size();
					while (srcEntries.hasMoreElements()) {
						ZipEntry entry = srcEntries.nextElement();
						InputStream in;
						ZipEntry patchEntry = optifinePatches.getEntry(entry.getName());
						if (patchEntry != null) {
							in = optifinePatches.getInputStream(patchEntry);
						} else {
							in = srcZip.getInputStream(entry);
						}
						patchedOut.putNextEntry(new ZipEntry(entry.getName()));
						IOUtils.copy(in, patchedOut);
						in.close();
						callback.setProgressBarValues(max, ctr);
						ctr++;
					}
					ctr = 0;
					max = optifinePatches.size();
					Enumeration<? extends ZipEntry> patchEntries = optifinePatches.entries();
					while (patchEntries.hasMoreElements()) {
						ZipEntry entry = patchEntries.nextElement();
						if (srcZip.getEntry(entry.getName()) == null) {
							patchedOut.putNextEntry(new ZipEntry(entry.getName()));
							InputStream in = optifinePatches.getInputStream(entry);
							IOUtils.copy(in, patchedOut);
							in.close();
						}
						callback.setProgressBarValues(max, ctr);
						ctr++;
					}
				}
			}
			callback.setProgressBarIndeterminate(true);
			callback.setTextStatus(Locale.get(Locale.MSG_UNPACKING_MAPPINGS));
			ZipFile mappingsFile = new ZipFile(mappings);
			ZipEntry joinedSrgEntry = mappingsFile.getEntry("joined.srg");
			if (joinedSrgEntry == null) {
				callback.setTextStatus(Locale.get(Locale.MSG_NO_MAPPINGS));
				callback.onDone(false);
				mappingsFile.close();
				return;
			}
			FileOutputStream srg = new FileOutputStream(joinedSrg);
			IOUtils.copy(mappingsFile.getInputStream(joinedSrgEntry), srg);
			mappingsFile.close();
			callback.setTextStatus(Locale.get(Locale.MSG_REMAPPING));
			net.md_5.specialsource.SpecialSource.main(new String[] { "--in-jar",
					enableOptifine ? patchedJar.getAbsolutePath() : creator.getSourceClient().getAbsolutePath(),
					"--out-jar", creator.getTargetClient().getAbsolutePath(), "--srg-in",
					joinedSrg.getAbsolutePath() });
			callback.setTextStatus(Locale.get(Locale.MSG_SAVING));
			creator.computeTargetClient();
			creator.addProperty("io.github.solclient.client.version", latest.getId());
			creator.addProperty("user.language", "en");
			creator.addProperty("user.country", "US");
			creator.setTweakerClass("io.github.solclient.client.tweak.Tweaker");
			creator.save("net.minecraft.launchwrapper.Launch");
			callback.setTextStatus(Locale.get(Locale.MSG_CREATING_PROFILE));
			callback.onDone(addProfile(creator.getTargetName()));
		} catch (Throwable e) {
			callback.setTextStatus(Locale.get(Locale.MSG_REMAP_FAILED), e);
			callback.onDone(false);
		}
	}

	private URL getOptiFineUrl() throws IOException {
		URLConnection connection = new URL("https://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_M5.jar")
				.openConnection();
		connection.setRequestProperty("User-Agent", Utils.USER_AGENT);
		String downloadPage = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
		String link = downloadPage.substring(downloadPage.indexOf("downloadx"));
		link = link.substring(0, link.indexOf("'"));
		link = "https://optifine.net/" + link;
		return new URL(link);
	}

	private boolean addProfile(String versionId) throws IOException {
		switch (launcherType) {
			default:
			case MOJANG:
				File launcherProfiles = new File(data, "launcher_profiles.json");
				File launcherProfilesMS = new File(data, "launcher_profiles_microsoft_store.json");
				File launcherUiState = new File(data, "launcher_ui_state.json");

				if (launcherProfilesMS.lastModified() > launcherProfiles.lastModified()) {
					launcherProfiles = launcherProfilesMS;
				}

				if (!launcherProfiles.exists()) {
					callback.setTextStatus(Locale.get(Locale.MSG_NO_LAUNCHER_PROFILES));
					return false;
				}

				JsonObject profilesData = JsonDeserializer
						.fromString(FileUtils.readFileToString(launcherProfiles, StandardCharsets.UTF_8)).asObject();
				JsonObject profiles = profilesData.get("profiles").asObject();

				JsonObject newProfile = new JsonObject();

				String now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString();

				newProfile.put("created", now);
				newProfile.put("lastUsed", now);
				newProfile.put("lastVersionId", versionId);
				newProfile.put("name", "Sol Client");
				newProfile.put("icon", "data:image/png;base64,"
						+ Base64.getEncoder().encodeToString(IOUtils.resourceToByteArray("/logo_128x.png")));

				profiles.put("sol-client", newProfile);

				FileUtils.writeStringToFile(launcherProfiles, profilesData.toString(), StandardCharsets.UTF_8);

				if (launcherUiState.exists()) {
					dismissInstallation(launcherUiState, versionId);
				}

				return true;
			case MULTIMC:
			case PRISM:
				return true; // everything is done in MultiMCVersionCreator/subclasses
		}
	}

	private static void dismissInstallation(File launcherUiState, String versionId) throws IOException {
		String data = FileUtils.readFileToString(launcherUiState, StandardCharsets.UTF_8);

		if (data.contains("$#")) {
			data = data.substring(data.indexOf("$#") + 2);

			while (data.startsWith("\n") || data.startsWith("\r")) {
				data = data.substring(1);
			}
		}

		JsonObject obj = JsonDeserializer.fromString(data).asObject();

		String uiEvents = "{}";

		if (obj.get("data").asObject().contains("UiEvents")) {
			uiEvents = obj.get("data").asObject().get("UiEvents").getStringValue();
		}

		JsonObject uiEventsObj = JsonDeserializer.fromString(uiEvents).asObject();

		if (!uiEventsObj.contains("hidePlayerSafetyDisclaimer")) {
			uiEventsObj.put("hidePlayerSafetyDisclaimer", new JsonObject());
		}

		JsonObject dismissedDisclaimers = uiEventsObj.get("hidePlayerSafetyDisclaimer").asObject();
		dismissedDisclaimers.put(versionId + "_sol-client", true);

		obj.get("data").asObject().put("UiEvents", uiEventsObj.toString());

		FileUtils.writeStringToFile(launcherUiState, obj.toString(), StandardCharsets.UTF_8);
	}

}

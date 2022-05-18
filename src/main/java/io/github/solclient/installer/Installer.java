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
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import static io.github.solclient.installer.Launchers.LAUNCHER_TYPE_MINECRAFT;
import static io.github.solclient.installer.Launchers.LAUNCHER_TYPE_POLYMC;
import io.github.solclient.installer.locale.Locale;
import io.github.solclient.installer.util.ClientRelease;
import io.github.solclient.installer.util.Utils;
import io.github.solclient.installer.util.VersionCreator;

import java.lang.reflect.Method;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Installer {

    private static final String MAPPINGS_URL = "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp/1.8.9/mcp-1.8.9-srg.zip";
    private File data;
    private boolean enableOptifine = false;
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
        callback.setTextStatus(Locale.getString(Locale.MSG_GETTING_VERSION_INFO));
        ClientRelease latest;
        try {
            latest = ClientRelease.latest();
        } catch (Throwable e) {
            callback.setTextStatus(Locale.getString(Locale.MSG_GETTING_VERSION_INFO_FAILED), e);
            callback.onDone(false);
            return;
        }
        VersionCreator creator;
        try {
            creator = new VersionCreator(data, "Sol Client " + latest.getId());
            if (!creator.load(callback)) {
                callback.onDone(false);
                return;
            }
            creator.removeLibrary("org.apache.logging.log4j:log4j-api:2.0-beta9");
            creator.removeLibrary("org.apache.logging.log4j:log4j-core:2.0-beta9");
            creator.removeLibrary("com.google.code.gson:gson:2.2.4");
        } catch (Exception ex) {
            callback.setTextStatus(Locale.getString(Locale.MSG_INITIALIZATION_FAILED), ex);
            callback.onDone(false);
            return;
        }
        callback.setTextStatus(Locale.getString(Locale.MSG_INSTALLING_VERSION, latest.getId()));
        File cacheFolder = new File(System.getProperty("java.io.tmpdir"), "sol-installer-cache");
        if (!cacheFolder.exists()) {
            if (!cacheFolder.mkdirs()) {
                callback.setTextStatus(Locale.getString(Locale.MSG_CACHE_FAILED));
                callback.onDone(false);
                return;
            }
        }
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
            callback.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_CLIENT));
            Utils.downloadFileMonitored(clientJar, new URL(gameJarUrl), callback);
            creator.putLibrary(clientJar, "io.github.solclient:client:" + latest.getId());
            callback.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_GENERIC, "OptiFine"));
            Utils.downloadFileMonitored(optifineJar, getOptifineUrl(), callback);
            callback.setTextStatus(Locale.getString(Locale.MSG_DOWNLOADING_MAPPINGS));
            Utils.downloadFileMonitored(mappings, new URL(MAPPINGS_URL), callback);
            if (!(creator.putFullLibrary("https://repo.maven.apache.org/maven2/org/slick2d/slick2d-core/1.0.2/slick2d-core-1.0.2.jar",
                    "org.slick2d:slick2d-core:1.0.2", callback)
                    && creator.putFullLibrary("https://repo.codemc.io/repository/maven-public/com/logisticscraft/occlusionculling/0.0.5-SNAPSHOT/occlusionculling-0.0.5-20210620.172315-1.jar",
                            "com.logisticscraft:occlusionculling:0.0.5-SNAPSHOT", callback)
                    && creator.putFullLibrary("https://repo.hypixel.net/repository/Hypixel/net/hypixel/hypixel-api-core/4.0/hypixel-api-core-4.0.jar",
                            "net.hypixel:hypixel-api-core:4.0", callback)
                    && creator.putFullLibrary("https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar",
                            "org.spongepowered:mixin:0.7.11-SNAPSHOT", callback)
                    && creator.putFullLibrary("https://libraries.minecraft.net/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar",
                            "net.minecraft:launchwrapper:1.12", callback)
                    && creator.putFullLibrary("https://repo.maven.apache.org/maven2/org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar",
                            "org.ow2.asm:asm-debug-all:5.2", callback)
                    && creator.putFullLibrary("https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-core/2.17.1/log4j-core-2.17.1.jar",
                            "org.apache.logging.log4j:log4j-core:2.17.1", callback)
                    && creator.putFullLibrary("https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-api/2.17.1/log4j-api-2.17.1.jar",
                            "org.apache.logging.log4j:log4j-api:2.17.1", callback)
                    && creator.putFullLibrary("https://libraries.minecraft.net/com/google/code/gson/gson/2.8.8/gson-2.8.8.jar",
                            "com.google.code.gson:gson:2.8.8", callback))) {
                callback.onDone(false);
                return;
            }
        } catch (Throwable e) {
            callback.setTextStatus(Locale.getString(Locale.MSG_DOWNLOAD_ERROR), e);
            callback.onDone(false);
            return;
        }
        try {
            if (enableOptifine) {
                callback.setProgressBarIndeterminate(false);
                callback.setTextStatus(Locale.getString(Locale.MSG_EXTRACTING_OPTIFINE));
                URLClassLoader classLoader = new URLClassLoader(new URL[]{optifineJar.toURI().toURL()}, null);
                Class<?> patcher = Class.forName("optifine.Patcher", false, classLoader);
                Method processMethod = patcher.getMethod("process", File.class, File.class, File.class);
                processMethod.invoke(processMethod, creator.getSourceClient(), optifineJar, optifineJarMod);
                callback.setTextStatus(Locale.getString(Locale.MSG_INSTALLING_OPTIFINE));
                try ( ZipFile optifinePatches = new ZipFile(optifineJarMod);
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
            callback.setTextStatus(Locale.getString(Locale.MSG_UNPACKING_MAPPINGS));
            ZipFile mappingsFile = new ZipFile(mappings);
            ZipEntry joinedSrgEntry = mappingsFile.getEntry("joined.srg");
            if (joinedSrgEntry == null) {
                callback.setTextStatus(Locale.getString(Locale.MSG_NO_MAPPINGS));
                callback.onDone(false);
                mappingsFile.close();
                return;
            }
            FileOutputStream srg = new FileOutputStream(joinedSrg);
            IOUtils.copy(mappingsFile.getInputStream(joinedSrgEntry), srg);
            mappingsFile.close();
            callback.setTextStatus(Locale.getString(Locale.MSG_REMAPPING));
            net.md_5.specialsource.SpecialSource.main(new String[]{
                "--in-jar", enableOptifine ? patchedJar.getAbsolutePath() : creator.getSourceClient().getAbsolutePath(),
                "--out-jar", creator.getTargetClient().getAbsolutePath(),
                "--srg-in", joinedSrg.getAbsolutePath()
            });
            callback.setTextStatus(Locale.getString(Locale.MSG_SAVING));
            creator.computeTargetClient();
            creator.setProperty("me.mcblueparrot.client.version", latest.getId());
            creator.setProperty("user.language", "en");
            creator.setProperty("user.country", "US");
            creator.addArguments("--tweakClass", "me.mcblueparrot.client.tweak.Tweaker");
            creator.save("net.minecraft.launchwrapper.Launch");
            callback.setTextStatus(Locale.getString(Locale.MSG_CREATING_PROFILE));
            callback.onDone(addProfile(creator.getTargetName()));
        } catch (Throwable e) {
            callback.setTextStatus(Locale.getString(Locale.MSG_REMAP_FAILED), e);
            callback.onDone(false);
        }
    }

	private URL getOptifineUrl() throws IOException {
		URLConnection connection = new URL("https://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_M5.jar").openConnection();
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
			case LAUNCHER_TYPE_MINECRAFT:
				File launcherProfiles = new File(data, "launcher_profiles.json");
				File launcherProfilesMS = new File(data, "launcher_profiles_microsoft_store.json");
				File launcherUiState = new File(data, "launcher_ui_state.json");

				if(launcherProfilesMS.lastModified() > launcherProfiles.lastModified()) {
					launcherProfiles = launcherProfilesMS;
				}

				if(!launcherProfiles.exists()) {
					callback.setTextStatus(Locale.getString(Locale.MSG_NO_LAUNCHER_PROFILES));
					return false;
				}

				JSONObject profilesData = new JSONObject(
						FileUtils.readFileToString(launcherProfiles, StandardCharsets.UTF_8));
				JSONObject profiles = profilesData.getJSONObject("profiles");

				JSONObject newProfile = new JSONObject();

				String now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString();

				newProfile.put("created", now);
				newProfile.put("lastUsed", now);
				newProfile.put("lastVersionId", versionId);
				newProfile.put("name", "Sol Client");
				newProfile.put("icon", "data:image/png;base64," + Base64.getEncoder().encodeToString(IOUtils.resourceToByteArray("/logo_128x.png")));

				profiles.put("sol-client", newProfile);

				FileUtils.writeStringToFile(launcherProfiles, profilesData.toString(), StandardCharsets.UTF_8);

				if(launcherUiState.exists()) {
					dismissInstallation(launcherUiState, versionId);
				}

				return true;
			case LAUNCHER_TYPE_POLYMC:
				return false;
		}
	}

	private static void dismissInstallation(File launcherUiState, String versionId) throws IOException {
		String data = FileUtils.readFileToString(launcherUiState, StandardCharsets.UTF_8);

		if(data.contains("$#")) {
			data = data.substring(data.indexOf("$#") + 2);

			while(data.startsWith("\n") || data.startsWith("\r")) {
				data = data.substring(1);
			}
		}

		JSONObject obj = new JSONObject(data);

		String uiEvents = "{}";

		if(obj.getJSONObject("data").has("UiEvents")) {
			uiEvents = obj.getJSONObject("data").getString("UiEvents");
		}

		JSONObject uiEventsObj = new JSONObject(uiEvents);

		if(!uiEventsObj.has("hidePlayerSafetyDisclaimer")) {
			uiEventsObj.put("hidePlayerSafetyDisclaimer", new JSONObject());
		}

		JSONObject dismissedDisclaimers = uiEventsObj.getJSONObject("hidePlayerSafetyDisclaimer");
		dismissedDisclaimers.put(versionId + "_sol-client", true);

		obj.getJSONObject("data").put("UiEvents", uiEventsObj.toString());

		FileUtils.writeStringToFile(launcherUiState, obj.toString(), StandardCharsets.UTF_8);
	}

}

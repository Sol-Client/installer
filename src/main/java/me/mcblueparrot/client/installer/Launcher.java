package me.mcblueparrot.client.installer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.formdev.flatlaf.json.Json;

import me.mcblueparrot.client.installer.util.ClientRelease;
import me.mcblueparrot.client.installer.util.OperatingSystem;

public enum Launcher {
	MINECRAFT(() -> Arrays.asList(new File(OperatingSystem.current().getDataDir(),
			OperatingSystem.current() == OperatingSystem.OSX ? "minecraft" : ".minecraft"))),
	POLYMC(() -> {
		switch(OperatingSystem.current()) {
			default:
				return Arrays.asList(new File(System.getProperty("user.home"), ".local/share/PolyMC"),
						new File(System.getProperty("user.home"), ".var/app/org.polymc.PolyMC/data/polymc"));
			case OSX:
			case WINDOWS:
				return Arrays.asList(new File(OperatingSystem.OSX.getDataDir(), "PolyMC"));
		}
	});

	private List<File> locations;

	private Launcher(Supplier<List<File>> locations) {
		this.locations = locations.get();
	}

	public List<File> getLocations() {
		return locations;
	}

	public File getDefaultLocation() {
		return locations.stream().filter(File::exists).sorted(Comparator.comparingLong((file) -> -deepLastModified(file)))
				.findFirst().orElse(new File("."));
	}

	private static long deepLastModified(File file) {
		long lastModified = file.lastModified();

		if(!file.isDirectory()) {
			return lastModified;
		}

		for(File subFile : file.listFiles()) {
			lastModified = Math.max(lastModified, deepLastModified(subFile));
		}

		return lastModified;
	}

	public boolean install(File data) throws IOException {
		ClientRelease latest = ClientRelease.latest();
		switch(this) {
			default:
			case MINECRAFT:
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
			case POLYMC:
				return false;
		}
	}

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.mcblueparrot.client.installer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import me.mcblueparrot.client.installer.util.OperatingSystem;

/**
 *
 * @author maks
 */
public class DefaultPaths {
	public static final int LAUNCHER_TYPE_POLYMC = 0;
	public static final int LAUNCHER_TYPE_MINECRAFT = 1;
	private static final List<File> minecraftLauncherPaths;
	private static final List<File> polymcPaths;
	static {
		List<File> polyPaths;
		switch (OperatingSystem.current()) {
					default:
						polyPaths = Arrays.asList(new File(System.getProperty("user.home"), ".local/share/PolyMC"),
								new File(System.getProperty("user.home"), ".var/app/org.polymc.PolyMC/data/polymc"));
					case OSX:
					case WINDOWS:
						polyPaths = Arrays.asList(new File(OperatingSystem.OSX.getDataDir(), "PolyMC"));
		}
		polymcPaths = polyPaths;
		minecraftLauncherPaths = Arrays.asList(new File(OperatingSystem.current().getDataDir(),
						OperatingSystem.current() == OperatingSystem.OSX ? "minecraft" : ".minecraft"));
	}
	public static List<File> getLocationsForLauncher(int type) {
		switch(type) {
			case LAUNCHER_TYPE_POLYMC:
				return polymcPaths;
			case LAUNCHER_TYPE_MINECRAFT:
				return minecraftLauncherPaths;
			default:
				throw new IllegalArgumentException();
		}
	}
	public static File getDefaultLocation(List<File> locations) {
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
}

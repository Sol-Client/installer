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

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import io.github.solclient.installer.util.OperatingSystem;

public class Launchers {
	/**
	 * MultiMC and forks.
	 */
	public static final int LAUNCHER_TYPE_MULTIMC = 0;
	public static final int LAUNCHER_TYPE_MINECRAFT = 1;
	private static final List<File> MINECRAFT_LAUNCHER_PATHS;
	private static final List<File> MULTIMC_PATHS;
	static {
		List<File> multimcPaths;
		switch(OperatingSystem.current()) {
			default:
				multimcPaths = Arrays.asList(new File(System.getProperty("user.home"), ".local/share/multimc"));
				break;
			case OSX:
			case WINDOWS:
				multimcPaths = Arrays.asList(new File(OperatingSystem.current().getDataDir(), "MultiMC"));
		}
		MULTIMC_PATHS = multimcPaths;
		MINECRAFT_LAUNCHER_PATHS = Arrays.asList(new File(OperatingSystem.current().getDataDir(),
						OperatingSystem.current() == OperatingSystem.OSX ? "minecraft" : ".minecraft"));
	}
	public static List<File> getLocationsForLauncher(int type) {
		switch(type) {
			case LAUNCHER_TYPE_MINECRAFT:
				return MINECRAFT_LAUNCHER_PATHS;
			case LAUNCHER_TYPE_MULTIMC:
				return MULTIMC_PATHS;
			default:
				throw new IllegalArgumentException();
		}
	}
	public static File getVersionJar(File data, String version, int type) {
		switch(type) {
			case LAUNCHER_TYPE_MINECRAFT:
				return new File(data, "versions/" + version + "/" + version + ".jar");
			case LAUNCHER_TYPE_MULTIMC:
				return new File(data, "libraries/com/mojang/minecraft/" + version + "/minecraft-" + version + "-client.jar");
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

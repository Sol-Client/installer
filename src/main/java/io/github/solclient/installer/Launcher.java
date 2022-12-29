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
import java.util.*;

import io.github.solclient.installer.util.OperatingSystem;

public final class Launcher {

	public static final int MULTIMC = 0, PRISM = 1, MOJANG = 2;
	private static final List<File> MOJANG_PATHS, MULTIMC_PATHS, PRISM_PATHS;

	static {
		List<File> multimcPaths, prismPaths;
		switch (OperatingSystem.current()) {
			default:
				multimcPaths = Arrays.asList(new File(System.getProperty("user.home"), ".local/share/multimc"));
				prismPaths = Arrays.asList(
						new File(System.getProperty("user.home"),
								".var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher"),
						new File(System.getProperty("user.home"),
								".local/share/org.prismlauncher.PrismLauncher/data/PrismLauncher"),
						new File(System.getProperty("user.home"),
								".var/app/org.polymc.PolyMC/data/polymc") /*
																			 * prism, for whatever reason, sometimes
																			 * uses the old data directory
																			 */);
				break;
			case OSX:
			case WINDOWS:
				multimcPaths = Arrays.asList(new File(OperatingSystem.current().getDataDir(), "MultiMC"));
				prismPaths = Arrays.asList(new File(OperatingSystem.current().getDataDir(), "PrismLauncher")); // best
																												// guess
		}
		MULTIMC_PATHS = multimcPaths;
		PRISM_PATHS = prismPaths;
		MOJANG_PATHS = Arrays.asList(new File(OperatingSystem.current().getDataDir(),
				OperatingSystem.current() == OperatingSystem.OSX ? "minecraft" : ".minecraft"));
	}

	public static List<File> getLocationsForLauncher(int type) {
		switch (type) {
			case MOJANG:
				return MOJANG_PATHS;
			case MULTIMC:
				return MULTIMC_PATHS;
			case PRISM:
				return PRISM_PATHS;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static File getVersionJar(File data, String version, int type) {
		switch (type) {
			case MOJANG:
				return new File(data, "versions/" + version + "/" + version + ".jar");
			case MULTIMC:
			case PRISM:
				return new File(data,
						"libraries/com/mojang/minecraft/" + version + "/minecraft-" + version + "-client.jar");
			default:
				throw new IllegalArgumentException();
		}
	}

	public static String getId(int type) {
		switch (type) {
			case MOJANG:
				return "mojang";
			case MULTIMC:
				return "multimc";
			case PRISM:
				return "prismlauncher";
			default:
				return "unknown";
		}
	}

	public static File getDefaultLocation(List<File> locations) {
		return locations.stream().filter(File::exists)
				.sorted(Comparator.comparingLong((file) -> -deepLastModified(file))).findFirst().orElse(new File("."));
	}

	private static long deepLastModified(File file) {
		long lastModified = file.lastModified();

		if (!file.isDirectory()) {
			return lastModified;
		}

		for (File subFile : file.listFiles()) {
			lastModified = Math.max(lastModified, deepLastModified(subFile));
		}

		return lastModified;
	}

}

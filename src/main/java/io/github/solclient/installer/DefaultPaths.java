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
						break;
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

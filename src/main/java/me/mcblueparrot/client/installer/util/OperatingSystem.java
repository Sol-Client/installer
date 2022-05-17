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

package me.mcblueparrot.client.installer.util;

import java.io.File;

public enum OperatingSystem {
	LINUX(""), // is the best
	WINDOWS("AppData\\Roaming"),
	OSX("Library/Application Support");

	private File dataDir;
	private static OperatingSystem current;

	private OperatingSystem(String dataDir) {
		this.dataDir = new File(System.getProperty("user.home"), dataDir);
	}

	public File getDataDir() {
		return dataDir;
	}

	public static OperatingSystem current() {
		if(current == null) {
			String os = System.getProperty("os.name").toLowerCase();
			if(os.contains("win")) {
				return current = WINDOWS;
			}
			else if(os.contains("mac")) {
				return current = OSX;
			}
			return current = LINUX;
		}
		return current;
	}

}

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

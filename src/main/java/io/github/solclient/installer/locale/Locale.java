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

package io.github.solclient.installer.locale;

import java.io.*;
import java.net.URL;

public final class Locale {

	public static final int MSG_GETTING_VERSION_INFO = 0, MSG_GETTING_VERSION_INFO_FAILED = 1,
			MSG_INSTALLING_VERSION = 2, MSG_DOWNLOADING_GENERIC = 3, MSG_DOWNLOADING_CLIENT = 4,
			MSG_DOWNLOADING_MAPPINGS = 5, MSG_NO_MAPPINGS = 6, MSG_REMAPPING = 7, MSG_SAVING = 8,
			MSG_DOWNLOAD_ERROR = 9, MSG_INITIALIZATION_FAILED = 10, MSG_CACHE_FAILED = 11, MSG_REMAP_FAILED = 12,
			MSG_UNPACKING_MAPPINGS = 13, MSG_CANT_CREATE_FOLDER = 14, MSG_DAMAGED_MC_JSON = 15, MSG_JAR_VERIFIED = 16,
			UI_TITLE = 17, UI_BACK = 18, UI_NEXT = 19, UI_SELECT_LAUNCHER = 20, UI_INSTALL_LOCATION = 21,
			UI_OH_DEAR = 22, UI_NO_GAMEDIR = 23, MSG_DONE = 24, MSG_EXTRACTING_OPTIFINE = 25,
			MSG_INSTALLING_OPTIFINE = 26, UI_FINISH = 27, UI_ENABLE_OPTIFINE = 28, UI_CUSTOMISE = 29,
			MSG_CREATING_PROFILE = 30, MSG_NO_LAUNCHER_PROFILES = 31, UI_SELECT = 32, UI_SELECT_GAMEDIR = 33,
			UI_ACCESSIBLE_DIRECTORIES = 34, MSG_INVALID_MANIFEST = 35, MSG_NO_MINECRAFT = 36,
			MSG_SEARCHING_MINECRAFT = 37, UI_ERROR = 38, UI_NO_SHA1 = 39;

	private static final String[] LOCALE_ARRAY = new String[UI_NO_SHA1 + 1];

	public static void setLocale(java.util.Locale locale) {
		loadDefault();
		String searchPath1 = "/lang/" + locale.getLanguage() + "_" + locale.getCountry() + ".txt";
		String searchPath2 = "/lang/" + locale.getLanguage() + ".txt";
		URL localeUrl = null;

		if ((localeUrl = Locale.class.getResource(searchPath1)) == null) {
			localeUrl = Locale.class.getResource(searchPath2);
		}

		if (localeUrl == null) {
			System.err.printf("No locale found for %s_%s\n", locale.getLanguage(), locale.getCountry());
			return;
		}

		parseLocale(localeUrl);

	}

	private static void parseLocale(URL is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is.openStream()));
			String line;
			for (int ctr = 0; (line = reader.readLine()) != null; ctr++) {
				LOCALE_ARRAY[ctr] = line.replace("\\n", "\n");
			}
		} catch (IOException error) {
			System.err.println("Could not read locale due to an I/O exception:");
			error.printStackTrace();
			return;
		}
	}

	private static void loadDefault() {
		LOCALE_ARRAY[MSG_GETTING_VERSION_INFO] = "Getting version info...";
		LOCALE_ARRAY[MSG_GETTING_VERSION_INFO_FAILED] = "Failed to get version info";
		LOCALE_ARRAY[MSG_INSTALLING_VERSION] = "Using version %s";
		LOCALE_ARRAY[MSG_CACHE_FAILED] = "Failed to create cache folder";
		LOCALE_ARRAY[MSG_DOWNLOADING_CLIENT] = "Downloading client...";
		LOCALE_ARRAY[MSG_DOWNLOADING_MAPPINGS] = "Downloading mappings...";
		LOCALE_ARRAY[MSG_DOWNLOADING_GENERIC] = "Downloading %s...";
		LOCALE_ARRAY[MSG_NO_MAPPINGS] = "Can't find mappings!";
		LOCALE_ARRAY[MSG_REMAPPING] = "Remapping...";
		LOCALE_ARRAY[MSG_SAVING] = "Saving...";
		LOCALE_ARRAY[MSG_DOWNLOAD_ERROR] = "Download failed";
		LOCALE_ARRAY[MSG_REMAP_FAILED] = "Remapping failed";
		LOCALE_ARRAY[MSG_INITIALIZATION_FAILED] = "Unable to initialize";
		LOCALE_ARRAY[MSG_UNPACKING_MAPPINGS] = "Unpacking mappings...";
		LOCALE_ARRAY[MSG_CANT_CREATE_FOLDER] = "Can't create folder %s";
		LOCALE_ARRAY[MSG_DAMAGED_MC_JSON] = "Damaged Minecraft JSON";
		LOCALE_ARRAY[MSG_JAR_VERIFIED] = "%s OK";
		LOCALE_ARRAY[UI_TITLE] = "Sol Client Installer";
		LOCALE_ARRAY[UI_BACK] = "< Back";
		LOCALE_ARRAY[UI_NEXT] = "Next >";
		LOCALE_ARRAY[UI_SELECT_LAUNCHER] = "Select your launcher";
		LOCALE_ARRAY[UI_INSTALL_LOCATION] = "Install location";
		LOCALE_ARRAY[UI_OH_DEAR] = "Oh Dear!";
		LOCALE_ARRAY[UI_NO_GAMEDIR] = "Could not access the specified game folder";
		LOCALE_ARRAY[MSG_DONE] = "Done!";
		LOCALE_ARRAY[MSG_EXTRACTING_OPTIFINE] = "Extracting OptiFine...";
		LOCALE_ARRAY[MSG_INSTALLING_OPTIFINE] = "Installing OptiFine...";
		LOCALE_ARRAY[UI_FINISH] = "Finish";
		LOCALE_ARRAY[UI_ENABLE_OPTIFINE] = "Install OptiFine";
		LOCALE_ARRAY[UI_CUSTOMISE] = "Customize";
		LOCALE_ARRAY[MSG_CREATING_PROFILE] = "Creating profile...";
		LOCALE_ARRAY[MSG_NO_LAUNCHER_PROFILES] = "Could not find launcher profiles";
		LOCALE_ARRAY[UI_SELECT] = "Choose";
		LOCALE_ARRAY[UI_SELECT_GAMEDIR] = "Choose the game directory";
		LOCALE_ARRAY[UI_ACCESSIBLE_DIRECTORIES] = "Accessible directories";
		LOCALE_ARRAY[MSG_INVALID_MANIFEST] = "The version manifest is not valid!";
		LOCALE_ARRAY[MSG_NO_MINECRAFT] = "Minecraft %s not found in manifest";
		LOCALE_ARRAY[MSG_SEARCHING_MINECRAFT] = "Searching for Minecraft %s...";
		LOCALE_ARRAY[UI_ERROR] = "Error";
		LOCALE_ARRAY[UI_NO_SHA1] = "SHA1 is not supported by this JVM";
	}

	public static String get(int msg) {
		return LOCALE_ARRAY[msg];
	}

	public static String get(int msg, Object... formatArgs) {
		return String.format(LOCALE_ARRAY[msg], formatArgs);
	}

}

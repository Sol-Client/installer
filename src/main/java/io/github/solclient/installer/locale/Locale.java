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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Locale {

	public static final int MSG_GETTING_VERSION_INFO = 0;
	public static final int MSG_GETTING_VERSION_INFO_FAILED = 1;
	public static final int MSG_INSTALLING_VERSION = 2;
	public static final int MSG_DOWNLOADING_GENERIC = 3;
	public static final int MSG_DOWNLOADING_CLIENT = 4;
	public static final int MSG_DOWNLOADING_MAPPINGS = 5;
	public static final int MSG_NO_MAPPINGS = 6;
	public static final int MSG_REMAPPING = 7;
	public static final int MSG_SAVING = 8;
	public static final int MSG_DOWNLOAD_ERROR = 9;
	public static final int MSG_INITIALIZATION_FAILED = 10;
	public static final int MSG_CACHE_FAILED = 11;
	public static final int MSG_REMAP_FAILED = 12;
	public static final int MSG_UNPACKING_MAPPINGS = 13;
	public static final int MSG_CANT_CREATE_FOLDER = 14;
	public static final int MSG_DAMAGED_MC_JSON = 15;
	public static final int MSG_JAR_VERIFIED = 16;
	public static final int UI_TITLE = 17;
	public static final int UI_BACK = 18;
	public static final int UI_NEXT = 19;
	public static final int UI_SELECT_LAUNCHER = 20;
	public static final int UI_INSTALL_LOCATION = 21;
	public static final int UI_OH_DEAR = 22;
	public static final int UI_NO_GAMEDIR = 23;
	public static final int MSG_DONE = 24;
	public static final int MSG_EXTRACTING_OPTIFINE = 25;
	public static final int MSG_INSTALLING_OPTIFINE = 26;
	public static final int UI_FINISH = 27;
	public static final int UI_ENABLE_OPTIFINE = 28;
	public static final int UI_CUSTOMISE = 29;
	public static final int MSG_CREATING_PROFILE = 30;
	public static final int MSG_NO_LAUNCHER_PROFILES = 31;
	public static final int UI_SELECT = 32;
	public static final int UI_SELECT_GAMEDIR = 33;
	public static final int UI_ACCESSIBLE_DIRECTORIES = 34;
	public static final int MSG_INVALID_MANIFEST = 35;
	public static final int MSG_NO_MINECRAFT = 36;
	public static final int MSG_SEARCHING_MINECRAFT = 37;
	public static final int UI_ERROR = 38;
	public static final int UI_NO_SHA1 = 39;
	private static final String[] LOCALE_ARRAY = new String[UI_NO_SHA1 + 1];

	public static void setLocale(java.util.Locale locale) {
		loadDefault();
		String searchPath1 = "/lang/"+locale.getLanguage()+"_"+locale.getCountry()+".txt";
		String searchPath2 = "/lang/"+locale.getLanguage()+".txt";
		URL localeUrl = null;
		if((localeUrl = Locale.class.getResource(searchPath1)) == null) {
			localeUrl = Locale.class.getResource(searchPath2);
		}
		if(localeUrl == null) {
			System.out.println("No locale found for " + locale.getLanguage()+"_"+locale.getCountry());
			return;
		}
		parseLocale(localeUrl);

	}

	private static void parseLocale(URL is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is.openStream()));
			String line;
			for(int ctr = 0; (line = reader.readLine()) != null; ctr++) {
				LOCALE_ARRAY[ctr] = line.replace("\\n", "\n");
			}
		}
		catch(IOException e) {
			System.err.println("Could not read locale");
			e.printStackTrace();
			return;
		}
	}

	private static void loadDefault() {
		LOCALE_ARRAY[MSG_GETTING_VERSION_INFO] = "Getting version info...";
		LOCALE_ARRAY[MSG_GETTING_VERSION_INFO_FAILED] = "Failed to get version info";
		LOCALE_ARRAY[MSG_INSTALLING_VERSION] = "Using version %s";
		LOCALE_ARRAY[MSG_CACHE_FAILED] = "Failed to create a cache folder";
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
		LOCALE_ARRAY[UI_SELECT_LAUNCHER] = "Select your launcher type";
		LOCALE_ARRAY[UI_INSTALL_LOCATION] = "Install location";
		LOCALE_ARRAY[UI_OH_DEAR] = "Oh Dear!";
		LOCALE_ARRAY[UI_NO_GAMEDIR] = "Could not access the specified game folder";
		LOCALE_ARRAY[MSG_DONE] = "Done!";
		LOCALE_ARRAY[MSG_EXTRACTING_OPTIFINE] = "Extracting OptiFine...";
		LOCALE_ARRAY[MSG_INSTALLING_OPTIFINE] = "Installing OptiFine...";
		LOCALE_ARRAY[UI_FINISH] = "Finish";
		LOCALE_ARRAY[UI_ENABLE_OPTIFINE] = "Add OptiFine";
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

	public static String getString(int msg) {
		return LOCALE_ARRAY[msg];
	}

	public static String getString(int msg, Object... varArg) {
		return String.format(LOCALE_ARRAY[msg], varArg);
	}

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.solclient.installer.locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author maks
 */
public class Locale {
    private static final HashMap<Integer,String> localeMap = new HashMap();
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
    
    public static void setLocale(java.util.Locale locale) {
        localeMap.clear();
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
            String line = null;
            int ctr = 0;
            while((line = reader.readLine()) != null) {
                localeMap.put(ctr, line.replace("\\n", "\n"));
                ctr++;
            }
        }catch(IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    private static void loadDefault() {
        localeMap.put(MSG_GETTING_VERSION_INFO, "Getting version info...");
        localeMap.put(MSG_GETTING_VERSION_INFO_FAILED, "Failed to get version info");
        localeMap.put(MSG_INSTALLING_VERSION, "Using version %s");
        localeMap.put(MSG_CACHE_FAILED, "Failed to create a cache folder");
        localeMap.put(MSG_DOWNLOADING_CLIENT, "Downloading client");
        localeMap.put(MSG_DOWNLOADING_MAPPINGS, "Downloading mappings");
        localeMap.put(MSG_DOWNLOADING_GENERIC, "Downloading %s");
        localeMap.put(MSG_NO_MAPPINGS, "Can't find mappings!");
        localeMap.put(MSG_REMAPPING, "Remapping...");
        localeMap.put(MSG_SAVING, "Saving...");
        localeMap.put(MSG_DOWNLOAD_ERROR, "Download failed");
        localeMap.put(MSG_REMAP_FAILED, "Remapping failed");
        localeMap.put(MSG_INITIALIZATION_FAILED, "Unable to initialize");
        localeMap.put(MSG_UNPACKING_MAPPINGS, "Unpacking mappings...");
        localeMap.put(MSG_CANT_CREATE_FOLDER, "Can't create folder %s");
        localeMap.put(MSG_DAMAGED_MC_JSON, "Damaged Minecraft JSON");
        localeMap.put(MSG_JAR_VERIFIED, "%s OK");
        localeMap.put(UI_TITLE, "Sol Client Installer");
        localeMap.put(UI_BACK, "< Back");
        localeMap.put(UI_NEXT, "Next >");
        localeMap.put(UI_SELECT_LAUNCHER, "Select your launcher type");
        localeMap.put(UI_INSTALL_LOCATION, "Install location");
        localeMap.put(UI_OH_DEAR, "Oh Dear!");
        localeMap.put(UI_NO_GAMEDIR, "This game directory does not exist or isn't accessible!");
        localeMap.put(MSG_DONE, "Done!");

    }
    
    public static String getString(int msg) {
        return localeMap.get(msg);
    }
    public static String getString(int msg, Object... varArg) {
        return String.format(localeMap.get(msg), varArg);
    }
}

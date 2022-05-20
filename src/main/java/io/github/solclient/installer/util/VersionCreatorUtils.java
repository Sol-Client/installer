/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.solclient.installer.util;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author maks
 */
public class VersionCreatorUtils {
	private static MessageDigest shaDigest;
	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean SHA1Supported() {
		return shaDigest != null;
	}
	
	public static String mavenNameToPath(String mavenName) {
		String[] mvnNameSplit = mavenName.split(":");
		return mvnNameSplit[0].replaceAll("\\.", "/") + "/" + mvnNameSplit[1] + "/" + mvnNameSplit[2] + "/" + mvnNameSplit[1] + "-" + mvnNameSplit[2] + ".jar";
	}
	

	public static String compute(File input) throws IOException {
		return byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
	}

	public static boolean verify(File input, String sha1) throws IOException {
		if(!input.exists())
			return false;
		if(sha1 == null)
			return true;
		String f_sha1 = byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
		return f_sha1.equals(sha1);
	}

	private static String byteToHex(final byte[] hash) {
		String result;
		try (Formatter formatter = new Formatter()) {
			for (byte b : hash) {
				formatter.format("%02x", b);
			}   result = formatter.toString();
		}
		return result;
	}
}

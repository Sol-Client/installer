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

package io.github.solclient.installer.util;

import java.io.*;
import java.security.*;
import java.util.Formatter;

import org.apache.commons.io.FileUtils;

public final class VersionCreatorUtils {

	private static MessageDigest shaDigest;

	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException error) {
			error.printStackTrace();
		}
	}

	public static boolean SHA1Supported() {
		return shaDigest != null;
	}

	public static String mavenNameToPath(String mavenName) {
		String[] mvnNameSplit = mavenName.split(":");
		return mvnNameSplit[0].replaceAll("\\.", "/") + "/" + mvnNameSplit[1] + "/" + mvnNameSplit[2] + "/"
				+ mvnNameSplit[1] + "-" + mvnNameSplit[2] + ".jar";
	}

	public static String compute(File input) throws IOException {
		return byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
	}

	public static boolean verify(File input, String sha1) throws IOException {
		if (!input.exists())
			return false;
		if (sha1 == null)
			return true;
		String f_sha1 = byteToHex(shaDigest.digest(FileUtils.readFileToByteArray(input)));
		return f_sha1.equals(sha1);
	}

	private static String byteToHex(final byte[] hash) {
		String result;
		try (Formatter formatter = new Formatter()) {
			for (byte b : hash) {
				formatter.format("%02x", b);
			}
			result = formatter.toString();
		}
		return result;
	}

}

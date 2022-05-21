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

import java.io.IOException;
import java.net.URL;
import org.json.JSONObject;

public class ClientRelease {

	private String id;
	private String gameJar;

	public static ClientRelease parse(JSONObject obj) {
		ClientRelease rel = new ClientRelease();
		rel.id = obj.getString("name");
		JSONObject gameAsset = null;

		for(Object assetObj : obj.getJSONArray("assets")) {
			if(!(assetObj instanceof JSONObject)) {
				continue;
			}

			JSONObject asset = (JSONObject) assetObj;
			if(asset.getString("name").equals("game.jar")) {
				gameAsset = asset;
				break;
			}
		}

		if(gameAsset == null) {
			throw new IllegalArgumentException("No game.jar found for version " + rel.id);
		}

		rel.gameJar = gameAsset.getString("browser_download_url");

		return rel;
	}

	public String getId() {
		return id;
	}

	public String getGameJar() {
		return gameJar;
	}

	public static ClientRelease latest() throws IOException {
		return parse(Utils.json(new URL(System.getProperty("me.mcblueparrot.client.install.api", "https://api.github.com/repos/TheKodeToad/Sol-Client/releases/latest"))));
	}

}

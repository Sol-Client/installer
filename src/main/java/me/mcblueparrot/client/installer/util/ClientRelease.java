package me.mcblueparrot.client.installer.util;

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

	public static ClientRelease latest() {
		return parse(Utils.json(System.getProperty("me.mcblueparrot.client.install.api", "https://api.github.com/repos/TheKodeToad/Sol-Client/releases/latest")));
	}

}

package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.binary.Base64;

public class MyTeamWorkParser {

	public static void main(String[] args) {

		MyManager mm = new MyManager();
	}

}

class MyManager {

	public String teamworkApiToken = "dryer20gum";
	public String teamworkUrl = "https://traineetestdel.teamwork.com/";

	public MyManager() {
		MYtreeBuldier mtbBuldier = new MYtreeBuldier();
		System.out.println(mtbBuldier.getAllProjects(teamworkUrl, teamworkApiToken));
	}

}

class MYtreeBuldier {

	public MYtreeBuldier() {

	}

	public JSONObject getAllProjects(String urlS, String apiToken) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "projects.json", credentials);
	}

	public JSONObject downloadJson(String urlBeginning, String urlEnding, String credentials) {
		String jsonString = "";
		JSONObject json;
		try {
			String encoding = "Basic "
					+ new String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
			URL url = new URL(urlBeginning + urlEnding);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			jsonString = br.readLine();
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		json = (JSONObject) JSONSerializer.toJSON(jsonString);
		return json;
	}
}

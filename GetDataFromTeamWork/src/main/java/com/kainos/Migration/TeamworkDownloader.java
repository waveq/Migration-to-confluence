package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.binary.Base64;

public class TeamworkDownloader {
	private String apiToken;
	private String url;

	public TeamworkDownloader(String apiToken, String url) {
		super();
		this.apiToken = apiToken;
		this.url = url;
		
		System.out.println(getAllProjects("https://traineetest.teamwork.com", "shark807dry"));
		JSONObject json = (JSONObject) JSONSerializer.toJSON( getAllProjects("https://traineetest.teamwork.com", "shark807dry") );  
		int altitude = json.getInt( "altitude" );
	}

	public String getAllProjects(String urlS, String apiToken) {
		String credentials = apiToken + ":X";
		return getProjectListJson(urlS, "/projects.json", credentials);
	}
	
	public String getProjectListJson(String urlBeginning, String urlEnding, String credentials) {
		String json = "";
		try {
			String encoding = "Basic "
					+ new String(Base64.encodeBase64(credentials
							.getBytes("UTF-8")), "UTF-8");
			URL url = new URL(urlBeginning + urlEnding);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			
			System.out.println("Output from Server .... \n");
			json = br.readLine();
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

}

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

public class TeamworkDownloader {
	private String apiToken;
	private String url;

	public TeamworkDownloader(String apiToken, String url) {
		super();
		this.apiToken = "shark807dry";
		this.url = url = "https://traineetest.teamwork.com";
		
		getProjectId(getAllProjects(this.url, this.apiToken));
	}
	
	public void getProjectId(String json) {
		JSONObject mainJsonObject = (JSONObject) JSONSerializer.toJSON(json);
		
		JSONArray array = (JSONArray) mainJsonObject.get("projects");

		Iterator i = array.iterator();
		while (i.hasNext()) {
			JSONObject innerObj = (JSONObject) i.next();
			System.out.println("#PROJEKT: " + innerObj.get("id"));
			JSONObject projectCategoriesJson = (JSONObject) JSONSerializer.toJSON(getAllCategoriesFromProject(this.url, this.apiToken, innerObj.getString("id")));
			JSONArray categories = (JSONArray) projectCategoriesJson.get("categories");
			
//			####
			Iterator j = categories.iterator();
			while(j.hasNext()) {
				JSONObject singleCategory = (JSONObject) j.next();
				if(singleCategory.get("parent-id").equals("")) {
					
					System.out.println("##KATEGORIA " + singleCategory.get("name"));
					JSONObject jsonFiles = (JSONObject) JSONSerializer.toJSON(getAllFilesFromProject(this.url, this.apiToken, innerObj.getString("id")));
					JSONObject project = (JSONObject) jsonFiles.get("project");
					JSONArray arrayFiles = (JSONArray) project.get("files");
					
					Iterator k = arrayFiles.iterator();
					while(k.hasNext()) {
						JSONObject singleFile = (JSONObject) k.next();
						if(singleFile.get("category-id").equals(singleCategory.get("id")))
							System.out.println("PLIK: " + singleFile.get("name"));
					}
				}
			}
		}
	}
	
	
	
	public String getAllCategoriesFromProject(String urlS, String apiToken, String projectId) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects/"+projectId+"/fileCategories.json", credentials);
	}
	
	public String getAllFilesFromProject(String urlS, String apiToken, String projectId) { 
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects/"+projectId+"/files.json", credentials);
	}


	public String getAllProjects(String urlS, String apiToken) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects.json", credentials);
	}

	public String downloadJson(String urlBeginning, String urlEnding,
			String credentials) {
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

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
	private ConfluenceManager uf;

	public TeamworkDownloader(String apiToken, String url) {
		super();
		this.apiToken = apiToken;
		this.url = url;

		uf = new ConfluenceManager();

		printTree(getAllProjects(url, apiToken));
	}

	public void printTree(JSONObject mainJson) {

		JSONArray array = (JSONArray) mainJson.get("projects");

		Iterator i = array.iterator();
		while (i.hasNext()) {
			JSONObject singleProject = (JSONObject) i.next();
			System.out.println("#PROJEKT: " + singleProject.get("name"));
//			uf.CreateSpace(innerObj.get("name").toString());
			
			JSONObject categoriesMainObject = getAllCategoriesFromProject(url, apiToken, singleProject.getString("id"));
			JSONArray categoriesArray = (JSONArray) categoriesMainObject.get("categories");

			Iterator j = categoriesArray.iterator();
			while (j.hasNext()) {
				JSONObject rootCategory = (JSONObject) j.next();
				getCategoriesAndFiles(rootCategory);
		
			}
		}
	}
	
	public void getCategoriesAndFiles (JSONObject category) {
		JSONObject singleCategory = (JSONObject) j.next();
		System.out.println("##KATEGORIA " + singleCategory.get("name"));
		
		JSONObject filesMainObject = (JSONObject) getAllFilesFromProject(url, apiToken, singleProject.getString("id"));
		JSONObject project = (JSONObject) filesMainObject.get("project");
		JSONArray filesArray = (JSONArray) project.get("files");

		Iterator k = filesArray.iterator();
		while (k.hasNext()) {
			JSONObject singleFile = (JSONObject) k.next();
			if (singleFile.get("category-id").equals(singleCategory.get("id")))
				System.out.println("PLIK: " + singleFile.get("name"));
		}
		
	}
	

	public JSONObject getAllCategoriesFromProject(String urlS, String apiToken, String projectId) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects/" + projectId + "/fileCategories.json", credentials);
	}

	public JSONObject getAllFilesFromProject(String urlS, String apiToken, String projectId) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects/" + projectId + "/files.json", credentials);
	}

	public JSONObject getAllProjects(String urlS, String apiToken) {
		String credentials = apiToken + ":X";
		return downloadJson(urlS, "/projects.json", credentials);
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

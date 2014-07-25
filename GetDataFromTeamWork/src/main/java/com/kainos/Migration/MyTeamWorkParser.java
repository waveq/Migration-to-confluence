package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
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
		MYtreeBuldier mtbBuldier = new MYtreeBuldier(teamworkUrl,teamworkApiToken);
		mtbBuldier.migrate();
	}

}

class MYtreeBuldier {

	private String teamworkUrl ;
	private String credentials ;
	private ConfluenceManager confluenceManager;
	
	public MYtreeBuldier(String teamworkUrl,String teamworkApiToken) {
		this.teamworkUrl = teamworkUrl;
		credentials = teamworkApiToken + ":X";
		confluenceManager = new ConfluenceManager();
	}
	
	public void migrate(){
		JSONArray projectsArray = getAllProjects().getJSONArray("projects");
		System.out.println(projectsArray.toString());
		
		for (int i = 0; i < projectsArray.size(); i++)
		{
		    String projectName = projectsArray.getJSONObject(i).getString("name");
		    String projectID = projectsArray.getJSONObject(i).getString("id");
		    CreateStructureOfProject(projectName,projectID);
		}
		
	}
	
	private void CreateStructureOfProject(String projectName,String projectID ) {
		confluenceManager.CreateSpace(projectName);
		//if project has categories (pages) create it,
		JSONArray categoriesArray = getAllCategoriesFromProject(projectID).getJSONArray("categories");
		CreateCategories(projectName,projectID,categoriesArray);
		
	}

	
	private void CreateCategories(String projectName, String projectID, JSONArray categoriesArray) {
		for (int i = 0; i < categoriesArray.size(); i++)
		{
	//	    String projectName = projectsArray.getJSONObject(i).getString("name");
	//	    String projectID = projectsArray.getJSONObject(i).getString("id");
		    CreateStructureOfProject(projectName,projectID);
		}
		
	}

	private JSONObject getFinalFileObject(String fileId) {
			return downloadJson("files/" + fileId + ".json");
	}

	public JSONObject getAllCategoriesFromProject(String projectId) {
		return downloadJson("projects/" + projectId + "/fileCategories.json");
	}

	public JSONObject getAllFilesFromProject(String projectId) {
		return downloadJson("projects/" + projectId + "/files.json");
	}

	public JSONObject getAllProjects() {
		return downloadJson("projects.json");
	}

	public JSONObject downloadJson(String urlEnding) {
		String jsonString = "";
		JSONObject json;
		try {
			String encoding = "Basic "	+ new String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
			URL url = new URL(teamworkUrl + urlEnding);
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

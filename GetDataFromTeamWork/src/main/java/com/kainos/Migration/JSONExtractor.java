package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public abstract class JSONExtractor {

	private String urlBeginning;
	private String credentials;
	
	static final int FILE_CATEGORY = 1;
	static final int NOTEBOOK_CATEGORY = 2;
	protected ConfluenceManager cm;

	public JSONExtractor(String apiToken, String url) {
		this.credentials = apiToken + ":X";
		this.urlBeginning = url;
		cm = new ConfluenceManager();
	}

	abstract void getCategories(JSONObject project, String parentName, String parentId,
			JSONArray categoriesArray);

	/**
	 * Iterates through projects array.
	 * 
	 * @param mainJson
	 *            - json object that contains list of all projects in
	 *            TeamworkPM.
	 */
	public void goThroughTree(int categoryType) {
		JSONArray array = (JSONArray) getAllProjects().get("projects");

		Iterator i = array.iterator();
		while (i.hasNext()) {
			JSONObject singleProject = (JSONObject) i.next();

			String modifiedProjectName = singleProject.getString("name").replaceAll(" ", "")
					.toLowerCase();
			singleProject.put("name", modifiedProjectName);
			cm.createSpace(singleProject.getString("name"));
			JSONObject categoriesMainObject = getAllCategoriesFromProject(singleProject
					.getString("id"), categoryType);
			JSONArray categoriesArray = (JSONArray) categoriesMainObject.get("categories");

			getCategories(singleProject, "", "", categoriesArray);
		}
	}

	/**
	 * Returns JSONObject that contains all categories from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	private JSONObject getAllCategoriesFromProject(String projectId, int categoryType) {
		if(categoryType == 1)
			return downloadJSON("projects/" + projectId + "/filecategories.json");
		else if(categoryType == 2)
			return downloadJSON("projects/" + projectId + "/notebookcategories.json");
		return null;
	}

	/**
	 * Returns JSONObject that contains all projects.
	 * 
	 * @return
	 */
	private JSONObject getAllProjects() {
		return downloadJSON("projects.json");
	}

	/**
	 * Connects to URL which contains JSON and returns it.
	 * 
	 * @param urlEnding
	 *            - end of url from which you want to download JSON
	 * @return JSONObject
	 */
	protected JSONObject downloadJSON(String urlEnding) {
		String jsonString = "";
		JSONObject json;
		try {
			String encoding = "Basic "
					+ new String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
			URL url = new URL(this.urlBeginning + urlEnding);
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

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

/**
 * 
 * @author szymonre
 *
 */
public class JSONExtractor {

	// Beginning of teamworkurl (https://yourpage.teamwork.com/).
	private String urlBeginning;
	private ConfluenceManager cm;
	private DownloadFileFromTW dfftw;
	// Credentials used to connect to teamwork's rest API (yourapiToken:X)
	private String credentials;

	public JSONExtractor(String apiToken, String url) {
		this.credentials = apiToken + ":X";
		this.urlBeginning = url;

		dfftw = new DownloadFileFromTW();
		cm = new ConfluenceManager();

		goThroughTree(getAllProjects());
	}

	/**
	 * Iterates through projects array.
	 * 
	 * @param mainJson
	 *            - json object that contains list of all projects in
	 *            TeamworkPM.
	 */
	public void goThroughTree(JSONObject mainJson) {
		JSONArray array = (JSONArray) mainJson.get("projects");

		Iterator i = array.iterator();
		while (i.hasNext()) {
			JSONObject singleProject = (JSONObject) i.next();
			cm.CreateSpace(singleProject.get("name").toString());
			JSONObject categoriesMainObject = getAllCategoriesFromProject(singleProject
					.getString("id"));
			JSONArray categoriesArray = (JSONArray) categoriesMainObject.get("categories");

			getCategories(singleProject, "", "", categoriesArray);
		}
	}

	/**
	 * Recursive method. If passed parentId is equals to current category's
	 * parentId then that category created in Confluence via ConfluenceManager's
	 * CreatePage method. After that getCategories method is called and then
	 * method calls itself and passes current category id and name as parentId
	 * and parentName.
	 * 
	 * If parentName equals "" it means it first invoke of method and we have to
	 * download and upload files that have no parent category.
	 * 
	 * @param project
	 *            - object of project that is currently migrated.
	 * @param parentName
	 *            - name of parent of current category. First parentName equals
	 *            "".
	 * @param parentId
	 *            - id of parent of current category. First parentId equals "".
	 * @param categoriesArray
	 *            - Array retrieved from categoriesMainObject. Contains list of
	 *            all categories.
	 */
	public void getCategories(JSONObject project, String parentName, String parentId,
			JSONArray categoriesArray) {
		Iterator i = categoriesArray.iterator();

		if (parentName.equals("")) {
			getFilesFromCategory(project, null);
		}

		while (i.hasNext()) {
			JSONObject category = (JSONObject) i.next();

			if (category.get("parent-id").equals(parentId)) {
				cm.CreatePage(project.getString("name"), category.getString("name"), parentName);
				getFilesFromCategory(project, category);
				getCategories(project, category.getString("name"), category.getString("id"),
						categoriesArray);
			}
		}
	}

	/**
	 * At the beginnig method is getting array of files which belong to the
	 * passed project. Then iterates over the array and searches for files which
	 * category-id is equals to passed category. If finds one downloads it with
	 * ConfluenceManager's AddAttachmentToPage.
	 * 
	 * If category == null it means files have to be added to root of space so
	 * their category-id is equals to "".
	 * 
	 * @param project
	 *            - json object of file's project used
	 * @param category
	 *            - json object of file's category
	 */
	public void getFilesFromCategory(JSONObject project, JSONObject category) {
		JSONObject filesMainObject = (JSONObject) getAllFilesFromProject(project.getString("id"));
		JSONObject innerProject = (JSONObject) filesMainObject.get("project");
		JSONArray filesArray = (JSONArray) innerProject.get("files");

		Iterator k = filesArray.iterator();
		while (k.hasNext()) {
			JSONObject singleFile = (JSONObject) k.next();

			if (category == null) {
				if (singleFile.getString("category-id").equals("")) {
					JSONObject finalFile = getFinalFileObject(singleFile.getString("id"));
					JSONObject finalFileContent = (JSONObject) finalFile.get("file");
					cm.AddAttatchmentToPage(project.getString("name"), "", dfftw.DownloadFileFrom(
							finalFileContent.get("download-URL").toString(),
							finalFileContent.get("name").toString()));
				}
			} else if (singleFile.get("category-id").equals(category.get("id"))) {
				JSONObject finalFile = getFinalFileObject(singleFile.getString("id"));
				JSONObject finalFileContent = (JSONObject) finalFile.get("file");
				cm.AddAttatchmentToPage(project.getString("name"), category.getString("name"),
						dfftw.DownloadFileFrom(finalFileContent.get("download-URL").toString(),
								finalFileContent.get("name").toString()));
			}
		}
	}

	/**
	 * Returns JSONObject that contains all informations (name, id, category-id,
	 * project-id, download-URL) of file which id was passed.
	 * 
	 * @param fileId
	 * @return
	 */
	private JSONObject getFinalFileObject(String fileId) {
		return downloadJson("files/" + fileId + ".json");
	}

	/**
	 * Returns JSONObject that contains all categories from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	public JSONObject getAllCategoriesFromProject(String projectId) {
		return downloadJson("projects/" + projectId + "/fileCategories.json");
	}

	/**
	 * Returns JSONObject that contains all files from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	public JSONObject getAllFilesFromProject(String projectId) {
		return downloadJson("projects/" + projectId + "/files.json");
	}

	/**
	 * Returns JSONObject that contains all projects.
	 * 
	 * @return
	 */
	public JSONObject getAllProjects() {
		return downloadJson("projects.json");
	}

	/**
	 * Connects to URL which contains JSON and returns it.
	 * 
	 * @param urlEnding
	 *            - end of url from which you want to download JSON
	 * @return JSONObject
	 */
	public JSONObject downloadJson(String urlEnding) {
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

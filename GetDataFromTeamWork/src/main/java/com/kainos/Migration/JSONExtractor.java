package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.kainos.mp4Converter.*;

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
	private Cut cut;
	private ArrayList<String> notUploadedFiles = new ArrayList<String>();
	
	private String[] ignoreFiles = {"Smart application images.tar.gz","Nuvo framework introduction.zip.008",
			"Nuvo framework introduction.zip.007", "Nuvo framework introduction.zip.006", "Nuvo framework introduction.zip.005", "Nuvo framework introduction.zip.004",
			"Nuvo framework introduction.zip.003", "Nuvo framework introduction.zip.002" ,"Nuvo framework introduction.zip.001" ,"Nuvo framework introduction.zip.000"};

	public JSONExtractor(String apiToken, String url) {
		this.credentials = apiToken + ":X";
		this.urlBeginning = url;

		dfftw = new DownloadFileFromTW();
		cm = new ConfluenceManager();
		cut = new Cut("", "");
	}

	/**
	 * Iterates through projects array.
	 * 
	 * @param mainJson
	 *            - json object that contains list of all projects in
	 *            TeamworkPM.
	 */
	public void goThroughTree() {
		JSONArray array = (JSONArray) getAllProjects().get("projects");

		Iterator i = array.iterator();
		while (i.hasNext()) {
			JSONObject singleProject = (JSONObject) i.next();
			
			String modifiedProjectName = singleProject.getString("name").replaceAll(" ", "").toLowerCase();
			singleProject.put("name", modifiedProjectName);
			cm.createSpace(singleProject.getString("name"));
			JSONObject categoriesMainObject = getAllCategoriesFromProject(singleProject
					.getString("id"));
			JSONArray categoriesArray = (JSONArray) categoriesMainObject.get("categories");

			getCategories(singleProject, "", "", categoriesArray);
		}
		
		System.out.println("NOT UPLOADED FILES: ");
		for(int j=0;j<notUploadedFiles.size();j++) {
			System.out.println(j + " " + notUploadedFiles.get(j));
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
				cm.createPage(project.getString("name"), category.getString("name"), parentName);
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
	 * 
	 *  
	 */
	public void getFilesFromCategory(JSONObject project, JSONObject category) {
		JSONObject filesMainObject = (JSONObject) getAllFilesFromProject(project.getString("id"));
		JSONObject innerProject = (JSONObject) filesMainObject.get("project");
		JSONArray filesArray = (JSONArray) innerProject.get("files");

		Iterator k = filesArray.iterator();
		while (k.hasNext()) {
	
		
			
			JSONObject singleFile = (JSONObject) k.next();

			if (category == null) {
				JSONObject customCategory = new JSONObject();
				customCategory.put("id", "");
				customCategory.put("name", "");
				category = customCategory;
			}
			if (singleFile.get("category-id").equals(category.get("id"))) {
				JSONObject finalFile = getFinalFileObject(singleFile.getString("id"));
				JSONObject finalFileContent = (JSONObject) finalFile.get("file");

				String fileName = finalFileContent.getString("name");
				boolean doContinue = false;
				for(String s : ignoreFiles) {
					if(fileName.equals(s)) {
						doContinue = true;
					}
				}
				if(doContinue){
					continue;
				}
				

				if (!fileWasUploaded(fileName, project.getString("name"))) {
					dfftw.DownloadFileFrom(finalFileContent.get("download-URL").toString(),
							fileName);
				} else {
					System.out.println("File with name: "
							+ (isMp4(fileName) ? cut.modifyName(fileName, 1) : fileName)
							+ " has been already added to confluence. Im skipping it.");
					continue;
				}
				uploadToConfluence(fileName, category.getString("name"), project.getString("name"));
			}
		}
	}

	private boolean fileWasUploaded(String fileName, String projectName) {
		boolean mp4 = isMp4(fileName);
		if (!cm.fileWasUploadedBefore(projectName, mp4 ? cut.modifyName(fileName, 1) : fileName)) {
			return false;
		}
		return true;
	}

	private boolean uploadToConfluence(String fileName, String categoryName, String projectName) {
		if (isMp4(fileName)) {
			System.out.println("MP4 FILE DETECTED");
			mp4File(projectName, categoryName, fileName);
		} else {
			boolean fileUploaded = false;
			int counter = 0;
			while (!fileUploaded && counter <= 6) {
				cm.addAttatchmentToPage(projectName, categoryName, fileName);
				fileUploaded = cm.fileWasUploadedBefore(projectName,fileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				counter++;
				if(counter == 7) {
					notUploadedFiles.add(fileName);
					System.out.println("NOT UPLOADED FILES: ");
					for(int j=0;j<notUploadedFiles.size();j++) {
						System.out.println(j + " " + notUploadedFiles.get(j));
					}
				}
			}
		}
		return true;
	}

	private boolean isMp4(String fileName) {
		if (fileName.substring(fileName.length() - 4, fileName.length()).equals(".mp4"))
			return true;
		else
			return false;
	}

	/**
	 * If the file has mp4 extension then it's parsed through Cut class and
	 * split if it's longer than specified in Cut class PART_LENGTH_IN_MINUTES
	 * parameter. After that every part is pushed to confluence via
	 * AddAttatchmentToPage.
	 * 
	 * @param projectName
	 * @param categoryName
	 * @param fileName
	 */
	private void mp4File(String projectName, String categoryName, String fileName) {
		File f = new File("./temp/");
		String path = f.getAbsolutePath() + "\\";
		cut = new Cut(fileName, path);
		int numberOfFiles = cut.start();

		for (int i = 0; i < numberOfFiles; i++) {
			boolean fileUploaded = false;
			String modifiedFileName = cut.modifyName(fileName, i + 1);
			int counter = 0;
			while (!fileUploaded && counter <= 6) {
				cm.addAttatchmentToPage(projectName, categoryName, modifiedFileName);
				fileUploaded = cm.fileWasUploadedBefore(projectName, modifiedFileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				counter++;
				if(counter == 7) {
					notUploadedFiles.add(modifiedFileName);
					System.out.println("NOT UPLOADED FILES: ");
					for(int j=0;j<notUploadedFiles.size();j++) {
						System.out.println(j + " " + notUploadedFiles.get(j));
					}
				}
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

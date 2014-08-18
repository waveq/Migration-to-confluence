package com.kainos.Migration;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public abstract class JSONExtractor extends JSONDownloader {

	protected String urlBeginning;
	protected String credentials;

	static final int FILE_CATEGORY = 1;
	static final int NOTEBOOK_CATEGORY = 2;
	protected ConfluenceManager cm;
	protected IgnoreReader ir;
	
	private int chosenCategory;
	
	/**
	 * how many times program is trying to upload file/notebook after fail.
	 */
	protected static final int ATTEMPTS_TO_UPLOAD = 3;

	public JSONExtractor(String apiToken, String url, int category) {
		this.credentials = apiToken + ":X";
		this.urlBeginning = url;
		chosenCategory = category;
		cm = new ConfluenceManager();
		ir = new IgnoreReader();
	}

	abstract void getObjectsFromCategory(JSONObject project, JSONObject category, String parentName);

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

			if (cm.spaceWasCreatedBefore(singleProject.getString("name"))) {
				System.out.println("Space with name: \"" + singleProject.getString("name")
						+ "\" already exists in confluence. Im skipping it.");
			} else {
				cm.createSpace(singleProject.getString("name"));
			}

			JSONObject categoriesMainObject = getAllCategoriesFromProject(
					singleProject.getString("id"), categoryType);
			JSONArray categoriesArray = (JSONArray) categoriesMainObject.get("categories");

			
			cm.setUploadedPages(singleProject.getString("name"));
			getCategories(singleProject, "", "", categoriesArray);
		}
	}
	
	
	/**
	 * Recursive method. If passed parentId is equals to current category's
	 * parentId then that category is created in Confluence via
	 * ConfluenceManager's CreatePage method. After that getCategories method is
	 * called and then method calls itself and passes current category id and
	 * name as parentId and parentName.
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
	protected void getCategories(JSONObject project, String parentName, String parentId,
			JSONArray categoriesArray) {
		Iterator i = categoriesArray.iterator();

		if (parentName.equals("")) {
			getObjectsFromCategory(project, null, "");
		}
		

		while (i.hasNext()) {
			JSONObject category = (JSONObject) i.next();
			

			if (category.get("parent-id").equals(parentId)) {
				if (cm.isPageInDownloadedList(category.getString("name"))) {
					category = pageWasCreatedBefore(project, category, parentName);
				}
				else {
					cm.createPage(project.getString("name"), category.getString("name"), parentName, chosenCategory);
					if(cm.pageWasCreatedBefore(project.getString("name"), category.getString("name"))) {
						cm.addNewPageToList(category.getString("name"));
					}
				}
				getObjectsFromCategory(project, category, "");
				getCategories(project, category.getString("name"), category.getString("id"),
						categoriesArray);
			}
		}
	}
	
	private JSONObject pageWasCreatedBefore(JSONObject project, JSONObject category, String parentName) {
		System.out.println("Page with name: \"" + category.getString("name") + "\" was created before. "
				+ "I'm checking if parent of the old and the new page is the same.");
		if(cm.theSamePage(project.getString("name"), parentName, category.getString("name"))) {
			System.out.println("Page with name: \"" + category.getString("name")
			+ "\" and parent page \""+ parentName + "\" already exists in confluence. I'm skipping it.");
		}
		else {
			category = catWithSameNameDifferentParent(project, category, parentName);
		}
		return category;
	}
	
	/**
	 * Method is checking if passed category with passed parentName already exists on confluence.
	 * @param project
	 * @param category
	 * @param parentName
	 * @return
	 */
	private JSONObject catWithSameNameDifferentParent(JSONObject project, JSONObject category, String parentName) {
		String tempParent = parentName;
		if(parentName.equals("")) {
			tempParent = "Home";
		}
		String categoryName = cm.createNameWithAncestor(tempParent, category.getString("name"));
		category.put("name", categoryName);
		if (cm.isPageInDownloadedList(categoryName)) {
			System.out.println("Page with name: \"" + categoryName
					+ "\" and parent page \""+ parentName + "\" already exists in confluence. Im skipping it.");
		}
		else {
			cm.createPage(project.getString("name"), categoryName, parentName, chosenCategory);
		}
		return category;
	}
	

	/**
	 * Returns JSONObject that contains all categories from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	private JSONObject getAllCategoriesFromProject(String projectId, int categoryType) {
		if (categoryType == 1)
			return downloadJSON(urlBeginning+"projects/" + projectId + "/filecategories.json", credentials);
		else if (categoryType == 2)
			return downloadJSON(urlBeginning+"projects/" + projectId + "/notebookcategories.json", credentials);
		return null;
	}

	/**
	 * Returns JSONObject that contains all projects.
	 * 
	 * @return
	 */
	private JSONObject getAllProjects() {
		return downloadJSON(urlBeginning+"projects.json", credentials);
	}

	
}

package com.kainos.Migration;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NotebookMigrator extends JSONExtractor {

	public NotebookMigrator(String apiToken, String url) {
		super(apiToken, url);
	}

	@Override
	void getCategories(JSONObject project, String parentName, String parentId,
			JSONArray categoriesArray) {
		Iterator i = categoriesArray.iterator();

		if (parentName.equals("")) {
			getNotebooksFromCategory(project, null);
		}

		while (i.hasNext()) {
			JSONObject category = (JSONObject) i.next();

			if (category.get("parent-id").equals(parentId)) {
				if (cm.pageWasCreatedBefore(project.getString("name"), category.getString("name"))) {
					System.out.println("Page with name: \"" + category.getString("name")
							+ "\" already exists in confluence. Im skipping it.");
				} else {
					cm.createPage(project.getString("name"), category.getString("name"), parentName);
				}
				getNotebooksFromCategory(project, category);
				getCategories(project, category.getString("name"), category.getString("id"),
						categoriesArray);
			}
		}
	}

	public void getNotebooksFromCategory(JSONObject project, JSONObject category) {
		JSONObject notebooksMainObject = (JSONObject) getAllNotebooksFromProject(project
				.getString("id"));
		JSONObject innerProject = (JSONObject) notebooksMainObject.get("project");
		JSONArray notebooksArray = (JSONArray) innerProject.get("notebooks");

		if(notebooksArray == null) 
			return;
		Iterator i = notebooksArray.iterator();
		while (i.hasNext()) {
			JSONObject singleNotebook = (JSONObject) i.next();

			if (category == null) {
				JSONObject customCategory = new JSONObject();
				customCategory.put("id", "");
				customCategory.put("name", "");
				category = customCategory;
			}
			if (singleNotebook.get("category-id").equals(category.get("id"))) {
				JSONObject finalNotebook = getFinalNotebookObject(singleNotebook.getString("id"));
				JSONObject finalNotebookContent = (JSONObject) finalNotebook.get("notebook");

				String notebookName = finalNotebookContent.getString("name");
				String notebookContent = finalNotebookContent.getString("content");
				
				if (cm.pageWasCreatedBefore(project.getString("name"), notebookName)) {
					System.out.println("Notebook with name: \"" + notebookName
							+ "\" already exists in confluence. Im skipping it.");
					continue;
				}
				uploadNotebookToConfluence(project.getString("name"), category.getString("name"),
						notebookName, notebookContent);
			}
		}
	}

	private void uploadNotebookToConfluence(String projectName, String categoryName,
			String notebookName, String notebookContent) {
		

		
		boolean notebookUploaded = false;
		int counter = 0;
		while(!notebookUploaded && counter <= ATTEMPTS_TO_UPLOAD) {
			cm.addNotebookToPage(projectName, categoryName, notebookName, notebookContent);
			notebookUploaded = cm.pageWasCreatedBefore(projectName, notebookName);
			System.out.println("Notebook was created [" + notebookUploaded + "]");
			counter++;
		}
	}

	private JSONObject getAllNotebooksFromProject(String projectId) {
		return downloadJSON("projects/" + projectId + "/notebooks.json");
	}

	private JSONObject getFinalNotebookObject(String notebookId) {
		return downloadJSON("notebooks/" + notebookId + ".json");
	}
}

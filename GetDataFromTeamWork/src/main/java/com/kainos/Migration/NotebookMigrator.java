package com.kainos.Migration;

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NotebookMigrator extends JSONExtractor {

	public ArrayList<String> notUploadedNotebooks = new ArrayList<String>();
	
	public NotebookMigrator(String apiToken, String url) {
		super(apiToken, url, NOTEBOOK_CATEGORY);
	}

	public void getObjectsFromCategory(JSONObject project, JSONObject category, String parentName) {
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
				uploadNotebookToConfluence(project.getString("name"), category.getString("name"), parentName,
						notebookName, notebookContent);
			}
		}
	}

	private void uploadNotebookToConfluence(String projectName, String categoryName, String parentName,
			String notebookName, String notebookContent) {
		boolean notebookUploaded = false;
		int counter = 1;
		while(!notebookUploaded && counter <= ATTEMPTS_TO_UPLOAD) {
			cm.addNotebookToPage(projectName, categoryName, notebookName, notebookContent);
			notebookUploaded = cm.pageWasCreatedBefore(projectName, notebookName);
			System.out.println("Notebook was created [" + notebookUploaded + "]");
			counter++;
			if (counter == ATTEMPTS_TO_UPLOAD+1) {
				notUploadedNotebooks.add(notebookName);
				System.out.println("NOT UPLOADED NOTEBOOKS: ");
				for (int j = 0; j < notUploadedNotebooks.size(); j++) {
					System.out.println(j + " " + notUploadedNotebooks.get(j));
				}
			}
		}
	}

	private JSONObject getAllNotebooksFromProject(String projectId) {
		return downloadJSON(urlBeginning+"projects/" + projectId + "/notebooks.json", credentials);
	}

	private JSONObject getFinalNotebookObject(String notebookId) {
		return downloadJSON(urlBeginning+"notebooks/" + notebookId + ".json", credentials);
	}
}

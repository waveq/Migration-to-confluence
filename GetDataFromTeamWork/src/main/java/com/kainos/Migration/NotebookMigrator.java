package com.kainos.Migration;

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NotebookMigrator extends JSONExtractor {

	private ArrayList<String> notUploadedNotebooks = new ArrayList<String>();
	private ArrayList<String> uploadedNotebooks = new ArrayList<String>();

	private ArrayList<String> ignoreNotebooks;
	
	private static final String IGNORED_FILES_FILE_NAME = "ignoredNotebooks";
	

	public NotebookMigrator(String apiToken, String url) {
		super(apiToken, url, NOTEBOOK_CATEGORY);
		
		ignoreNotebooks = ir.getIgnoredObjects(IGNORED_FILES_FILE_NAME);
		System.out.println("Notebooks that will not be uploaded to confluence:\n"+ ignoreNotebooks);
	}

	public void getObjectsFromCategory(JSONObject project, JSONObject category, String parentName) {
		JSONObject notebooksMainObject = (JSONObject) getAllNotebooksFromProject(project
				.getString("id"));
		JSONObject innerProject = (JSONObject) notebooksMainObject.get("project");
		JSONArray notebooksArray = (JSONArray) innerProject.get("notebooks");

		if (notebooksArray == null)
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

				boolean doContinue = false;
				for (String s : ignoreNotebooks) {
					if (notebookName.equals(s)) {
						doContinue = true;
					}
				}
				if (doContinue) {
					continue;
				}

				if (cm.isPageInDownloadedList(notebookName)) {
					System.out.println("Notebook with name: \"" + notebookName
							+ "\" already exists in confluence. Im skipping it.");
					continue;
				}
				uploadNotebookToConfluence(project.getString("name"), category.getString("name"),
						parentName, notebookName, notebookContent);
			}
		}
	}

	private void uploadNotebookToConfluence(String projectName, String categoryName,
			String parentName, String notebookName, String notebookContent) {
		boolean notebookUploaded = false;
		int counter = 1;
		while (!notebookUploaded && counter <= ATTEMPTS_TO_UPLOAD) {
			cm.addNotebookToPage(projectName, categoryName, notebookName, notebookContent);
			notebookUploaded = cm.pageWasCreatedBefore(projectName, notebookName);
			System.out.println("Notebook was created [" + notebookUploaded + "]");
			if(notebookUploaded) {
				cm.addNewPageToList(notebookName);
				uploadedNotebooks.add(notebookName);
			}
			counter++;
			if (counter == ATTEMPTS_TO_UPLOAD + 1) {
				notUploadedNotebooks.add(notebookName);
				System.out.println("NOT UPLOADED NOTEBOOKS: ");
				for (int j = 0; j < notUploadedNotebooks.size(); j++) {
					System.out.println(j + " " + notUploadedNotebooks.get(j));
				}
			}
		}
	}
	
	public void printUploadedNotebooks() {
		System.out.println("UPLOADED NOTEBOOKS: ");
		for (int j = 0; j < uploadedNotebooks.size(); j++) {
			System.out.println(j + " " + uploadedNotebooks.get(j));
		}
	}
	
	public void printNotUploadedNotebooks() {
		System.out.println("NOT UPLOADED NOTEBOOKS: ");
		for (int j = 0; j < notUploadedNotebooks.size(); j++) {
			System.out.println(j + " " + notUploadedNotebooks.get(j));
		}
	}

	private JSONObject getAllNotebooksFromProject(String projectId) {
		return downloadJSON(urlBeginning + "projects/" + projectId + "/notebooks.json", credentials);
	}

	private JSONObject getFinalNotebookObject(String notebookId) {
		return downloadJSON(urlBeginning + "notebooks/" + notebookId + ".json", credentials);
	}
}

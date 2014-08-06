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
				cm.createPage(project.getString("name"), category.getString("name"), parentName);
				getNotebooksFromCategory(project, category);
				getCategories(project, category.getString("name"), category.getString("id"),
						categoriesArray);
			}
		}
	}

	public void getNotebooksFromCategory(JSONObject project, JSONObject category) {
		JSONObject notebooksMainObject = (JSONObject) getAllNotebooksFromProject(project.getString("id"));
		JSONObject innerProject = (JSONObject) notebooksMainObject.get("project");
		JSONArray notebooksArray = (JSONArray) innerProject.get("notebooks");
		
		Iterator i = notebooksArray.iterator();
		while(i.hasNext()) {
			JSONObject singleNotebook = (JSONObject) i.next();
			
			if (category == null) {
				JSONObject customCategory = new JSONObject();
				customCategory.put("id", "");
				customCategory.put("name", "");
				category = customCategory;
			}
			if (singleNotebook.get("category-id").equals(category.get("id"))) {
				JSONObject finalNotebook = getFinalNotebookObject(singleNotebook.getString("ïd"));
				JSONObject finalNotebookContent = (JSONObject) finalNotebook.get("notebook");
				
				String notebookName = finalNotebookContent.getString("name");
				String notebookContent = finalNotebookContent.getString("content");
			}
		}
	}
	
	private JSONObject getAllNotebooksFromProject(String projectId) {
		return downloadJSON("projects/" + projectId + "/notebooks.json");
	}
	
	private JSONObject getFinalNotebookObject(String notebookId) {
		return downloadJSON("notebooks/" + notebookId + ".json");
	}
}

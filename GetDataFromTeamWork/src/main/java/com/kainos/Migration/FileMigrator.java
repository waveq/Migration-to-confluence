package com.kainos.Migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.kainos.mp4Converter.*;

/**
 * 
 * @author Szymon Rekawek
 *
 */
public class FileMigrator extends JSONExtractor {

	private DownloadFileFromTW dfftw;
	private Cut cut;
	public ArrayList<String> notUploadedFiles = new ArrayList<String>();

	private String[] ignoreFiles = { "Smart application images.tar.gz",
			"Nuvo framework introduction.zip.008", "Nuvo framework introduction.zip.007",
			"Nuvo framework introduction.zip.006", "Nuvo framework introduction.zip.005",
			"Nuvo framework introduction.zip.004", "Nuvo framework introduction.zip.003",
			"Nuvo framework introduction.zip.002", "Nuvo framework introduction.zip.001",
			"Nuvo framework introduction.zip.000" };

	public FileMigrator(String apiToken, String url) {
		super(apiToken, url);
		cut = new Cut("", "");
		dfftw = new DownloadFileFromTW();
	}

	/**
	 * Recursive method. If passed parentId is equals to current category's
	 * parentId then that category is created in Confluence via ConfluenceManager's
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
	@Override
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
	 * At the beginning method is getting array of files which belong to the
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
	private void getFilesFromCategory(JSONObject project, JSONObject category) {
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
				for (String s : ignoreFiles) {
					if (fileName.equals(s)) {
						doContinue = true;
					}
				}
				if (doContinue) {
					continue;
				}

				if (!fileWasUploaded(fileName, project.getString("name"), category.getString("name"))) {
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

	public boolean fileWasUploaded(String fileName, String projectName, String categoryName) {
		boolean mp4 = isMp4(fileName);
		if (!cm.fileWasUploadedBefore(projectName, categoryName, mp4 ? cut.modifyName(fileName, 1) : fileName)) {
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
				fileUploaded = cm.fileWasUploadedBefore(projectName, categoryName, fileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				counter++;
				if (counter == 7) {
					notUploadedFiles.add(fileName);
					System.out.println("NOT UPLOADED FILES: ");
					for (int j = 0; j < notUploadedFiles.size(); j++) {
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
				fileUploaded = cm.fileWasUploadedBefore(projectName, categoryName, modifiedFileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				counter++;
				if (counter == 7) {
					notUploadedFiles.add(modifiedFileName);
					System.out.println("NOT UPLOADED FILES: ");
					for (int j = 0; j < notUploadedFiles.size(); j++) {
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
		return downloadJSON("files/" + fileId + ".json");
	}

	/**
	 * Returns JSONObject that contains all files from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	private JSONObject getAllFilesFromProject(String projectId) {
		return downloadJSON("projects/" + projectId + "/files.json");
	}
}

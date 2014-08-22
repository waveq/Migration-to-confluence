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
	private int globalFileCounter = 1;
	private ArrayList<String> notUploadedFiles = new ArrayList<String>();
	private ArrayList<String> uploadedFiles = new ArrayList<String>();

	private ArrayList<String> ignoreFiles;
	
	private static final String IGNORED_FILES_FILE_NAME = "ignoredFiles";

	public FileMigrator(String apiToken, String url) {
		super(apiToken, url, FILE_CATEGORY);
		cut = new Cut("", "");
		dfftw = new DownloadFileFromTW();
		ignoreFiles = ir.getIgnoredObjects(IGNORED_FILES_FILE_NAME);
		System.out.println("Files that will not be uploaded to confluence:\n"+ignoreFiles);
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
	public void getObjectsFromCategory(JSONObject project, JSONObject category, String parentName) {
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
				System.out.println("<- File: #"+globalFileCounter++ +" ->");

				fileName = fileName.replaceAll("\\s{2,}", " ");
				if (!fileWasUploaded(fileName, project.getString("name"),
						category.getString("name"))) {
					dfftw.DownloadFileFrom(finalFileContent.get("download-URL").toString(),
							fileName);
				} else {
					System.out.println("File with name: \""
							+ (isMp4(fileName) ? cut.modifyName(fileName, 1) : fileName)
							+ "\" and parent page name \"" + category.getString("name") + "\" has been already added to confluence. I'm skipping it.");
					continue;
				}
				uploadToConfluence(fileName, category.getString("name"), project.getString("name"));
			}
		}
	}

	public boolean fileWasUploaded(String fileName, String projectName, String categoryName) {
		boolean mp4 = isMp4(fileName);
		if (!cm.fileWasUploadedBefore(projectName, categoryName, mp4 ? cut.modifyName(fileName, 1)
				: fileName)) {
			return false;
		}
		return true;
	}

	private void uploadToConfluence(String fileName, String categoryName, String projectName) {
		if (isMp4(fileName)) {
			System.out.println("MP4 FILE DETECTED");
			uploadMp4File(projectName, categoryName, fileName);
		} else {
			boolean fileUploaded = false;
			int counter = 1;
			while (!fileUploaded && counter <= ATTEMPTS_TO_UPLOAD) {
				cm.addAttatchmentToPage(projectName, categoryName, fileName);
				fileUploaded = cm.fileWasUploadedBefore(projectName, categoryName, fileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				if(fileUploaded)
					uploadedFiles.add(fileName);
				counter++;
				if (counter == ATTEMPTS_TO_UPLOAD+1) {
					notUploadedFiles.add(fileName);
					System.out.println("NOT UPLOADED FILES: ");
					for (int j = 0; j < notUploadedFiles.size(); j++) {
						System.out.println(j + " " + notUploadedFiles.get(j));
					}
				}
			}
		}
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
	private void uploadMp4File(String projectName, String categoryName, String fileName) {
		File f = new File("./temp/");
		String path = f.getAbsolutePath() + "\\";
		cut = new Cut(fileName, path);
		int numberOfFiles = cut.start();

		for (int i = 0; i < numberOfFiles; i++) {
			boolean fileUploaded = false;
			String modifiedFileName = cut.modifyName(fileName, i + 1);			
			
			if(cm.fileWasUploadedBefore(projectName, categoryName, modifiedFileName)) {
				continue;
			}

			
			int counter = 1;
			while (!fileUploaded && counter <= ATTEMPTS_TO_UPLOAD) {
				cm.addAttatchmentToPage(projectName, categoryName, modifiedFileName);
				fileUploaded = cm
						.fileWasUploadedBefore(projectName, categoryName, modifiedFileName);
				System.out.println("File uploaded [" + fileUploaded + "]");
				if(fileUploaded)
					uploadedFiles.add(modifiedFileName);
				counter++;
				if (counter == ATTEMPTS_TO_UPLOAD+1) {
					notUploadedFiles.add(modifiedFileName);
					System.out.println("NOT UPLOADED FILES: ");
					for (int j = 0; j < notUploadedFiles.size(); j++) {
						System.out.println(j + " " + notUploadedFiles.get(j));
					}
				}
			}
		}
	}
	
	public void printUploadedFiles() {
		System.out.println("UPLOADED FILES: ");
		for (int j = 0; j < uploadedFiles.size(); j++) {
			System.out.println(j + " " + uploadedFiles.get(j));
		}
	}
	
	public void printNotUploadedFiles() {
		System.out.println("NOT UPLOADED FILES: ");
		for (int j = 0; j < notUploadedFiles.size(); j++) {
			System.out.println(j + " " + notUploadedFiles.get(j));
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
		return downloadJSON(urlBeginning+"files/" + fileId + ".json", credentials);
	}

	/**
	 * Returns JSONObject that contains all files from project which id was
	 * passed.
	 * 
	 * @param projectId
	 * @return
	 */
	private JSONObject getAllFilesFromProject(String projectId) {
		return downloadJSON(urlBeginning+"projects/" + projectId + "/files.json", credentials);
	}
}

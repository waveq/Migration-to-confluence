package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ConfluenceManager extends JSONDownloader {

	private File file;
	private String dirPath;

	private Properties prop = new Properties();
	String server;
	String credentials;

	public void getProperties() {
		InputStream input;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			server = prop.getProperty("server");

			String user = prop.getProperty("user");
			String password = prop.getProperty("password");
			credentials = user + ":" + password;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ConfluenceManager() {
		file = new File("./confluence-cli-4.0.0-SNAPSHOT/confluence.bat");
		dirPath = file.getAbsoluteFile().getAbsolutePath();
		getProperties();
	}

	/**
	 * Wrapper method. Run appropriate command
	 * 
	 * @param cmd
	 *            String contains Confluence command
	 */
	private String execCmd(String cmd) {
		try {
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(dirPath + cmd);
			p.waitFor();
			// OutputReaders:
			return printOutput(p);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String printOutput(Process process) {
		BufferedReader stdInput = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(
				new InputStreamReader(process.getErrorStream()));
		String line, output = "", error = "";
		try {
			while ((line = stdError.readLine()) != null) {
				error += (line + "\n");
			}
			while ((line = stdInput.readLine()) != null) {
				output += (line + "\n");
			}

			if (!output.equals(""))
				System.out.println(output);
			if (!error.equals(""))
				System.out.println(error);

			if (output.contains("in list"))
				return output.substring(0, 1);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Create Space in Confluence
	 * 
	 * @param SpaceName
	 */
	public void createSpace(String SpaceName) {
		execCmd(ConfluenceCommand.addSpace(SpaceName));
	}

	/**
	 * Create Page in Confluence. If ParentPageName is equals "", Page will be
	 * added to Space otherwise Page will be nested to other Page
	 * 
	 * @param spaceName
	 * @param pageName
	 * @param parentPageName
	 */
	public void createPage(String spaceName, String pageName, String parentPageName,
			int chosenCategory) {
		parentPageName = emptyParent(parentPageName);

		if (chosenCategory == 1) {
			execCmd(ConfluenceCommand.addNestedPageFile(spaceName, pageName, parentPageName));
		} else if (chosenCategory == 2) {
			execCmd(ConfluenceCommand.addNestedPageNotebook(spaceName, pageName, parentPageName));
		} else {
			System.out.println("### FATAL ERROR CHECK JSONEXTRACTOR.CHOSEN_CATEGORY ###");
			System.out.println("page: " + pageName + " parentPage: " + parentPageName
					+ " chosenCategory: " + chosenCategory);
			System.out.println("##### ### #####");
		}
	}

	/**
	 * Add file to selected Page. If you want add file to main space set
	 * toPageName=@home
	 * 
	 * @param spaceName
	 * @param pageName
	 * @param fileName
	 */
	public void addAttatchmentToPage(String spaceName, String pageName, String fileName) {
		File fileToUpload = new File("./temp/" + fileName);
		if (pageName.equals(""))
			pageName = "@home";

		execCmd(ConfluenceCommand.addAttatchmentToPage(spaceName, pageName, fileToUpload
				.getAbsoluteFile().getAbsolutePath()));
		// fileToUpload.delete();
	}

	public void addNotebookToPage(String spaceName, String parentPageName, String pageName,
			String notebookContent) {
		System.out.println("I'm adding notebook: " + pageName);

		notebookContent = modifyNotebookContent(notebookContent);

		if (parentPageName.equals("")) {
			parentPageName = "Home";
		}

		parentPageName = parentPageName.replaceAll("\\s", "+");
		parentPageName = parentPageName.replaceAll("&", "%26");

		String url = server + "rest/api/content?title="
				+ parentPageName.replaceAll("\\s", "+").replaceAll("&", "%26") + "&spaceKey="
				+ spaceName;
		JSONObject mainJson = downloadJSON(url, credentials);
		JSONArray array = (JSONArray) mainJson.getJSONArray("results");
		JSONObject result = array.getJSONObject(0);
		String parentId = result.getString("id");

		String postUrl = server + "rest/api/content/";

		String json = createJSONToPostUpload(spaceName, pageName, notebookContent, parentId);
		try {
			this.uploadNotebookPost(postUrl, credentials, json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String createJSONToPostUpload(String spaceName, String pageName,
			String notebookContent, String id) {
		return "{\"type\":\"page\",\"ancestors\":[{\"type\":\"page\",\"id\":" + id
				+ "}],\"title\":\"" + pageName + "\",\"space\":{\"key\":\"" + spaceName
				+ "\"},\"body\":{\"storage\":{\"value\":\"" + notebookContent
				+ "\",\"representation\":\"storage\"}}}";
	}

	String downloadedPage = null;
	ArrayList<String> fileNames = new ArrayList<String>();

	/**
	 * If passed pageName is not equals to last invoke pageName then it iterates
	 * through json tree and adds fileNames to arrayList. If passed pageName is
	 * equals to last invoke pageName then it iterates through that arrayList.
	 * 
	 * @param spaceName
	 * @param pageName
	 * @param fileName
	 * @return true if file was uploaded before, false if not
	 */
	public boolean fileWasUploadedBefore(String spaceName, String pageName, String fileName) {
		if (downloadedPage == null || !downloadedPage.equals(pageName)) {
			boolean exist = false;
			fileNames = new ArrayList<String>();
			downloadedPage = pageName;
			if (pageName.equals(""))
				pageName = "Home";

			pageName = pageName.replaceAll("\\s", "+");
			pageName = pageName.replaceAll("&", "%26");

			String url = server + "rest/api/content?title=" + pageName + "&spaceKey=" + spaceName
					+ "&expand=descendants";
			JSONObject mainJson = downloadJSON(url, credentials);
			JSONArray array = (JSONArray) mainJson.getJSONArray("results");
			JSONObject result = array.getJSONObject(0);

			JSONObject desc = result.getJSONObject("descendants");
			JSONObject links = desc.getJSONObject("_links");
			String descUrl = links.getString("self");

			JSONObject attachmentsMainJson = downloadJSON(descUrl + "?expand=attachment",
					credentials);
			JSONObject attachment = attachmentsMainJson.getJSONObject("attachment");

			JSONObject links2 = attachment.getJSONObject("_links");
			String descUrl2 = links2.getString("self");

			JSONObject attachmentsMainJson2 = downloadJSON(descUrl2 + "?limit=300", credentials);
			JSONArray attachmentArray = attachmentsMainJson2.getJSONArray("results");

			Iterator i = attachmentArray.iterator();
			while (i.hasNext()) {

				JSONObject singleAttachment = (JSONObject) i.next();
				String title = singleAttachment.getString("title");
				fileNames.add(title);
				if (fileName.equals(title)) {
					exist = true;
				}
			}
			if (!exist)
				downloadedPage = null;
			return exist;
		} else {
			if (fileNames.contains(fileName))
				return true;
		}
		downloadedPage = null;
		return false;
	}

	private ArrayList<String> addedPages;
	
	public void addNewPageToList(String pageName) {
		addedPages.add(pageName);
	}
	
	public boolean isPageInDownloadedList(String pageName) {
		pageName = pageName.trim();
		for(String s : addedPages) {
			if(s.equals(pageName))
				return true;
		}
		return false;
	}

	public void setUploadedPages(String spaceName) {
		addedPages = new ArrayList<String>();

		String url = server + "rest/api/content";

		getAllPagesLimiter(spaceName, url, 0);
		getAllPagesLimiter(spaceName, url, 1);
		getAllPagesLimiter(spaceName, url, 2);
		getAllPagesLimiter(spaceName, url, 3);
		getAllPagesLimiter(spaceName, url, 4);
	}

	private void getAllPagesLimiter(String spaceName, String url, int part) {
		String jsonurl = url + "?limit="+(100)+"&start="+part*100+"";
		System.out.println(jsonurl); 
		JSONObject mainJson2 = downloadJSON(jsonurl, credentials);
		JSONArray results = mainJson2.getJSONArray("results");
		

		Iterator i = results.iterator();
		while (i.hasNext()) {
			JSONObject singlePage = (JSONObject) i.next();
			String title = singlePage.getString("title");
			
			JSONObject _expandable = singlePage.getJSONObject("_expandable");
			String space = _expandable.getString("space");
			
			if(("/rest/api/space/" + spaceName).equals(space)) {
				addedPages.add(title);
			}
		}
	}

	/**
	 * 
	 * @param spaceName
	 * @param pageName
	 * @return true if page exists in space, false if not
	 */
	public boolean pageWasCreatedBefore(String spaceName, String pageName) {
		pageName = regexModify(pageName);
		String s = execCmd(ConfluenceCommand.searchForPage(spaceName, pageName));
		if (!s.equals("")) {
			return Integer.parseInt(s) != 0;
		}
		return false;
	}

	public boolean theSamePage(String spaceName, String parentPageName, String pageName) {
		parentPageName = emptyParent(parentPageName);

		pageName = pageName.replaceAll("\\s", "+");
		pageName = pageName.replaceAll("&", "%26");

		String url = server + "rest/api/content?title=" + pageName + "&spaceKey=" + spaceName
				+ "&expand=ancestors";
		JSONObject mainJson = downloadJSON(url, credentials);
		JSONArray array = (JSONArray) mainJson.getJSONArray("results");
		JSONObject result = array.getJSONObject(0);
		JSONArray ancestors = result.getJSONArray("ancestors");
		JSONObject directAncestor = ancestors.getJSONObject(ancestors.size() - 1);
		String ancestorName = directAncestor.getString("title");

		if (ancestorName.equals("Home")) {
			ancestorName = "@home";
		}

		System.out.println("ParentPassed: " + parentPageName);
		System.out.println("ParentFound: " + ancestorName);

		return parentPageName.equals(ancestorName);
	}

	public String createNameWithAncestor(String ancestor, String name) {
		return ancestor + "_ " + name;
	}

	public boolean spaceWasCreatedBefore(String spaceName) {
		String s = execCmd(ConfluenceCommand.searchSpace(spaceName));
		if (!s.equals("")) {
			return Integer.parseInt(s) != 0;
		}
		return false;
	}

	public String emptyParent(String parentName) {
		if (parentName.equals("")) {
			parentName = "@home";
		}
		return parentName;
	}

	/**
	 * replace all ( and { with \( and \{ so regex in confluence CLI could find
	 * it.
	 * 
	 * @param fileName
	 * @return
	 */
	private String regexModify(String fileName) {
		fileName = fileName.replaceAll("\\(", "\\\\(");
		fileName = fileName.replaceAll("\\)", "\\\\)");
		fileName = fileName.replaceAll("\\{", "\\\\{");
		fileName = fileName.replaceAll("\\}", "\\\\}");
		fileName = fileName.replaceAll("\\+", "\\\\+");
		return fileName;
	}

	private String modifyNotebookContent(String notebook) {
		// notebook = notebook.replaceAll("<div>", "");
		// notebook = notebook.replaceAll("</div>", "");
		// notebook = notebook.replaceAll("<a href=\".*\">", "");
		// notebook = notebook.replaceAll("</a>", "");
		// notebook = notebook.replaceAll("<p>", "");
		// notebook = notebook.replaceAll("</p>", "");
		// notebook = notebook.replaceAll("<br />", "");
		notebook = notebook.replaceAll("<br>", "<br />");
		// notebook = notebook.replaceAll("\\\\", "\\\\\\");
		notebook = notebook.replaceAll("\"", "\\\\\"");
		// notebook = notebook.replaceAll("<span style=\".*\">", "");
		// notebook = notebook.replaceAll("</span>", "");
		// notebook = notebook.replaceAll("<strong>", "");
		// notebook = notebook.replaceAll("</strong>", "");

		notebook = notebook.replaceAll("\n", "").replace("\r", "");
		return notebook;
	}

	public void removeSpace(String spaceName) {
		execCmd(ConfluenceCommand.removeSpace(spaceName));
	}
}

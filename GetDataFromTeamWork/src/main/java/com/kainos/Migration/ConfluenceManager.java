package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
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

public class ConfluenceManager {

	private File file;
	private String dirPath;

	public ConfluenceManager() {
		file = new File("./confluence-cli-4.0.0-SNAPSHOT/confluence.bat");
		dirPath = file.getAbsoluteFile().getAbsolutePath();
	}

	/**
	 * Wrapper method. Run appropriate command
	 * 
	 * @param cmd
	 *            String contains Confluence command
	 */
	private String execCmd(String cmd) {
		try {
			System.out.println(dirPath + cmd);
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
			System.out.println(output);
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
	 * @param SpaceName
	 * @param PageName
	 * @param ParentPageName
	 */
	public void createPage(String SpaceName, String PageName, String ParentPageName) {
		if (ParentPageName.equals(""))
			execCmd(ConfluenceCommand.addNestedPage(SpaceName, PageName, "@home"));
		else
			execCmd(ConfluenceCommand.addNestedPage(SpaceName, PageName, ParentPageName));
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
		notebookContent = modifyNotebookContent(notebookContent);
		if (parentPageName.equals(""))
			parentPageName = "@home";

		for (int i = 0; i <= notebookContent.length() / 8000; i++) {
			if (i == 0) {
				System.out.println("DODAJE PAGE");
				execCmd(ConfluenceCommand.addNotebook(spaceName, parentPageName, pageName,
						notebookSubstring(notebookContent, i)));
			} else {
				execCmd(ConfluenceCommand.editNotebook(spaceName, parentPageName, pageName,
						notebookSubstring(notebookContent, i)));
			}
		}
	}

	private String notebookSubstring(String notebook, int pt) {
		int begin = pt * 7500;
		int end = begin + 7500;
		if (end > notebook.length()) {
			end = notebook.length();
		}
		return notebook.substring(begin, end);
	}

	/**
	 * 
	 * @param spaceName
	 * @param pageName
	 * @param fileName
	 * @return true if file was uploaded before, false if not
	 */
	public boolean fileWasUploadedBefore(String spaceName, String pageName, String fileName) {
		if (pageName.equals(""))
			pageName = "Home";
		
		String url = "https://myconfluence.atlassian.net/wiki/rest/api/content?title="+pageName+"&spaceKey="+spaceName+"&expand=descendants";
		JSONObject mainJson = downloadJSON(url);
		JSONArray array = (JSONArray) mainJson.getJSONArray("results");
		JSONObject result = array.getJSONObject(0);
		
		JSONObject desc = result.getJSONObject("descendants");
		JSONObject links = desc.getJSONObject("_links");
		String descUrl = links.getString("self");
		
		JSONObject attachmentsMainJson = downloadJSON(descUrl+"?expand=attachment");
		JSONObject attachment = attachmentsMainJson.getJSONObject("attachment");
		
		JSONObject links2 = attachment.getJSONObject("_links");
		String descUrl2 = links2.getString("self");
		
		JSONObject attachmentsMainJson2 = downloadJSON(descUrl2+"?limit=100");
		
		JSONArray attachmentArray = attachmentsMainJson2.getJSONArray("results");
		
		Iterator i = attachmentArray.iterator();
		while(i.hasNext()) {
			JSONObject singleAttachment = (JSONObject) i.next();
			String title = singleAttachment.getString("title");
			if(fileName.equals(title)) {
				return true;
			}
		}
		return false;
	}

	protected JSONObject downloadJSON(String jsonUrl) {
		String credentials = "admin:tajnehaslo";
		String jsonString = "";
		JSONObject json;
		try {
			String encoding = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
			URL url = new URL(jsonUrl);
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

	/**
	 * 
	 * @param spaceName
	 * @param pageName
	 * @return true if page exists in space, false if not
	 */
	public boolean pageWasCreatedBefore(String spaceName, String parentPageName, String pageName) {
		if (parentPageName.equals(""))
			parentPageName = "@home";
		String s = execCmd(ConfluenceCommand.searchForPage(spaceName, parentPageName, pageName));
		if (!s.equals("")) {
			return Integer.parseInt(s) != 0;
		}
		return false;
	}

	public boolean spaceWasCreatedBefore(String spaceName) {
		String s = execCmd(ConfluenceCommand.searchSpace(spaceName));
		if (!s.equals("")) {
			return Integer.parseInt(s) != 0;
		}
		return false;
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
		notebook = notebook.replaceAll("\"", "'");
		notebook = notebook.replaceAll("line-height", "font-size");
		return notebook;
	}

	public void removeSpace(String spaceName) {
		execCmd(ConfluenceCommand.removeSpace(spaceName));
	}
}

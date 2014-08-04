package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
	static int counter = 0;

	private String execCmd(String cmd) {
		try {
			Process p = Runtime.getRuntime().exec(dirPath + cmd);
			p.waitFor();
			System.out.println(dirPath + cmd);
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

			if (output.contains("attachments in list"))
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
	 * @param toPageName
	 * @param fileName
	 */
	public void addAttatchmentToPage(String spaceName, String toPageName, String fileName) {
		File fileToUpload = new File("./temp/" + fileName);
		if (toPageName.equals(""))
			toPageName = "@home";
		execCmd(ConfluenceCommand.addAttatchmentToPage(spaceName, toPageName, fileToUpload
				.getAbsoluteFile().getAbsolutePath()));
		// fileToUpload.delete();
	}

	public boolean fileWasUploadedBefore(String spaceName, String fileName) {
		String s = execCmd(ConfluenceCommand.searchForFile(spaceName, regexModify(fileName)));
		if (!s.equals("")) {
			return Integer.parseInt(s) != 0;
		}
		return false;
	}
	
	/**
	 * replace all ( and { with \( and \{ so regex in confluence CLI could find it.
	 * @param fileName
	 * @return
	 */
	private String regexModify (String fileName) {
		fileName = fileName.replaceAll("\\(", "\\\\(");
		fileName = fileName.replaceAll("\\)", "\\\\)");
		fileName = fileName.replaceAll("\\{", "\\\\{");
		fileName = fileName.replaceAll("\\}", "\\\\}");
		return fileName;
	}
	

	public void removeSpace(String spaceName) {
		execCmd(ConfluenceCommand.removeSpace(spaceName));
	}
}

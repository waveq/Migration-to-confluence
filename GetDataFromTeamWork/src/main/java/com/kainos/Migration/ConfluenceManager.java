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
	 * @param cmd String contains Confluence command
	 */
	private void ExecCmd(String cmd) {
		try {
			System.out.println(dirPath);
			Process p = Runtime.getRuntime().exec("cmd.exe /c " + dirPath + cmd);
			p.waitFor();

			// print some result from cmd
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "", sb = "";
			while ((line = reader.readLine()) != null) {
				sb += line + "\n";
			}
			System.out.print(sb);
			// end code

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create Space in Confluence
	 * @param SpaceName
	 */
	public void CreateSpace(String SpaceName) {
		ExecCmd(ConfluenceCommand.AddSpace(SpaceName));
	}


	/**
	 * Create Page in Confluence. If ParentPageName is equals "", Page will be added to Space otherwise Page will be nested to other Page
	 * @param SpaceName
	 * @param PageName
	 * @param ParentPageName
	 */
	public void CreatePage(String SpaceName, String PageName, String ParentPageName) {
		if(ParentPageName.equals(""))
			ExecCmd(ConfluenceCommand.AddNestedPage(SpaceName, PageName, "@home"));//ExecCmd(ConfluenceCommand.AddPage(SpaceName, PageName));
		else
			ExecCmd(ConfluenceCommand.AddNestedPage(SpaceName, PageName, ParentPageName));
	}
	
	/**
	 * Add file to selected Page. If you want add file to main space set toPageName=@home
	 * @param spaceName
	 * @param toPageName
	 * @param nameOfFile
	 */
	public void AddAttatchmentToPage(String spaceName, String toPageName, String nameOfFile) {
		File fileToUpload = new File("./temp/"+nameOfFile);
		fileToUpload.getAbsoluteFile().getAbsolutePath();
		ExecCmd(ConfluenceCommand.AddAttatchmentToPage(spaceName, toPageName, fileToUpload.getAbsoluteFile().getAbsolutePath()));
	}
	
	public void RemoveSpace(String spaceName){
		ExecCmd(ConfluenceCommand.RemoveSpace(spaceName));
	}
}

package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UploadFiles {

	File file;
	String dirPath;

	public UploadFiles() {
		file = new File("./confluence-cli-4.0.0-SNAPSHOT/confluence.bat");
		dirPath = file.getAbsoluteFile().getAbsolutePath();
	}

	private void ExecCmd(String cmd) {
		try {
			System.out.println(dirPath);
			Process p = Runtime.getRuntime().exec("cmd.exe /c " + dirPath + cmd);
			p.waitFor();

			// this code can be deleted - only print some result from cmd
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

	public void CreateSpace(String SpaceName) {
		ExecCmd(ConfluenceCommand.AddSpace(SpaceName));
	}

	public void CreatePage(String SpaceName, String PageName) {
		ExecCmd(ConfluenceCommand.AddPage(SpaceName, PageName));
	}

	public void CreateNestedPage(String SpaceName, String PageName, String ParentPageName) {
		ExecCmd(ConfluenceCommand.AddNestedPage(SpaceName, PageName, ParentPageName));
	}
	
	public void AddAttatchmentToPage(String spaceName, String toPageName, String pathToFile) {
		ExecCmd(ConfluenceCommand.AddAttatchmentToPage(spaceName, toPageName, pathToFile));
	}
}

package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Manager {
	String teamworkApiToken;
	String teamworkUrl;
	FileMigrator fm;
	private Properties prop = new Properties();
	InputStream input;

	public Manager() {
		GetAndSetRequiredProperties();
		fm = new FileMigrator(teamworkApiToken, teamworkUrl);
		fm.goThroughTree(JSONExtractor.FILE_CATEGORY);
		
		System.out.println("NOT UPLOADED FILES: ");
		for(int j=0;j<fm.notUploadedFiles.size();j++) {
			System.out.println(j + " " + fm.notUploadedFiles.get(j));
		}
	}

	/**
	 * Set appropriate properties in .bat file
	 * @param ConfluenceServer
	 * @param ConfluenceUserName
	 * @param ConfluencePassword
	 */
	private void SetProperiesInConfluenceFiles(String ConfluenceServer, String ConfluenceUserName,
			String ConfluencePassword) {

		String oldFileName = "confluence.bat";
		String tmpFileName = "tmp_" + oldFileName;

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(oldFileName));
			bw = new BufferedWriter(new FileWriter(tmpFileName));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("set server="))
					line = "set server=" + ConfluenceServer;
				else if (line.contains("set username="))
					line = "set username=" + ConfluenceUserName;
				else if (line.contains("set password="))
					line = "set password=" + ConfluencePassword;
				bw.write(line + "\n");
			}
		} catch (Exception e) {
			return;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Once everything is complete, delete old file..
		File oldFile = new File(oldFileName);
		oldFile.delete();

		// And rename tmp file's name to old file name
		File newFile = new File(tmpFileName);
		newFile.renameTo(oldFile);

	}

	/**
	 * Initialize properties from file config.properties and set it in properties and .bat file
	 */
	public void GetAndSetRequiredProperties() {
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			teamworkApiToken = prop.getProperty("apikey");
			teamworkUrl = prop.getProperty("addres");
			SetProperiesInConfluenceFiles(prop.getProperty("server"), prop.getProperty("user"), prop.getProperty("password"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

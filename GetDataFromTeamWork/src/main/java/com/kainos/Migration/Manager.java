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
	NotebookMigrator nm;
	private Properties prop = new Properties();
	InputStream input;

	public Manager() {
		long start = System.currentTimeMillis();
		GetAndSetRequiredProperties();

		nm = new NotebookMigrator(teamworkApiToken, teamworkUrl);
		nm.goThroughTree(JSONExtractor.NOTEBOOK_CATEGORY);
		long end1 = System.currentTimeMillis();
		System.out.println("Finished uploading notebooks: " + (end1 - start) / 1000 / 60 + " minutes.");
		notUploadedNotebooks();
		
		
		fm = new FileMigrator(teamworkApiToken, teamworkUrl);
		fm.goThroughTree(JSONExtractor.FILE_CATEGORY);
		long end2 = System.currentTimeMillis();
		notUploadedNotebooks();
		notUploadedFiles();
		
		System.out.println("Notebooks: "+ (end1 - start) / 1000 / 60 + " minutes.");
		System.out.println("Files: "+ (end2 - end1) / 1000 / 60 + " minutes.");
		System.out.println("Everything: "+ (end2 - start) / 1000 / 60 + " minutes." );
	}
	
	private void notUploadedNotebooks() {
		System.out.println("NOT UPLOADED NOTEBOOKS: ");
		for (int j = 0; j < nm.notUploadedNotebooks.size(); j++) {
			System.out.println(j + " " + nm.notUploadedNotebooks.get(j));
		}
	}
	private void notUploadedFiles() {
		System.out.println("NOT UPLOADED FILES: ");
		for (int j = 0; j < fm.notUploadedFiles.size(); j++) {
			System.out.println(j + " " + fm.notUploadedFiles.get(j));
		}
	}

	/**
	 * Set appropriate properties in .bat file
	 * 
	 * @param ConfluenceServer
	 * @param ConfluenceUserName
	 * @param ConfluencePassword
	 */
	private void SetProperiesInConfluenceFiles(String ConfluenceServer, String ConfluenceUserName,
			String ConfluencePassword) {

		File oldBat = new File("./confluence-cli-4.0.0-SNAPSHOT/confluence.bat");
		File newBat = new File("./confluence-cli-4.0.0-SNAPSHOT/temp_confluence.bat");

		BufferedReader br = null;
		BufferedWriter bw = null;

		try {
			if (!newBat.exists()) {
				newBat.createNewFile();
			}

			br = new BufferedReader(new FileReader(oldBat.getAbsoluteFile()));
			bw = new BufferedWriter(new FileWriter(newBat.getAbsoluteFile()));
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
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		oldBat.delete();
		newBat.renameTo(oldBat);
	}

	/**
	 * Initialize properties from file config.properties and set it in
	 * properties and .bat file
	 */
	public void GetAndSetRequiredProperties() {
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			teamworkApiToken = prop.getProperty("apikey");
			teamworkUrl = prop.getProperty("addres");
			SetProperiesInConfluenceFiles(prop.getProperty("server"), prop.getProperty("user"),
					prop.getProperty("password"));

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

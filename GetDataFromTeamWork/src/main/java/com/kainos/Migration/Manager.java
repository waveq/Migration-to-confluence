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
	private String teamworkApiToken;
	private String teamworkUrl;
	private FileMigrator fm;
	private NotebookMigrator nm;
	private Properties prop = new Properties();
	private InputStream input;

	public Manager() {
		GetAndSetRequiredProperties();

		nm = new NotebookMigrator(teamworkApiToken, teamworkUrl);
		fm = new FileMigrator(teamworkApiToken, teamworkUrl);
	}

	public void go() {
		long start = System.currentTimeMillis();
		nm.goThroughTree(JSONExtractor.NOTEBOOK_CATEGORY);
		long endNotebooks = System.currentTimeMillis();
		System.out.println("Finished uploading notebooks: " + (endNotebooks - start) / 1000 / 60 + " minutes.");
		nm.printNotUploadedNotebooks();
		
		
		fm.goThroughTree(JSONExtractor.FILE_CATEGORY);
		long endFiles = System.currentTimeMillis();
		nm.printNotUploadedNotebooks();
		fm.printNotUploadedFiles();
		
		System.out.println("######");
		
		fm.printUploadedFiles();
		nm.printUploadedNotebooks();
		
		System.out.println("Notebooks: "+ (endNotebooks - start) / 1000 / 60 + " minutes.");
		System.out.println("Files: "+ (endFiles - endNotebooks) / 1000 / 60 + " minutes.");
		System.out.println("Everything: "+ (endFiles - start) / 1000 / 60 + " minutes." );
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

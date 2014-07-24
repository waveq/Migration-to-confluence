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
import java.util.Scanner;

public class Manager {
	String teamworkApiToken;
	String teamworkUrl;
	TeamworkDownloader td;
	Properties prop = new Properties();
	InputStream input = null;

	public Manager() {
		getDataToConnectTeamwork();
		td = new TeamworkDownloader(teamworkApiToken, teamworkUrl);
	}
	
	public void Migrate(){
		//zmienna = td.download
		
	}

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
					line = line.replace("set server=", "set server=" + ConfluenceServer);
				if (line.contains("set username="))
					line = line.replace("set username=", "set username=" + ConfluenceUserName);
				if (line.contains("set password="))
					line = line.replace("set password=", "set password=" + ConfluencePassword);
				bw.write(line + "\n");
			}
		} catch (Exception e) {
			return;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				//
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				//
			}
		}
		// Once everything is complete, delete old file..
		File oldFile = new File(oldFileName);
		oldFile.delete();

		// And rename tmp file's name to old file name
		File newFile = new File(tmpFileName);
		newFile.renameTo(oldFile);

	}

	public void getDataToConnectTeamwork() {
		/*
		 * Scanner consoleScanner = new Scanner(System.in);
		 * 
		 * System.out.println("Api key to Teamwork: "); teamworkApiToken =
		 * consoleScanner.nextLine();
		 * 
		 * System.out.println(
		 * "URL to your teamwork (e.g. https://example.teamwork.com): ");
		 * teamworkUrl = consoleScanner.nextLine();
		 * 
		 * consoleScanner.close();
		 */
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
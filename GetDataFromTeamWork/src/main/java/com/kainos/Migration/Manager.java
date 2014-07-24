package com.kainos.Migration;

import java.util.Scanner;

public class Manager {
	String teamworkApiToken;
	String teamworkUrl;
	TeamworkDownloader td;
	
	public Manager() {
		getDataToConnectTeamwork();
		td = new TeamworkDownloader(teamworkApiToken, teamworkUrl);
	}
	
	public void getDataToConnectTeamwork() {
		Scanner consoleScanner = new Scanner(System.in);

		System.out.println("Api key to Teamwork: ");
		teamworkApiToken = consoleScanner.nextLine();

		System.out.println("URL to your teamwork (e.g. https://example.teamwork.com): ");
		teamworkUrl = consoleScanner.nextLine();
		
		consoleScanner.close();
	}
}

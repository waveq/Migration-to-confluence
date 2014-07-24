package com.kainos.Migration;

public class ConfluenceCommand {

	// deleted comment parameter
	private static final String AddSpace = " --action addSpace --space \"%s\" --title \"%s\" ";
	private static final String AddPage = " --action addPage --space \"%s\" --title \"%s\" --parent \"Home\"";
	private static final String AddNastedPage = " --action addPage --space \"%s\" --title \"%s\" --parent \"%s\"";
	/*
	 * space - space
	 * title - pageName (main place of page is @home)file - path to
	 * file
	 */
	private static final String AddAttatchmentToPage = " --action addAttachment --space \"%s\" --title \"%s\" --file \"%s\"";

	public static String AddSpace(String spaceName) {
		System.out.println(String.format(AddSpace, spaceName, spaceName));
		return String.format(AddSpace, spaceName, spaceName);
	}

	public static String AddPage(String spaceName, String pageName) {
		return String.format(AddPage, spaceName, pageName);
	}

	public static String AddNestedPage(String spaceName, String pageName, String paentPageName) {
		return String.format(AddNastedPage, spaceName, pageName, paentPageName);
	}

	public static String AddAttatchmentToPage(String spaceName, String toPageName, String pathToFile) {
		return String.format(AddAttatchmentToPage, spaceName, toPageName, pathToFile);
	}
}
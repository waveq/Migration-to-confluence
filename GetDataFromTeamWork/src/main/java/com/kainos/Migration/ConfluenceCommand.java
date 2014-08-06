package com.kainos.Migration;

public class ConfluenceCommand {

	// deleted comment parameter
	private static final String ADD_SPACE = " --action addSpace --space \"%s\" --title \"%s\" ";
	private static final String ADD_NESTED_PAGE = " --action addPage --space \"%s\" --title \"%s\" --parent \"%s\"";
	private static final String SEARCH_FOR_FILE_IN_SPACE = " --action getAttachmentList --space \"%s\" --title \"%s\" --regex \"%s\"";
	/*
	 * space - space
	 * title - pageName (main place of page is @home)file - path to
	 * file
	 */
	private static final String ADD_ATTATCHMENT_TO_PAGE = " --action addAttachment --space \"%s\" --title \"%s\" --file \"%s\"";
	private static final String REMOVE_SPACE = " --action removeSpace --space \"%s\"";

	public static String addSpace(String spaceName) {
		return String.format(ADD_SPACE, spaceName, spaceName);
	}
	
	public static String searchForFile(String space, String page, String fileName) {
		return String.format(SEARCH_FOR_FILE_IN_SPACE, space, page, fileName);
	}

	public static String addNestedPage(String spaceName, String pageName, String paentPageName) {
		return String.format(ADD_NESTED_PAGE, spaceName, pageName, paentPageName);
	}

	public static String addAttatchmentToPage(String spaceName, String toPageName, String pathToFile) {
		return String.format(ADD_ATTATCHMENT_TO_PAGE, spaceName, toPageName, pathToFile);
	}
	
	public static String removeSpace(String spaceName) {
		return String.format(REMOVE_SPACE, spaceName);
	}
}
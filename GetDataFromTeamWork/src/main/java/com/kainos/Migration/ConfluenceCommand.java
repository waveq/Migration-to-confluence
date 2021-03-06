package com.kainos.Migration;

public class ConfluenceCommand {
	
	private static final String CHILD_PAGES_MACRO = "{pagetree:root=@self|sort=natural|excerpt=true|reverse=false|startDepth=3|expandCollapseAll=false|searchBox=false}";
	private static final String ATTACHMENTS_MACRO = "{attachments:upload=false}";
	
	private static final String ADD_SPACE = " --action addSpace --space \"%s\" --title \"%s\" ";
	private static final String ADD_NESTED_PAGE = " --action addPage --space \"%s\" --title \"%s\" --parent \"%s\" --content \"{attachments:upload=false}\"";
	private static final String ADD_NESTED_PAGE_NOTEBOOK = " --action addPage --space \"%s\" --title \"%s\" --parent \"%s\" --content \"%s %s\"";
	
	private static final String SEARCH_FOR_FILE_IN_SPACE = " --action getAttachmentList --space \"%s\" --title \"%s\" --regex \"%s\"";
	private static final String ADD_NOTEBOOK = " --action addPage --space \"%s\" --parent \"%s\" --title \"%s\" --markdown --content \"%s\" ";
	private static final String SEARCH_FOR_PAGE_IN_SPACE = " --action getPageList --space \"%s\" --regex \"%s\"";
	private static final String SEARCH_FOR_SPACE = " --action getSpaceList --regex \"%s\"";
	private static final String EDIT_NOTEBOOK = " --action modifyPage --space \"%s\" --parent \"%s\" --title \"%s\" --markdown --content2 \"%s\"";
	private static final String ALL_ATTACHMENTS_IN_PAGE = " --action getAttachmentList --space \"%s\" --title \"%s\" --regex \"\"";
	
	/*
	 * space - space
	 * title - pageName (main place of page is @home)file - path to
	 * file
	 */
	private static final String ADD_ATTATCHMENT_TO_PAGE = " --action addAttachment --space \"%s\" --title \"%s\" --file \"%s\"";
	private static final String REMOVE_SPACE = " --action removeSpace --space \"%s\"";

	public static String addNestedPageNotebook(String spaceName, String pageName, String parentPageName) {
		return String.format(ADD_NESTED_PAGE_NOTEBOOK, spaceName, pageName, parentPageName, CHILD_PAGES_MACRO, ATTACHMENTS_MACRO);
	}
	
	public static String addNestedPageFile(String spaceName, String pageName, String parentPageName) {
		return String.format(ADD_NESTED_PAGE, spaceName, pageName, parentPageName);
	}
	
	public static String addSpace(String spaceName) {
		return String.format(ADD_SPACE, spaceName, spaceName);
	}
	
	public static String searchForFile(String space, String pageName, String fileName) {
		return String.format(SEARCH_FOR_FILE_IN_SPACE, space, pageName, fileName);
	}

	public static String addAttatchmentToPage(String spaceName, String pageName, String pathToFile) {
		return String.format(ADD_ATTATCHMENT_TO_PAGE, spaceName, pageName, pathToFile);
	}
	
	public static String removeSpace(String spaceName) {
		return String.format(REMOVE_SPACE, spaceName);
	}
	
	public static String addNotebook(String spaceName,  String parentPageName, String pageName, String notebook) {
		return String.format(ADD_NOTEBOOK, spaceName, parentPageName, pageName, notebook);
	}
	
	public static String editNotebook(String spaceName,  String parentPageName, String pageName, String notebook) {
		return String.format(EDIT_NOTEBOOK, spaceName, parentPageName, pageName, notebook);
	}
	
	public static String searchForPage(String spaceName, String pageName) {
		return String.format(SEARCH_FOR_PAGE_IN_SPACE, spaceName, pageName);
	}
	
	public static String searchSpace(String spaceName) {
		return String.format(SEARCH_FOR_SPACE, spaceName);
	}
	
	public static String getAllAttachmentsFromPage(String spaceName, String pageName) {
		return String.format(ALL_ATTACHMENTS_IN_PAGE, spaceName, pageName);
	}
}
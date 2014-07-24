package com.kainos.Migration;

public class ConfluenceCommand {

	// deleted comment parameter
	private static final String AddSpace = "confluence --action addSpace --space \"%s\" --title \"%s\" ";
	private static final String AddPage = "confluence --action addPage --space \"%s\" --title \"%s\" --parent \"Home\"";
	private static final String AddNastedPage = "confluence --action addPage --space \"%s\" --title \"%s\" --parent \"%s\"";
	/*
	 * space - space
	 * title - pageName (main place of page is @home)file - path to
	 * file
	 */
	private static final String AddAttatchmentToPage = "confluence --action addAttachment --space \"%s\" --title \"%s\" --file \"%s\"";

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

// confluence --action addPage --space "zclipermissions" --title "child1-2" --parent "child1"
// confluence --action addPage --space "mojtest" --title "Test"
// confluence --action addPage --space "mojtest" --title "Test Nested Page" --parent "Test3"
// confluence --action addAttachment --space "mojtest" --title "@home" --file "README.txt"

// confluence --action addAttachment --space "zconfluencecli" --title
// "This is title 0" --file "src/itest/resources/binary.bin"
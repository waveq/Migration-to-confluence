package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class IgnoreReader {

	public IgnoreReader() {

	}

	public ArrayList<String> getIgnoredObjects(String fileName) {
//		fileName = "ignoredFiles.txt";
		File file = new File(fileName);

		BufferedReader br = null;
		ArrayList<String> ignoredFilesArray = new ArrayList<String>();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
			String line;
			while ((line = br.readLine()) != null) {
				ignoredFilesArray.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ignoredFilesArray;
	}
}

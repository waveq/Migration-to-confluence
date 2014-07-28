package com.kainos.Migration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Main {
	public static void main(String[] args) {
		Manager manager = new Manager();
		try {
			FileUtils.cleanDirectory(new File("./temp"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}

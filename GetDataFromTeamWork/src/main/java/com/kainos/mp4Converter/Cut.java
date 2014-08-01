package com.kainos.mp4Converter;

import java.io.File;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

public class Cut {

	public Cut(String fileName, String path) {
		this.fileName = fileName;
		this.path = path;
		this.pathToMain = this.path + fileName;
	}

	private String fileName;
	private String path;
	private String pathToMain = path + fileName;

	double bytes = new File(pathToMain).length();
	double kilobytes = (bytes / 1024);
	double megabytes = (kilobytes / 1024);

	IMediaWriter writer;
	private long cutMinute;
	private long minuteCounter;

	private static final int PART_LENGTH_IN_MINUTES = 5;
	private static final int MULTIPLIER_TO_MINUTE = 60 * 1000000;

	public int start() {
		cutMinute = PART_LENGTH_IN_MINUTES * MULTIPLIER_TO_MINUTE;
		minuteCounter = cutMinute;
		IMediaReader reader = ToolFactory.makeReader(pathToMain);
		CutChecker cutChecker = new CutChecker();
		reader.addListener(cutChecker);
		String newName = modifyName(fileName, 1);
		System.out.println("ROBIE TU: " + path + newName);
		writer = ToolFactory.makeWriter(path + newName, reader);
		cutChecker.addListener(writer);
		int counter = 2;
		while (reader.readPacket() == null) {
			long timePassed = cutChecker.timeInMilisec;
			if(timePassed % 100 == 0) {
				System.out.println("Progress in compressing part of file: " + (timePassed / 1000000)+ " / " + (cutMinute / 1000000));
			}
			if (cutChecker.timeInMilisec >= cutMinute) {
				cutChecker.removeListener(writer);
				writer.close();
				counter = generateNewFile(reader, writer, cutChecker, counter);
			}
		}
		return counter - 1;
	}

	public int generateNewFile(IMediaReader reader, IMediaWriter writer, CutChecker cutChecker,
			int pt) {
		String newName = modifyName(fileName, pt);
		System.out.println("ROBIE TU: " + path + newName);
		this.writer = ToolFactory.makeWriter(path + newName, reader);
		cutChecker.addListener(this.writer);

		cutMinute += minuteCounter;
		return pt + 1;
	}

	public String modifyName(String fileName, int pt) {
		String newName = fileName.substring(0, fileName.length() - 4);
		newName += "_part" + pt + ".mp4";

		return newName;
	}
}

package com.kainos.Migration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DownloadFileFromTW {
	private URL website;
	private FileOutputStream fos;

	public String DownloadFileFrom(String Url, String fileName) {

		try {
			website = new URL(Url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			System.out.println("Start download " + fileName);
			long middle = System.currentTimeMillis();
			fos = new FileOutputStream("temp/" + fileName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			System.out.println("Finish download " + fileName);
			long after = System.currentTimeMillis();
			System.out.println("Downloading time " + (double) (after - middle) / 1000 + " s");
			return fileName;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

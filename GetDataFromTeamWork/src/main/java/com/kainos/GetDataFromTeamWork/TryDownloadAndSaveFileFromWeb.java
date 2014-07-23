package com.kainos.GetDataFromTeamWork;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class TryDownloadAndSaveFileFromWeb {

	public static void main(String[] args) {
		URL website;
		FileOutputStream fos;
		try {
			website = new URL("https://traineetest.teamwork.com/?action=viewFile&sd=2257b31b-6e94-3279434B428DDB5FA5C07490ABB79A65E7A8BE4AB6300BA894416166910E7BB0146EE31E9ADDD8837296153FCB357277");
			ReadableByteChannel rbc;
			System.out.println("Open webpage");
			long start = System.currentTimeMillis();
			rbc = Channels.newChannel(website.openStream());
			System.out.println("Before download");
			long middle = System.currentTimeMillis();
			fos = new FileOutputStream("temp/jre-8u11-windows-x64.exe");
			
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			System.out.println("After download");
			long after = System.currentTimeMillis();
			
			System.out.println("Middle - start = "+(double)(middle-start)/1000);
			System.out.println("After - start = "+(double)(after-start)/1000);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

}

package com.kainos.Migration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.binary.Base64;

public abstract class JSONDownloader {

	/**
	 * Connects to URL which contains JSON and returns it.
	 * 
	 * @param passedUrl
	 *            - from which you want to download JSON
	 * @return JSONObject
	 */
	protected JSONObject downloadJSON(String passedUrl, String credentials) {
		String jsonString = "";
		JSONObject json;
		try {
			String encoding = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")),
					"UTF-8");
			URL url = new URL(passedUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			jsonString = br.readLine();
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		json = (JSONObject) JSONSerializer.toJSON(jsonString);
		return json;
	}

	//
	// protected void addNotebook(String passedUrl, String credentials) {
	// try {
	// String encoding = new
	// String(Base64.encodeBase64(credentials.getBytes("UTF-8")), "UTF-8");
	// URL url = new URL(passedUrl);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setRequestMethod("POST");
	// conn.setRequestProperty("Authorization", "Basic " + encoding);
	// conn.setRequestProperty("Accept", "application/json");
	//
	// if (conn.getResponseCode() != 200) {
	// throw new RuntimeException("Failed : HTTP error code : " +
	// conn.getResponseCode());
	// }
	//
	// BufferedReader br = new BufferedReader(new
	// InputStreamReader((conn.getInputStream())));
	//
	// jsonString = br.readLine();
	// conn.disconnect();
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	public void uploadNotebookPost(String passedUrl, String credentials, String json) throws IOException {
		String urlParameters = json.toString();

		// https://myconfluence.atlassian.net/wiki/rest/api/content/
		String encoding = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")),
				"UTF-8");
		String url=passedUrl;
		URL object=new URL(url);
		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "Basic " + encoding);
		OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
		wr.write(json);
		wr.flush();
		//display what returns the POST request
		StringBuilder sb = new StringBuilder();  
		int HttpResult =con.getResponseCode(); 
		if(HttpResult ==HttpURLConnection.HTTP_OK){
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		    sb.append(line + "\n");  
		    }  
		    br.close();  
		    System.out.println(""+sb.toString());  
		}else{
		    System.out.println(con.getResponseMessage());  
		}
	}
}
			
		
//		try {
//			String encoding = new String(Base64.encodeBase64(credentials.getBytes("UTF-8")),
//					"UTF-8");
//			URL url = new URL(passedUrl);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setDoOutput(true);
//			connection.setDoInput(true);
//			connection.setInstanceFollowRedirects(false);
//			connection.setRequestMethod("POST");
//			connection.setRequestProperty("Authorization", "Basic " + encoding);
//			connection.setRequestProperty("Content-Type", "application/json");
//			connection.setRequestProperty("Content-Length",
//					"" + Integer.toString(urlParameters.getBytes().length));
//			connection.setUseCaches(false);
//	
//			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//			wr.writeBytes(urlParameters);
//			wr.flush();
//			wr.close();
//			connection.disconnect();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

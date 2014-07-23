import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class NetClientGet {
	
	
	public static void main(String[] args) {

		try {

			String username = "shark807dry";
			String password = "X";

			String userPassword = username + ":" + password;
			// String encoding = new
			// sun.misc.BASE64Encoder().encode(userPassword
			// .getBytes());

			String encoding = "Basic "
					+ new String(Base64.encodeBase64(userPassword
							.getBytes("UTF-8")), "UTF-8");

			URL url = new URL(
					"https://traineetest.teamwork.com/projects/69383/files.json");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Basic " + encoding);
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}
}

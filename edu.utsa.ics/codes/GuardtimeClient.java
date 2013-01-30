package codes;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


/**
 * This program connects to a Web server and downloads the specified URL
 * from it.  It uses the HTTP protocol directly.
 **/
public class GuardtimeClient {
	private static String url = "http://localhost:8080/test";
	private static String filename = "/data/sample";

	public static void main(String[] args) {

//		clientHttpPostSign();
		clientHttpPostVerify();

	}


	public static void clientHttpPostSign(){
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://localhost:8080/test");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);


			//Send a string for signing
			//			nameValuePairs.add(new BasicNameValuePair("registrationid",
			//					"123456789"));
			//			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));


			//Send a file for signing

			File file = new File(System.getProperty("user.dir"), filename);
			//			System.out.println(file.exists());
			//			System.out.println(System.getProperty("user.dir"));
			FileEntity entity = new FileEntity(file, ContentType.create("text/plain", "UTF-8"));
			post.setEntity(entity);


			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clientHttpPostVerify(){
		HttpClient client = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		try {
			FileBody data = new FileBody(new File(System.getProperty("user.dir"), filename));
			FileBody gtts = new FileBody(new File(System.getProperty("user.dir"), filename));
//			StringBody comment = new StringBody("Filename: " + filename);

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("data", data);
			reqEntity.addPart("gtts", gtts);
//			reqEntity.addPart("comment", comment);
			httppost.setEntity(reqEntity);

			HttpResponse response = client.execute(httppost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			System.out.println("Timestamp received: \n");
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void clientHttpGet(){

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:8080/test");
		HttpResponse response;
		try {
			response = client.execute(request);

			// Get the response
			BufferedReader rd = new BufferedReader
					(new InputStreamReader(response.getEntity().getContent()));

			StringBuffer textView = new StringBuffer("");
			String line = "";
			while ((line = rd.readLine()) != null) {
				textView.append(line);
			}

			System.out.println("client says: " + textView.toString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

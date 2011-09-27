package android.droidnetkey;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Handler;

public class FirewallAction extends AsyncTask<>{

	private boolean state;
	private final String url = "fw.sun.ac.za";
	private final int port = 950;
	private DefaultHttpClient client;
	private String username;
	private String password;
	private String sid;
	//Debug:
	private String resp;	
    
    private Thread doFwLogin = new Thread() {
        public void run() {
            try {
                FwLogin();
            } catch (Exception e) {
            }
        }
    };

	
	public FirewallAction(Context context) {
		// TODO Auto-generated constructor stub
		state = false;
		client = new StbFwHttpsClient(context);
		
	}
	
	public boolean connect(String username, String password)
	{
		this.username = username;
		this.password = password;
		
		doFwLogin.start();
		
		return true;
	}
	
	private String getResponse(HttpPost httpClient)
	{
		HttpResponse getResponse = null;
		try {
			getResponse = client.execute((HttpUriRequest) httpClient);
			InputStream response = getResponse.getEntity().getContent();
			
			StringBuilder sb = inputStreamToString(response);
			return sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String getResponse(HttpGet httpClient)
	{
		HttpResponse getResponse = null;
		try {
			getResponse = client.execute((HttpUriRequest) httpClient);
			InputStream response = getResponse.getEntity().getContent();
			
			StringBuilder sb = inputStreamToString(response);
			return sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void FwLogin()
	{
		HttpGet get = new HttpGet("https://fw.sun.ac.za:950");
		HttpPost post = new HttpPost("https://fw.sun.ac.za:950");
		// Execute the GET call and obtain the response
		
		try {
			resp = getResponse(get);
			
			Pattern pattern = Pattern.compile("<input type=\"hidden\" name=\"ID\" value=\"([^\"\\r\\n]*)\">");
			Matcher matcher = pattern.matcher(resp);
			
			if(matcher.find())
			{
				this.sid = matcher.group(1);
			} else
			{
				throw new Exception();
			}
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	        nameValuePairs.add(new BasicNameValuePair("ID", this.sid));
	        nameValuePairs.add(new BasicNameValuePair("STATE", "1"));
	        nameValuePairs.add(new BasicNameValuePair("DATA", this.username));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        resp = getResponse(post);
	        
	        nameValuePairs.clear();
	        
	        nameValuePairs.add(new BasicNameValuePair("ID", this.sid));
	        nameValuePairs.add(new BasicNameValuePair("STATE", "2"));
	        nameValuePairs.add(new BasicNameValuePair("DATA", this.password));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        resp = getResponse(post);
	        
	        pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">Access denied");
			matcher = pattern.matcher(resp);
			
			if(matcher.find())
			{
				throw new Exception();
			} 
			
			nameValuePairs.clear();
			
	        nameValuePairs.add(new BasicNameValuePair("ID", this.sid));
	        nameValuePairs.add(new BasicNameValuePair("STATE", "3"));
	        nameValuePairs.add(new BasicNameValuePair("DATA", "1"));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        resp = getResponse(post);
	        
	        pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">User authorized");
			matcher = pattern.matcher(resp);
			
			if(matcher.find())
			{
				this.state = true;
			} 
	        
			FwCallback();
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private StringBuilder inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    try {
			while ((line = rd.readLine()) != null) { 
			    total.append(line); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    // Return full string
	    return total;
	}

		public String getResp()
		{
			return resp;
		}

}

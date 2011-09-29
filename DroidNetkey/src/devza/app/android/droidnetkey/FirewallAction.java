package devza.app.android.droidnetkey;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class FirewallAction extends AsyncTask<String, Void, Integer>{
	
	private final String url = "https://fw.sun.ac.za:950";
	private DefaultHttpClient client;
	
	private HttpGet get;
	private HttpPost post;
	
	private String sid;
	
	private ProgressDialog d;
	private boolean fwstatus;
	
	private final int TIMEOUT = 15000;
	
	private final int SUCCESS = 0;
	private final int INVALID_CREDENTIALS = -1;
	private final int TIMED_OUT = -2;
	private final int GENERAL_ERROR = -3;
	
	private Context context;
	
	//Debug:
	private String resp;	
	
	public FirewallAction(Context context, boolean status)
	{
		this.context = context;
		this.fwstatus = status;
		
		client = new StbFwHttpsClient(this.context);
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);

		client.setParams(params);
	}
	
	@Override
	protected void onPreExecute()
	{
		
		String st;
    	//String action;
    	
    	if(!fwstatus)
    	{
    		st = "Connecting...";
    		//action = "login";
    	}
    	else
    	{
    		st = "Disconnecting...";
    		//action = "logout";
    	}
    	d = new ProgressDialog(this.context);
    	d.setCancelable(false);
    	d.setIndeterminate(true);
    	d.setMessage(st);
    	
    	try {
			d.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		String username = arg0[0];
		String password = arg0[1];
		String action = arg0[2];
		//boolean status = false;

		get = new HttpGet(url);
		post = new HttpPost(url);
		// Execute the GET call and obtain the response
		
		int authstatus = authenticate(username, password);
		
		if(authstatus == SUCCESS)
		{
			if(action == "login")
			{
				return(openFirewall());
			} else
			{
				return(closeFirewall());
			}
			//status = true;
		}
		
		return authstatus;
	}
	
	@Override
	protected void onPostExecute(Integer result)
	{
		/*INVALID_CREDENTIALS = -1;
		TIMED_OUT = -2;
		GENERAL_ERROR = -3;*/
		
		d.dismiss();
		
		if(result < 0)
		{
			AlertDialog error = new AlertDialog.Builder(this.context).create();
			error.setTitle("Error");
			
			String msg;
			
			switch(result)
			{
				case INVALID_CREDENTIALS: 	msg = "Invallid Username or Password"; 	break;
				case TIMED_OUT:				msg = "Operation Timed Out";			break;
				case GENERAL_ERROR:			msg = "General Error";					break;
				default: 					msg = "General Error";					break;
			}
			error.setMessage(msg);
			error.show();
		}
		
	}
	
	private int authenticate(String username, String password)
	{
		//TODO add timeout exception catcher
		try {
			resp = getResponse(get);
			
			Pattern pattern = Pattern.compile("<input type=\"hidden\" name=\"ID\" value=\"([^\"\\r\\n]*)\">");
			Matcher matcher = pattern.matcher(resp);
			
			if(matcher.find())
			{
				sid = matcher.group(1);
			} else
			{
				throw new Exception();
			}
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	        nameValuePairs.add(new BasicNameValuePair("ID", sid));
	        nameValuePairs.add(new BasicNameValuePair("STATE", "1"));
	        nameValuePairs.add(new BasicNameValuePair("DATA", username));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        resp = getResponse(post);
	        
	        nameValuePairs.clear();
	        
	        nameValuePairs.add(new BasicNameValuePair("ID", sid));
	        nameValuePairs.add(new BasicNameValuePair("STATE", "2"));
	        nameValuePairs.add(new BasicNameValuePair("DATA", password));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        resp = getResponse(post);

			nameValuePairs.clear();
	        
	        pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">User "+username+" authenticated");
			matcher = pattern.matcher(resp);
			
			if(matcher.find())
			{
				return SUCCESS;
			}
			
	        
	        pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">Access denied");
			matcher = pattern.matcher(resp);
						
			if(matcher.find())
			{
				return INVALID_CREDENTIALS;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GENERAL_ERROR;
	}
	
	private Integer openFirewall()
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("ID", sid));
        nameValuePairs.add(new BasicNameValuePair("STATE", "3"));
        nameValuePairs.add(new BasicNameValuePair("DATA", "1"));
        try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			resp = getResponse(post);
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			return TIMED_OUT;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			return GENERAL_ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return GENERAL_ERROR;
		}
        
        Pattern pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">User authorized");
		Matcher matcher = pattern.matcher(resp);
		
		if(matcher.find())
		{
			return SUCCESS;
		} 
		
		return INVALID_CREDENTIALS;
	}
	
	private Integer closeFirewall()
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("ID", sid));
        nameValuePairs.add(new BasicNameValuePair("STATE", "3"));
        nameValuePairs.add(new BasicNameValuePair("DATA", "2"));
        try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			resp = getResponse(post);
        } catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			return TIMED_OUT;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			return GENERAL_ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return GENERAL_ERROR;
		}
        
        Pattern pattern = Pattern.compile("<font face=\"verdana\" size=\"3\">User was signed off");
		Matcher matcher = pattern.matcher(resp);
		
		if(matcher.find())
		{
			return SUCCESS;
		} 
		
		return INVALID_CREDENTIALS;
	}
	
	private String getResponse(HttpPost httpClient) throws ClientProtocolException, IOException, SocketTimeoutException
	{
		HttpResponse getResponse = null;
		
			getResponse = client.execute((HttpUriRequest) httpClient);
			InputStream response = getResponse.getEntity().getContent();
			
			StringBuilder sb = inputStreamToString(response);
			return sb.toString();
		
	}
	
	private String getResponse(HttpGet httpClient) throws ClientProtocolException, IOException, SocketTimeoutException
	{
		HttpResponse getResponse = null;
		
			getResponse = client.execute((HttpUriRequest) httpClient);
			InputStream response = getResponse.getEntity().getContent();
			
			StringBuilder sb = inputStreamToString(response);
			return sb.toString();
		
		
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

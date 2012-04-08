/*
    Droidnetkey - An Inetkey implementation for Android
    Copyright (C) 2012  Gerrit N. Maritz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package devza.app.android.droidnetkey;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;

public class FirewallAction extends AsyncTask<String, Void, Integer>{
	
	private final String url = "https://fw.sun.ac.za:950";
	private DefaultHttpClient client;
	
	private HttpGet get;
	private HttpPost post;
	
	private String sid;
	
	private ProgressDialog d;
	private boolean fwstatus;
	protected ConnectionService s;
	private boolean refresh;
	
	private final int TIMEOUT = 30000;
	
	private final int DISCONNECTED = 2;
	private final int CONNECTED = 1;
	private final int SUCCESS = 0;
	private final int GENERAL_ERROR = -1;
	private final int INVALID_CREDENTIALS = -2;
	private final int TIMED_OUT = -3;
	private final int UNKNOWN_HOST = -4;
	
	private Context context;
	
	//Debug:
	private String resp;	
	
	public FirewallAction(Context context, boolean status, boolean refresh)
	{
		this.context = context;
		this.fwstatus = status;
		this.refresh = refresh;
		
		client = new StbFwHttpsClient(this.context);
		
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);

		client.setParams(params);
		
		if(!refresh)
		this.context.bindService(new Intent(this.context, ConnectionService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	protected ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			s = ((ConnectionService.MyBinder) binder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			s = null;
		}
	};
	
	@Override
	protected void onPreExecute()
	{
		if(!refresh)
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
	}
	
	@Override
	protected Integer doInBackground(String... arg0) 
	{
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
		if(!refresh)
		{
			d.dismiss();
			
			if(result < 0)
			{
				AlertDialog error = new AlertDialog.Builder(this.context).create();
				error.setTitle("Error");
				error.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int id)
					{
						d.cancel();
					}
				});
				
				String msg;
				
				switch(result)
				{
					case INVALID_CREDENTIALS: 	msg = "Invalid Username or Password"; 	break;
					case TIMED_OUT:				msg = "Connection Timed Out. Is your device connected to the internet?";			break;
					case UNKNOWN_HOST:			msg = "Could not connect. Is your device connected to the internet?";			break;
					case GENERAL_ERROR:			msg = "General Error.\n Please send some more information about this error to 15629368@sun.ac.za";					break;
					default: 					msg = "General Error.\n Please send some more information about this error to 15629368@sun.ac.za";					break;
				}
				error.setMessage(msg);
				error.show();
			}
			else
			{
				if(result == CONNECTED)
				{
					Intent usage = new Intent(this.context, UsageActivity.class);
					this.context.startActivity(usage);
					this.s.showNotification(true);
					this.s.startTimer();
				} else {
					Intent main = new Intent(this.context, MainActivity.class);
					this.context.startActivity(main);
					this.s.showNotification(false);
				}
			}
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
			
		} catch (ConnectTimeoutException e)
		{
			return TIMED_OUT;
		}catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}catch (SocketTimeoutException e) { 
			return TIMED_OUT;
		}catch(SocketException e) {
			return UNKNOWN_HOST;
		}catch (Exception e)
		{
			return GENERAL_ERROR;
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
			return CONNECTED;
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
			return DISCONNECTED;
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

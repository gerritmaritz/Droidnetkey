package devza.app.android.droidnetkey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class IWS {
	
	public static final int API_OPEN   = 1;
	public static final int API_CLOSE  = 2;
	public static final int API_UPDATE = 3;
	
	private XMLRPCCallback listener;
	private XMLRPCClient client;
	
	private long open_id;
	private long close_id;
	private long refresh_id;
	
	public IWS(String version)
	{
		 try {
			this.client = new XMLRPCClient(new URL("https://maties2.sun.ac.za:443/RTAD4-RPC3"),"Inetkey/Android "+version);
		} catch (MalformedURLException e) {}
		 
		 XMLRPCCallback listener = new XMLRPCCallback() {
				public void onResponse(long id, Object result) {
			        // Handling the servers response
					response(id, result);
			    }
			    public void onError(long id, XMLRPCException error) {
			        // Handling any error in the library
			    }
			    public void onServerError(long id, XMLRPCServerException error) {
			        // Handling an error response from the server
			    }
		};
		
	}
	
	public void response(long id, Object result)
	{
		if(id == this.open_id)
		{

		}
	}

	public void open(String requser, String reqpwd)
	{
		    Map<Object, Object> mp = new HashMap<Object, Object>();
		    mp.put("requser", TextUtils.htmlEncode(requser));
		    mp.put("reqpwd", TextUtils.htmlEncode(reqpwd));
		    mp.put("platform", "any");
		    
			this.open_id = this.client.callAsync(listener,"rtad4inetkey_api_open", mp);
			
			Log.d("IWS", "Open Executed");

	}
	
	public void close(String requser, String reqpwd)
	{
		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\">"
						+"<methodCall>"
						+"<methodName>rtad4inetkey_api_open</methodName>"
						+"<params>"
						+"<param>"
						+"<value>"
						+"<struct>"
						+"<member>"
						+"<name>requser</name>"
						+"<value><string>"+requser+"</string></value>"
						+"</member>"
						+"<member>"
						+"<name>reqpwd</name>"
						+"<value><string>"+reqpwd+"</string></value>"
						+"</member>"
						+"<member>"
						+"<name>platform</name>"
						+"<value><string>any</string></value>"
						+"</member>"
						+"</struct>"
						+"</value>"
						+"</param>"
						+"</params>"
						+"</methodCall>";
	}
	
	public void parseResp(String resp)
	{
		
	}
	
	public void refresh()
	{
		
	}
}

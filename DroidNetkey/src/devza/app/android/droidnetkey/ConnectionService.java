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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class ConnectionService extends Service {

	private Notification inetNotification;
	final int NOT_ID = 1;
	
	private String username;
	private String password;

	private Timer updateTimer;
	
	private Boolean state;
	private Map<String, Object> api_stat;
	
	private XMLRPCCallback listener;
	private XMLRPCClient client;
	
	private long open_id;
	private long close_id;
	private long refresh_id;
	
	private String versionCode = "";
	
	private InetCallback outCall;
	
	private URL API_URL;
	
	public Map<String, Object> getApi_stat() {
		return api_stat;
	}

	public Boolean getState() {
		return state;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		boolean isDebuggable = false;
		
		if(isDebuggable)
		{
			try {
				API_URL = new URL("https://rtaddev.sun.ac.za/RTAD4-RPC3");
			} catch (MalformedURLException e) {}
			
		}else
		{
			try {
				API_URL = new URL("https://maties2.sun.ac.za/RTAD4-RPC3");
			} catch (MalformedURLException e) {}
		}

		try {
			versionCode = String.valueOf(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0 ).versionCode);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.client = new XMLRPCClient(API_URL,"Inetkey/Android "+versionCode);
		 
		 this.listener = new XMLRPCCallback() {
				public void onResponse(long id, Object result) {
			        // Handling the servers response
					
					api_stat = (Map<String, Object>)result;

					if(id == open_id || id == close_id)
					{
						outCall.statusCallback(((Number)api_stat.get("resultcode")).intValue(), (String)api_stat.get("resultmsg"));
					}
			    }
			    public void onError(long id, XMLRPCException error) {
			    	outCall.statusCallback(-1, error.getMessage());
			    }
			    public void onServerError(long id, XMLRPCServerException error) {
			    	outCall.statusCallback(-1, error.getMessage());
			    }
		};
		Log.d("IWS", "Service Created");
	}
	
	@Override
	public void onDestroy(){
		Log.d("IWS", "Service Destroyed");
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<ConnectionService>(this);
    }
	
	public void foreground(boolean fwStatus)
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText;
		CharSequence contentText;
		long when = System.currentTimeMillis();
		Intent notificationIntent;
		
		if(fwStatus)
		{
			tickerText = "Inetkey Connected";
			contentText = "Connected";
			notificationIntent = new Intent(this, UsageActivity.class);
		}
		else
		{
			stopForeground(true);
			return;
		}

		inetNotification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Inetkey";
		
		
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		inetNotification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		startForeground(NOT_ID, inetNotification);

		//inetNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		
		//mNotificationManager.notify(NOT_ID, inetNotification);
	}
	
	public void fwConnect(InetCallback callback)
	{
		outCall = callback;

		Map<Object, Object> mp = new HashMap<Object, Object>();
		mp.put("requser", TextUtils.htmlEncode(username));
		mp.put("reqpwd", TextUtils.htmlEncode(password));
		//Log.d("DNK", TextUtils.htmlEncode(password));
		mp.put("platform", "any");
		    
		this.open_id = this.client.callAsync(listener,"rtad4inetkey_api_open", mp);
			
		Log.d("IWS", "Open Executed");
	}
	
	public void fwDisconnect(InetCallback callback)
	{
    	stopUpdateTimer();
    	
    	outCall = callback;
		
		Map<Object, Object> mp = new HashMap<Object, Object>();
	    mp.put("requser", TextUtils.htmlEncode(username));
	    mp.put("reqpwd", "");
	    
		this.close_id = this.client.callAsync(listener,"rtad4inetkey_api_close", mp);
		
		Log.d("IWS", "Close Executed");
		
		foreground(false);
	}
	
	private void fwUpdate()
    {
		Map<Object, Object> mp = new HashMap<Object, Object>();
	    mp.put("requser", TextUtils.htmlEncode(username));
	    mp.put("reqpwd", "");
	    mp.put("platform", "any");
	    
		this.refresh_id = this.client.callAsync(listener,"rtad4inetkey_api_renew", mp);
		
		Log.d("IWS", "Update Executed");
		
    }
	
	public void stop()
	{
		this.stopSelf();
		Log.d("IWS", "Service: Service Stopped");
	}
	
	public void startUpdateTimer()
	{		
		updateTimer = new Timer("Refresh");
		updateTimer.schedule(new TimerTask(){
			public void run(){
					fwUpdate();
			}
		}
		, 10*60*1000, 10*60*1000); //10min as per API spec
		Log.d("IWS", "Service: FW Timer Started");

	}
	
	public void stopUpdateTimer()
	{
		updateTimer.cancel();
	}

}

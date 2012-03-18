package devza.app.android.droidnetkey;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ConnectionService extends Service {

	private final IBinder mBinder = new MyBinder();
	private Notification inetNotification;
	final int NOT_ID = 1;
	private String username;
	private String password;
	
	private FirewallAction fw;
	private Timer updateTimer;
	

	public void update()
    {
		String[] fwparams = {username, password, "login"};
    	
    	fw = new FirewallAction(this, false, true);
    	fw.execute(fwparams);
    	Log.d("DNK", "Service: FW Timer Executed");
    }
	
	@Override
	public IBinder onBind(Intent arg0) {	
		return mBinder;
	}
	
	public void showNotification(boolean fwStatus)
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
	
	public void fwConnect(Context context)
	{
		username = MainActivity.getUsername();
		password = MainActivity.getPassword();
		
		String[] fwparams = {username, password, "login"};
    	
    	fw = new FirewallAction(context, false, false);
    	fw.execute(fwparams);
	}
	
	public void fwDisconnect(Context context)
	{
		String[] fwparams = {username, password, "logout"};
    	
    	fw = new FirewallAction(context, true, false);
    	fw.execute(fwparams);
    	updateTimer.cancel();
    	stop();
	}
	
	public void stop()
	{
		this.stopSelf();
		Log.d("DNK", "Service: Service Stopped");
	}
	
	public void startTimer()
	{
		updateTimer = new Timer("Refresh");
		updateTimer.schedule(new TimerTask(){
			public void run(){
					update();
			}
		}
		, 10*60*1000, 10*60*1000); //10min as per pynetkey
		Log.d("DNK", "Service: FW Timer Started");

	}
	
	public class MyBinder extends Binder {
		ConnectionService getService() {
			return ConnectionService.this;
		}
	}

}

package devza.app.android.droidnetkey;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ConnectionService extends Service {

	private final IBinder mBinder = new MyBinder();
	private Notification inetNotification;
	private String username;
	private String password;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public void showNotification()
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.icon;
		CharSequence tickerText = "Inetkey Connected";
		long when = System.currentTimeMillis();

		inetNotification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Inetkey";
		CharSequence contentText = "Connected";
		
		Intent notificationIntent = new Intent(this, UsageActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		inetNotification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		inetNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		final int HELLO_ID = 1;

		mNotificationManager.notify(HELLO_ID, inetNotification);
	}
	
	public void fwConnect()
	{
		
	}
	
	public class MyBinder extends Binder {
		ConnectionService getService() {
			return ConnectionService.this;
		}
	}

}

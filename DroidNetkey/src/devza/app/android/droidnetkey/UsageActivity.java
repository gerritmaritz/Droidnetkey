/*
    Droidnetkey - An Inetkey implementation for Android
    Copyright (C) 2011  Gerrit N. Maritz

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

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UsageActivity extends Activity{
	
	private NotificationManager mNotificationManager;
	
	private FirewallAction fw;
	
	private Timer updateTimer;
	//private static final int HELLO_ID = 1;
	 /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);

        updateTimer = new Timer("Test");
        update();
        updateTimer.schedule(new TimerTask(){
        	public void run(){
        		update();
        	}
        }
        , 60000); //300000ms = 5min
        
        
        /*String ns = this.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        
        int icon = R.drawable.ic_menu_refresh;
        CharSequence tickerText = "Inetkey Connected";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        
        Context context = getApplicationContext();
        CharSequence contentTitle = "Inetkey";
        CharSequence contentText = "Connected";
        Intent notificationIntent = new Intent(this, UsageActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        
        notification.defaults |= Notification.FLAG_ONGOING_EVENT;

        try {
			mNotificationManager.notify(HELLO_ID, notification);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}*/
    }
    
    private void update()
    {
    	UsageAction update = new UsageAction(this, (TextView)findViewById(R.id.textView4), (TextView)findViewById(R.id.textView2));
    	String[] args = {DroidNetkeyActivity.getUsername(), DroidNetkeyActivity.getPassword()};
		update.execute(args);
    }
    public void disconnectFirewall(View view)
    {
    	String action = "logout";
    	
    	String[] fwparams = {DroidNetkeyActivity.getUsername(), DroidNetkeyActivity.getPassword(), action};
    	
    	fw = new FirewallAction(this, true);
    	fw.execute(fwparams);
    }
    
    @Override
    public void onBackPressed() {

    	moveTaskToBack (true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	AlertDialog popup = new AlertDialog.Builder(this).create();
    	String msg = "";
        switch (item.getItemId()) {
            case R.id.help:     popup.setTitle("Help");
                                msg = "Inetkey for Android allows you to open the firewall of Stellenbosch University from you mobile device. Simply enter you SU username and password and click Connect. Passwords are encrypted on the device to ensure security.";
                                break;
            case R.id.about:    popup.setTitle("About");
            					msg = "Inetkey for Android Copyright (C) 2011 Gerrit N. Maritz. This program comes with ABSOLUTELY NO WARRANTY. This program is released under the GPLv3 licence. Icon: David Vignoni. Reference Program: Pynetkey, Copyright 2009 Janto Dreijer";
            					break;
            case R.id.feedback: popup.setTitle("Feedback");
            					msg = "Please help me improve this software by sending feedback! Send me an email at 15629368@sun.ac.za";
                                break;
        }
        popup.setMessage(msg);
        popup.show();
        return true;
    }
}

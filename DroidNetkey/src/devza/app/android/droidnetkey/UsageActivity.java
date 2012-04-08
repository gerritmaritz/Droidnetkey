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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;*/
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class UsageActivity extends DroidnetkeyActivity{
	
	private Timer updateTimer;
	private ConnectionService cService = null;
	
	
	private ProgressDialog d;
	
	//private static final int HELLO_ID = 1;
	 /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);

        Intent cs = new Intent(this, ConnectionService.class);
        
        if(!isConServiceRunning())
        {
        	startService(cs);
        }
        
        if(cService == null)
        {
        	bindService(cs, s, Context.BIND_AUTO_CREATE);
        }
        //updateTimer = new Timer("Update Data");
        //update();
        startTimer();
        
    }
    
    private ServiceConnection s = new ServiceConnection() {
        @SuppressWarnings("unchecked")
		public void onServiceConnected(ComponentName className, IBinder service) {
              cService = ((LocalBinder<ConnectionService>) service).getService();
     
          }

          public void onServiceDisconnected(ComponentName className) {
              // As our service is in the same process, this should never be called
          }
     }; 
     
     private InetCallback listener = new InetCallback() {
		
		@Override
		public void statusCallback(int code, String message) {
			// TODO Auto-generated method stub
			Log.d("DNK", "Code: "+code+" : "+message);
			d.dismiss();
			
			switch(code)
			{
				case 0:
					startActivity(new Intent(UsageActivity.this, MainActivity.class));
					break;
				default:
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(UsageActivity.this, "An error has occured, please try again.", Toast.LENGTH_SHORT).show();
						}
					});
					break;
			}
			
		}
	};
    
    private void update()
    {
    	
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, Object> api_stat = cService.getApi_stat();
				
				DecimalFormat df = new DecimalFormat("0.00");
				
				//Log.d("test", df.format(((Number)api_stat.get("monthusage")).doubleValue()));
				
				((TextView)findViewById(R.id.textMonthUsage)).setText("R "+df.format(((Number)api_stat.get("monthusage")).doubleValue()));
		    	((TextView)findViewById(R.id.textYearUsage)).setText("R "+df.format(((Number)api_stat.get("yearusage")).doubleValue()));
		    	
		    	int inetstate = ((Number)api_stat.get("inetstate")).intValue();
		    	String inetstatemsg = "";
		    	switch(inetstate)
		    	{
		    		case 0:
		    			inetstatemsg = "Internet OK";
		    			((TextView)findViewById(R.id.textInetState)).setTextColor(0xFF669900);
		    			break;
		    		case 1:
		    			inetstatemsg = "No International Connection";
		    			((TextView)findViewById(R.id.textInetState)).setTextColor(0xFFCC0000);
		    			break;
		    		case 2:
		    			inetstatemsg = "No Internet Connection";
		    			((TextView)findViewById(R.id.textInetState)).setTextColor(0xFFCC0000);
		    			break;
		    	}
		    	
		    	((TextView)findViewById(R.id.textInetState)).setText(inetstatemsg);
		    	((TextView)findViewById(R.id.textInetMessage)).setText((String)api_stat.get("inetmsg"));
			}
		});
    	
    	
    	//UsageAction update = new UsageAction(this, (TextView)findViewById(R.id.textView4), (TextView)findViewById(R.id.textView2));
    	//String[] args = {MainActivity.getUsername(), MainActivity.getPassword()};
    	
    	Log.d("DNK", "Updated");
    }
    
    public void startTimer()
	{
		updateTimer = new Timer("Update Data");
		updateTimer.schedule(new TimerTask(){
			public void run(){
					update();
			}
		}
		, 0, 1*60*1000);

	}
    
    public void disconnectFirewall(View view)
    {
    	d = new ProgressDialog(this);
    	d.setCancelable(true);
    	d.setIndeterminate(true);
    	d.setMessage("Disconnecting...");
    	
    	try {
			d.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	updateTimer.cancel();
    	cService.fwDisconnect(listener);
    }
    
    @Override
    public void onBackPressed() {
    	//unbindService(mConnection);
    	moveTaskToBack (true);
    } 
    
    @Override
    public void onDestroy()
    {
    	updateTimer.cancel();
    }
}

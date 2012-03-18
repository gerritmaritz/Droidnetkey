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

/*import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;*/
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UsageActivity extends DroidnetkeyActivity{
	
	private Timer updateTimer;
	
	//private static final int HELLO_ID = 1;
	 /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage);

        updateTimer = new Timer("Update Data");
        update();
        startTimer();
        
        bindService(new Intent(this, ConnectionService.class), mConnection,
				Context.BIND_AUTO_CREATE);
        
    }
    
    private void update()
    {
    	Log.d("DNK", "Updated");
    	
    	UsageAction update = new UsageAction(this, (TextView)findViewById(R.id.textView4), (TextView)findViewById(R.id.textView2));
    	String[] args = {MainActivity.getUsername(), MainActivity.getPassword()};
		update.execute(args);
    }
    
    public void startTimer()
	{
		updateTimer = new Timer("Update Data");
		updateTimer.schedule(new TimerTask(){
			public void run(){
					update();
			}
		}
		, 5*60*1000, 5*60*1000); //6min as per pynetkey

	}
    
    public void disconnectFirewall(View view)
    {
    	
    	updateTimer.cancel();
    	s.fwDisconnect(this);
    	unbindService(mConnection);
    }
    
    @Override
    public void onBackPressed() {
    	unbindService(mConnection);
    	moveTaskToBack (true);
    }  
}

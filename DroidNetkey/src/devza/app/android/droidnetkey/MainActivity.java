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

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends DroidnetkeyActivity {

	CheckBox checkBox_savepass;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEdit;
	private static String username;
	private static String password;

	private EditText editUsername;
	private EditText editPassword;
	
	private ConnectionService cService = null;
	private InetCallback listener;
	
	private ProgressDialog d;
	
	private final int INVALID_CREDENTIALS = 10000;
	private final int NO_HOST = -3;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Intent cs = new Intent(this, ConnectionService.class);
        
        if(!isConServiceRunning())
        {
        	startService(cs);
        }
        
        if(cService == null)
        {
        	bindService(cs, s, Context.BIND_AUTO_CREATE);
        }
        
        prefs = getPreferences(MODE_PRIVATE);
        prefEdit = prefs.edit();
        
        checkBox_savepass = (CheckBox) findViewById(R.id.checkBox1);
        editUsername = (EditText) findViewById(R.id.editText1);
        editPassword = (EditText) findViewById(R.id.editText2);
        
        loadSettings();
        
        listener = new InetCallback() {
			
			@Override
			public void statusCallback(final int code, final String message) {
				// TODO Auto-generated method stub
				d.dismiss();
				
				//if(code == 0)
				//{
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (code == 0)
						{
							cService.foreground(true);
							cService.startUpdateTimer();
							startActivity(new Intent(MainActivity.this, UsageActivity.class));
						}else
						{
							AlertDialog error = new AlertDialog.Builder(MainActivity.this).create();
							error.setTitle("Error");
							error.setButton("Ok", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface d, int id)
								{
									d.cancel();
								}
							});
							
							String msg;
							
							switch(code)
							{
								case INVALID_CREDENTIALS: 	msg = "Invalid Username or Password"+message; break;
								default: 					msg = "Connection Error.\n"+message; break;
							}
							
							error.setMessage(msg);
							error.show();
						}
						
						
					}
				});
				
				
					
				//}
			}
		};
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
     
     
    
    public void openFirewall(View view)
    {
    	username = editUsername.getText().toString();	
    	password = editPassword.getText().toString();
    	
    	if(username.length() == 0 || password.length() == 0)
    	{
    		Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
    	} else
    	{
	    	storeSettings();
    	}
    	
    	cService.setUsername(username);
    	cService.setPassword(password);
    	
    	d = new ProgressDialog(this);
    	d.setCancelable(false);
    	d.setIndeterminate(true);
    	d.setMessage("Loging in...");
    	
    	try {
			d.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	cService.fwConnect(listener);
    }
    
    public void showPassword(View view)
    {
    	
    }
    
    public void storeSettings()
    {
    	prefEdit.putString("username", username);
    	    	
    	if(checkBox_savepass.isChecked())
    	{
    		try {
				prefEdit.putString("password", SimpleCrypto.encrypt(Settings.Secure.ANDROID_ID, password) );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else
    	{
    		prefEdit.putString("password", "");
    	}
    	
    	prefEdit.commit();
    }
    
    public void loadSettings()
    {
    	username = prefs.getString("username", "");
    	editUsername.setText(username);
    	
    	String pass = prefs.getString("password", "");
    	
    	String passplain = "";
    	
    	if(pass != "")
    	{
    		try {
				passplain = SimpleCrypto.decrypt(Settings.Secure.ANDROID_ID, pass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		checkBox_savepass.setChecked(true);
    	}
    	password = passplain;
    	//TODO: "show password"
    	editPassword.setText(passplain);
    	
    	boolean firstLaunch = prefs.getBoolean("firstLaunch", true);
    	
    	if(firstLaunch)
    	{
    		AlertDialog notice = new AlertDialog.Builder(this).create();
    		notice.setTitle("Important");
    		notice.setButton("Ok", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				dialog.dismiss();
    				prefEdit.putBoolean("firstLaunch", false);
    				prefEdit.commit();
    			}
    		});
    		notice.setMessage("This software is not provided or endorsed by the University of Stellenbosch. To use Maties WiFi please register your device at Student IT first.");
    		notice.show();
    	}
    }

    @Override
    public void onBackPressed() {
    	
    	moveTaskToBack (true);
    }
}
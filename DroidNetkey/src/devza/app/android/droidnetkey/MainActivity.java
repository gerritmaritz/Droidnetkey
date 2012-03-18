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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends DroidnetkeyActivity {

	public boolean fwstatus = false;
	
	CheckBox checkBox;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEdit;
	private static String username;
	private static String password;
	
	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

	private EditText editUsername;
	private EditText editPassword;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        prefs = getPreferences(MODE_PRIVATE);
        prefEdit = prefs.edit();
        
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        editUsername = (EditText) findViewById(R.id.editText1);
        editPassword = (EditText) findViewById(R.id.editText2);
        
       bindService(new Intent(this, ConnectionService.class), mConnection,
				Context.BIND_AUTO_CREATE);
        
        loadSettings();
    }
    
    public void toggleFirewall(View view)
    {
    	username = editUsername.getText().toString();
    	
    	password = editPassword.getText().toString();
    	
    	if(username.length() == 0 || password.length() == 0)
    	{
    		Toast.makeText(this, "Please enter a valid username and password", Toast.LENGTH_SHORT).show();
    	} else
    	{
	    	storeSettings();
	    	
	    	s.fwConnect(this);
	    	
	    	if(mConnection != null)
	    	{
	    	//	unbindService(mConnection);
	    	}
    	}
    }
    
    public void showPassword(View view)
    {
    	
    }
    
    public void storeSettings()
    {
    	prefEdit.putString("username", username);
    	    	
    	if(checkBox.isChecked())
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
    		checkBox.setChecked(true);
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
    	s.stop();
    	moveTaskToBack (true);
    }
}
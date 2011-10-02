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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class DroidNetkeyActivity extends Activity {
    /** Called when the activity is first created. */
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
	
	private FirewallAction fw;
	
	private Handler timeoutHandler;
	public static Handler fwHandler;
	
	private Runnable timeout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        prefs = getPreferences(MODE_PRIVATE);
        prefEdit = prefs.edit();
        
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        editUsername = (EditText) findViewById(R.id.editText1);
        editPassword = (EditText) findViewById(R.id.editText2);
        
        timeoutHandler = new Handler(); 
        fwHandler = new Handler();
        
        loadSettings();
    }
    
    public void fwDialogTimeout(long time, final ProgressDialog d){
        
        timeoutHandler.postDelayed(timeout, time); 
    }
    
    public void toggleFirewall(View view)
    {
    	
    	storeSettings();
    	
    	String st;
    	String action;
    	
    	if(!fwstatus)
    	{
    		st = "Connecting...";
    		action = "login";
    	}
    	else
    	{
    		st = "Disconnecting...";
    		action = "logout";
    	}
    	
    	String[] fwparams = {username, password, action};
    	
    	fw = new FirewallAction(this, fwstatus);
    	fw.execute(fwparams);
    }
    
    public void storeSettings()
    {
    	this.username = editUsername.getText().toString();
    	prefEdit.putString("username", username);
    	
    	this.password = editPassword.getText().toString();
    	
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
    	editPassword.setText(passplain);
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
    
    
   // public static void set
    
}
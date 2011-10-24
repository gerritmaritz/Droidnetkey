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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends DroidnetkeyActivity {
    /** Called when the activity is first created. */
	private final int REFRESH_RATE = 300000;
	
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
	
	private Timer updateTimer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        prefs = getPreferences(MODE_PRIVATE);
        prefEdit = prefs.edit();
        
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        editUsername = (EditText) findViewById(R.id.editText1);
        editPassword = (EditText) findViewById(R.id.editText2);
        
       /* updateTimer = new Timer("Refresh");
        update();
        updateTimer.schedule(new TimerTask(){
        	public void run(){
        		update();
        	}
        }
        , 60000); //300000ms = 5min
*/        
        
        loadSettings();
    }
    
    /*public void update()
    {
    	
    }*/
    
    public void toggleFirewall(View view)
    {
    	
    	storeSettings();
    	
    	String action;
    	
    	if(!fwstatus)
    	{
    		action = "login";
    	}
    	else
    	{
    		action = "logout";
    	}
    	
    	String[] fwparams = {username, password, action};
    	
    	fw = new FirewallAction(this, fwstatus);
    	fw.execute(fwparams);
    }
    
    public void showPassword(View view)
    {
    	
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
    	//TODO: "show password"
    	editPassword.setText(passplain);
    }

    @Override
    public void onBackPressed() {

    	moveTaskToBack (true);
    }
}
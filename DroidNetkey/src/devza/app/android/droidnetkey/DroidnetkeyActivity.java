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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class DroidnetkeyActivity extends Activity {
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
		
	//TODO: Move these to @strings
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	AlertDialog popup = new AlertDialog.Builder(this).create();
    	String msg = "";
        switch (item.getItemId()) {
            case R.id.help:     popup.setTitle("Help");
                                msg = "Inetkey for Android allows you to open the firewall of Stellenbosch University from your mobile device to gain internet access. Simply enter your SU username and password and connect. Passwords are encrypted on the device to ensure security. This application will only work if connected to a SU WiFi network. Remember that to connect to a SU WiFi network, your device needs to be registered.";
                                break;
            case R.id.about:    popup.setTitle("About");
            					msg = "Inetkey for Android \nCopyright \251 2012 \nGerrit N. Maritz \n\nThis program comes with ABSOLUTELY NO WARRANTY. This program is released under the GPLv3 licence. \n\nIcon: David Vignoni";
            					break;
            case R.id.feedback: popup.setTitle("Feedback");
            					msg = "Please help me improve this software by sending feedback, suggestions or complaints. Enter your message bellow:";
            					final EditText input = new EditText(this);
            					input.setLines(2);
            					popup.setView(input,30,0,30,0);
            					popup.setButton2("Send Feedback", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent email = new Intent(android.content.Intent.ACTION_SEND);
										email.setType("plain/text");
										email.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"gerrit.n.maritz@gmail.com"});
										email.putExtra(android.content.Intent.EXTRA_SUBJECT , "Inetkey for Android Feedback");
										email.putExtra(android.content.Intent.EXTRA_TEXT, input.getText());
										DroidnetkeyActivity.this.startActivity(Intent.createChooser(email, "Send mail..."));
									}
								});
            					break;
        }
        popup.setButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
        popup.setMessage(msg);
        popup.show();
        return true;
    }
    
    protected boolean isConServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("devza.app.android.droidnetkey.ConnectionService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
    
    /*public void showMenu(View view)
    {
    	this.getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent 
				(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
    }*/
	
}

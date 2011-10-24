package devza.app.android.droidnetkey;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

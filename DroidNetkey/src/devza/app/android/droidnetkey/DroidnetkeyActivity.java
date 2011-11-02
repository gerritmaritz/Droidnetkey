package devza.app.android.droidnetkey;

import android.app.Activity;
import android.app.AlertDialog;
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
                                msg = "Inetkey for Android allows you to open the firewall of Stellenbosch University from you mobile device. Simply enter you SU username and password and click Connect. Passwords are encrypted on the device to ensure security.";
                                break;
            case R.id.about:    popup.setTitle("About");
            					msg = "Inetkey for Android \nCopyright \251 2011 \nGerrit N. Maritz \n\nThis program comes with ABSOLUTELY NO WARRANTY. This program is released under the GPLv3 licence. \n\nIcon: David Vignoni \n\nReference Saftware: Pynetkey, Copyright 2009 Janto Dreijer";
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
										email.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"15629368@sun.ac.za"});
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
    
    public void showMenu(View view)
    {
    	this.getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new KeyEvent 
				(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
    }
	
}

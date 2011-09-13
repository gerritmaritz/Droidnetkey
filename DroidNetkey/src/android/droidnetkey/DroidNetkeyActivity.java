package android.droidnetkey;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class DroidNetkeyActivity extends Activity {
    /** Called when the activity is first created. */
	boolean fwstatus = false;
	ProgressDialog d;
	CheckBox checkBox;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEdit;
	String username;
	private String password;
	
	private EditText editUsername;
	private EditText editPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        prefs = getPreferences(MODE_PRIVATE);
        prefEdit = prefs.edit();
        
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        editUsername = (EditText) findViewById(R.id.editText1);
        editPassword = (EditText) findViewById(R.id.editText2);
        
        loadSettings();
    }
    
    public void fwDialogTimeout(long time, final ProgressDialog d){
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() {           
            public void run() {                
                d.dismiss();
                timeoutDialog();
            }
        }, time); 
    }
    
    public void toggleFirewall(View view)
    {
    	
    	storeSettings();
    	
    	String st = "";
    	
    	if(!fwstatus)
    		st = "Connecting...";
    	else
    		st = "Disconnecting...";
    	d = ProgressDialog.show(DroidNetkeyActivity.this, "", 
                st, true);
    	fwDialogTimeout(15000, d);
    }
    
    public void storeSettings()
    {
    	
    	prefEdit.putString("username", editUsername.getText().toString());
    	
    	if(checkBox.isChecked())
    	{
    		try {
				prefEdit.putString("password", SimpleCrypto.encrypt(Settings.Secure.ANDROID_ID, editPassword.getText().toString()) );
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
    	editUsername.setText(prefs.getString("username", ""));
    	
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
    	editPassword.setText(passplain);
    }
    
    public void timeoutDialog()
    {
    	AlertDialog error = new AlertDialog.Builder(DroidNetkeyActivity.this).create();
        error.setTitle("Error");
        error.setMessage("Connection Timeout");
        error.show();
    }
}
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
import android.widget.Toast;

public class DroidNetkeyActivity extends Activity {
    /** Called when the activity is first created. */
	boolean fwstatus = false;
	ProgressDialog d;
	CheckBox checkBox;
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefEdit;
	private String username;
	private String password;
	
	private EditText editUsername;
	private EditText editPassword;
	
	private FirewallAction fw;
	private boolean fwStatus;
	
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
        
        timeout = new Runnable() {           
            public void run() {                
                d.dismiss();
                timeoutDialog();
            }
        };
        
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
    	
    	String st = "";
    	
    	if(!fwstatus)
    		st = "Connecting...";
    	else
    		st = "Disconnecting...";
    	d = ProgressDialog.show(DroidNetkeyActivity.this, "", 
                st, true);
    	fwDialogTimeout(30000, d);
    	
    	fw = new Firewall(getApplicationContext());
    	fw.connect(this.username, this.password);
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
    
    public void timeoutDialog()
    {
    	AlertDialog error = new AlertDialog.Builder(DroidNetkeyActivity.this).create();
        error.setTitle("Error");
        error.setMessage("Connection Timeout");
        error.show();
    }
    
    public interface FWCallBack
    {
    	void FwCallback();
    }
    
    public void FwCallback(){
        
        
		d.dismiss();
		try {
			timeoutHandler.removeCallbacks(timeout);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(DroidNetkeyActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
	
}
}
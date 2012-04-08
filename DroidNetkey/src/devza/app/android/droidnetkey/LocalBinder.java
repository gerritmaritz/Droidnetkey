package devza.app.android.droidnetkey;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import android.os.Binder;

public class LocalBinder<T> extends Binder {
	 private  WeakReference<T> mService;
	    
	    
	    public LocalBinder(T service){
	        mService = new WeakReference<T>(service);
	    }
	    
	    public T getService() {
	        return mService.get();
	    }

}

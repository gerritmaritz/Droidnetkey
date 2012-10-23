package devza.app.android.droidnetkey;

public interface InetCallback {
	void statusCallback(int code, String message);
	void statusCallback(Exception e);
}

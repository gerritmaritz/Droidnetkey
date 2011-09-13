package android.droidnetkey;

import java.net.Socket;

import javax.net.SocketFactory;


public class Firewall {

	private boolean state;
	private final String url = "fw.sun.ac.za";
	private final int port = 950;
	private Socket sock;
	
	public Firewall() {
		// TODO Auto-generated constructor stub
		state = false;
		
	}
	
	public boolean connect()
	{
		SocketFactory factory =
		sock = createSocket(url, port)
		return true;
	}

}

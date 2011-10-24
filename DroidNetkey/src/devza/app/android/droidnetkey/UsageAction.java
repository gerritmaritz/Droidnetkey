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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

public class UsageAction extends AsyncTask<String, Void, String[]>{
	
	private final String USER_AGENT = "DroidNetKey";
	private final int TIMEOUT = 15000;
	private final String FW_URL = "https://maties2.sun.ac.za/fwusage/";
	
	private final int DISCONNECTED = 2;
	private final int CONNECTED = 1;
	private final int SUCCESS = 0;
	private final int INVALID_CREDENTIALS = -1;
	private final int TIMED_OUT = -2;
	private final int GENERAL_ERROR = -3;
	
	private Context context;
	private TextView cost;
	private TextView usage;
	
	HttpClient client;
	
	public UsageAction(Context context,TextView usage, TextView cost)
	{
		this.context = context;
		this.cost = cost;
		this.usage = usage;
				
		SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        
        //HTTP Paramaters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, USER_AGENT );
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        SingleClientConnManager cm = new SingleClientConnManager(params, registry);
        
        this.client = new DefaultHttpClient(cm, params);
        
	}
	
/*	@Override
	protected void onPreExecute()
	{
		
	}*/
	
	@Override
	protected String[] doInBackground(String... arg0)
	{
		AuthScope scope = new AuthScope("maties2.sun.ac.za", 443);
		Credentials mCredentials = new UsernamePasswordCredentials(arg0[0], arg0[1]);
		CredentialsProvider mProvider = new BasicCredentialsProvider();
		mProvider.setCredentials(scope, mCredentials);
		((AbstractHttpClient) this.client).setCredentialsProvider(mProvider);
		
		HttpGet fwusage = new HttpGet(FW_URL);
		
		String response;
		
		try {
			response = getResponse(fwusage);
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		
		response = response.replace("\r\n", "");
		
		//<td align="right"><font size="1">(.*)</font></td> ([0-9\\.]+)
		Pattern pattern = Pattern.compile("<td><font size=\"1\"><strong>Total</strong></font></td>    <td align=\"right\"><font size=\"1\">([0-9\\.]+)</font></td>    <td align=\"right\"><font size=\"1\">([0-9\\.]+)</font></td>");
		Matcher matcher = pattern.matcher(response);
		
		String[] out = new String[2];

		if(matcher.find())
		{
			out[0] = matcher.group(1);
			out[1] = matcher.group(2);
		}
		
		
		
		return out;
		
	}
	
	private String getResponse(HttpGet httpClient) throws ClientProtocolException, IOException, SocketTimeoutException
	{
			HttpResponse getResponse = null;
		
			getResponse = client.execute((HttpUriRequest) httpClient);
			InputStream response = getResponse.getEntity().getContent();
			
			StringBuilder sb = inputStreamToString(response);
			return sb.toString();
		
		
	}
	
	private StringBuilder inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    try {
			while ((line = rd.readLine()) != null) { 
			    total.append(line); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    // Return full string
	    return total;
	}

	
	@Override
	protected void onPostExecute(String[] arg0)
	{
		cost.setText(arg0[0]+" MB");
		usage.setText("R "+arg0[1]);
			}
}

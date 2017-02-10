package app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.omg.CORBA.Environment;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;

import utils.ErrorLogger;

public class Heartbeat {
	
	private int port;
	private int errorsEncountered;
	private int errorLimit;
	private int interval;
	
	public Heartbeat(int port, int errorLimit, int interval){
		this.port = port;
		this.errorLimit = errorLimit;
		this.interval = interval;
		this.errorsEncountered = 0;
		
		this.start();
	}
	
	public void start() {
		new Thread(this::startHeartbeat).start();
	}

	private void startHeartbeat() {
		while(errorsEncountered < errorLimit){
			
			try {
				Thread.sleep(interval*1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			NetHttpTransport transport = new NetHttpTransport();

			HttpRequest request = null;
			HttpResponse response = null;
			try {
				request = transport.createRequestFactory()
						.buildGetRequest(new GenericUrl(new URL("http://localhost:"+ port + "/index.html")));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				try{
					response = request.execute();
					errorsEncountered = 0;
				}catch(HttpResponseException e){
					
				}
			} catch (IOException e) {
				ErrorLogger.getInstance().error("Heartbeat failed");
				errorsEncountered++;
			}
			
		}
		ErrorLogger.getInstance().error(errorLimit +" heartbeats failed. Killing server to restart");
		System.exit(1);
	}
}

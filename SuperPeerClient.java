import java.rmi.*;
import java.util.Calendar;

public class SuperPeerClient {
	static private SuperPeer server = null;
	static private Tracker tracker = null;
	
	static private Thread listRetriever = null;
	
	static private String timestamp;
	
	public SuperPeerClient (SuperPeer server, Tracker tracker) {
		this.server = server;
		this.tracker = tracker;
		this.startupListRetriever();
	}
	
	private void setTimestamp() {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH); // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int millis = now.get(Calendar.MILLISECOND);
		this.timestamp = String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
	}
	
	public void setCoordinator(String risorsa) {
		try {
			tracker.cambioCoordinatore(server.getIP(), risorsa);
		} catch (RemoteException e) {
			System.out.println("Exception while setting coordinator: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void startupListRetriever() {
		  listRetriever = new Thread(
				  new Runnable() {
		                public void run() {
		                    try {
		                    	server.setList(tracker.getList(timestamp));
		                    	Thread.sleep(10000);
		                    } catch (Exception e) {
		                    	System.out.println("Exception in thread:" + e.getMessage());
		                    	e.printStackTrace();
		                    }
		                }
		            });
		  listRetriever.start();
	}
	
	@SuppressWarnings("deprecation")
	private void stopListRetriever() {
		listRetriever.stop();
	}
}

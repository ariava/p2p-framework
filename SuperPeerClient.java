import java.rmi.*;
import java.util.Calendar;

public class SuperPeerClient {
	static private SuperPeer server = null;
	static private Tracker tracker = null;
	
	static private Thread listRetriever = null;
	
	static private String timestamp;
	
	/*
	 * Costruttore della classe SuperPeerClient.
	 * Il costruttore si occupa di inizializzare le variabili private necessarie
	 * per le funzioni di coordinatore di zona e di avviare il thread dedicato al
	 * recupero periodico della tabella dei coordinatori dal tracker.
	 * 
	 * Parametri:
	 * server: riferimento al server coordinatore
	 * tracker: riferimento al tracker
	 * */
	public SuperPeerClient (SuperPeer server, Tracker tracker) {
		this.server = server;
		this.tracker = tracker;
		this.startupListRetriever();
	}
	
	/*
	 * Funzione privata di calcolo del timestamp, secondo un formato
	 * comprensibile per un oggetto di tipo TrackerServer.
	 * 
	 * TODO: usare una libreria condivisa e lo stesso metodo SPOT?
	 * */
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
	
	/*
	 * Metodo per impostare il server SuperPeer come coordinatore di una risorsa data.
	 * 
	 * Il metodo invoca il rispettivo metodo dell'oggetto Tracker passandogli l'indirizzo
	 * IP del nuovo coordinatore e l'identificativo univoco della risorsa data.
	 * 
	 * Parametri:
	 * risorsa: stringa identificativa della risorsa della quale si vuole impostare
	 *          il nuovo coordinatore
	 * */
	public void setCoordinator(String risorsa) {
		try {
			tracker.cambioCoordinatore(server.getIP(), risorsa);
		} catch (RemoteException e) {
			System.out.println("Exception while setting coordinator: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/*
	 * Metodo privato per l'avviamento di un nuovo thread.
	 * Tale thread periodicamente richiede all'oggetto Tracker la lista dei
	 * coordinatori aggiornata e la passa all'oggetto server SuperPeer perché
	 * la memorizzi internamente.
	 * */
	private void startupListRetriever() {
		  // XXX: se listRetriever è già non null?
		  listRetriever = new Thread(
				  new Runnable() {
		                public void run() {
		                    try {
		                    	server.setList(tracker.getList(timestamp));
		                    	Thread.sleep(10000); // XXX: quale intervallo?
		                    } catch (Exception e) {
		                    	System.out.println("Exception in thread:" + e.getMessage());
		                    	e.printStackTrace();
		                    }
		                }
		            });
		  listRetriever.start();
	}
	
	/*
	 * Metodo privato per arrestare il thread incaricato di rinfrescare la
	 * tabella dei coordinatori nel server SuperPeer.
	 * */
	@SuppressWarnings("deprecation")
	private void stopListRetriever() {
		// XXX: fare qualsiasi cosa solo se listRetriever != null!
		listRetriever.stop();
	}
}

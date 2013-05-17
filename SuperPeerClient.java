import java.net.UnknownHostException;
import java.rmi.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

public class SuperPeerClient extends PeerClient {
	static private SuperPeer server = null;
	static private Tracker tracker = null;
	
	static private Thread listRetriever = null;
	
	static private String timestamp;

	static private boolean debug = true;
	
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
	public SuperPeerClient (PeerClient pc, SuperPeer server, Tracker tracker) throws UnknownHostException {
		//super();
		assert(server != null);
		assert(tracker != null);
		this.server = server;
		this.tracker = tracker;
		
		this.myIp = pc.myIp;
		
		
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
	 * Metodo di debug che stampa la tabella dei coordinatori passata come parametro.
	 * 
	 * Parametri:
	 * table: tabella hash dei coordinatori
	 * */
	private void printCoordTable(Hashtable<String, String> table) {
		assert(table != null && table.size() != 0);
		Enumeration<String> e = table.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			System.out.println("Risorsa: " + key + " | Coord: " + table.get(key));
		}
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
		assert(risorsa != null && risorsa != "");
		try {
			String coord_ip = server.getIP();
			assert(coord_ip != null && coord_ip != "");
			if (debug) {
				System.out.println("SuperPeerClient - Impostazione del coordinatore");
			    System.out.println("Impostazione di " + coord_ip + " per la risorsa " + risorsa);
			}
			tracker.cambioCoordinatore(coord_ip, risorsa);
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
		  if(listRetriever == null)
			  return;
		  if (debug)
			  System.out.println("SuperPeerClient: avviamento del thread di rinfresco della tabella");
		  listRetriever = new Thread(
				  new Runnable() {
		                public void run() {
		                    try {
		                    	/* Aggiornamento del timestamp */
		                    	setTimestamp();
		                    	/* Recupero della tabella dei coordinatori dal tracker */
		                    	Hashtable<String, String> table = tracker.getList(timestamp);
		                    	/* Impostazione della tabella sul server se non è nulla */
		                    	if (table != null) {
		                    		if (debug) {
			                    		System.out.println("Thread: impostazione della coordTable al timestamp " + timestamp);
			                    		printCoordTable(table);
			                    	}
		                    		server.setList(table);
		                    	} else {
		                    		if (debug)
		                    			System.out.println("Thread: al timestamp " + timestamp + " la tabella non è cambiata");
		                    	}
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
		if (debug)
			System.out.println("SuperPeerClient: tentativo di arresto del thread di rinfresco della coordTable");
		if (listRetriever != null) {
			if (debug)
				System.out.println("Thread in funzione, arrestato");
			listRetriever.stop();
			listRetriever = null;
		} else {
			if (debug)
				System.out.println("Thread non attivo");
		}
	}
	
	/*
	 * Metodo accessibile al Peer da cui questa classe deriva per impostare
	 * la modalità di debug.
	 * */
	protected void setDebug(boolean value) {
		this.debug = value;
	}
}

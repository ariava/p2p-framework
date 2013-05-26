import java.net.UnknownHostException;
import java.rmi.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

public class SuperPeerClient extends PeerClient {
	private SuperPeer server = null;
	private Tracker tracker = null;
	
	private Thread listRetriever = null;
	
	private String timestamp;
	
	/**
	 * Costruttore della classe SuperPeerClient.
	 * Il costruttore si occupa di inizializzare le variabili private necessarie
	 * per le funzioni di coordinatore di zona e di avviare il thread dedicato al
	 * recupero periodico della tabella dei coordinatori dal tracker.
	 * 
	 * @param pc riferimento al PeerClient
	 * @param server riferimento al server coordinatore
	 * @param tracker riferimento al tracker
	 * @param trIp indirizzo ip del tracker
	 */
	public SuperPeerClient (PeerClient pc, SuperPeer server, Tracker tracker) throws UnknownHostException {
		assert(server != null);
		assert(tracker != null);
		this.server = server;
		this.tracker = tracker;
		
		this.myIp = pc.myIp;
		
		this.startupListRetriever();
	}
	
	/**
	 * Funzione privata di calcolo del timestamp, secondo un formato
	 * comprensibile per un oggetto di tipo TrackerServer.
	 * 
	 * TODO: usare una libreria condivisa e lo stesso metodo SPOT?
	 */
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
	
	/**
	 * Metodo di debug che stampa la tabella dei coordinatori passata come parametro.
	 * 
	 * @param table tabella hash dei coordinatori
	 */
	private void printCoordTable(Hashtable<String, String> table) {
		assert(table != null && table.size() != 0);
		
		System.out.println("Sono nel thread, stampo la tabella:");
		
		Enumeration<String> e = table.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			System.out.println("Risorsa: " + key + " | Coord: " + table.get(key));
		}
	}
	
	/**
	 * Metodo per impostare il server SuperPeer come coordinatore di una risorsa data.
	 * 
	 * Il metodo invoca il rispettivo metodo dell'oggetto Tracker passandogli l'indirizzo
	 * IP del nuovo coordinatore e l'identificativo univoco della risorsa data.
	 * 
	 * @param risorsa stringa identificativa della risorsa della quale si vuole impostare
	 *        il nuovo coordinatore
	 */
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
	
	/**
	 * Metodo privato per l'avviamento di un nuovo thread.
	 * Tale thread periodicamente richiede all'oggetto Tracker la lista dei
	 * coordinatori aggiornata e la passa all'oggetto server SuperPeer perché
	 * la memorizzi internamente.
	 */
	private void startupListRetriever() {
		  if(listRetriever != null)
			  return;
		  if (debug)
			  System.out.println("SuperPeerClient: avviamento del thread di rinfresco della tabella");
		  listRetriever = new Thread(
				  new Runnable() {
		                public void run() {
		                	Hashtable<String, String> table = null;
		                	boolean down = false;
		                	
		                	if (debug)
		                		System.out.println("Avviato il thread, prelevo la tabella iniziale");
		                	
							try {
								table = tracker.getList("1970-01-01 00:00:00.000");
								setTimestamp();
								printCoordTable(table);
							} catch (RemoteException e1) {
								e1.printStackTrace();
							}
		                	while(true) {
			                    try {
			                    	/* Faccio il lookup al tracker, che magari e' tornato disponibile */
			                    	String s = "rmi://"+trackerIp+"/Tracker";
			                    	tracker = (Tracker)Naming.lookup(s);

			                    	if (down) {
			                    		if (debug)
			                    			System.out.println("Thread: il tracker era down ed e' tornato up, gli mando la tabella");
			                    		tracker.setList(server.getCoordTable()); 
			                    		down = false;
			                    		printCoordTable(server.getCoordTable());
			                    	}
			                    	else
			                    		/* Recupero della tabella dei coordinatori dal tracker */
			                    		table = tracker.getList(timestamp);
			                    		
			                    	/* Impostazione della tabella sul server se non è nulla */
			                    	if (table != null) {
			                    		if (debug) {
				                    		System.out.println("Thread: impostazione della coordTable al timestamp " + timestamp);
				                    		setTimestamp();
				                    		printCoordTable(table);
				                    	}
			                    		server.setList(table);
			                    	} else {
			                    		if (debug)
			                    			System.out.println("Thread: al timestamp " + timestamp + " la tabella non è cambiata");
			                    	}
			                    	Thread.sleep(5000); 
			                    } catch (Exception e) {
			                    	down = true;
			                    	System.out.println("Tracker's dead baby, tracker's dead." + e.getMessage());
			                    	try {
										Thread.sleep(5000);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
			                    }
			                }
		                }
		            });
		  listRetriever.start();
	}
	
	/**
	 * Metodo per arrestare il thread incaricato di rinfrescare la tabella dei 
	 * coordinatori nel server SuperPeer.
	 */
	@SuppressWarnings("deprecation")
	protected void stopListRetriever() {
		if (debug)
			System.out.println("Tentativo di arresto del thread di rinfresco della coordTable");
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
}

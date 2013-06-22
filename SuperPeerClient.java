import java.net.UnknownHostException;
import java.rmi.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * La classe SuperPeerClient è una estensione della classe PeerClient
 * aggiungendo di fatto le funzionalità offerte da un SuperPeer
 * (coordinatore di zona).
 * 
 * In particolare
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
public class SuperPeerClient extends PeerClient {

	private SuperPeer server = null;
	private Tracker tracker = null;
	
	private Thread listRetriever = null;
	private final int listRetrieverSleep = 5000;
	private static boolean listRetrieverContinue;
	
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
	 */
	public SuperPeerClient (PeerClient pc, SuperPeer server, Tracker tracker) throws UnknownHostException {
		assert server != null : "Campo server nullo";
		assert tracker != null : "Campo tracker nullo";
		this.server = server;
		this.tracker = tracker;
		this.trackerIp = pc.trackerIp;
		this.trackerIsDown = false;
		
		this.myIp = pc.myIp;
		this.pollingWorker = pc.pollingWorker;
		if (debug) {
			System.out.println("PeerClient " + pc + ": pollingWorker " + pc.pollingWorker);
			System.out.println("SuperPeerClient " + this + ": pollingWorker " + this.pollingWorker);
		}

		listRetrieverContinue = true;
		this.startupListRetriever();
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
		assert risorsa != null : "Campo risorsa nullo nell'impostazione del coordinatore";
		assert !risorsa.equals("") : "Campo risorsa vuoto nell'impostazione del coordinatore";
		try {
			String coordIp = server.getIP();
			assert coordIp != null : "IP coordinatore nullo";
			assert !coordIp.equals("") : "IP coordinatore vuoto";
			if (debug) {
				System.out.println("SuperPeerClient - Impostazione del coordinatore");
			    System.out.println("Impostazione di " + coordIp + " per la risorsa " + risorsa);
			}
			tracker.cambioCoordinatore(coordIp, risorsa);
		} catch (RemoteException e) {
			if (debug) {
				System.out.println("Eccezione nel settaggio del coordinatore: " + e.getMessage());
				e.printStackTrace();
			}
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
								timestamp = Common.setTimestamp();
								if (debug) {
									System.out.println("Sono nel thread, stampo la tabella iniziale");
									Common.printCoordTable(table);
								}
							} catch (RemoteException e1) {
								if (debug)
									e1.printStackTrace();
							}
		                	while (listRetrieverContinue) {
			                    try {
			                    	/* Faccio il lookup al tracker, che magari e' tornato disponibile */
			                    	String s = "rmi://"+trackerIp+"/Tracker";
			                    	tracker = (Tracker)Naming.lookup(s);
			                    	tracker.ping(); //pingo in caso che il registry sia su ma il tracker no
			                    	if (down) {
			                    		Hashtable<String, String> coordTable = server.getCoordTable();
			                    		if (debug) {
			                    			System.out.println("Thread: il tracker era down ed e' tornato up, gli mando la tabella");
			                    			Common.printCoordTable(coordTable);
			                    		}
				                   		Hashtable<String, String> tempTable = new Hashtable<String, String>();
										Enumeration<String> ec = coordTable.keys();
										while (ec.hasMoreElements()) {
											String key = ec.nextElement();
											if (coordTable.get(key).equals(myIp))
												tempTable.put(key, coordTable.get(key));
										}
							            tracker.setList(tempTable);
										if (debug) {
											System.out.println("Tabella impostata sul tracker");
											Common.printCoordTable(tempTable);
										}
					                   	down = false;
					                   	trackerIsDown = false;
				                   	} else
				                   		/* Recupero della tabella dei coordinatori dal tracker */
				                   		table = tracker.getList(timestamp);
			                    		
			                    	/* Impostazione della tabella sul server se non è nulla */
			                    	if (table != null) {
			                    		if (debug) {
				                    		System.out.println("Thread: impostazione della coordTable al timestamp " + timestamp);
				                    		Common.printCoordTable(table);
				                    	}
			                    		timestamp = Common.setTimestamp();
			                    		server.setList(table);
			                    	} else if (debug)
			                    			System.out.println("Thread: al timestamp " + timestamp + " la tabella non è cambiata");
			                    	Thread.sleep(listRetrieverSleep); 
			                    } catch (Exception e) {
			                    	down = true;
			                    	trackerIsDown = true;
			                    	if (debug)
			                    		System.out.println("Il Tracker è morto:" + e.getMessage());
			                    	try {
										Thread.sleep(listRetrieverSleep);
									} catch (InterruptedException e1) {
										if (debug)
											e1.printStackTrace();
									}
			                    }
			                }
		                }
		            });
		  /* Avvio il thread di rinfresco della tabella */
		  listRetriever.start();
	}
	
	/**
	 * Metodo per arrestare il thread incaricato di rinfrescare la tabella dei 
	 * coordinatori nel server SuperPeer.
	 */
	protected void stopListRetriever() {
		assert (listRetriever == null && listRetrieverContinue == false) ||
		       (listRetriever != null && listRetrieverContinue == true) :
			"Discordanza tra il riferimento listRetriever e il flag";
		if (debug)
			System.out.println("Tentativo di arresto del thread di rinfresco della coordTable");
		if (listRetriever != null) {
			if (debug)
				System.out.println("Thread in funzione, arrestato");
			listRetrieverContinue = false;
			listRetriever = null;
		} else {
			if (debug)
				System.out.println("Thread non attivo");
		}
	}

}

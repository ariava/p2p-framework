import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.*;
import java.rmi.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La classe TrackerServer implementa le funzionalità del
 * Tracker centralizzato.
 * 
 * Il punto d'accesso alla rete peer to peer è centralizzato 
 * ed è costituito da un server tracker. Il tracker mantiene 
 * una tabella che descrive il layout della rete: ad ogni risorsa 
 * registrata nella rete, tale tabella associa l'identificativo 
 * univoco (indirizzo ip) di un peer eletto quale coordinatore 
 * per quella risorsa, mantenendo di fatto informazioni solamente 
 * su un sottoinsieme molto ristretto dei peer che fanno parte 
 * della rete.
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
public class TrackerServer extends UnicastRemoteObject implements Tracker {
	
	private static final long serialVersionUID = 1L;
	
	private static boolean debug;
	
	private String timestamp;
	private Hashtable<String, String> table;
	
	// XXX (Arianna): controllo del formato sull'IP in libreria accessibile
	//                anche dalle altre classi?
	private static final String PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	/**
	 * Metodo che controlla la correttezza del formato
	 * che ha un indirizzo ip sulla stringa in input
	 * 
	 * @param ip indirizzo ip
	 * 
	 * @return vero se la stringa in input ha il formato
	 * di un indirizzo ip, falso altrimenti
	 */
	public boolean validate(final String ip) {
		if(ip.isEmpty()) 
			return false;
		
	    Pattern pattern = Pattern.compile(PATTERN);
	    Matcher matcher = pattern.matcher(ip);
	    return matcher.matches();             
	}  
	
	/**
	 * Metodo che stampa la tabella posseduta dal tracker
	 */
	private void stampaTabella() {
		Enumeration<String> enumKey = table.keys();
		System.out.println("Tabella Tracker");
		System.out.println("timestamp: " + timestamp);
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    String val = table.get(key);   
		    System.out.println("risorsa: " + key + " | coordinatore: " + val);
		   
		}
		System.out.println();
	}
	
	/**
	 * Costruttore della classe TrackerServer.
	 * 
	 * Crea una tabella vuota con chiave = nome_risorsa e valore l'indirizzo
	 * ip (il coordinatore) per quella risorsa
	 */
	public TrackerServer() throws RemoteException {
		super();
		
		this.table = new Hashtable<String, String>();
		this.timestamp = Common.setTimestamp();
	}
	
	/**
	 * Metodo che ritorna l'ip del tracker, usato dal thread di servizio del
	 * SuperPeerClient per fargli polling
	 * 
	 * @return una stringa contenente l'ip del TrackerServer
	 */
	public String getIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			if (debug)
				e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Metodo usato per impostare la lista dei coordinatori quando il tracker è
	 * tornato disponibile dopo un periodo di downtime
	 * 
	 * @param l la hashtable contenente la tabella dei coordinatori utilizzata dal
	 * TrackerServer
	 */
	public void setList(Hashtable<String, String> l) {
		assert l != null : "La tabella non e' definita";
		
		//merge lists:
		if(this.table.isEmpty()) {
			this.table = l;
			return;
		} else {
			Enumeration<String> e = l.keys();
			while(e.hasMoreElements()) {
				String key = e.nextElement();
				if(!this.table.containsKey(key)) {
					this.table.put(key, l.get(key));
				}		
			}
		}
		if(debug) {
			System.out.println("Ho ricevuto una nuova tabella da qualcuno dopo esser tornato up, stampo la tabella attuale");
			this.stampaTabella();
		}
	}
	
	/**
	 * Metodo invocato per accertare la raggiungibilità del TrackerServer
	 * 
	 * @return il valore booleano "true".
	 */
	public boolean ping() throws RemoteException {
		return true;
	}

	/**
	 * Metodo invocato quando un nuovo nodo vuole entrare a far parte della rete
	 * p2p.
	 * Questo metodo per ogni risorsa controlla se c'è già un coordinatore di zona
	 * per quella risorsa o no.
	 * Se si ritorna l'indirizzo del coordinatore di zona;
	 * Se no aggiunge una entry nella tabella con chiave il nome della risorsa e con
	 * valore l'indirizzo ip del nodo richiedente
	 * 
	 * @param ip ip del nodo che vuole entrare a far parte della rete p2p
	 * @param risorse le risorse possedute dal nodo che vuole entrare a far
	 * parte della rete p2p
	 * 
	 * @return la lista dei coordinatori delle risorse registrate dal nodo che vuole
	 * entrare a far parte della rete p2p
	 */
	public Vector<String> registrazione(String ip, Vector<String> risorse) throws RemoteException {
		
		assert this.validate(ip) == true : "Indirizzo ip non valido";
		
		if (debug)
			System.out.println("Inizio registrazione risorse del peer " + ip);
		
		Vector<String> ipCoordinatori = new Vector<String>();
		
		for (int i = 0 ; i < risorse.size() ; i++) {
			if (table.containsKey(risorse.get(i))) {
				ipCoordinatori.add(table.get(risorse.get(i)));
			}
			else {
				table.put(risorse.get(i), ip);
				this.timestamp = Common.setTimestamp();
				ipCoordinatori.add(ip);
			}
		}
		
		assert ipCoordinatori.size() == risorse.size() : "Dimensione vettori non bilanciata";
		
		if (debug) {
			System.out.println("Registrazione effettuata");
			this.stampaTabella();
		}
		
		return ipCoordinatori;
	}
	
	/**
	 * Metodo che viene invocato quando un nodo richiede una certa risorsa.
	 * Tale metodo controlla la tabella alla ricerca della risorsa e, se c'è,
	 * restituisce il coordinatore per quella zona, altrimenti restituisce null
	 * 
	 * @param risorsa la risorsa che sta richiedendo un certo Peer
	 * 
	 * @return il coordinatore per la risorsa richiesta o null se il coordinatore
	 * non esiste
	 */
    public String richiesta(String risorsa) throws RemoteException {
    	
    	if (debug) 
    		System.out.println("Inizio richiesta semplice per la risorsa " + risorsa);
    	
    	if (table.containsKey(risorsa)) {
    		if (debug) 
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + table.get(risorsa));
    		return table.get(risorsa);
    	}
    	else {
    		if (debug) 
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " non esiste");
    		return null;
    	}
    }
    
    /**
     * Metodo che viene invocato quando un Peer aveva già invocato il metodo 
     * {@link #richiesta(String)} ma per un qualche motivo non è riuscito a 
     * connettersi al coordinatore.
     * Questo metodo controlla se il coordinatore corrente per una certa risorsa è rimasto
     * lo stesso di prima o se è cambiato:
     * 1) se è rimasto quello precedente, pinga (non si fida del client) il coordinatore e se
     * 	  va tutto bene ritorna ancora quel coordinatore; se non pinga effettivamente quel
     * 	  coordinatore è andato giù e quindi viene rimosso dalla tabella e ritorna null;
     * 2) se è cambiato allora restituisce semplicemente il nuovo coordinatore
     * 
     * @param risorsa la risorsa che sta richiedendo un certo Peer
     * @param ipPrecedente il coordinatore a cui non è riuscito a connettersi il client
     * 
     * @return il coordinatore per la risorsa specificata o null nel caso in cui il
     * coordinatore per quella risorsa non esiste
     */
    public String richiesta(String risorsa, String ipPrecedente) throws RemoteException {
    	
    	assert this.validate(ipPrecedente) == true : "Indirizzo ip non valido";
    	
    	if (debug)
    		System.out.println("Inizio richiesta per la risorsa " + risorsa + " dato che in precedenza ho risposto con il coordinatore " + ipPrecedente);
    	
    	String coordinatoreCorrente = this.richiesta(risorsa);
    	
    	if (coordinatoreCorrente.equals(ipPrecedente)) {
    		if (this.pingUrl(coordinatoreCorrente)) {
    			if (debug) {
    				System.out.println("Il ping del coordinatore" + coordinatoreCorrente + "ha dato esito positivo");
        			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + coordinatoreCorrente);
        			this.stampaTabella();
        		}
    			return coordinatoreCorrente;
    		} else {
    			this.eliminateCoordinatorFromTable(ipPrecedente);
    			if (debug) {
    				System.out.println("Il ping del coordinatore " + coordinatoreCorrente + " ha dato esito negativo");
        			System.out.println("Il coordinatore per la risorsa " + risorsa + " non esiste");
        			this.stampaTabella();
        		}
    			return null;
    		}
    	} else {
    		if (debug) {
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + coordinatoreCorrente);
    			this.stampaTabella();
    		}
    		return coordinatoreCorrente;
    	}
    }
    
    /**
     * Metodo che viene invocato dal tracker per pingare un nodo
     * 
     * @param address l'indirizzo ip del nodo che vuole pingare
     * 
     * @return vero se pinga, falso altrimenti
     */
    private boolean pingUrl(String address) {
    	
    	boolean status = false;
    	
    	if (debug)
    		System.out.println("Inizio ping sull'indirizzo ip " + address);
    	
        try {
            InetAddress adr = InetAddress.getByName(address);
            status = adr.isReachable(2000); // 2 secondi
            return status;
        } catch (IOException e) {
        	if (debug)
        		e.printStackTrace();
        }
        
        return status;
    }
    
    /**
     * Metodo che viene invocato dal tracker per eliminare un coordinatore
     * dalla tabella.
     * In particolare vengono eliminate tutte le risorse associate ad un
     * certo coordinatore.
     *
     * @param coordinator il coordinatore da eliminare nella tabella
     */
    private void eliminateCoordinatorFromTable(String coordinator) {
    	
    	if (debug)
    		System.out.println("Elimino tutte le risorse delle quali è coordinatore " + coordinator);
    	
    	Enumeration<String> enumKey = table.keys();
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    String val = table.get(key);
		    if(val.equals(coordinator)) {
		        table.remove(key);
		        this.timestamp = Common.setTimestamp();
		    }
		}
		
		assert table.containsValue(coordinator) == false : "Non è stato eliminato il coordinatore per una certa risorsa";
    }
    
    /**
     * Metodo che viene invocato da un Peer per cambiare il coordinatore per
     * una certa risorsa o per aggiungere un nuovo coordinatore per una certa
     * risorsa
     * 
     * @param ip indirizzo ip del coordinatore (nuovo o già esistente)
     * @param risorsa la risorsa per il quale ip è diventato il nuovo coordinatore
     */
    public void cambioCoordinatore(String ip, String risorsa) throws RemoteException {
    	
    	if(ip.isEmpty()) {
    		this.table.remove(risorsa);
    		return;
    	}
    	
    	assert this.validate(ip) == true : "Indirizzo ip non valido";
    	
    	if (debug)
    		System.out.println("Inizio cambio coordinatore");
    	
    	// se la chiave esiste già viene fatto un replace
    	// se la chiave non esiste, viene aggiunta
    	table.put(risorsa, ip);
    	this.timestamp = Common.setTimestamp();
    	
    	if (debug) {
    		System.out.println("Cambio coordinatore eseguito");
    		this.stampaTabella();
    	}
    	
    	assert table.get(risorsa) == ip : "Non è stato cambiato il coordinatore per la risorsa " + risorsa;
    }
    
    /**
     * Metodo che viene invocato periodicamente dai coordinatori per farsi
     * restituire la tabella aggiornata posseduta dal tracker.
     * In particolare la tabella viene restituita se e solo se il timestamp
     * del coordinatore è più piccolo del timestamp del tracker (il server 
     * ha modificato la tabella e il coordinatore ha una versione non aggiornata)
     * 
     * @param timestamp il timestamp dell'ultima modifica effettuata dal coordinatore
     * 
     * @return la tabella aggiornata posseduta dal tracker o null nel caso in cui
     * il coordinatore possiede già la tabella aggiornata
     */
    public Hashtable<String, String> getList(String timestamp) throws RemoteException {
    	
    	assert timestamp.length() == this.timestamp.length() : "I due timestamp hanno dimensione diversa";
    	
    	if (debug)
    		System.out.println("Chiamata la getList da parte di un peer");
    	
    	if (timestamp.compareTo(this.timestamp) < 0) {
    		if (debug) 
        		System.out.println("Ritorno la mia tabella (aggiornata) al peer che l'ha richiesta");
    		return table;
    	} else {
    		if (debug) 
        		System.out.println("Il peer ha già la tabella aggiornata percui non gli passo nulla");
    		return null;
    	}
    }

    
    /**
     * Il main inizializza il Tracker e lo fa eseguire
     * 
     * @param args array passato al lancio del tracker. Il primo elemento
     * di tale array indica se far partire il Tracker in modalità di debug
     * (args[0] = "debug") o no (senza parametri)
     */
    public static void main(String[] args) {
    	debug = (args.length > 0 && args[0].equals("debug")) ? true : false;
    	System.setSecurityManager(new RMISecurityManager());
        try {
            TrackerServer obj = new TrackerServer();
            Naming.rebind("Tracker", obj);
            if (debug)
            	System.out.println("Il server è in esecuzione, digitare CTRL+C per terminarlo.");
        } catch(Exception e) {
        	if (debug)
        		e.printStackTrace();
        }
    }

}
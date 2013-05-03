import java.io.IOException;
import java.net.InetAddress;
import java.rmi.server.*;
import java.rmi.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackerServer extends UnicastRemoteObject implements Tracker {
	
	private static final long serialVersionUID = 1L;
	
	private boolean debug = true;
	
	private String timestamp;
	private Hashtable<String, String> table;
	
	private static final String PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	private boolean validate(final String ip){          

	      Pattern pattern = Pattern.compile(PATTERN);
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}
	
	private void stampaTabella() {
		
		Enumeration<String> enumKey = table.keys();
		System.out.println("*** TABELLA TRACKER ***");
		System.out.println("timestamp: " + timestamp);
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    String val = table.get(key);   
		    System.out.println("risorsa: " + key + " | coordinatore: " + val);
		    System.out.println();
		}
	}
	
	/*
	 * Setta il timestamp al momento corrente
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
		assert timestamp.length() == 23 : "Il timestamp ha lunghezza " + timestamp.length();
	}
	
	/*
	 * Crea una tabella vuota con chiave = nome_risorsa e valore l'indirizzo
	 * ip (il coordinatore) per quella risorsa
	 */
	public TrackerServer() throws RemoteException {
		super();
		
		this.table = new Hashtable<String, String>();
		this.setTimestamp();
	}
	
	/*
	 * Viene invocata quando un nuovo nodo vuole entrare a far parte della rete
	 * p2p.
	 * Questo metodo per ogni risorsa controlla se c'è già un coordinatore di zona
	 * per quella risorsa o no.
	 * Se si ritorna l'indirizzo del coordinatore di zona;
	 * Se no aggiunge una entry nella tabella con chiave il nome della risorsa e con
	 * valore l'indirizzo ip del nodo richiedente
	 */
	public Vector<String> registrazione(String ip, Vector<String> risorse) throws RemoteException {
		
		assert this.validate(ip) == true : "Indirizzo ip non valido";
		
		if (debug) {
			System.out.println("Inizio registrazione risorse del peer " + ip + "...");
		}
		
		Vector<String> ipCoordinatori = new Vector<String>();
		
		for (int i = 0; i < risorse.size(); i++) {
			if (table.containsKey(risorse.get(i))) {
				ipCoordinatori.add(table.get(risorse.get(i)));
			}
			else {
				table.put(risorse.get(i), ip);
				this.setTimestamp();
				ipCoordinatori.add(ip);
			}
		}
		
		assert ipCoordinatori.size() == risorse.size() : "Dimensione vettori non bilanciata";
		
		if (debug) {
			System.out.println("Registrazione effettuata");
			System.out.println("I coordinatori sono:");
			for (int i = 0; i < ipCoordinatori.size(); i++) {
				System.out.println(ipCoordinatori.get(i));
			}
			this.stampaTabella();
		}
		
		return ipCoordinatori;
	}
	
	/*
	 * Questo metodo viene invocato quando un nodo richiede una certa risorsa.
	 * Tale metodo controlla la tabella alla ricerca della risorsa e se c'è
	 * restituisce il coordinatore per quella zona, altrimenti restituisce la
	 * stringa vuota
	 */
    public String richiesta(String risorsa) throws RemoteException {
    	
    	if (debug) {
    		System.out.println("Inizio richiesta semplice per la risorsa " + risorsa + "...");
    	}
    	
    	if (table.containsKey(risorsa)) {
    		if (debug) {
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + table.get(risorsa));
    		}
    		return table.get(risorsa);
    	}
    	else {
    		if (debug) {
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " non esiste");
    		}
    		return "";
    	}
    }
    
    /*
     * Se il client ha già invocato il metodo richiesta(String) ma per un qualche motivo
     * non è riuscito a connettersi al coordinatore, chiama questo metodo dove il secondo
     * parametro indica il coordinatore a cui non è riuscito a connettersi il client.
     * Questo metodo controlla se il coordinatore corrente per una certa risorsa è rimasto
     * lo stesso di prima o se è cambiato:
     * 1) se è rimasto quello precedente, pinga (non si fida del client) il coordinatore e se
     * 	  va tutto bene ritorna ancora quel coordinatore; se non pinga effettivamente quel
     * 	  coordinatore è andato giù e quindi viene rimosso dalla tabella e ritorna la stringa
     *    vuota.
     * 2) se è cambiato allora restituisce semplicemente il nuovo coordinatore
     */
    public String richiesta(String risorsa, String ipPrecedente) throws RemoteException {
    	
    	assert this.validate(ipPrecedente) == true : "Indirizzo ip non valido";
    	
    	if (debug) {
    		System.out.println("Inizio richiesta per la risorsa " + risorsa + " dato che in precedenza ho risposto con il coordinatore " + ipPrecedente + "...");
    	}
    	
    	String coordinatore_corrente = this.richiesta(risorsa);
    	
    	if (coordinatore_corrente.equals(ipPrecedente)) {
    		if (this.pingUrl(coordinatore_corrente)) {
    			if (debug) {
    				System.out.println("Il ping del coordinatore" + coordinatore_corrente + "ha dato esito positivo");
        			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + coordinatore_corrente);
        			this.stampaTabella();
        		}
    			return coordinatore_corrente;
    		}
    		else {
    			this.eliminateCoordinatorFromTable(ipPrecedente);
    			if (debug) {
    				System.out.println("Il ping del coordinatore " + coordinatore_corrente + " ha dato esito negativo");
        			System.out.println("Il coordinatore per la risorsa " + risorsa + " non esiste");
        			this.stampaTabella();
        		}
    			return "";
    		}
    	}
    	else {
    		if (debug) {
    			System.out.println("Il coordinatore per la risorsa " + risorsa + " è " + coordinatore_corrente);
    			this.stampaTabella();
    		}
    		return coordinatore_corrente;
    	}
    }
    
    /*
     * Questo metodo ritorna true se pinga un certo indirizzo ip, ritorna
     * false altrimenti
     */
    private boolean pingUrl(String address) {
    	
    	boolean status = false;
    	
    	if (debug) {
    		System.out.println("Inizio ping...");
    	}
    	
        try {
            InetAddress adr = InetAddress.getByName(address);
            status = adr.isReachable(2000); // 2 secondi
            return status;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return status;
    }
    
    /*
     * Questo metodo elimina tutte le entry della tabella in cui
     * coordinator è valore
     */
    private void eliminateCoordinatorFromTable(String coordinator) {
    	
    	if (debug) {
    		System.out.println("Elimino tutte le risorse delle quali è coordinatore " + coordinator);
    	}
    	
    	Enumeration<String> enumKey = table.keys();
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    String val = table.get(key);
		    if(val.equals(coordinator)) {
		        table.remove(key);
		        this.setTimestamp();
		    }
		}
		
		assert table.containsValue(coordinator) == false : "Non è stato eliminato il coordinatore per una certa risorsa";
    }
    
    /*
     * Invocato dopo l'algoritmo di elezione.
     * Questo metodo cambia/aggiunge una entry della tabella settando un nuovo
     * coordinatore per una certa risorsa
     */
    public void cambioCoordinatore(String ip, String risorsa) throws RemoteException {
    	
    	assert this.validate(ip) == true : "Indirizzo ip non valido";
    	
    	if (debug) {
    		System.out.println("Inizio cambio coordinatore...");
    	}
    	
    	// se la chiave esiste già viene fatto un replace
    	// se la chiave non esiste, viene aggiunta
    	table.put(risorsa, ip);
    	this.setTimestamp();
    	
    	if (debug) {
    		System.out.println("Cambio coordinatore eseguito");
    		this.stampaTabella();
    	}
    	
    	assert table.get(risorsa) == ip : "Non è stato cambiato il coordinatore per la risorsa " + risorsa;
    }
    
    /*
     * Invocato periodicamente dai coordinatori.
     * Questo metodo restituisce la tabella al coordinatore solo se il timestamp
     * del coordinatore è più piccolo del timestamp del server (il server ha modificato
     * la tabella e il coordinatore ha una versione non aggiornata)
     */
    public Hashtable<String, String> getList(String timestamp) throws RemoteException {
    	
    	assert timestamp.length() == this.timestamp.length() : "I due timestamp hanno dimensione diversa";
    	
    	if (debug) {
    		System.out.println("Chiamata la getList da parte di un peer");
    	}
    	
    	if (timestamp.compareTo(this.timestamp) < 0) {
    		if (debug) {
        		System.out.println("Ritorno la mia tabella (aggiornata) al peer che l'ha richiesta");
        	}
    		return table;
    	}
    	else {
    		if (debug) {
        		System.out.println("Il peer ha già la tabella aggiornata percui non gli passo nulla");
        	}
    		return null;
    	}
    }

    
    
    public static void main(String[] args) {
    	System.setSecurityManager(new RMISecurityManager());
        try {
            TrackerServer obj = new TrackerServer();
            Naming.rebind("Tracker", obj);
            System.out.println("Il server è in esecuzione, digitare CTRL+C per terminarlo.");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
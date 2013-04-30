import java.io.IOException;
import java.net.InetAddress;
import java.rmi.server.*;
import java.rmi.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class TrackerServer extends UnicastRemoteObject implements Tracker {
	
	private static final long serialVersionUID = 1L;
	
	private String timestamp;
	private Hashtable<String, String> table;
	
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
		
		Vector<String> ipCoordinatori = new Vector<String>();
		
		for (int i = 0; i < risorse.capacity(); i++) {
			if (table.containsKey(risorse.elementAt(i))) {
				ipCoordinatori.add(table.get(risorse.elementAt(i)));
			}
			else {
				table.put(risorse.elementAt(i), ip);
				this.setTimestamp();
				ipCoordinatori.add(ip);
			}
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
    	
    	if (table.containsKey(risorsa)) {
    		return table.get(risorsa);
    	}
    	else {
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
    	
    	String coordinatore_corrente = this.richiesta(risorsa);
    	
    	if (coordinatore_corrente.equals(ipPrecedente)) {
    		if (this.pingUrl(coordinatore_corrente)) {
    			return coordinatore_corrente;
    		}
    		else {
    			this.eliminateCoordinatorFromTable(ipPrecedente);
    			return "";
    		}
    	}
    	else {
    		return coordinatore_corrente;
    	}
    }
    
    /*
     * Questo metodo ritorna true se pinga un certo indirizzo ip, ritorna
     * false altrimenti
     */
    private boolean pingUrl(String address) {
    	
    	boolean status = false;
    	
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
    	
    	Enumeration<String> enumKey = table.keys();
		while(enumKey.hasMoreElements()) {
		    String key = enumKey.nextElement();
		    String val = table.get(key);
		    if(val.equals(coordinator)) {
		        table.remove(key);
		        this.setTimestamp();
		    }
		}
    }
    
    /*
     * Invocato dopo l'algoritmo di elezione.
     * Questo metodo cambia/aggiunge una entry della tabella settando un nuovo
     * coordinatore per una certa risorsa
     */
    public void cambioCoordinatore(String ip, String risorsa) throws RemoteException {
    	
    	// se la chiave esiste già viene fatto un replace
    	// se la chiave non esiste, viene aggiunta
    	table.put(risorsa, ip);
    	this.setTimestamp();
    }
    
    /*
     * Invocato periodicamente dai coordinatori.
     * Questo metodo restituisce la tabella al coordinatore solo se il timestamp
     * del coordinatore è più piccolo del timestamp del server (il server ha modificato
     * la tabella e il coordinatore ha una versione non aggiornata)
     */
    public Hashtable<String, String> getList(String timestamp) throws RemoteException {
    	
    	if (timestamp.compareTo(this.timestamp) < 0) {
    		return table;
    	}
    	else {
    		return null;
    	}
    }

    
    
    public static void main(String[] args) {
    	System.setSecurityManager(new RMISecurityManager());
        try {
            TrackerServer obj = new TrackerServer();
            Naming.rebind("helloserver", obj);
            System.out.println("Il server è in esecuzione, digitare CTRL+C per terminarlo.");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
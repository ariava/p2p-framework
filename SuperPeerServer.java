import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.util.*;

public class SuperPeerServer extends PeerServer implements SuperPeer {

	private static final long serialVersionUID = 1L;
	
	private final String name = "SuperPeer";
	private String ip = null;
	private String id;
	private Peer myPS;
	
	private Hashtable<String, String> coordTable = null;

	private static boolean debug;
	
	/**
	 * Costruttore della classe SuperPeerServer.
	 * Il costruttore si occupa di inizializzare le variabili private quali
	 * tabella dei coordinatori, IP dell'host sul quale il server è in
	 * esecuzione e identificativo del SuperPeer.
	 */
	protected SuperPeerServer() throws RemoteException, UnknownHostException {
		super();
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
			if (debug)
				System.out.println("IP del SuperPeerServer: " + ip);
		} catch (UnknownHostException e) {
			System.out.println("Error while getting IP: " + e.getMessage());
			e.printStackTrace();
		}
		this.coordTable = new Hashtable<String, String>();
		this.id = name + ip;
		if (debug)
			System.out.println("Id del SuperPeerServer: " + id);
		
		String ps = "rmi://"+this.ip+"/Peer"+this.ip;
		try {
			this.myPS = (Peer)Naming.lookup(ps);
		} catch (Exception e) {
			System.out.println("Error while getting the remote object: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo usato per testare se il coordinatore effettivamente risponde
	 */
	 public boolean ping() {
		 return true;
	 }
	
	/**
	 * Metodo che restituisce il nome del server SuperPeer.
	 * 
	 * @return una stringa contenente il nome del server SuperPeer
	 */
	public String getName() throws RemoteException {
		return this.name;
	}
	
	/**
	 * Metodo accesso dell'identificativo di un server SuperPeer.
	 * 
	 * @return una stringa contenente l'identificativo del server SuperPeer.
	 * 
	 * TODO: eliminare la parte remota?
	 */
	public String getId() throws RemoteException {
		return this.id;
	}
	
	/**
	 * Metodo accessore dell'IP di un server SuperPeer.
	 * 
	 * @return una stringa contenente l'IP del server SuperPeer.
	 */
	public String getIP() throws RemoteException {
		return this.ip;
	}
	
	/**
	 * Metodo di debug che stampa la tabella dei coordinatori passata come parametro.
	 * 
	 * @param table tabella hash dei coordinatori
	 */
	private void printCoordTable(Hashtable<String, String> table) {
		assert(table != null && table.size() != 0);
		Enumeration<String> e = table.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			System.out.println("Risorsa: " + key + " | Coord: " + table.get(key));
		}
	}
	
	/**
	 * Metodo che ritorna la table dei coordinatori clonata dal tracker
	 * 
	 * @return la tabella dei coordinatori clonata dal tracker
	 */
	public Hashtable<String, String> getCoordTable() {
		return this.coordTable;
	}
	
	/**
	 * Metodo invocato da un peer per registrare una nuova risorsa nella rete peer-to-peer
	 * quando il peer invocante si trova già nella rete.
	 * 
	 * @param requestorIp indirizzo IP del peer richiedente
	 * @param resources vettore di identificatori univoci delle risorse registrate
	 * 
	 * @return un vettore di indirizzi IP dei coordinatori delle risorse registrate
	 */
	@SuppressWarnings("unchecked")
	public Vector<String> register(String requestorIp, Vector<String> resources)
			throws RemoteException {
		assert requestorIp != null : "Requestor IP is null";
		assert requestorIp != "" : "Requestor IP is empty";
		assert(resources != null && resources.size() != 0);
		Vector<String> coordinators = new Vector<String>();
		for (int i = 0 ; i < resources.size() ; i++) {
			String coord = this.coordTable.get(resources.get(i));
			System.out.println("Sono la register del SuperPeer, coord per la risorsa " + resources.get(i) + " e': " + coord);
			if (coord == null) {
				/*
				 * Se non esiste un coordinatore per la risorsa, il richiedente diventa
				 * coordinatore.
				 */
				coord = new String(requestorIp);
				coordTable.put(resources.get(i), coord);
			}
			/*
			 * Se nella PeerTable del mio peer non c'è la risorsa, la inserisco
			 * anche lì per non creare inconsistenze.
			 */
			PeerTable pt = this.myPS.getTable().get(resources.get(i));
			boolean found = false;
			for(int j=0 ; j<pt.get().size() ; ++j) {
				if(pt.get().get(j).peer.equals(requestorIp)) {
					found = true;
					break;
				}
			}
			if(!found) {
				pt.add(new PeerTableData(requestorIp, 4,
					   false, coord.equals(requestorIp) ? true : false)); // XXX Hopcount?
				this.myPS.addToTable(resources.get(i), pt); 
			}
			coordinators.add(coord);
		}
		assert(resources.size() == coordinators.size());
		if (debug) {
			System.out.println("SuperPeerServer: funzione register()");
			Common.printStringVectors(new String[]{"Risorse", "Coordinatori"}, resources, coordinators);
		}
			
		return coordinators;
	}
	
	/**
	 * Metodo invocato da un peer per richiedere una nuova risorsa quando il peer
	 * invocante si trova già nella rete. Questo metodo è eseguito la prima volta
	 * che tale risorsa è richiesta.
	 * 
	 * @param resource l'identificativo univoco della risorsa richiesta
	 * 
	 * @return una stringa contenente l'IP del coordinatore per la risorsa richiesta, una
	 * stringa vuota se non esiste
	 */
	public String request(String resource) throws RemoteException {
		assert resource != null : "Resource is null";
		assert resource != "" : "Resource field is empty";
		String coordinator = "";
		String coord = this.coordTable.get(resource);
		if (coord != null)
			coordinator = coord;
		if (debug) {
			System.out.println("SuperPeerServer - Richiesta semplice");
			System.out.println("Coordinatore " + coordinator + " trovato per la risorsa " + resource);
		}
		return coordinator;
	}
	
	/**
	 * Metodo privato per il controllo di raggiungibilità di un IP.
	 * 
	 * Il metodo ritorna vero se l'indirizzo IP passato come parametro
	 * risulta raggiungibile, falso altrimenti.
	 * 
	 * @param ip l'indirizzo IP dell'host del quale verificare la raggiungibilità
	 * 
	 * @return true se l'IP è raggiungibile, false se non lo è
	 */
	private boolean pingIP(String ip) {
		assert ip != null : "IP is null";
		assert ip != "" : "IP field is empty";
		boolean reachable = false;
		try {
			InetAddress address = InetAddress.getByName(ip);
            reachable = address.isReachable(2000);
            if (debug)
				System.out.println("SuperPeerServer - IP " + ip + ": raggiungibilità " + reachable);
        } catch (Exception e) {
            System.out.println("Error in ping: " + e.getMessage());
            e.printStackTrace();
        }
		return reachable;
	}
	
	/**
	 * Metodo invocato da un peer per richiedere una nuova risorsa quando il peer
	 * invocante si trova già nella rete. Questo metodo è eseguito la seconda volta
	 * che tale risorsa è richiesta, nel momento in cui il precedente coordinatore
	 * non ha risposto al successivo tentativo di instaurare una connessione.
	 * 
	 * @param resource l'identificativo univoco della risorsa richiesta
	 * @param lastCoord l'indirizzo IP dell'ultimo coordinatore noto per la risorsa richiesta
	 * 
	 * @return una stringa contenente l'indirizzo IP dell'attale coordinatore per la risorsa
	 * richiesta, una stringa vuota se non esiste
	 */
	public String request(String resource, String lastCoord)
			throws RemoteException {
		assert lastCoord != null : "Last coordinator is null";
		assert lastCoord != "" : "Last coordinator field is empty";
		assert(resource != null && resource != "");
		if (debug) {
			System.out.println("SuperPeerServer - Richiesta avanzata");
			System.out.println("Risorsa: " + resource + " - precedente coordinatore: " + lastCoord);
		}
		/* Se il coordinatore è cambiato, ritorniamo il nuovo */
		String new_coord = this.coordTable.get(resource);
		if (new_coord != null && lastCoord != new_coord) {
			if (debug)
				System.out.println("Nuovo coordinatore: " + new_coord);
			return new_coord;
		}
		/* Altrimenti, verifichiamo che il vecchio coordinatore sia giù */
		boolean last_coord_is_alive = this.pingIP(lastCoord);
		if (last_coord_is_alive) {
			if (debug)
				System.out.println("Il precedente coordinatore è ancora vivo ");
			return lastCoord;
		}
		if (debug)
			System.out.println("Nessun coordinatore disponibile");
		/*
		 * A questo punto del codice, il vecchio coordinatore non è più valido:
		 * rimuoviamo la entry della tabella dei coordinatori.
		 */
		this.coordTable.remove(resource);
		/* Rimuoviamo la entry anche dalla resourceTable della risorsa */
		PeerTable pt = this.myPS.getTable().get(resource);
		if (pt != null)
			pt.getCoord().coordinator = false;
		/* Ritorniamo una stringa vuota perché non esiste un coordinatore valido */
		return null;
	}
	
	/**
	 * Metodo invocato da un peer per uscire dalla rete in modo "pulito".
	 * 
	 * @param peerIp indirizzo Ip del peer invocante il metodo
	 */
	public void goodbye(String peerIp) throws RemoteException {
		assert peerIp != null : "Peer IP is null";
		assert peerIp != "" : "Peer IP field is empty";
		if (debug)
			System.out.println("SuperPeerServer: uscita pulita dell'host " + peerIp);
		Enumeration<String> e = this.myPS.getTable().keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			PeerTable pt = this.myPS.getTable().get(key);
			PeerTableData ptd = pt.getIP(peerIp);
			if (ptd != null) {
				if (debug)
					System.out.println("Rimozione dell'ip " + peerIp + " dalla tabella della risorsa " + key);
				pt.remove(ptd);
				this.myPS.addToTable(key, pt);
			}
		}
		e = coordTable.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			if (coordTable.get(key).equals(peerIp)) {
				if (debug)
					System.out.println("Rimozione dell'ip " + peerIp + " dalla tabella coordinatori per la risorsa " + key);
				coordTable.remove(key);
			}
		}			
	}
	
	/**
	 * Metodo invocato da un peer per uscire dalla rete in modo "pulito".
	 * 
	 * @param peerIp indirizzo Ip del peer invocante il metodo
	 * @param resName stringa contenente il nome della risorsa da rimuovere
	 */
	public void goodbye(String peerIp, String resName) throws RemoteException {
		assert peerIp != null : "Peer IP is null";
		assert peerIp != "" : "Peer IP field is empty";
		if (debug)
			System.out.println("SuperPeerServer: rimozione della risorsa "+resName+" dall'host " + peerIp);
		
		System.out.println(resName);
		PeerTable pt = myPS.getTable().get(resName);
		PeerTableData ptd = pt.getIP(peerIp);
		if (ptd != null) {
			if (debug)
				System.out.println("Rimozione dell'ip " + peerIp + " dalla tabella della risorsa " + resName);
			pt.remove(ptd);
			myPS.addToTable(resName, pt);
		}
		
		String coord = coordTable.get(resName);
		if (coord != null && coord.equals(peerIp)) {
			if (debug)
				System.out.println("Rimozione dell'ip " + peerIp + " dalla tabella coordinatori per la risorsa " + resName);
			coordTable.remove(resName);
		}
					
	}
	
	/**
	 * Metodo invocato da un peer quando ha appena ricevuto dal root tracker
	 * l'informazione di chi sia il coordinatore per la risorsa richiesta.
	 * 
	 * @param resourceName identificativo univoco della risorsa per la quale si
	 * effettua la richiesta dei possessori
	 * 
	 * @return una lista non vuota di indirizzi IP dei possessori della risorsa richiesta
	 */
	@SuppressWarnings("unchecked")
	public Vector<String> getList(String resourceName) throws RemoteException {
		assert resourceName != null : "Resource name is null";
		assert resourceName != "" : "Resource name field is empty";
		/* Costruzione di una lista degli IP possessori della richiesta */
		Vector<String> possessors = new Vector<String>();
		/* Ricerca nella tabella delle risorse */
		PeerTable pt = this.myPS.getTable().get(resourceName);
		if (pt != null) {
			Vector<PeerTableData> data = pt.get();
			assert(data != null);
			for(int i = 0 ; i < data.size() ; i++)
				possessors.add(data.get(i).peer);
		}
		assert(possessors.size() != 0);
		if (debug) {
			System.out.println("SuperPeerServer: funzione getList() per la risorsa " + resourceName);
			Common.printStringVectors(new String[]{"Possessori"}, possessors);
		}
		return possessors;
	}
	
	/**
	 * Metodo per il rinfresco della tabella dei coordinatori nel server.
	 * 
	 * Il metodo rinfresca completamente il contenuto della tabella dei
	 * coordinatori posseduta dal server SuperPeer.
	 * 
	 * @param table una tabella hash che associa a un identificativo di risorsa il
	 * suo coordinatore.
	 */
	public void setList(Hashtable<String, String> table) {
		/*
		 * Se esiste un coordinatore (ed esiste perché si sta invocando
		 * questo metodo), allora la tabella non può essere vuota.
		 */
		assert(table != null && table.size() != 0);
		if (debug) {
			System.out.println("SuperPeerServer: impostazione della tabella dei coordinatori");
			this.printCoordTable(table);
		}
		this.coordTable = table;
	}
	
	/**
	 * Metodo principale di funzionamento del server SuperPeer.
	 * 
	 * Il metodo registra il server nell'RMI registry in attività sulla
	 * stessa JVM. 
	 * 
	 * @param args array passato al lancio del PeerServer. Il primo elemento
     * di tale array indica se far partire il PeerServer in modalità di debug
     * (args[0] = "debug") o no (senza parametri)
	 */
	public static void main(String[] args) {
		if (args.length > 0)
			debug = args[0].equals("debug") ? true : false;
		System.setSecurityManager(new RMISecurityManager());
		try {
			SuperPeerServer server = new SuperPeerServer();
			Naming.rebind(server.getId(), server);
			if (debug)
				System.out.println("Bind del SuperPeerServer con nome " + server.getId());
		} catch (Exception e) {
			System.out.println("Error while binding server: " + e.getMessage());
			e.printStackTrace();
		}
	}

}

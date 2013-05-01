import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class SuperPeerServer extends PeerServer implements SuperPeer {

	private static final long serialVersionUID = 1L;
	private final String name = "SuperpeerServer";
	private String ip = null;
	private String id;
	
	private Hashtable<String, String> coordTable = null;
	
	/*
	 * Costruttore della classe SuperPeerServer.
	 * Il costruttore si occupa di inizializzare le variabili private quali
	 * tabella dei coordinatori, IP dell'host sul quale il server è in
	 * esecuzione e identificativo del SuperPeer.
	 * */
	protected SuperPeerServer() throws RemoteException, UnknownHostException {
		super();
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Error while getting IP: " + e.getMessage());
			e.printStackTrace();
		}
		this.coordTable = new Hashtable<String, String>();
		this.id = name + ":" + ip;
	}
	
	/*
	 * Metodo che restituisce il nome del server SuperPeer.
	 * 
	 * Valore di ritorno:
	 * una stringa contenente il nome del server SuperPeer
	 * */
	public String getName() throws RemoteException {
		return this.name;
	}
	
	/*
	 * Metodo accessore dell'identificativo di un server SuperPeer.
	 * 
	 * Valore di ritorno:
	 * una stringa contenente l'identificativo del server SuperPeer.
	 * 
	 * TODO: eliminare la parte remota?
	 * */
	public String getId() throws RemoteException {
		return this.id;
	}
	
	/*
	 * Metodo accessore dell'IP di un server SuperPeer.
	 * 
	 * Valore di ritorno:
	 * una stringa contenente l'IP del server SuperPeer.
	 * */
	public String getIP() throws RemoteException {
		return this.ip;
	}
	
	/*
	 * Metodo invocato da un peer per registrare una nuova risorsa nella rete peer-to-peer
	 * quando il peer invocante si trova già nella rete.
	 * 
	 * Parametri:
	 * requestor_ip: indirizzo IP del peer richiedente
	 * resources: vettore di identificatori univoci delle risorse registrate
	 * Valore di ritorno:
	 * un vettore di indirizzi IP dei coordinatori delle risorse registrate
	 * */
	public Vector<String> register(String requestor_ip, Vector<String> resources)
			throws RemoteException {
		Vector<String> coordinators = new Vector<String>();
		for (int i = 0 ; i < resources.size() ; i++) {
			String coord = this.coordTable.get(resources.get(i));
			if (coord == null) {
				/*
				 * Se non esiste un coordinatore per la risorsa, il richiedente diventa
				 * coordinatore.
				 */
				coord = new String(requestor_ip);
				coordTable.put(resources.get(i), coord);
			}
			coordinators.add(coord);
		}
		assert(resources.size() == coordinators.size());
		return coordinators;
	}
	
	/*
	 * Metodo invocato da un peer per richiedere una nuova risorsa quando il peer
	 * invocante si trova già nella rete. Questo metodo è eseguito la prima volta
	 * che tale risorsa è richiesta.
	 * 
	 * Parametri:
	 * resource: l'identificativo univoco della risorsa richiesta
	 * Valore di ritorno:
	 * una stringa contenente l'IP del coordinatore per la risorsa richiesta, una
	 * stringa vuota se non esiste
	 * */
	public String request(String resource) throws RemoteException {
		String coordinator = "";
		String coord = this.coordTable.get(resource);
		if (coord != null)
			coordinator = coord;
		return coordinator;
	}
	
	/*
	 * Metodo privato per il controllo di raggiungibilità di un IP.
	 * 
	 * Il metodo ritorna vero se l'indirizzo IP passato come parametro
	 * risulta raggiungibile, falso altrimenti.
	 * 
	 * Parametri:
	 * ip: l'indirizzo IP dell'host del quale verificare la raggiungibilità
	 * Valore di ritorno:
	 * true se l'IP è raggiungibile, false se non lo è
	 * */
	private boolean pingIP(String ip) {
		boolean reachable = false;
		try {
			InetAddress address = InetAddress.getByName(ip);
            reachable = address.isReachable(2000);
        } catch (Exception e) {
            System.out.println("Error in ping: " + e.getMessage());
            e.printStackTrace();
        }
		return reachable;
	}
	
	/*
	 * Metodo invocato da un peer per richiedere una nuova risorsa quando il peer
	 * invocante si trova già nella rete. Questo metodo è eseguito la seconda volta
	 * che tale risorsa è richiesta, nel momento in cui il precedente coordinatore
	 * non ha risposto al successivo tentativo di instaurare una connessione.
	 * 
	 * Parametri:
	 * resource: l'identificativo univoco della risorsa richiesta
	 * last_coord: l'indirizzo IP dell'ultimo coordinatore noto per la risorsa richiesta
	 * Valore di ritorno:
	 * una stringa contenente l'indirizzo IP dell'attale coordinatore per la risorsa
	 * richiesta, una stringa vuota se non esiste
	 * */
	public String request(String resource, String last_coord)
			throws RemoteException {
		assert(last_coord != null && last_coord != "");
		assert(resource != null && resource != "");
		/* Se il coordinatore è cambiato, ritorniamo il nuovo */
		String new_coord = this.coordTable.get(resource);
		if (new_coord != null && last_coord != new_coord)
			return new_coord;
		/* Altrimenti, verifichiamo che il vecchio coordinatore sia giù */
		boolean last_coord_is_alive = this.pingIP(last_coord);
		if (last_coord_is_alive)
			return last_coord;
		/*
		 * A questo punto del codice, il vecchio coordinatore non è più valido:
		 * rimuoviamo la entry della tabella dei coordinatori.
		 */
		this.coordTable.remove(resource);
		/* Rimuoviamo la entry anche dalla resourceTable della risorsa */
		PeerTable pt = this.resourceTable.get(resource);
		if (pt != null)
			pt.getCoord().coordinator = false;
		/* Ritorniamo una stringa vuota perché non esiste un coordinatore valido */
		return "";
	}
	
	/*
	 * Metodo invocato da un peer per uscire dalla rete in modo "pulito".
	 * 
	 * Parametri:
	 * peer_ip: indirizzo Ip del peer invocante il metodo
	 * */
	public void goodbye(String peer_ip) throws RemoteException {
		assert(peer_ip != null && peer_ip != "");
		Enumeration<String> e = resourceTable.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			PeerTable pt = resourceTable.get(key);
			PeerTableData ptd = pt.getIP(peer_ip);
			if (ptd != null) {
				pt.remove(ptd);
				resourceTable.put(key, pt);
			}
		}
		e = coordTable.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			if (coordTable.get(key) == peer_ip)
				coordTable.remove(key);
		}			
	}
	
	/*
	 * Metodo invocato da un peer quando ha appena ricevuto dal root tracker
	 * l'informazione di chi sia il coordinatore per la risorsa richiesta.
	 * 
	 * Parametri:
	 * resource_name: identificativo univoco della risorsa per la quale si
	 *                effettua la richiesta dei possessori
	 * Valore di ritorno:
	 * una lista di indirizzi IP dei possessori della risorsa richiesta
	 * */
	public Vector<String> getList(String resource_name) throws RemoteException {
		PeerTable pt = resourceTable.get(resource_name);
		if (pt == null)
			return null;
		Vector<PeerTableData> data = pt.get();
		if (data.size() == 0)
			return null;
		/* Costruzione di una lista degli IP possessori della richiesta */
		Vector<String> possessors = new Vector<String>();
		for(int i = 0 ; i < data.size() ; i++)
			possessors.add(data.get(i).peer);
		return possessors;
	}
	
	/*
	 * Metodo per il rinfresco della tabella dei coordinatori nel server.
	 * 
	 * Il metodo rinfresca completamente il contenuto della tabella dei
	 * coordinatori posseduta dal server SuperPeer.
	 * 
	 * Parametri:
	 * una tabella hash che associa a un identificativo di risorsa il
	 * suo coordinatore.
	 * */
	public void setList(Hashtable<String, String> table) {
		this.coordTable = table;
	}
	
	/*
	 * Metodo principale di funzionamento del server SuperPeer.
	 * 
	 * Il metodo registra il server nell'RMI registry in attività sulla
	 * stessa JVM. 
	 * */
	public static void main(String[] args) {
		System.setSecurityManager(new RMISecurityManager());
		
		try {
			SuperPeerServer server = new SuperPeerServer();
			Naming.rebind(server.getName(), server);
		} catch (Exception e) {
			System.out.println("Error while binding server: " + e.getMessage());
			e.printStackTrace();
		}
	}
}

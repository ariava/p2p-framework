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
	
	public String getName() throws RemoteException {
		return this.name;
	}
	
	// TODO: eliminare la parte remota?
	public String getId() throws RemoteException {
		return this.id;
	}
	
	public String getIP() throws RemoteException {
		return this.ip;
	}
	
	/*
	 * Metodo invocato da un peer per registrare una nuova risorsa nella rete peer-to-peer
	 * quando il peer invocante si trova già nella rete.
	 */
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
	 */
	public String request(String resource) throws RemoteException {
		String coordinator = "";
		String coord = this.coordTable.get(resource);
		if (coord != null)
			coordinator = coord;
		return coordinator;
	}
	
	/*
	 * Il metodo ritorna vero se l'indirizzo IP passato come parametro
	 * risulta raggiungibile, falso altrimenti.
	 */
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
	 */
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
	 */
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
	 */
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
	
	public void setList(Hashtable<String, String> table) {
		this.coordTable = table;
	}
	
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

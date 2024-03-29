import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * La classe PeerServer è una classe remota che contiene la tabella
 * delle risorse possedute da un certo Peer. Il compito di questa
 * classe è fornire tutte le funzionalità necessarie al relativo
 * PeerClient per gestire il funzionamento dell'architettura peer-to-peer
 * sviluppata.
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
public class PeerServer extends UnicastRemoteObject implements Peer {

	private static final long serialVersionUID = 1L;

	private float avgDist;
	private String myIp;
	static private boolean debug = true;
	
	protected Hashtable<String, PeerTable> resourceTable;
	
	
	/**
	 * Costruttore della classe PeerServer.
	 * 
	 * Inizializza la distanza media a 0 e l'ip all'ip corrente della
	 * macchina. Crea una hashtable vuota.
	 */
	public PeerServer() throws RemoteException, UnknownHostException {
		super();
		this.avgDist = 0;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		resourceTable = new Hashtable<String, PeerTable>();
	}
	
	/**
	 * Metodo per sincronizzare la tabella del client con quella del
	 * rispettivo server.
	 * 
	 * @param rt l'oggetto di tipo Hashtable da sostituire con la propria tabella
	 *  
	 *  XXX: problema di sicurezza, non ci sono controlli sul fatto che questo
	 *  metodo possa essere chiamato solo ed esclusivamente dal SUO client.
	 */
	public void syncTable(Hashtable<String, PeerTable> rt) {
		if(debug) {
			System.out.println("chiamata la sync");
		}
		this.resourceTable = rt;
	}
	
	/**
	 * Metodo che rimuove completamente la tabella della risorsa data
	 * 
	 * @param resName il nome della risorsa da eliminare
	 */
	public void removeFromTable(String resName) {
		
		this.resourceTable.remove(resName);
	}
	
	
	/**
	 * Getter di avgDist
	 * 
	 * @return la distanza media
	 */
	public float getAvgDist() {
		return this.avgDist;
	}
	
	/**
	 * Goodbye sul peer, rimuove il chiamante dalla propria tabella
	 * 
	 * @param resName risorsa per cui il chiamante lascia
	 * @param ip indirizzo ip del chiamante
	 */
	public void goodbye(String resName, String ip) throws RemoteException {
		PeerTable pt = this.resourceTable.get(resName);
		
		if (debug)
			System.out.println("Sto rimuovendo la risorsa " + resName + " con ip " + ip);
		pt.remove(pt.getIP(ip));
		this.resourceTable.put(resName, pt);
	}
	
	/**
	 * Setter di avgDist
	 * 
	 * @param avg la distanza media
	 */
	public void setAvgDist(float avg) {
		this.avgDist = avg;
	}
	
	/**
	 * Metodo che dato l'indirizzo ip del chiamante ritorna la distanza in termini
	 * di hopcount dalla macchina corrente.
	 * 
	 * @param ip stringa contenente l'ip del chiamante
	 * 
	 * @return la distanza in termini di hopcount dalla macchina corrente
	 */
	public float discovery(String ip) throws RemoteException {
		if(debug) {
			System.out.println("Chiamata la discovery() dal peer con ip "+ip);
		}
		//TODO: hopcount..
		return 4;		
	}
	
	/**
	 * Metodo che ritorna un array di byte che contiene la risorsa richiesta.
	 * 
	 * @param resName stringa contenente il nome della risorsa richiesta
	 * @param ip indirizzo ip del chiamante, usato  per aggiungerlo alla tabella dopo
	 * 
	 * @return un array di byte contenente la risorsa richiesta (resName)
	 */
	public byte[] getResource(String resName,String ip) throws RemoteException {

		if(debug) 
			System.out.println("Chiamata la getResource() dal peer "+ip+" per la risorsa "+resName);
		
		try {
			File file = new File("resources/"+resName);
	
			byte buffer[] = new byte[(int)file.length()];
	
			BufferedInputStream input = new	BufferedInputStream(new FileInputStream("resources/"+resName));
			input.read(buffer,0,buffer.length);
			input.close();
			
			assert buffer.length > 0 : "Qualcosa e' andato storto nella lettura del file ma non e' stata sollevata alcuna eccezione";
			this.addNewPeer(resName, ip);
			return buffer;
		} catch(Exception e) {
			if (debug) {
				System.out.println("Qualcosa e' andato storto nella lettura del file: "+e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Metodo per aggiungere un peer alla tabella dei possessori della risorsa.
	 * Ora il metodo e' generico ed e' invocato anche in fase di prelievo 
	 * di una risorsa per notificare agli altri peer nella zona il nuovo
	 * arrivo
	 * 
	 * @param resName stringa contenente il nome della risorsa da aggiungere
	 * @param ip indirizzo ip del peer da aggiungere
	 */
	@SuppressWarnings("unchecked")
	public void addNewPeer(String resName, String ip) throws RemoteException {
		Peer p = null;
		try {
			p = (Peer)Naming.lookup("rmi://"+ip+"/Peer"+ip);
		} catch (Exception e) {
			if (debug) {
				System.out.println("Errore nel recupero dell'oggetto remoto: "+e.getMessage());
				e.printStackTrace();	
			}
		}
		
		PeerTable pt = this.resourceTable.get(resName);
		try {
			pt.add(new PeerTableData(ip, p.discovery(this.myIp),false,false ));
		} catch (RemoteException e) {
			if (debug) {
				System.out.println("Fallito il metodo discovery() sul Peer "+e.getMessage());
				e.printStackTrace();
			}
		}
		this.resourceTable.put(resName, pt);
		if (debug) {
			System.out.println("Tabella ora:");
			this.resourceTable.get(resName).print();
		}
		//ricalcolo la distanza media
		this.avgDist = this.resourceTable.get(resName).getAvgDist(this.myIp);
		if (debug) {
			System.out.println("La nuova distanza media calcolata e': "+this.avgDist);
			Vector<String >poss = new Vector<String>();
			for (int i=0 ; i<pt.get().size() ; ++i)
				poss.add(pt.get().get(i).peer);
			Common.printStringVectors(new String[]{"Possessori"}, poss);
		}
	}
	
	/**
	 * Metodo invocato da un altro peer una volta avviata la procedura di
	 * elezione per ricevere i vari id (distanze medie).
	 * 
	 * @param res risorsa per la quale si avvia l'election
	 * @param ipCaller indirizzo ip del chiamante
	 * 
	 * @return distanza media
	 */
	public float election(String res, String ipCaller) throws RemoteException {
		if(debug)
			System.out.println("Chiamata la election() per la risorsa "+res);
		assert this.avgDist > 0 || ipCaller.equals(this.myIp): "Chiamata la election() ma avgDist non e' un numero valido!";
		return this.avgDist;
	}
	
	/**
	 * Metodo invocato da un altro peer per annunciare il nuovo coordinatore per
	 * una risorsa.
	 * 
	 * @param newCoord stringa contenente l'ip del nuovo coordinatore
	 * @param res stringa contenente la risorsa per cui e' stato eletto il nuovo coordinatore
	 */
	public void coordinator(String newCoord, String res) throws RemoteException {
		if(debug)
			System.out.println("Chiamata la coordinator() per la risorsa"+res+", il nuovo coordinatore e' "+newCoord);
		
		boolean elected = false;
		PeerTable pt = this.resourceTable.get(res);
		
		for(int i=0 ; i<pt.get().size() ; ++i) {
			pt.get().get(i).coordinator = false;
			if(pt.get().get(i).peer.equals(newCoord)) {
				if (debug)
					System.out.println("Trovato il nuovo coordinatore della risorsa " + res + " nella PeerTable");
				pt.get().get(i).coordinator = true;
				elected = true;
			}
			
		}

		this.resourceTable.put(res, pt);
		if (debug) {
			System.out.println("Stampa tabella:");
			this.resourceTable.get(res).print();
		}
		assert elected == true : "Nessun coordinatore settato!";
	}
	
	/**
	 * Getter della resourceTable
	 * 
	 * @return la tabella delle risorse
	 */
	public Hashtable<String, PeerTable> getTable() {		
		return this.resourceTable;		
	}
	
	/**
	 * Metodo che aggiunge una entry alla resourceTable
	 * 
	 * @param resName il nome della risorsa
	 * @param pt la peerTable da aggiungere a quella risorsa
	 */
	public void addToTable(String resName, PeerTable pt) {
		this.resourceTable.put(resName, pt);
	}
	
	/**
	 * Metodo che aggiunge un nuovo Peer nella PeerTable
	 * 
	 * @param ip indirizzo ip del nuovo Peer
	 * @param resName il nome della risorsa posseduto dal nuovo Peer
	 */
	public void newPeer(String ip, String resName) {
		PeerTable pt = this.resourceTable.get(resName);
		pt.add(new PeerTableData(ip, 4, false, false));
		
		this.resourceTable.put(resName, pt);
	}

	/**
	 * Metodo invocato per accertare la raggiungibilità del @PeerServer
	 * 
	 * @return il valore booleano "true"
	 */
	public boolean ping() {
		return true;
	}
	
	/**
	 * Il main inizializza il PeerServer e lo fa eseguire
	 * 
	 * @param args array passato al lancio del PeerServer. Il primo elemento
     * di tale array indica se far partire il PeerServer in modalità di debug
     * (args[0] = "debug") o no (senza parametri)
	 */
	public static void main(String[] args) {
		debug = args.length > 0 && args[0].equals("debug") ? true : false;
		
		if(debug)
			System.out.println("Avviato il PeerServer");
		
		System.setSecurityManager(new RMISecurityManager());
		
		try {
			PeerServer obj = new PeerServer();
			Naming.rebind("Peer"+InetAddress.getLocalHost().getHostAddress(), obj);
			if(debug) {
				System.out.println("Effettuato il binding con nome: Peer"+InetAddress.getLocalHost().getHostAddress());
			}
		}
		catch (Exception e) {
			if (debug) {
				System.out.println("non va: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}

}

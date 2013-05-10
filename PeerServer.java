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


public class PeerServer extends UnicastRemoteObject implements Peer {

	private static final long serialVersionUID = 1L;

	private float avgDist;
	private String myIp;
	static private boolean debug = false;
	
	protected Hashtable<String, PeerTable> resourceTable; //TODO: sincronizzare con quella del client..
	
	
	/*
	 * Costruttore della classe PeerServer.
	 * 
	 * Inizializza la distanza media a -1 e l'ip all'ip corrente della macchina. Crea una hashtable vuota.
	 * */
	public PeerServer() throws RemoteException, UnknownHostException {
		
		super();
		this.avgDist = -1;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		resourceTable = new Hashtable<String, PeerTable>();
		
	}
	
	/*
	 * Metodo orribile per sincronizzare la tabella del client con quella del rispettivo server
	 * 
	 *  Parametri:
	 *  rt: l'oggetto di tipo Hashtable da sostituire con la propria tabella
	 *  
	 *  XXX: problema di sicurezza, non ci sono controlli sul fatto che questo metodo possa essere
	 *  chiamato solo ed esclusivamente dal SUO client.
	 * */
	public void syncTable(Hashtable<String, PeerTable> rt) {
		if(debug) {
			System.out.println("chiamata la sync");
		}
		this.resourceTable = rt;
		
	}
	
	/*
	 * Metodo che dato l'indirizzo ip del chiamante ritorna la distanza in termini di hopcount dalla macchina corrente.
	 * 
	 * Parametri: 
	 * ip: stringa contenente l'ip del chiamante
	 * */
	public float discovery(String ip) throws RemoteException {
		
		if(debug) {
			System.out.println("Chiamata la discovery() dal peer con ip "+ip);
		}
		//TODO: hopcount..
		return 4;
		
	}
	
	/*
	 * Metodo che ritorna un array di byte che contiene la risorsa richiesta.
	 * 
	 * Parametri: 
	 * resName: stringa contenente il nome della risorsa richiesta
	 * ip: indirizzo ip del chiamante, usato  per aggiungerlo alla tabella dopo
	 * */
	public byte[] getResource(String resName,String ip) throws RemoteException {

		if(debug) {
			System.out.println("Chiamata la getResource() per la risorsa "+resName);
		}
		
		try {

			File file = new File("resources/"+resName);
	
			byte buffer[] = new byte[(int)file.length()];
	
			BufferedInputStream input = new	BufferedInputStream(new FileInputStream("resources/"+resName));
	
			input.read(buffer,0,buffer.length);
	
			input.close();
			assert buffer.length > 0 : "Something went wrong while reading file, but no exception were rised..";
			this.addNewPeer(resName, ip);
			return(buffer);

		} catch(Exception e){

			System.out.println("Something went wrong while reading file: "+e.getMessage());
	
			e.printStackTrace();
	
			return(null);

		}
	}
	
	/*
	 * blablabla TODO:commenta
	 * */
	private static void printStringVectors(String[] labels, Vector<String>... vecs) {
		int labelnum = 0;
		for (Vector<String> vec : vecs) {
			System.out.print("[");
			System.out.print(labels[labelnum] + ": ");
			for (int i = 0 ; i < vec.size() ; i++) {
				System.out.print(vec.get(i));
				if (i != vec.size()-1)
					System.out.print(", ");
			}
			System.out.println("]");
			labelnum++;
		}
	}
	/*
	 * Metodo (privato) per aggiungere un peer alla tabella dei possessori della risorsa dopo che gli e' stata data
	 * */
	@SuppressWarnings("unchecked")
	private void addNewPeer(String resName, String ip) {
		Peer p = null;
		try {
			p = (Peer)Naming.lookup("rmi://"+ip+"/Peer"+ip);
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			e.printStackTrace();			
		}
		
		PeerTable pt = this.resourceTable.get(resName);
		try {
			pt.add(new PeerTableData(ip, p.discovery(this.myIp),false,false ));
		} catch (RemoteException e) {
			System.out.println("Discovery failed.."+e.getMessage());
			e.printStackTrace();
		}
		this.resourceTable.put(resName, pt);
		
		//ricalcolo avgdist
		this.avgDist = this.resourceTable.get(resName).getAvgDist();
		
		Vector<String >poss = new Vector<String>();
		for(int i=0;i<pt.get().size();++i) {
			poss.add( pt.get().get(i).peer);
		}
		printStringVectors(new String[]{"Possessori"}, poss);
		
	}
	
	/*
	 * Metodo invocato da un altro peer una volta avviata la procedura di elezione per ricevere i vari id (distanze medie)
	 * 
	 * res: risorsa per la quale si avvia l'election
	 * */
	public float election(String res) throws RemoteException {
		
		if(debug) {
			System.out.println("Chiamata la election() per la risorsa "+res);
		}
		
		assert this.avgDist > 0 : "Called election but avgDist is not a valid number!";
		return this.avgDist;
		
	}
	
	/*
	 * Metodo invocato da un altro peer per annunciare il nuovo coordinatore per una risorsa.
	 * 
	 * Parametri:
	 * newCoord: stringa contenente l'ip del nuovo coordinatore
	 * res: stringa contenente la risorsa per cui e' stato eletto il nuovo coordinatore
	 * */
	public void coordinator(String newCoord, String res) throws RemoteException {
		if(debug) {
			System.out.println("Chiamata la coordinator() per la risorsa"+res+", il nuovo coordinatore e' "+newCoord);
		}
		
		boolean elected = false;
		PeerTable pt = this.resourceTable.get(res);
		
		for(int i=0;i<pt.get().size();++i) {
			pt.get().get(i).coordinator = false;
			if(pt.get().get(i).peer.equals(newCoord)) {
				pt.get().get(i).coordinator = true;
				elected = true;
			}
			
		}
		assert elected == true : "No coordinator set!";
		this.resourceTable.put(res, pt);
	}
	
	/*
	 * Getter della resourceTable
	 * */
	public Hashtable<String, PeerTable> getTable() {		
		return this.resourceTable;		
	}
	
	/*
	 * Metodo che aggiunge una entry alla resourceTable
	 * 
	 * Parametri:
	 * resName: il nome della risorsa
	 * pt: la peerTable da aggiungere a quella risorsa
	 * */
	public void addToTable(String resName, PeerTable pt) {
		
		this.resourceTable.put(resName, pt);
		
	}

	
	public static void main(String[] args) {
		
		if(args[0].equals("debug"))
			debug = true;
		
		if(debug) {
			System.out.println("Avviato il PeerServer");
		}
		
		System.setSecurityManager(new RMISecurityManager());
		
		try {
			PeerServer obj = new PeerServer();
			Naming.rebind("Peer"+InetAddress.getLocalHost().getHostAddress(), obj);
			if(debug) {
				System.out.println("Effettuato il binding con nome: Peer"+InetAddress.getLocalHost().getHostAddress());
			}
		}
		catch (Exception e) {
			System.out.println("non va: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
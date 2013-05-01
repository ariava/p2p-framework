import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;


public class PeerClient {

	static private PeerClient self = null;
	private String myIp;
	private float avgDist;
	private Hashtable<String, PeerTable> resourceTable;
	
	public PeerClient() throws UnknownHostException {
		this.self = this;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		this.avgDist = -1;
		this.resourceTable = new Hashtable<String, PeerTable>();
		
	}
	
	/*
	 * Metodi chiamati sul tracker
	 * */
	

	/*
	 * Metodo privato utilizzato per chiamare il metodo registrazione del tracker, utilizzato per gestire le eventuali
	 * eccezioni.
	 * Parametri:
	 * server: oggetto di tipo tracker sul quale chiamare il metodo
	 * resources: Vector<String> contenente i nomi delle risorse da registrare
	 * */
	private Vector<String> registerResources(Tracker server, Vector<String> resources) {
		assert server != null : "Tracker object is undefined!";
		try {	
			return server.registrazione(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			return null;
		}
	}
	
	/*
	 * Metodo privato utilizzato per chiamare il metodo registrazione del SuperPeer, utilizzato per gestire le eventuali
	 * eccezioni.
	 * Parametri:
	 * server: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resources: Vector<String> contenente i nomi delle risorse da registrare
	 * */
	private Vector<String> registerResources(SuperPeer server, Vector<String> resources) {
		assert server != null : "SuperPeer object is undefined!";
		try {	
			return server.register(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			return null;
		}		
	}
	
	/*
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta del tracker. Gestisce la richiesta semplice
	 * 
	 * Parametri:
	 * server: oggetto di tipo tracker
	 * resource: il nome della risorsa richiesta
	 * */
	private String simpleResourceRequest(Tracker server, String resource) {
		assert server != null : "Tracker object is undefined!";
		try {
			return server.richiesta(resource);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}	
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata al metodo richiesta del tracker. Effettua una richiesta avanzata
	 * Parametri:
	 * server: oggetto Tracker sul quale chiamare i metodi
	 * resource: stringa contenente il nome della risorsa
	 * prevCoord: stringa contenente il nome del coordinatore ottenuto in precedenza
	 * */
	private String advancedResourceRequest(Tracker server, String resource, String prevCoord) {
		assert server != null : "Tracker object is undefined!";
		try {
			return server.richiesta(resource, prevCoord);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}
	}
	
	/*
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta del SuperPeer. Gestisce la richiesta semplice
	 * 
	 * Parametri:
	 * server: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resource: il nome della risorsa richiesta
	 * */
	private String simpleResourceRequest(SuperPeer server, String resource) {
		assert server != null : "SuperPeer object is undefined!";
		try {
			return server.request(resource);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata al metodo richiesta del tracker. Effettua una richiesta avanzata
	 * Parametri:
	 * server: oggetto SuperPeer sul quale chiamare i metodi
	 * resource: stringa contenente il nome della risorsa
	 * prevCoord: stringa contenente il nome del coordinatore ottenuto in precedenza
	 * */
	private String advancedResourceRequest(SuperPeer server, String resource, String prevCoord) {
		assert server != null : "SuperPeer object is undefined!";
		try {
			return server.request(resource, prevCoord);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}		
	}
	
	/*
	 * Metodi sul coordinatore
	 * */
	
	/*
	 * Metodo per gestire le eccezioni della chiamata del metodo goodbye sul coordinatore
	 * Parametri:
	 * coord: oggetto di tipo SuperPeer sul quale effettuare la chiamata
	 * */
	private void goodbye(SuperPeer coord) {
		assert coord != null : "SuperPeer object is undefined!";
		try {
			coord.goodbye(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while exiting politely: "+e.getMessage());
		}
		
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata di getList sul coordinatore.
	 * 
	 * Parametri:
	 * coord: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resName: stringa contenente il nome della risorsa
	 * */
	private Vector<String> getList(SuperPeer coord, String resName) {
		assert coord != null : "SuperPeer object is undefined!";
		try {
			return coord.getList(resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving the zone list");
			return null;
		}
		
	}
	
	/*
	 * Metodi su altri peer 
	 * */
	
	/*
	 * Metodo per gestire le eccezioni della chiamata a discovery di un altro peer.
	 * Parametri:
	 * p: oggetto di tipo Peer sul quale effettuare la chiamata
	 * */
	private float discovery(Peer p) {
		assert p != null : "Peer object is undefined!";
		try {
			return p.discovery(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving distances: "+e.getMessage());
			return -1;
		}
		
	}
	
	/*
	 * Metodo per recuperare una risorsa da un altro peer.
	 * 
	 * Effettua la chiamata al metodo getResource del peer in questione e scrive su file lo stream di byte ricevuto
	 * Parametri:
	 * p: oggetto di tipo Peer sul quale chiamare la getResource
	 * resName: stringa contenente il nome della risorsa
	 * */
	private boolean getResource(Peer p, String resName) {
		assert p != null : "Peer object is undefined!";
		
		byte[] filedata;
		try {
			filedata = p.getResource(resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving the data from the peer: "+e.getMessage());
			return false;
		}
		if(filedata == null) {
			System.out.println("Something went wrong while retrieving the data from the peer and he handled the exception");
			return false;
		}
		File file = new File(resName);
		try {
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file.getName()));
	
			output.write(filedata,0,filedata.length);
	
			output.flush();
	
			output.close();
		}
		catch (Exception e) {
			
			System.out.println("Something went wrong while writing the retrieved file: "+e.getMessage());
			return false;
		}
		return true;
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata a election sul peer remoto.
	 * 
	 * Parametri:
	 * p: oggetto di tipo Peer sul quale effettuare la chiamata
	 * resName: stringa contenente il nome della risorsa
	 * */
	private float election(Peer p, String resName) {
		assert p != null : "Peer object is undefined!";
		
		try {
			return p.election(resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while polling for election candidates: "+e.getMessage());
			return -1;
		}
	}
	
	/*
	 * Metodo usato per gestire le eccezioni della chiamata al coordinator di un altro peer.
	 * 
	 * Parametri: 
	 * p: oggetto di tipo Peer sul quale effettuare la chiamata
	 * resName: stringa contenente il nome della risorsa
	 * ipCoord: stringa contenente l'indirizzo ip del coordinatore da impostare
	 * */
	private void coordinator(Peer p, String resName, String ipCoord) {
		assert p != null : "Peer object is undefined!";
		
		try {
			p.coordinator(ipCoord, resName);
		}
		catch (Exception e) {
			System.out.println("Non va");
		}
		
	}
	
	/*
	 * Metodi privati di gestione del ciclo di vita di un peer
	 * */
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un tracker ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del tracker.
	 * */
	private Tracker getTracker(String server) {
		
		try {
			Tracker obj = (Tracker)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			return null;
			
		}
	}
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un SuperPeer ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del SuperPeer.
	 * */
	private SuperPeer getCoord(String server) {
		
		try {
			SuperPeer obj = (SuperPeer)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			return null;
			
		}
	}
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un Peer ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del Peer.
	 * */
	private Peer getPeer(String server) {
		
		try {
			PeerServer obj = (PeerServer)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			return null;
			
		}
	}
	
	/*
	 * Metodo chiamato dal peer per avviare la procedura di elezione.
	 * 
	 * Chiama la election su ogni altro peer ricevendo le loro distanze medie ed elegge come coordinatore il peer
	 * con la distanza media piu' bassa.
	 * 
	 * Parametri:
	 * resName: stringa contenente il nome della risorsa per cui e' necessario eleggere un nuovo coordinatore.
	 * */
	private void startElection(String resName) {
		
		float answers[] = new float[this.resourceTable.get(resName).get().size()];
		String peers[] = new String[this.resourceTable.get(resName).get().size()];
		//per ogni peer nella lista chiama la election su di loro per ottenere le loro avgDist
		for(int i=0;i<this.resourceTable.get(resName).get().size();++i) {
			String server = this.resourceTable.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"PeerServer";
			Peer p = this.getPeer(server);
			
			assert p != null : "Peer object is undefined!";
			
			try {
				answers[i] = p.election(resName);
			} catch (RemoteException e) {
				System.out.println("Exception in election procedure: " + e.getMessage());
				e.printStackTrace();
			}
			peers[i] = this.resourceTable.get(resName).get().get(i).peer;
		}
		
		//tra tutto quello che ho ricevuto trovo quello col minimo (considerando anche me stesso)
		float min = this.avgDist;
		String peerMin = this.myIp;
		for(int i=0;i<answers.length;++i) {
			if(answers[i] < min) { 
				min = answers[i];
				peerMin = peers[i];
			}
		}
		
		//peerMin ora sara' il nuovo coordinatore per la risorsa resName
		for(int i=0;i<this.resourceTable.get(resName).get().size();++i) {
			String server = this.resourceTable.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"PeerServer";
			Peer p = this.getPeer(server);
			
			assert p != null : "Peer object is undefined!";
			
			try {
				p.coordinator(peerMin, resName);
			} catch (RemoteException e) {
				System.out.println("Exception while announcing coordinator: " + e.getMessage());
				e.printStackTrace();
			}
			//TODO: il nuovo coordinatore deve notificarlo al tracker poi..arianna?
		}
	}
	
	public static void main(String args[]) {
		
		String tracker = args[0];
		String mode = args[1];
		
		String server = "rmi://"+tracker+"/"+"ServerImpl";
		
		Tracker tr = self.getTracker(server);
		
		assert tr != null : "Tracker object is undefined!";
		
		if(mode.equals("register")) {
			Vector<String> resNames = new Vector<String>();
			resNames.add("prova.txt");
			
			//register new resources
			Vector<String> coords = self.registerResources(tr, resNames);
			
			assert coords.size() == resNames.size() : "coords and resNames size doesn't match!";
			
			//add coordinators in the hashtable
			for(int i=0;i<coords.size();++i) {
				self.resourceTable.put(resNames.get(i), new PeerTable(new PeerTableData(coords.get(i),-1,false,true)));
				if(coords.get(i) != self.myIp) {
					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeerServer";
					SuperPeer c = self.getCoord(coord);
					
					assert c != null : "SuperPeer object is undefined!";
					
					self.registerResources(c, resNames);
				}
					
			}
		} else {
			//request resource prova.txt
			String resName = "prova.txt";
			String prevC = self.simpleResourceRequest(tr, resName);
			String coord = "rmi://"+prevC+"/"+"SuperPeerServer";
			
			SuperPeer c = self.getCoord(coord);
			
			assert c != null : "SuperPeer object is undefined!";
			
			Vector<String> ipList = self.getList(c, resName);
			while(ipList == null) {
				System.out.println("Coordinator isn't responding..");
				try {
					Thread.sleep(5000);	//TODO: trovare un tempo di sleep realistico
				} catch (InterruptedException e) {
					System.out.println("Exception while sleeping: " + e.getMessage());
					e.printStackTrace();
				}
				
				prevC = self.advancedResourceRequest(tr, resName, prevC);
				coord = "rmi://"+prevC+"/"+"SuperPeerServer";				
				SuperPeer c1 = self.getCoord(coord);
				
				assert c1 != null : "SuperPeer object is undefined!"; //FIXME: ha senso questa assert? come gestisce rmi il non
																	   //		 rispondere..? fa ritornare un null?
				
				ipList = self.getList(c1, resName);
			}
			
			PeerTable pt = new PeerTable();
			for(int i=0;i<ipList.size();++i) {
				
				String peer = ipList.get(i);
				peer = "rmi://"+peer+"/"+"PeerServer";
				Peer p = self.getPeer(peer);
				
				assert p != null : "Peer object is undefined!";
				
				PeerTableData pd = new PeerTableData(ipList.get(i),self.discovery(p),false,ipList.get(i)==prevC?true:false);
				pt.add(pd);
			}
			self.resourceTable.put(resName, pt);
			
			pt = self.resourceTable.get(resName);
			
			self.avgDist = pt.getAvgDist();
			
			String closestPeer = pt.getMinDistPeer();
			closestPeer = "rmi://"+closestPeer+"/"+"PeerServer";
			Peer p = self.getPeer(closestPeer);
			
			assert p != null : "Peer object is undefined!";
			
			//richiedi la risorsa..
			if(self.getResource(p, resName))
				System.out.println("tutto e' andato a buon fine, yeah!");
			else
				System.out.println("Trasferimento della risorsa fallito..");
			
		}
		
		
	}

}

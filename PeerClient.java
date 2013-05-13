import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

public class PeerClient {

	static public PeerClient self = null;
	public Peer myPS = null;
	public String myIp;
	static public boolean debug = true;
	public String trackerIp = null;
	
	
	public PeerClient() throws UnknownHostException {
		self = this;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
			
		String ps = "rmi://"+this.myIp+"/"+"Peer"+this.myIp;
		this.myPS = self.getPeer(ps);
	}
	
	public PeerClient(String tr) throws UnknownHostException {
		self = this;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		
		String ps = "rmi://"+this.myIp+"/"+"Peer"+this.myIp;
		this.myPS = self.getPeer(ps);
		this.trackerIp = tr;
	}
	
	/*
	 * Metodi chiamati sul tracker
	 * */
	

	/*
	 * Metodo privato utilizzato per chiamare il metodo registrazione del tracker,
	 * utilizzato per gestire le eventuali eccezioni.
	 * Parametri:
	 * server: oggetto di tipo tracker sul quale chiamare il metodo
	 * resources: Vector<String> contenente i nomi delle risorse da registrare
	 * */
	public Vector<String> registerResources(Tracker server, Vector<String> resources) {
		assert server != null : "Tracker object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la registrazione() sul tracker: "+server.toString()+" per queste risorse:");
			System.out.println(resources.toString());
		}
		
		try {	
			return server.registrazione(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Metodo privato utilizzato per chiamare il metodo registrazione del SuperPeer,
	 * utilizzato per gestire le eventuali eccezioni.
	 * Parametri:
	 * server: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resources: Vector<String> contenente i nomi delle risorse da registrare
	 * */
	public Vector<String> registerResources(SuperPeer server, Vector<String> resources) {
		assert server != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la registrazione() sul coordinatore: "+server.toString()+" per queste risorse:");
			System.out.println(resources.toString());
		}
		
		try {	
			return server.register(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			return null;
		}		
	}
	
	/*
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta
	 * del tracker. Gestisce la richiesta semplice.
	 * 
	 * Parametri:
	 * server: oggetto di tipo tracker
	 * resource: il nome della risorsa richiesta
	 * */
	public String simpleResourceRequest(Tracker server, String resource) {
		assert server != null : "Tracker object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la simpleResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource);
		}
		try {
			return server.richiesta(resource);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}	
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata al metodo richiesta del
	 * tracker. Effettua una richiesta avanzata.
	 * Parametri:
	 * server: oggetto Tracker sul quale chiamare i metodi
	 * resource: stringa contenente il nome della risorsa
	 * prevCoord: stringa contenente il nome del coordinatore ottenuto in precedenza
	 * */
	public String advancedResourceRequest(Tracker server, String resource, String prevCoord) {
		assert server != null : "Tracker object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la advancedResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
		}
		try {
			return server.richiesta(resource, prevCoord);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}
	}
	
	/*
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta
	 * del SuperPeer. Gestisce la richiesta semplice
	 * 
	 * Parametri:
	 * server: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resource: il nome della risorsa richiesta
	 * */
	public String simpleResourceRequest(SuperPeer server, String resource) {
		assert server != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la simpleResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource);
		}
		
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
	public String advancedResourceRequest(SuperPeer server, String resource, String prevCoord) {
		assert server != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la advancedResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
		}
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
	public void goodbye(SuperPeer coord) {
		assert coord != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
		}
		try {
			coord.goodbye(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while exiting politely: "+e.getMessage());
		}	
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata del metodo goodbye per una particolare risorsa sul coordinatore
	 * Parametri:
	 * coord: oggetto di tipo SuperPeer sul quale effettuare la chiamata
	 * resName: stringa contenente il nome della risorsa per la quale ci si rimuove
	 * */
	public void goodbye(SuperPeer coord, String resName) {
		assert coord != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
		}
		try {
			System.out.println(this.myIp+"   "+resName);
			coord.goodbye(this.myIp, resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while exiting politely: "+e.getMessage());
			e.printStackTrace();
		}	
	}
	
	/*
	 * Metodo per gestire le eccezioni della chiamata di getList sul coordinatore.
	 * 
	 * Parametri:
	 * coord: oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * resName: stringa contenente il nome della risorsa
	 * */
	public Vector<String> getList(SuperPeer coord, String resName) {
		assert coord != null : "SuperPeer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la getList sul coordinatore: "+coord.toString()+" per la risorsa "+resName);
		}
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
	public float discovery(Peer p) {
		assert p != null : "Peer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la discovery() sul peer: "+p.toString());
		}
		try {
			return p.discovery(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving distances: "+e.getMessage());
			return 0;
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
	public boolean getResource(Peer p, String resName) {
		assert p != null : "Peer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la getResource() sul peer: "+p.toString()+" per la risorsa "+resName);
		}
		
		byte[] filedata;
		try {
			filedata = p.getResource(resName,this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving the data from the peer: "+e.getMessage());
			return false;
		}
		if(filedata == null) {
			System.out.println("Something went wrong while retrieving the data from the peer and he handled the exception");
			return false;
		}
		File file = new File("resources/"+resName);
		try {
			if(debug)
				System.out.println("resources/"+file.getName());
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("resources/"+file.getName()));
	
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
	public float election(Peer p, String resName) {
		assert p != null : "Peer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la election sul peer "+p.toString());
		}
		
		try {
			return p.election(resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while polling for election candidates: "+e.getMessage());
			return 0;
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
	public void coordinator(Peer p, String resName, String ipCoord) {
		assert p != null : "Peer object is undefined!";
		
		if(debug) {
			System.out.println("Chiamata la coordinator() sul peer "+p.toString()+" per la risorsa: "+resName+
					", il nuovo coordinatore e' "+ipCoord);
		}
		
		try {
			p.coordinator(ipCoord, resName);
		}
		catch (Exception e) {
			System.out.println("Non va");
		}
		
	}
	
	/*
	 * Metodi pubblici di gestione del ciclo di vita di un peer
	 * */
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un tracker ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del tracker.
	 * */
	public Tracker getTracker(String server) {
		if(debug) {
			System.out.println("Chiamata la getTracker, faccio il lookup di "+server);
		}
		try {
			Tracker obj = (Tracker)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			e.printStackTrace();
			return null;
			
		}
	}
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un SuperPeer ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del SuperPeer.
	 * */
	public SuperPeer getCoord(String server) {
		if(debug) {
			System.out.println("Chiamata la getCoord, faccio il lookup di "+server);
		}
		try {
			SuperPeer obj = (SuperPeer)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			e.printStackTrace();
			return null;
			
		}
	}
	
	/*
	 * Metodo che dato in ingresso il percorso rmi di un Peer ne ritorna l'oggetto corrispondente.
	 * 
	 * Parametri:
	 * server: stringa contenente il percorso rmi del Peer.
	 * */
	public Peer getPeer(String server) {
		if(debug) {
			System.out.println("Chiamata la getPeer, faccio il lookup di "+server);
		}
				
		try {
			Peer obj = (Peer)Naming.lookup(server);
			return obj;
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			e.printStackTrace();
			return null;
			
		}
	}
	
	/*
	 * Metodo chiamato dal peer per avviare la procedura di elezione.
	 * 
	 * Chiama la election su ogni altro peer ricevendo le loro distanze medie ed
	 * elegge come coordinatore il peer con la distanza media piu' bassa.
	 * 
	 * Parametri:
	 * resName: stringa contenente il nome della risorsa per cui e' necessario
	 *          eleggere un nuovo coordinatore;
	 * noSelf : true se l'elezione non deve coinvolgere il peer invocante;
	 * tr     : riferimento al tracker.
	 * */
	public void startElection(String resName, boolean noSelf, Tracker tr) {
		
		if(debug) {
			System.out.println("Chiamata la election() per la risorsa: "+resName);
		}
		Hashtable<String, PeerTable> rt = null;
		try {
			rt = this.myPS.getTable();
		} catch (RemoteException e1) {
			System.out.println("Unable to get ResourceTable from my server: "+e1.getMessage());
			e1.printStackTrace();
		}
		
		assert rt != null:"Unable to get resourceTable..";
		
		float answers[] = new float[rt.get(resName).get().size()];
		String peers[] = new String[rt.get(resName).get().size()];
		//per ogni peer nella lista chiama la election su di loro per ottenere le loro avgDist
		for(int i=0;i<rt.get(resName).get().size();++i) {
			String server = rt.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"Peer"+server;
			Peer p = this.getPeer(server);
			
			assert p != null : "Peer object is undefined!";
			
			try {
				answers[i] = p.election(resName);
			} catch (RemoteException e) {
				System.out.println("Exception in election procedure: " + e.getMessage());
				e.printStackTrace();
			}
			peers[i] = rt.get(resName).get().get(i).peer;
		}
		
		//tra tutto quello che ho ricevuto trovo quello col minimo (considerando anche me stesso)
		float min = 0;
		try {
			if(!noSelf)
				min = this.myPS.getAvgDist();
			else
				min = 999;
		} catch (RemoteException e1) {
			System.out.println("Unable to get avgDist from server");
			e1.printStackTrace();
		}
		String peerMin = "";
		if(!noSelf)
			peerMin = this.myIp;
		
		for(int i=0;i<answers.length;++i) {
			if(answers[i] < min) {
				if(!noSelf) {
					min = answers[i];
					peerMin = peers[i];
				}
				else
					if(!peers[i].equals(this.myIp)) {
						min = answers[i];
						peerMin = peers[i];
					}
						
			}
		}
		if(debug) {
			System.out.println("Election: il nuovo coordinatore e': "+peerMin+", ha distanza media "+min);
		}
		//peerMin ora sara' il nuovo coordinatore per la risorsa resName
		for(int i=0;i<rt.get(resName).get().size();++i) {
			String server = rt.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"Peer"+server;
			Peer p = this.getPeer(server);
			
			assert p != null : "Peer object is undefined!";
			
			try {
				p.coordinator(peerMin, resName);
			} catch (RemoteException e) {
				System.out.println("Exception while announcing coordinator: " + e.getMessage());
				e.printStackTrace();
			}
			
			try {
				tr.cambioCoordinatore(peerMin, resName);
			} catch (RemoteException e) {
				System.out.println("Unable to change coordinator: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/*public static void main(String args[]) throws UnknownHostException {
		
		
		
		if(debug) {
			System.out.println("Avviato il client");
		}
		String tracker = args[0];
		String mode = args[1];
		String resName = args[2];
		
		
		String server = "rmi://"+tracker+"/"+"Tracker";
		
		self = new PeerClient();
		if(args[2].equals("debug"))
			debug = true;
		System.out.println(self);
		Tracker tr = self.getTracker(server);
		
		assert tr != null : "Tracker object is undefined!";
		
		if(mode.equals("register")) {
			
			if(debug) {
				System.out.println("Client in modalita' registrazione");
			}
			Vector<String> resNames = new Vector<String>();
			resNames.add(resName);
			
			//register new resources
			Vector<String> coords = self.registerResources(tr, resNames);
			
			assert coords.size() == resNames.size() : "coords and resNames size doesn't match!";
			
			//add coordinators in the hashtable
			for(int i=0;i<coords.size();++i) {
				self.resourceTable.put(resNames.get(i), new PeerTable(new PeerTableData(coords.get(i),-1,false,true)));
				if(coords.get(i) != self.myIp) {
					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer";
					SuperPeer c = self.getCoord(coord);
					
					assert c != null : "SuperPeer object is undefined!";
					
					self.registerResources(c, resNames);
				}
				else {
					if(debug) {
						System.out.println("Sono io il nuovo coordinatore per la risorsa "+resNames.get(i));
					}
					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer";
					SuperPeer c = self.getCoord(coord);
					self = new SuperPeerClient(c,tr);
				}
					
			}
			try {
				self.myPS.syncTable(self.resourceTable);
			} catch (RemoteException e) {
				System.out.println("Unable to sync table with my server! I'll die horribly");
				
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			
			if(debug) {
				System.out.println("Client in modalita' request");
			}
			
			String prevC = self.simpleResourceRequest(tr, resName);
			String coord = "rmi://"+prevC+"/"+"SuperPeer";
			
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
				coord = "rmi://"+prevC+"/"+"SuperPeer";				
				SuperPeer c1 = self.getCoord(coord);
				
				assert c1 != null : "SuperPeer object is undefined!"; //FIXME: ha senso questa assert? come gestisce rmi il non
																	   //		 rispondere..? fa ritornare un null?
				
				ipList = self.getList(c1, resName);
			}
			
			PeerTable pt = new PeerTable();
			for(int i=0;i<ipList.size();++i) {
				
				String peer = ipList.get(i);
				peer = "rmi://"+peer+"/"+"Peer"+peer;
				Peer p = self.getPeer(peer);
				
				assert p != null : "Peer object is undefined!";
				
				PeerTableData pd = new PeerTableData(ipList.get(i),self.discovery(p),false,ipList.get(i)==prevC?true:false);
				pt.add(pd);
			}
			self.resourceTable.put(resName, pt);
			
			pt = self.resourceTable.get(resName);
			
			self.avgDist = pt.getAvgDist();
			
			String closestPeer = pt.getMinDistPeer();
			closestPeer = "rmi://"+closestPeer+"/"+"Peer"+closestPeer;
			Peer p = self.getPeer(closestPeer);
			
			assert p != null : "Peer object is undefined!";
			
			//richiedi la risorsa..
			if(self.getResource(p, resName))
				System.out.println("tutto e' andato a buon fine, yeah!");
			else
				System.out.println("Trasferimento della risorsa fallito..");
			
		}
		
		
	}
*/
}

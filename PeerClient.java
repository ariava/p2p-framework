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
		
		if(debug)
			System.out.println("Chiamata la simpleResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource);
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
		
		if(debug)
			System.out.println("Chiamata la advancedResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
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
		
		if(debug)
			System.out.println("Chiamata la simpleResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource);
		
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
		
		if(debug)
			System.out.println("Chiamata la advancedResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
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
		
		if(debug)
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
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
	public void goodbye(SuperPeer coord, String resName) throws RemoteException {
		assert coord != null : "SuperPeer object is undefined!";
		
		if(debug)
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
		try {
			System.out.println(this.myIp+"   "+resName);
			coord.goodbye(this.myIp, resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while exiting politely: "+e.getMessage());
			e.printStackTrace();
		}	
		
		Vector<PeerTableData> list = this.myPS.getTable().get(resName).get();
		
		for(int i=0;i<list.size();++i) {
			
			String peer = "rmi://"+list.get(i).peer+"/Peer"+list.get(i).peer;
			if (debug)
				System.out.println("goodbye(): uscita pulita dal PeerServer " + list.get(i));
			Peer p = this.getPeer(peer);
			p.goodbye(resName, this.myIp);
		}
		if (debug) {
			System.out.println("***Tabella dopo goodbye***");
			this.myPS.getTable().get(resName).print();
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
		
		if(debug)
			System.out.println("Chiamata la getList sul coordinatore: "+coord.toString()+" per la risorsa "+resName);
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
		
		if(debug)
			System.out.println("Chiamata la discovery() sul peer: "+p.toString());
		try {
			return p.discovery(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving distances: "+e.getMessage());
			return 0;
		}
		
	}
	
	private void addNewPeer(String resName, String ip) {
		try {
		Peer p = null;
		try {
			p = (Peer)Naming.lookup("rmi://"+ip+"/Peer"+ip);
		}
		catch (Exception e) {
			
			System.out.println("Error while getting the remote object: "+e.getMessage());
			e.printStackTrace();			
		}
		
		PeerTable pt = this.myPS.getTable().get(resName);
		try {
			pt.add(new PeerTableData(ip, p.discovery(this.myIp),false,false ));
		} catch (RemoteException e) {
			System.out.println("Discovery failed.."+e.getMessage());
			e.printStackTrace();
		}
		this.myPS.addToTable(resName, pt);
		System.out.println("Tabella ora:");
		this.myPS.getTable().get(resName).print();
		//ricalcolo avgdist
		this.myPS.setAvgDist(this.myPS.getTable().get(resName).getAvgDist(this.myIp));
		if (debug)
			System.out.println("La nuova distanza media calcolata e': "+this.myPS.getAvgDist());
		}
		catch (Exception e) {}
		
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
		
		if(debug)
			System.out.println("Chiamata la getResource() sul peer: "+p.toString()+" per la risorsa "+resName);
		
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
		
		this.addNewPeer(resName, this.myIp);
		
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
		
		if(debug)
			System.out.println("Chiamata la election sul peer "+p.toString());
		
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
		
		if(debug)
			System.out.println("Chiamata la coordinator() sul peer "+p.toString()+" per la risorsa: "+resName+
					", il nuovo coordinatore e' "+ipCoord);
		
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
		if(debug)
			System.out.println("Chiamata la getTracker, faccio il lookup di "+server);
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
		if(debug)
			System.out.println("Chiamata la getCoord, faccio il lookup di "+server);
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
		if(debug)
			System.out.println("Chiamata la getPeer, faccio il lookup di "+server);
				
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
		
		if(debug)
			System.out.println("Chiamata la election() per la risorsa: "+resName);
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
		if(debug)
			System.out.println("Election: il nuovo coordinatore e': "+peerMin+", ha distanza media "+min);
		//peerMin ora sara' il nuovo coordinatore per la risorsa resName
		for(int i=0;i<rt.get(resName).get().size();++i) {
			String server = rt.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"Peer"+server;
			Peer p = this.getPeer(server);
			
			assert p != null : "Peer object is undefined!";
			System.out.println("ORA DOVREI CHIAMARE LA COORDINATORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			try {
				System.out.println(p.toString());
				p.coordinator(peerMin, resName);
			} catch (Exception e) {
				System.out.println("Exception while announcing coordinator: " + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("Asdashudashfaifhdiuashduihuashiuqegflllllllllllllllllllllgi");
			try {
				tr.cambioCoordinatore(peerMin, resName);
			} catch (RemoteException e) {
				System.out.println("Unable to change coordinator: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
}

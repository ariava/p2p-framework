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
	 * Metodi sul tracker
	 * */
	//connect to tracker and register resources
	private Vector<String> registerResources(Tracker server, Vector<String> resources) {
	
		try {	
			return server.registrazione(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			return null;
		}
	}
	private Vector<String> registerResources(SuperPeer server, Vector<String> resources) {
		
		try {	
			return server.register(this.myIp, resources);
		}
		catch (Exception e){
			
			System.out.println("Something went wrong while registering resources "+resources);
			return null;
		}		
	}
	
	private String simpleResourceRequest(Tracker server, String resource) {
		
		try {
			return server.richiesta(resource);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}	
	}
	
	private String advancedResourceRequest(Tracker server, String resource, String prevCoord) {
		
		try {
			return server.richiesta(resource, prevCoord);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}
	}
	
	private String simpleResourceRequest(SuperPeer server, String resource) {
		
		try {
			return server.request(resource);
		}
		catch(Exception e) {
			System.out.println("Something went wrong while requesting resource "+resource);
			return "";
		}
	}
	
	private String advancedResourceRequest(SuperPeer server, String resource, String prevCoord) {
		
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
	
	private void goodbye(SuperPeer coord) {
		
		try {
			coord.goodbye(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while exiting politely: "+e.getMessage());
		}
		
	}
	
	private Vector<String> getList(SuperPeer coord, String resName) {
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
	private float discovery(Peer p) {
		
		try {
			return p.discovery(this.myIp);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while retrieving distances: "+e.getMessage());
			return -1;
		}
		
	}
	
	private boolean getResource(Peer p, String resName) {
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
	
	private float election(Peer p, String resName) {
		try {
			return p.election(resName);
		}
		catch (Exception e) {
			System.out.println("Something went wrong while polling for election candidates: "+e.getMessage());
			return -1;
		}
	}
	
	//metodo chiamato per registrare un nuovo coordinatore
	private void coordinator(Peer p, String resName, String ipCoord) {
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
	
	private void startElection(String resName) {
		
		float answers[] = new float[this.resourceTable.get(resName).get().size()];
		String peers[] = new String[this.resourceTable.get(resName).get().size()];
		//per ogni peer nella lista chiama la election su di loro per ottenere le loro avgDist
		for(int i=0;i<this.resourceTable.get(resName).get().size();++i) {
			String server = this.resourceTable.get(resName).get().get(i).peer;
			server = "rmi://"+server+"/"+"PeerServer";
			Peer p = this.getPeer(server);
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
		
		if(mode.equals("register")) {
			Vector<String> resNames = new Vector<String>();
			resNames.add("prova.txt");
			
			//register new resources
			Vector<String> coords = self.registerResources(tr, resNames);
			
			//add coordinators in the hashtable
			for(int i=0;i<coords.size();++i) {
				self.resourceTable.put(resNames.get(i), new PeerTable(new PeerTableData(coords.get(i),-1,false,true)));
				if(coords.get(i) != self.myIp) {
					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeerServer";
					SuperPeer c = self.getCoord(coord);
					self.registerResources(c, resNames);
				}
					
			}
		} else {
			//request resource prova.txt
			String resName = "prova.txt";
			String prevC = self.simpleResourceRequest(tr, resName);
			String coord = "rmi://"+prevC+"/"+"SuperPeerServer";
			
			SuperPeer c = self.getCoord(coord);
			Vector<String> ipList = self.getList(c, resName);
			while(ipList == null) {
				System.out.println("Coordinator isn't responding..");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.println("Exception while sleeping: " + e.getMessage());
					e.printStackTrace();
				}
				
				prevC = self.advancedResourceRequest(tr, resName, prevC);
				coord = "rmi://"+prevC+"/"+"SuperPeerServer";
				
				SuperPeer c1 = self.getCoord(coord);
				ipList = self.getList(c1, resName);
			}
			
			PeerTable pt = new PeerTable();
			for(int i=0;i<ipList.size();++i) {
				
				String peer = ipList.get(i);
				peer = "rmi://"+peer+"/"+"PeerServer";
				Peer p = self.getPeer(peer);
				
				PeerTableData pd = new PeerTableData(ipList.get(i),self.discovery(p),false,ipList.get(i)==prevC?true:false);
				pt.add(pd);
			}
			self.resourceTable.put(resName, pt);
			
			pt = self.resourceTable.get(resName);
			
			self.avgDist = pt.getAvgDist();
			
			String closestPeer = pt.getMinDistPeer();
			closestPeer = "rmi://"+closestPeer+"/"+"PeerServer";
			Peer p = self.getPeer(closestPeer);
			
			//richiedi la risorsa..
			if(self.getResource(p, resName))
				System.out.println("tutto e' andato a buon fine, yeah!");
			
			
		}
		
		
	}

}

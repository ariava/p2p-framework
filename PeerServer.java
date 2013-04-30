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
	
	protected Hashtable<String, PeerTable> resourceTable; //TODO: sincronizzare con quella del client..
	
	public PeerServer() throws RemoteException, UnknownHostException {
		
		super();
		this.avgDist = -1;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		resourceTable = new Hashtable<String, PeerTable>();
		
	}
	
	public float discovery(String ip) throws RemoteException {
		
		return 4;
		
	}
	

	public byte[] getResource(String resName) throws RemoteException {

		try {

			File file = new File(resName);
	
			byte buffer[] = new byte[(int)file.length()];
	
			BufferedInputStream input = new	BufferedInputStream(new FileInputStream(resName));
	
			input.read(buffer,0,buffer.length);
	
			input.close();
	
			return(buffer);

		} catch(Exception e){

			System.out.println("FileImpl: "+e.getMessage());
	
			e.printStackTrace();
	
			return(null);

		}
	}
	
	/*
	 * Metodo invocato da un altro peer una volta avviata la procedura di elezione per ricevere i vari id
	 * 
	 * res: risorsa per la quale si avvia l'election
	 * */
	public float election(String res) throws RemoteException {
		
		return this.avgDist;
		
	}
	
	/*
	 * Metodo invocato da un altro peer per annunciare il nuovo coordinatore per una risorsa.
	 * 
	 * newCoord: ip del nuovo coordinatore
	 * res: risorsa per cui e' stato eletto il nuovo coordinatore
	 * */
	public void coordinator(String newCoord, String res) throws RemoteException {
		
		PeerTable pt = this.resourceTable.get(res);
		
		for(int i=0;i<pt.get().size();++i) {
			pt.get().get(i).coordinator = false;
			if(pt.get().get(i).peer.equals(newCoord))
				pt.get().get(i).coordinator = true; 
			
		}
		
		this.resourceTable.put(res, pt);
	}
	
	public static void main(String[] args) {
	
		System.setSecurityManager(new RMISecurityManager());
		
		try {
			PeerServer obj = new PeerServer();
			Naming.rebind("PeerServer"+InetAddress.getLocalHost().getHostAddress(), obj);
		}
		catch (Exception e) {
			System.out.println("non va: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
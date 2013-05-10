import java.rmi.*;
import java.util.Hashtable;

public interface Peer extends Remote {
	
	public float discovery(String ip) throws RemoteException;
	public byte[] getResource(String resName) throws RemoteException;
	public float election(String res)throws RemoteException; 
	public void coordinator(String newCoord, String res)throws RemoteException;
	public void syncTable(Hashtable<String, PeerTable> rt) throws RemoteException;
	public Hashtable<String, PeerTable> getTable() throws RemoteException;
	public void addToTable(String r, PeerTable p) throws RemoteException;

}

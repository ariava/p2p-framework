import java.rmi.*;
import java.util.Hashtable;

public interface Peer extends Remote {
	
	public float discovery(String ip) throws RemoteException;
	public byte[] getResource(String resName,String ip) throws RemoteException;
	public float election(String res, String ipCaller)throws RemoteException; 
	public void coordinator(String newCoord, String res)throws RemoteException;
	public void syncTable(Hashtable<String, PeerTable> rt) throws RemoteException;
	public Hashtable<String, PeerTable> getTable() throws RemoteException;
	public void addToTable(String r, PeerTable p) throws RemoteException;
	public void goodbye(String resName, String ip) throws RemoteException;
	public float getAvgDist() throws RemoteException;
	public void setAvgDist(float avg) throws RemoteException;
	public void removeFromTable(String resName) throws RemoteException;
	public boolean noElection() throws RemoteException;
	public void newPeer(String ip, String resName) throws RemoteException;

}

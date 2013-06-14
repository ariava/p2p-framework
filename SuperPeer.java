import java.rmi.*;
import java.util.Hashtable;
import java.util.Vector;

public interface SuperPeer extends Peer {

	public Vector<String> register(String requestorIp, Vector<String> resources) throws RemoteException;
	public String request(String resource) throws RemoteException;
	public String request(String resource, String lastCoord) throws RemoteException;
	public void goodbye(String peerIp) throws RemoteException;
	public void goodbye(String peerIp, String resName) throws RemoteException;
	public Vector<String> getList(String resourceName) throws RemoteException;
	public void setList(Hashtable<String, String> table) throws RemoteException;
	public  Hashtable<String, String> getCoordTable() throws RemoteException;
	public void setCoordinator(String resourceName, String newCoord) throws RemoteException;

	public String getId() throws RemoteException;
	public String getName() throws RemoteException;
	public String getIP() throws RemoteException;
	public boolean ping() throws RemoteException;

}

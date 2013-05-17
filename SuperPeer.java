import java.rmi.*;
import java.util.Hashtable;
import java.util.Vector;

public interface SuperPeer extends Peer {
	public Vector<String> register(String requestor_ip, Vector<String> resources) throws RemoteException;
	public String request(String resource) throws RemoteException;
	public String request(String resource, String last_coord) throws RemoteException;
	public void goodbye(String peer_ip) throws RemoteException;
	public void goodbye(String peer_ip, String resName) throws RemoteException;
	public Vector<String> getList(String resource_name) throws RemoteException;
	public void setList(Hashtable<String, String> table) throws RemoteException;
	public  Hashtable<String, String> getCoordTable() throws RemoteException;
	
	// TODO: eliminare metodo getId()?
	public String getId() throws RemoteException;
	public String getName() throws RemoteException;
	public String getIP() throws RemoteException;
}

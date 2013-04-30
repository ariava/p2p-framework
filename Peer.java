import java.rmi.*;

public interface Peer extends Remote {
	
	public float discovery(String ip) throws RemoteException;
	public byte[] getResource(String resName) throws RemoteException;
	public float election(String res)throws RemoteException; 
	public void coordinator(String newCoord, String res)throws RemoteException;

}

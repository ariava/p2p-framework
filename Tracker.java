import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

public interface Tracker extends Remote {
	
    public Vector<String> registrazione(String ip, Vector<String> risorse) throws RemoteException;
    public String richiesta(String risorsa) throws RemoteException;
    public String richiesta(String risorsa, String ipPrecedente) throws RemoteException;
    public void cambioCoordinatore(String ip, String risorsa) throws RemoteException;
    public Hashtable<String, String> getList(String timestamp) throws RemoteException;
    public String getIp() throws RemoteException;
    public void setList(Hashtable<String, String> l) throws RemoteException;
    
}
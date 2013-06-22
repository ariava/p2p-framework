import java.io.Serializable;
import java.util.Vector;

public class PeerTable implements Serializable {

	private static final long serialVersionUID = 1L;
	private Vector<PeerTableData> data;
	private boolean debug;
	
	/**
	 * Costruttore della classe PeerTable.
	 * 
	 * Inizializza il vettore di oggetti PeerTableData
	 */
	public PeerTable() {
		data = new Vector<PeerTableData>();
		debug = false;
	}
	
	/**
	 * Costruttore della classe PeerTable con i dati contenuti in d
	 * 
	 * @param d elemento di tipo PeerTableData
	 */
	public PeerTable(PeerTableData d) {
		this.data = new Vector<PeerTableData>();
		this.data.add(d);
	}
	
	/**
	 * Tale metodo aggiunge un elemento di tipo PeerTableData alla PeerTable
	 * 
	 * @param pd elemento di tipo PeerTableData
	 */
	public void add(PeerTableData pd) {
		this.data.add(pd);
	}
	
	/**
	 * Tale metodo ritorna un elemento di tipo PeerTableData contenente i dati relativi
	 * al coordinatore per la risorsa a cui la PeerTable si riferisce.
	 * 
	 * @return elemento di tipo PeerTableData
	 */
	public PeerTableData getCoord() {
		for (int i=0 ; i<data.size() ; ++i) {
			if(data.get(i).coordinator)
				return data.get(i);
		}
		return null;
	}
	
	/**
	 * Ritorna un elemento di tipo PeerTableData contenente tutte le informazioni
	 * su un peer avente come indirizzo ip quello passato come parametro se esiste,
	 * null altrimenti.
	 * 
	 * @param ip indirizzo ip del peer
	 * 
	 * @return elemento di tipo PeerTableData
	 */
	public PeerTableData getIP(String ip) {
		for (int i=0 ; i < data.size() ; ++i) {
			if(data.get(i).peer.equals(ip))
				return data.get(i);
		}
		return null;
	}
	
	/**
	 * Metodo che elimina un elemento dalla PeerTable
	 * 
	 * @param ptd elemento di tipo PeerTableData da eliminare
	 */
	public void remove(PeerTableData ptd) {
		data.remove(ptd);
	}
	
	/**
	 * Getter per la PeerTable
	 * 
	 * @return il vettore di elementi PeerTableData
	 */
	public Vector<PeerTableData> get() {
		return this.data;
	}
	
	/**
	 * Setter per la PeerTable
	 * 
	 * @param d il vettore di elementi PeerTableData
	 */
	public void set(Vector<PeerTableData> d) {
		this.data = d;
	}
	
	/**
	 * Tale metodo ritorna la distanza media dei peer nella PeerTable
	 * 
	 * @param indirizzo ip richiedente
	 * 
	 * @return la distanza media
	 */
	public float getAvgDist(String myIp) {
		if (debug) {
			System.out.println("Chiamata la getAvgDist");
		}
		float avg = 0;
		int i, numSamples = 0;
		for (i=0 ; i<this.data.size() ; ++i) {
			if (debug)
				System.out.println("myIp: "+myIp+"    "+this.data.get(i).peer+": "+this.data.get(i).dist);
			if(!this.data.get(i).peer.equals(myIp)) {
				avg += this.data.get(i).dist;
				numSamples++;
			}
		}
		return avg/numSamples;
	}
	
	/**
	 * Tale metodo ritorna l'indirizzo ip del peer con distanza piu' bassa.
	 * 
	 * Utilizzato prima del trasferimento di una risorsa per decidere a
	 * chi richiederla.
	 * 
	 * @return l'indirizzo ip del peer con distanza più bassa
	 */
	public String getMinDistPeer() {
		float min = this.data.get(0).dist;
		int pos = 0;
		for (int i=1 ; i<this.data.size() ; ++i) {
			if (this.data.get(i).dist<min) {
				min = this.data.get(i).dist;
				pos = i;
			}
		}
		return this.data.get(pos).peer;
	}

	/**
	 * Stampa la tabella
	 */
	public void print() {
		System.out.println("Stampa della peerTable");
		for (int i=0 ; i<data.size() ; ++i) {
			System.out.println("Ip: "+data.get(i).peer);
			System.out.println("Dst: "+data.get(i).dist);
			System.out.println(data.get(i).coordinator);
			System.out.println(data.get(i).down);
			System.out.println();
		}
	}
	
	/**
	 * Imposta la modalità di debug
	 * 
	 * @param value vale vero se vogliamo attivare la modalità
	 * di debug, falso altrimenti
	 */
	protected void setDebug(boolean value) {
		this.debug = value;
	}

}

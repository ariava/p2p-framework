import java.util.Vector;


public class PeerTable {

	private Vector<PeerTableData> data;
	
	public PeerTable() {
		data = new Vector<PeerTableData>();
	}
	
	public PeerTable(PeerTableData d) {
		this.data.add(d);
	}
	
	/*
	 * Aggiunge un elemento di tipo PeerTableData alla PeerTable
	 * */
	public void add(PeerTableData pd) {
		
		this.data.add(pd);
	}
	
	/*
	 * Ritorna un elemento di tipo PeerTableData contenente i dati relativi al coordinatore
	 * per la risorsa a cui la PeerTable si riferisce.
	 * */
	public PeerTableData getCoord() {
		
		for(int i=0;i<data.size();++i) {
			if(data.get(i).coordinator)
				return data.get(i);
		}
		
		return null;
	}
	
	/*
	 * Ritorna un elemento di tipo PeerTableData contenente tutte le informazioni su un peer
	 * avente come indirizzo ip quello passato come parametro se esiste, null altrimenti
	 * */
	public PeerTableData getIP(String ip) {
		for (int i=0 ; i < data.size() ; ++i) {
			if(data.get(i).peer == ip)
				return data.get(i);
		}
		return null;
	}
	
	/*
	 * Elimina un elemento dalla PeerTable
	 * */
	public void remove(PeerTableData ptd) {
		data.remove(ptd);
	}
	
	/*
	 * Getter per la PeerTable
	 * */
	public Vector<PeerTableData> get() {
		return this.data;
	}
	
	/*
	 * Setter per la PeerTable
	 * */
	public void set(Vector<PeerTableData> d) {
		this.data = d;
	}
	
	/*
	 * Ritorna la distanza media dei peer nella PeerTable
	 * */
	public float getAvgDist() {
		float avg = 0;
		int i;
		for(i=0;i<this.data.size();++i) {
			avg += this.data.get(i).dist;
		}
		return avg/i;
	}
	
	/*
	 * Ritorna l'indirizzo ip del peer con distanza piu' bassa.
	 * 
	 * Utilizzato prima del trasferimento di una risorsa per decidere a chi richiederla.
	 * */
	public String getMinDistPeer() {
		float min = this.data.get(0).dist;
		int pos = 0;
		for(int i=1;i<this.data.size();++i) {
			if(this.data.get(i).dist<min) {
				min = this.data.get(i).dist;
				pos = i;
			}
		}
		return this.data.get(pos).peer;
	}
	
}

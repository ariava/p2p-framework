import java.util.Vector;


public class PeerTable {

	private Vector<PeerTableData> data;
	
	public PeerTable() {}
	
	public PeerTable(PeerTableData d) {
		this.data.add(d);
	}
	
	public void add(PeerTableData pd) {
		
		this.data.add(pd);
	}
	
	public PeerTableData getCoord() {
		
		for(int i=0;i<data.size();++i) {
			if(data.get(i).coordinator)
				return data.get(i);
		}
		
		return null;
	}
	
	public PeerTableData getIP(String ip) {
		for (int i=0 ; i < data.size() ; ++i) {
			if(data.get(i).peer == ip)
				return data.get(i);
		}
		return null;
	}
	
	public void remove(PeerTableData ptd) {
		data.remove(ptd);
	}
	
	public Vector<PeerTableData> get() {
		return this.data;
	}
	
	public void set(Vector<PeerTableData> d) {
		this.data = d;
	}
	
	public float getAvgDist() {
		float avg = 0;
		int i;
		for(i=0;i<this.data.size();++i) {
			avg += this.data.get(i).dist;
		}
		return avg/i;
	}
	
	public String getMinDistPeer() {
		float min = 999;
		int pos = -1;
		for(int i=0;i<this.data.size();++i) {
			if(this.data.get(i).dist<min) {
				min = this.data.get(i).dist;
				pos = i;
			}
		}
		return this.data.get(pos).peer;
	}
	
}

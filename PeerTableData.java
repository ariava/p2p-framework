

public class PeerTableData {

	public String peer;
	public float dist;
	public boolean down;
	public boolean coordinator;
	
	public PeerTableData(String peer, float dist, boolean down, boolean coord) {
		this.peer = peer;
		this.dist = dist;
		this.down = down;
		this.coordinator = coord;
		
	}
	
}

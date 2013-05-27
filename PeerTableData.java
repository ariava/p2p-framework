import java.io.Serializable;



public class PeerTableData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String peer;
	public float dist;
	public boolean down;
	public boolean coordinator;
	
	/**
	 * Costruttore della classe PeerTableData
	 * 
	 * @param peer indirizzo ip del peer
	 * @param dist distanza dal peer
	 * @param down flag che indica se il peer è down
	 * @param coord flag che indica se peer è il coordinatore
	 */
	public PeerTableData(String peer, float dist, boolean down, boolean coord) {
		this.peer = peer;
		this.dist = dist;
		this.down = down;
		this.coordinator = coord;	
	}
	
}

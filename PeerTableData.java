import java.io.Serializable;

/**
 * La classe PeerTableData implementa una entry della tabella
 * gestita dalla classe PeerTable. 
 * 
 * In particolare una entry contiene i seguenti campi:
 * peer: l'indirizzo ip del peer che contiene la risorsa
 * dist: la distanza dal peer che contiene la risorsa in termini di hop count
 * coord: flag che indica se peer è coordinatore per quella risorsa
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
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
	 * 
	 * XXX il flag down non serve a niente
	 */
	public PeerTableData(String peer, float dist, boolean down, boolean coord) {
		this.peer = peer;
		this.dist = dist;
		this.down = down;
		this.coordinator = coord;	
	}
	
}

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Common {

	private final static int timestampLength = 23;
	
	/**
	 * Metodo che ritorna un timestamp relativo al momento corrente
	 * 
	 * @return una stringa contenente il timestamp
	 */
	public static String setTimestamp() {
		String timestamp = null;
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH); // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int millis = now.get(Calendar.MILLISECOND);
		timestamp = String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", year, month + 1, day, hour, minute, second, millis);
		assert timestamp.length() == timestampLength : "Il timestamp ha lunghezza " + timestamp.length();
		return timestamp;
	}

	/**
	 * Metodo di debug che stampa un insieme di vettori di stringhe in modo "ordinato"
	 * 
	 * @param labels etichette dei vettori di stringhe
	 * @param vecs un numero variabile di vettori di stringhe
	 */
	public static final void printStringVectors(String[] labels, Vector<String>... vecs) {
		int labelnum = 0;
		for (Vector<String> vec : vecs) {
			System.out.print("[");
			System.out.print(labels[labelnum] + ": ");
			for (int i = 0 ; i < vec.size() ; i++) {
				System.out.print(vec.get(i));
				if (i != vec.size()-1)
					System.out.print(", ");
			}
			System.out.println("]");
			labelnum++;
		}
	}
	
	/**
	 * Metodo di debug che stampa la tabella dei coordinatori passata come parametro.
	 * 
	 * @param table tabella hash dei coordinatori
	 */
	public static void printCoordTable(Hashtable<String, String> table) {
		assert table != null : "Tabella coordinatori nulla nella funzione di stampa";
		assert table.size() != 0 : "Tabella coordinatori di dimensione zero nella funzione di stampa";
		System.out.println("*** Tabella dei coordinatori ***");
		Enumeration<String> e = table.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			System.out.println("Risorsa: " + key + " | Coord: " + table.get(key));
		}
	}

}

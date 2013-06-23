import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * La classe PeerClient implementa le funzionalita' offerte da 
 * un semplice Peer che entra a far parte della rete peer to peer.
 * Tale classe si interfaccia con la GUI e comunica direttamente 
 * con il relativo PeerServer/SuperPeerServer
 * 
 * In particolare ciascun Peer può: 
 * 1) Registrare le proprie risorse nella rete peer to peer
 * 2) Effettuare la ricerca di una certa risorsa
 * 3) Cancellare dalla rete peer to peer una o più risorse possedute
 * 4) Far partire l'elezione per una certa risorsa
 * 
 * Inoltre questa classe fornisce un Thread che effettua un ping su
 * tutti i coordinatori delle risorse possedute dal Peer. Nel caso
 * in cui un coordinatore non risponde, viene avviata un'elezione
 * per quella risorsa. Alla fine dell'elezione viene comunicato il
 * nuovo coordinatore al relativo PeerServer e al Tracker.
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
public class PeerClient {

	static public PeerClient self = null;
	public Peer myPS = null;
	public String myIp;
	static public boolean debug;
	public String trackerIp = null;

	public Thread pollingWorker = null;
	private final int pollingWorkerSleep = 5000;
	
	public boolean trackerIsDown;
	
	/**
	 * Costruttore della classe PeerClient senza parametri
	 */
	public PeerClient() throws UnknownHostException {
		self = this;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
			
		String ps = "rmi://"+this.myIp+"/"+"Peer"+this.myIp;
		this.myPS = self.getPeer(ps);
		assert this.myPS != null : "PeerServer e' null!";
	}
	
	/**
	 * Costruttore della classe PeerClient
	 * 
	 * @param tr indirizzo ip del tracker
	 */
	public PeerClient(String tr) throws UnknownHostException {
		self = this;
		this.myIp = InetAddress.getLocalHost().getHostAddress();
		
		String ps = "rmi://"+this.myIp+"/"+"Peer"+this.myIp;
		this.myPS = self.getPeer(ps);
		this.trackerIp = tr;
		assert this.myPS != null : "PeerServer e' null!";
	}
		
	/**
	 * Metodo privato che esegue il ping su tutti i SuperPeer presenti nella tabella
	 */
	public void startPollingWorker() {
		if (pollingWorker != null) {
			if (debug)
				System.out.println("Thread di polling già attivo");
			return;
		}
		pollingWorker = new Thread(
				  new Runnable() {
		                public void run() {
		                	if (debug)
		                		System.out.println("Avviato il thread di polling");
		                	while (true) { // Il thread deve avere lo stesso tempo di vita
		                				   // dell'oggetto (Super)PeerClient che lo ospita
		                		String key = null;
		                		String coord = null;
		                		try {
		                			Enumeration<String> e = myPS.getTable().keys();
		                			while (e.hasMoreElements()) {
		                				key = e.nextElement();
			                			PeerTable pt = myPS.getTable().get(key);
			                			coord = pt.getCoord().peer;
			                			if (coord.equals(myIp))
			                				continue;
			                			SuperPeer c = getCoord("rmi://"+coord+"/SuperPeer"+coord);
			                			if (c == null)
			                				throw new RemoteException();
			                			c.ping();
			                			if (debug)
			                				System.out.println("Il 'ping' al coordinatore di "+key+" e' andato a buon fine, e' tutto ok.");
		                			}
		                		} catch (RemoteException e1) {
		                			if (debug)
		                				System.out.println("Il coordinatore di "+key+" non ha risposto, faccio partire l'elezione.");
		                			Tracker tr;
	                				tr = getTracker("rmi://"+trackerIp+"/Tracker");
	                				startElection(key, false, tr, coord);
		                		} catch (NullPointerException ne) {
		                			if (debug)
		                				System.out.println("Thread di polling: ho catturato la NullPointerException");
		                		}
			                    try {
									Thread.sleep(pollingWorkerSleep);
								} catch (InterruptedException e) {
									if (debug)
										e.printStackTrace();
								} 
			                }
		                }
		            });
		//fine thread
		pollingWorker.start();
	}
	
	
	
	/*
	 * Metodi chiamati sul tracker
	 */

	/**
	 * Metodo privato utilizzato per chiamare il metodo registrazione del tracker,
	 * utilizzato per gestire le eventuali eccezioni.
	 * 
	 * @param server oggetto di tipo Tracker sul quale chiamare il metodo
	 * @param resources vettore di stringhe contenente i nomi delle risorse da registrare
	 * 
	 * @return i coordinatori delle risorse contenute in resources o null in caso di errori
	 */
	public Vector<String> registerResources(Tracker server, Vector<String> resources) {
		assert server != null : "L'oggetto Tracker non e' definito!";
		
		if (debug) {
			System.out.println("Chiamata la registrazione() sul tracker: "+server.toString()+" per queste risorse:");
			System.out.println(resources.toString());
		}
		
		try {	
			return server.registrazione(this.myIp, resources);
		} catch (Exception e) {
			if (debug) {
				System.out.println("Qualcosa e' andato storto durante la registrazione delle risorse "+resources);
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta
	 * del tracker. Gestisce la richiesta semplice.
	 * 
	 * @param server oggetto di tipo tracker
	 * @param resource il nome della risorsa richiesta
	 * 
	 * @return il coordinatore della risorsa richiesta
	 */
	public String simpleResourceRequest(Tracker server, String resource) {
		assert server != null : "L'oggetto Tracker non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la simpleResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource);
		try {
			return server.richiesta(resource);
		} catch(Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante la richiesta semplice della risorsa "+resource);
			return "exception";
		}	
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata al metodo richiesta del
	 * tracker. Effettua una richiesta avanzata.
	 * 
	 * @param server oggetto Tracker sul quale chiamare i metodi
	 * @param resource stringa contenente il nome della risorsa
	 * @param prevCoord stringa contenente il nome del coordinatore ottenuto in precedenza
	 * 
	 * @return il coordinatore della risorsa richiesta
	 */
	public String advancedResourceRequest(Tracker server, String resource, String prevCoord) {
		assert server != null : "L'oggetto Tracker non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la advancedResourceRequest sul tracker: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
		try {
			return server.richiesta(resource, prevCoord);
		} catch(Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante la richiesta avanzata della risorsa "+resource);
			return "";
		}
	}
	
	
	
	/*
	 * Metodi sul coordinatore
	 */
	
	/**
	 * Metodo privato utilizzato per chiamare il metodo registrazione del SuperPeer,
	 * utilizzato per gestire le eventuali eccezioni.
	 * 
	 * @param server oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * @param resources vettore di stringhe contenente i nomi delle risorse da registrare
	 * 
	 * @return i coordinatori delle risorse contenute in resources o null in caso di errori
	 */
	public Vector<String> registerResources(SuperPeer server, Vector<String> resources) {
		assert server != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug) {
			System.out.println("Chiamata la registrazione() sul coordinatore: "+server.toString()+" per queste risorse:");
			System.out.println(resources.toString());
		}
		
		try {	
			return server.register(this.myIp, resources);
		} catch (Exception e) {
			if(debug)
				System.out.println("Qualcosa e' andato storto durante la registrazione delle risorse "+resources);
			return null;
		}		
	}
	
	/**
	 * Metodo usato per gestire le eccezioni della chiamata al metodo richiesta
	 * del SuperPeer. Gestisce la richiesta semplice
	 * 
	 * @param server oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * @param resource il nome della risorsa richiesta
	 * 
	 * @return il coordinatore della risorsa richiesta
	 */
	public String simpleResourceRequest(SuperPeer server, String resource) {
		assert server != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la simpleResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource);
		
		try {
			return server.request(resource);
		} catch(Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante la richiesta della risorsa "+resource);
			return "";
		}
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata al metodo richiesta del tracker. 
	 * Effettua una richiesta avanzata
	 * 
	 * @param server oggetto SuperPeer sul quale chiamare i metodi
	 * @param resource stringa contenente il nome della risorsa
	 * @param prevCoord stringa contenente il nome del coordinatore ottenuto in precedenza
	 * 
	 * @return il coordinatore della risorsa richiesta
	 */
	public String advancedResourceRequest(SuperPeer server, String resource, String prevCoord) {
		assert server != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la advancedResourceRequest sul coordinatore: "+server.toString()+" per la risorsa "+resource+
					" dato come prevCoord "+prevCoord);
		try {
			return server.request(resource, prevCoord);
		} catch(Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante la richiesta della risorsa "+resource);
			return "";
		}		
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata del metodo goodbye sul coordinatore
	 * 
	 * @param coord oggetto di tipo SuperPeer sul quale effettuare la chiamata
	 */
	public void goodbye(SuperPeer coord) {
		assert coord != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
		try {
			coord.goodbye(this.myIp);
		} catch (Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante l'uscita politely: "+e.getMessage());
		}	
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata del metodo goodbye 
	 * per una particolare risorsa sul coordinatore
	 * 
	 * @param coord oggetto di tipo SuperPeer sul quale effettuare la chiamata
	 * @param resName stringa contenente il nome della risorsa per la quale ci si rimuove
	 */
	public void goodbye(SuperPeer coord, String resName) throws RemoteException {
		assert coord != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la goodbye sul coordinatore: "+coord.toString());
		try {
			if (debug)
				System.out.println(this.myIp+" desidera chiamare la goodbye per la risorsa "+resName);
			coord.goodbye(this.myIp, resName);
		} catch (Exception e) {
			if (debug) {
				System.out.println("Qualcosa e' andato storto durante l'uscita politely: "+e.getMessage());
				e.printStackTrace();
			}
		}	
		
		Vector<PeerTableData> list = this.myPS.getTable().get(resName).get();
		
		for(int i=0 ; i<list.size() ; ++i) {
			String peer = "rmi://"+list.get(i).peer+"/Peer"+list.get(i).peer;
			if (debug)
				System.out.println("goodbye(): uscita pulita dal PeerServer " + list.get(i));
			Peer p = this.getPeer(peer);
			if(p!=null)
				p.goodbye(resName, this.myIp);
		}
		if (debug) {
			System.out.println("***Tabella dopo goodbye***");
			this.myPS.getTable().get(resName).print();
		}
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata di getList sul coordinatore.
	 * 
	 * @param coord oggetto di tipo SuperPeer sul quale chiamare il metodo
	 * @param resName stringa contenente il nome della risorsa
	 * 
	 * @return la lista dei Peer che hanno la risorsa resName
	 */
	public Vector<String> getList(SuperPeer coord, String resName) {
		assert coord != null : "L'oggetto SuperPeer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la getList sul coordinatore: "+coord.toString()+" per la risorsa "+resName);
		try {
			return coord.getList(resName);
		} catch (Exception e) {
			if (debug) {
				System.out.println("Qualcosa e' andato storto durante il recupero della zone list: "+e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	/*
	 * Metodi su altri peer 
	 */
	
	/**
	 * Metodo per gestire le eccezioni della chiamata a discovery di un altro peer.
	 * 
	 * @param p oggetto di tipo Peer sul quale effettuare la chiamata
	 * 
	 * @return la distanza in termini di hopcount al Peer p
	 */
	public float discovery(Peer p) {
		assert p != null : "L'oggetto Peer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la discovery() sul peer: "+p.toString());
		try {
			return p.discovery(this.myIp);
		} catch (Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante il recupero delle distanze: "+e.getMessage());
			return 0;
		}
	}
	
	/**
	 * Metodo che aggiunge una entry nella tabella per una certa risorsa
	 * 
	 * @param resName nome della risorsa
	 * @param ip indirizzo ip del peer che ha scaricato la risorsa resName
	 */
	private void addNewPeer(String resName, String ip) {
		try {
			Peer p = null;
			try {
				p = (Peer)Naming.lookup("rmi://"+ip+"/Peer"+ip);
			} catch (Exception e) {
				if (debug) {
					System.out.println("Errore nel recupero dell'oggetto remoto: "+e.getMessage());
					e.printStackTrace();	
				}
			}
			
			PeerTable pt = this.myPS.getTable().get(resName);
			try {
				pt.add(new PeerTableData(ip, p.discovery(this.myIp),false,false));
			} catch (RemoteException e) {
				System.out.println("Il metodo discovery e' fallito: "+e.getMessage());
				e.printStackTrace();
			}
			this.myPS.addToTable(resName, pt);
			if (debug) {
				System.out.println("Tabella ora:");
				this.myPS.getTable().get(resName).print();
			}
			// ricalcolo la distanza media
			this.myPS.setAvgDist(this.myPS.getTable().get(resName).getAvgDist(this.myIp));
			if (debug)
				System.out.println("La nuova distanza media calcolata e': "+this.myPS.getAvgDist());
		} catch (Exception e) {}
	}
	
	/**
	 * Metodo per recuperare una risorsa da un altro peer.
	 * 
	 * Effettua la chiamata al metodo getResource del peer in questione
	 * e scrive su file lo stream di byte ricevuto
	 * 
	 * @param p oggetto di tipo Peer sul quale chiamare la getResource
	 * @param resName stringa contenente il nome della risorsa
	 * 
	 * @return vero se il trasferimento del file è avvenuto con successo, falso altrimenti
	 */
	public boolean getResource(Peer p, String resName) {
		assert p != null : "L'oggetto Peer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la getResource() sul peer: "+p.toString()+" per la risorsa "+resName);
		
		byte[] filedata;
		try {
			filedata = p.getResource(resName,this.myIp);
		} catch (Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante il recupero dei dati dal Peer: "+e.getMessage());
			return false;
		}
		if(filedata == null) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante il recupero dei dati dal Peer ma lui ha gestito l'eccezione");
			return false;
		}
		File file = new File("resources/"+resName);
		try {
			if(debug)
				System.out.println("resources/"+file.getName());
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("resources/"+file.getName()));
			output.write(filedata, 0, filedata.length);
			output.flush();
			output.close();
		} catch (Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante la scrittura del file recuperato: "+e.getMessage());
			return false;
		}
		
		this.addNewPeer(resName, this.myIp);
		
		return true;
	}
	
	/**
	 * Metodo per gestire le eccezioni della chiamata a election sul peer remoto.
	 * 
	 * @param p oggetto di tipo Peer sul quale effettuare la chiamata
	 * @param resName stringa contenente il nome della risorsa
	 * 
	 * @return la distanza media verso i Peer che hanno la risorsa resName
	 */
	public float election(Peer p, String resName) {
		assert p != null : "L'oggetto Peer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la election sul peer "+p.toString());
		
		try {
			return p.election(resName,this.myIp);
		} catch (Exception e) {
			if (debug)
				System.out.println("Qualcosa e' andato storto durante il polling per i candidati dell'elezione: "+e.getMessage());
			return 0;
		}
	}
	
	/**
	 * Metodo usato per gestire le eccezioni della chiamata al coordinator di un altro peer.
	 * 
	 * @param p oggetto di tipo Peer sul quale effettuare la chiamata
	 * @param resName stringa contenente il nome della risorsa
	 * @param ipCoord stringa contenente l'indirizzo ip del coordinatore da impostare
	 */
	public void coordinator(Peer p, String resName, String ipCoord) {
		assert p != null : "L'oggetto Peer non e' definito!";
		
		if(debug)
			System.out.println("Chiamata la coordinator() sul peer "+p.toString()+" per la risorsa: "+resName+
					", il nuovo coordinatore e' "+ipCoord);
		
		try {
			p.coordinator(ipCoord, resName);
		} catch (Exception e) {
			if(debug)
				System.out.println("Impossibile annunciare il nuovo coordinatore " + ipCoord);
		}
		
	}
	
	
	
	/*
	 * Metodi pubblici di gestione del ciclo di vita di un peer
	 */
	
	/**
	 * Metodo che dato in ingresso il percorso rmi di un tracker ne ritorna l'oggetto corrispondente.
	 * 
	 * @param server stringa contenente il percorso rmi del tracker.
	 * 
	 * @return l'oggetto del Tracker
	 */
	public Tracker getTracker(String server) {
		if(debug)
			System.out.println("Chiamata la getTracker, faccio il lookup di "+server);
		try {
			Tracker obj = (Tracker)Naming.lookup(server);
			return obj;
		} catch (Exception e) {
			if(debug) {
				System.out.println("Errore nel recupero dell'oggetto remoto: "+e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Metodo che dato in ingresso il percorso rmi di un SuperPeer ne ritorna l'oggetto corrispondente.
	 * 
	 * @param server stringa contenente il percorso rmi del SuperPeer.
	 * 
	 * @return l'oggetto SuperPeer
	 */
	public SuperPeer getCoord(String server) {
		if(debug)
			System.out.println("Chiamata la getCoord, faccio il lookup di "+server);
		try {
			SuperPeer obj = (SuperPeer)Naming.lookup(server);
			return obj;
		} catch (Exception e) {
			if (debug)
				System.out.println("Errore nel recupero dell'oggetto remoto: "+e.getMessage());
			return null;
			
		}
	}
	
	/**
	 * Metodo che dato in ingresso il percorso rmi di un Peer ne ritorna l'oggetto corrispondente.
	 * 
	 * @param server stringa contenente il percorso rmi del Peer.
	 * 
	 * @return l'oggetto Peer
	 */
	public Peer getPeer(String server) {
		if(debug)
			System.out.println("Chiamata la getPeer, faccio il lookup di "+server);
				
		try {
			Peer obj = (Peer)Naming.lookup(server);
			obj.ping();
			return obj;
		} catch (Exception e) {
			System.out.println("Errore nel recupero dell'oggetto remoto: " + e.getMessage());
			
			// Rimuoviamo un peer che non risponde dalla PeerTable
			Enumeration<String> en = null;
			try {
				en = myPS.getTable().keys();
			} catch (RemoteException e1) {
				if (debug)
					e1.printStackTrace();
			}
			while (en.hasMoreElements()) {
				String key = en.nextElement();
    			PeerTable pt = null;
				try {
					pt = myPS.getTable().get(key);
				} catch (RemoteException e1) {
					if (debug)
						e1.printStackTrace();
				}
    			Vector<PeerTableData> peers = pt.get();
    			for (int i = 0 ; i < peers.size() ; ++i) {
    				String peerip = peers.get(i).peer;
    				String address = "rmi://"+peerip+"/"+"Peer"+peerip;
    				if (address.equals(server))
    					pt.remove(peers.get(i));
    			}
    			try {
					myPS.addToTable(key, pt);
				} catch (RemoteException e1) {
					if (debug)
						e1.printStackTrace();
				}
			}
			
			return null;
		}
	}
	
	/**
	 * Metodo chiamato dal peer per avviare la procedura di elezione.
	 * 
	 * Chiama la election su ogni altro peer ricevendo le loro distanze medie ed
	 * elegge come coordinatore il peer con la distanza media piu' bassa.
	 * 
	 * @param resName stringa contenente il nome della risorsa per cui e' necessario
	 *        eleggere un nuovo coordinatore;
	 * @param noSelf true se l'elezione non deve coinvolgere il peer invocante;
	 * @param tr riferimento al tracker.
	 * @param oldCoord stringa che indica il coordinatore defunto
	 */
	public void startElection(String resName, boolean noSelf, Tracker tr, String oldCoord)  {
		if(debug)
			System.out.println("Chiamata la election() per la risorsa: "+resName);
		
		Hashtable<String, PeerTable> rt = null;
		try {
			rt = this.myPS.getTable();
		} catch (RemoteException e1) {
			if (debug) {
				System.out.println("Impossibile recuperare la ResourceTable dal mio server: "+e1.getMessage());
				e1.printStackTrace();
			}
		}
		
		assert rt != null : "La resourceTable non e' stata recuperata";
		
		int dim = oldCoord==null?rt.get(resName).get().size():rt.get(resName).get().size()-1;
		float answers[] = new float[dim];
		String peers[] = new String[dim];
		//per ogni peer nella lista chiama la election su di loro per ottenere le loro avgDist
		int j=0;
		for(int i=0;i<rt.get(resName).get().size();++i) {
			String server = rt.get(resName).get().get(i).peer;
			if(server.equals(oldCoord))
				continue;
			server = "rmi://"+server+"/"+"Peer"+server;
			Peer p = this.getPeer(server);
			if(debug)
				System.out.println("Sono nella election, contatto il peer "+server+", il suo riferimento e' "+p);
			try {
				if(p == null)
					answers[j] = -1;
				else
					answers[j] = p.election(resName,this.myIp);
			} catch (RemoteException e) {
				if (debug) {
					System.out.println("Eccezione nel metodo election sul peer: " + e.getMessage());
					e.printStackTrace();
				}
			}
			peers[j++] = rt.get(resName).get().get(i).peer;
		}
		
		//tra tutto quello che ho ricevuto trovo quello col minimo (considerando anche me stesso)
		float min = 0;
		try {
			if(!noSelf)
				min = this.myPS.getAvgDist();
			else
				min = 999;
		} catch (RemoteException e1) {
			if (debug) {
				System.out.println("Impossibile recuperare avgDist dal server");
				e1.printStackTrace();
			}
		}
		String peerMin = "";
		if(!noSelf)
			peerMin = this.myIp;
		
		
		for(int i=0 ; i<answers.length ; ++i) {
			if(answers[i] == -1)
				continue;
			if(answers[i] < min) {
				if (!noSelf) {
					min = answers[i];
					peerMin = peers[i];
				} else
					if(!peers[i].equals(this.myIp)) {
						min = answers[i];
						peerMin = peers[i];
					}		
			}
		}
		
		if (peerMin.isEmpty()) {
			if (debug)
				System.out.println("Elezione finita ma nessun coordinatore e' stato eletto");
			try {
				tr.cambioCoordinatore(peerMin, resName);
			} catch (RemoteException e) {
				if (debug) {
					System.out.println("Impossibile cambiare il coordinatore: " + e.getMessage());
					e.printStackTrace();
				}
			}
			return;
		}
		
		if(debug)
			System.out.println("Elezione: il nuovo coordinatore e': "+peerMin+", ha distanza media "+min);
		//peerMin ora sara' il nuovo coordinatore per la risorsa resName
		for (int i=0 ; i<rt.get(resName).get().size() ; ++i) {
			String server = rt.get(resName).get().get(i).peer;
			if(server.equals(oldCoord)) // se c'era un precedente coordinatore che ora non
										// risponde più, non gli notifico nulla
				continue;
			server = "rmi://"+server+"/"+"Peer"+server;
			Peer p = this.getPeer(server);
			
			if(p == null)
				continue;
			
			// XXX: guardare qui!!
			/*if (debug)
				System.out.println("ORA DOVREI CHIAMARE LA COORDINATOR");*/
			try {
				if (debug)
					System.out.println(p.toString());
				p.coordinator(peerMin, resName);
			} catch (Exception e) {
				if (debug) {
					System.out.println("Eccezione durante l'annuncio del coordinatore: " + e.getMessage());
					e.printStackTrace();
				}
			}
			if (debug)
				System.out.println("Fine dell'annuncio coordinatore");
			try {
				tr.cambioCoordinatore(peerMin, resName);
			} catch (RemoteException e) {
				if (debug) {
					System.out.println("Impossibile cambiare il coordinatore: " + e.getMessage());
					System.out.println("Sto per contattare il SuperPeer " + peerMin);
				}
				// Se il tracker è giù, modifico la coordTable del nuovo
				// coordinatore per mantenere la consistenza nella zona
				SuperPeer c = getCoord("rmi://"+peerMin+"/SuperPeer"+peerMin);
    			if (c == null) {
    				if (debug)
    					System.out.println("Impossibile contattare il nuovo SuperPeer");
    			} else {
    				try {
						c.setCoordinator(resName, peerMin);
					} catch (RemoteException e1) {
						if (debug)
							System.out.println("Il nuovo SuperPeer esiste ma e' morto");
					}
    			}
			}
		}
	}
	
	/**
	 * Metodo accessibile all'applicazione da cui questa classe deriva per impostare
	 * la modalità di debug.
	 * 
	 * @param value vale vero se voglio attivare la modalità di debug, falso altrimenti
	 */
	protected void setDebug(boolean value) {
		PeerClient.debug = value;
	}
	
	/* XXX: GUARDARE ANCHE QUA
	 * Metodo vuoto da implementare nel @SuperPeerClient.
	 * */
	protected void stopListRetriever() {
	}

}

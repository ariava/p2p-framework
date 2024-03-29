import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.datatransfer.DataFlavor;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import  java.io.*;

/**
 * La classe fileSharingApplication implementa una semplice interfaccia
 * grafica di trasferimento file.
 * 
 * In particolare tale classe implementa tutti i componenti grafici
 * dell'applicazione e tutti i listener associati ai bottoni e ai
 * campi di testo.
 * Tale classe sfrutta il livello software costituito dal framework,
 * che in ogni caso risulta indipendente da qualsiasi applicazione 
 * soprastante.
 * 
 * @author Arianna Avanzini <73628@studenti.unimore.it>, 
 * Stefano Alletto <72056@studenti.unimore.it>, 
 * Daniele Cristofori <70982@studenti.unimore.it>
 */
public class fileSharingApplication {

	private JFrame frmTestFrameworkGui;
	private JTextField txtInsertFileTo;
	private JTable table;
	private JTextField txtIpTracker;
	private static PeerClient pc;
	private Tracker tr;
	private static boolean debug;
	private static Lock l;
	private static boolean firstConnect = true;
	private static boolean disconnect = false;
	private static boolean connect = true; // boolean usato per vedere se la import l'ha chiamata l'utente o il doClick su connect
	private static boolean elected = false;
	private static File tmpFile = null;
	final JPopupMenu cutpasteMenu = new JPopupMenu();
    final JMenuItem cutMenuItem = new JMenuItem("Cut", new ImageIcon(((new ImageIcon("icons/cut.png")).getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
    final JMenuItem copyMenuItem = new JMenuItem("Copy", new ImageIcon(((new ImageIcon("icons/copy.png")).getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
    final JMenuItem pasteMenuItem = new JMenuItem("Paste", new ImageIcon(((new ImageIcon("icons/paste.png")).getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
    final JPopupMenu runFileMenu = new JPopupMenu();
    final JMenuItem runMenuItem = new JMenuItem("Run", new ImageIcon(((new ImageIcon("icons/runFile.jpg")).getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
    final JMenuItem deleteMenuItem = new JMenuItem("Delete", new ImageIcon(((new ImageIcon("icons/remove.png")).getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
    JButton btnDelete = null;
    
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
	

	/**
	 * Avvio dell'applicazione
	 * 
	 * @param args array passato all'avvio dell'applicazione. Il primo elemento
     * di tale array indica se far partire l'applicazione in modalità di debug
     * (args[0] = "debug") o no (senza parametri)
	 */
	public static void main(String[] args) {
		debug = (args.length > 0 && args[0].equals("debug")) ? true : false;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					fileSharingApplication window = new fileSharingApplication();
					window.frmTestFrameworkGui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Metodo privato che controlla se la risorsa e' gia' nella tabella.
	 * 
	 * Utilizzato in import e download per evitare di ripetere le chiamate.
	 * 
	 * @param resName il nome della risorsa da controllare
	 * 
	 * @return vero se la risorsa resName è già nella tabella, falso altrimenti
	 */
	private static boolean alreadyExists(String resName) {
		File resFolder = new File("resources/");
		File[] list = resFolder.listFiles();
		if (debug)
			System.out.println("Stampa dei file nella cartella resources:");
		for (int i=0 ; i<list.length ; ++i) {
			if (debug)
				System.out.println(list[i].getName());
			if(list[i].getName().equals(resName))
				return true;
		}
		return false;
	}
	
	/**
	 * Metodo privato che copia un file.
	 * 
	 * @param src percorso assoluto del file;
	 * @param dst percorso assoluto della destinazione scelta.
	 * 
	 * @return vero se la copia è avvenuta con successo, falso altrimenti
	 */
	private boolean copyFile(String src, String dst) {
		InputStream inStream = null;
        OutputStream outStream = null;
        try {
        	File file1 =new File(src);
        	File file2 =new File(dst);
 
            inStream = new FileInputStream(file1);
            outStream = new FileOutputStream(file2); 
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0)
            	outStream.write(buffer, 0, length);
 
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
 
            if (debug)
            	System.out.println("File " + src + " copiato");
            return true;
        } catch(IOException e) {
        	if (debug)
        		e.printStackTrace();
        	return false;
        }
	}
	
	/**
	 * Metodo che fa partire l'applicazione
	 */
	public fileSharingApplication() {
		initialize();
	}
	

	/**
	 * Metodo che consente al client di uscire in modo pulito
	 */
	private void close() {
		disconnect = true;
		boolean old_enabled = btnDelete.isEnabled();
		btnDelete.setEnabled(true);
		btnDelete.doClick();
		btnDelete.setEnabled(old_enabled);
	}
		
	/**
	 * Metodo privato che abilita o meno gli item del popup su un
	 * un campo di testo
	 * 
	 * @param component una stringa che indica il componente da abilitare/disabilitare
	 */
	private void enabledDisabledMenuItems(String component) {
		Transferable clipboardContent = clipboard.getContents(null);
		if (clipboardContent!=null && (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor)))
			pasteMenuItem.setEnabled(true);
		else
			pasteMenuItem.setEnabled(false);
		if ((component.equals("txtIpTracker") ? txtIpTracker : txtInsertFileTo).getSelectedText() != null) {
			cutMenuItem.setEnabled(true);
			copyMenuItem.setEnabled(true);
		} else {
			cutMenuItem.setEnabled(false);
			copyMenuItem.setEnabled(false);
		}
	}
	
	/**
	 * Metodo privato che abilita o meno gli item del popup sulla tabella
	 */
	private void enabledDisabledMenuItems() {
		
		if(!pc.trackerIsDown) {
			deleteMenuItem.setEnabled(true);
		}
		else {
			deleteMenuItem.setEnabled(false);
		}
	}
	
	final JButton btnImport = new JButton("Import...");
	final JLabel lblStatus = new JLabel("Status: Offline");
	final JButton btnConnect = new JButton("    Connect   ");

	/**
	 * Metodo privato che inizializza l'interfaccia grafica e tutti i listener associati
	 * ai vari bottoni e ai campi di testo
	 */
	private void initialize() {
		frmTestFrameworkGui = new JFrame();
		frmTestFrameworkGui.setResizable(false);
		frmTestFrameworkGui.setTitle("MyFileSharingApp");
		frmTestFrameworkGui.setBounds(100, 100, 733, 493);
		
		/* Inizializzazione del lock utilizzato per l'elezione */
		l = new ReentrantLock();
		
		Thread electionWorker = new Thread(
				  new Runnable() {
		                public void run() {
		                	if (debug)
		                		System.out.println("Avviato il thread di elezione");
		                	while(true) {	
		                		
		                		if(pc == null) {
		                			try {
		                				Thread.sleep(5000);
		                			} catch (InterruptedException e) {}
		                			continue;
		                		}
		                		
		                		// non riguarda l'elezione. Controllo se il tracker è giu
		                		// o no per impostare correttamente la label riguardo lo
		                		// status
		                		if(!pc.trackerIsDown) {
		                			lblStatus.setText("Status: Online");
		                			btnImport.setEnabled(true);
		                			btnConnect.setEnabled(true);
		                			if (table.getSelectedRows().length > 0) {
		                				btnDelete.setEnabled(true);
		                			}
		                		}
		                		else {
		                			lblStatus.setText("Status: Online, but tracker is down!");
		                			btnImport.setEnabled(false);
		                			btnConnect.setEnabled(false);
		                			btnDelete.setEnabled(false);
		                		}
		                		
		                		l.lock(); /* I dati protetti dal lock sono l'attributo
		                				   * booleano "election" e l'attributo PeerClient
		                				   * "pc"
		                				   */
		                		try {
		                			Enumeration<String> e = pc.myPS.getTable().keys();
		                			while(e.hasMoreElements()) {
		                				String key = e.nextElement();
			                			PeerTable pt = pc.myPS.getTable().get(key);
			                			// se si trova almeno una risorsa per la quale il PeerClient è coordinatore,
			                			// allora il PeerClient deve diventare un SuperPeerClient
			                			if (pt.getCoord() != null && pt.getCoord().peer.equals(pc.myIp) && !elected) {
			                				String coord = "rmi://"+pc.myIp+"/"+"SuperPeer"+pc.myIp;
			    		    				SuperPeer c = pc.getCoord(coord);
			                				try {
												pc = new SuperPeerClient(pc, c, tr);
											} catch (UnknownHostException e1) {
												if (debug) {
													System.out.println("Thread di elezione: errore nella creazione di un SuperPeerClient");
													e1.printStackTrace();
												}
											}
			                				if (debug)
			                					System.out.println("Mi istanzio un nuovo oggetto SuperPeer!");
											pc.setDebug(debug);
											elected = true;
											break;
			                			}
			                			
		                			}
		                		} catch (RemoteException e1) {
		                			if (debug)
		                				e1.printStackTrace();
		                		}
								l.unlock();
			                    try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									if (debug)
										e.printStackTrace();
								} 
			                }
		                }
		            });
		electionWorker.start();
		
		frmTestFrameworkGui.addWindowStateListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	close();
	        }
	    });
		
		JPanel panel = new JPanel();
		JPanel panel_1 = new JPanel();
		JPanel panel_4 = new JPanel();
		GroupLayout groupLayout = new GroupLayout(frmTestFrameworkGui.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_4, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
						.addComponent(panel_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(41, Short.MAX_VALUE)
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 347, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
		
	    cutpasteMenu.add(cutMenuItem);
        cutpasteMenu.add(copyMenuItem);
        cutpasteMenu.add(pasteMenuItem);
        runFileMenu.add(runMenuItem);
        runFileMenu.add(deleteMenuItem);
	    cutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JTextField jte = (JTextField)cutpasteMenu.getInvoker();
	            jte.cut();
			}
	    });
	    copyMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JTextField jte = (JTextField)cutpasteMenu.getInvoker();
	            jte.copy();
			}
	    });
	    pasteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JTextField jte = (JTextField)cutpasteMenu.getInvoker();
	            jte.paste();
			}
	    });
		
		txtIpTracker = new JTextField();
		txtIpTracker.setText("insert tracker ip");
		panel_4.add(txtIpTracker);
		txtIpTracker.setColumns(10);		
		txtIpTracker.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
            	if (SwingUtilities.isLeftMouseButton(e) && txtIpTracker.isEnabled() && txtIpTracker.getText().equals("insert tracker ip")) {
            		txtIpTracker.setText("");
            	}
            }
        });
		txtIpTracker.addKeyListener
			(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER)
						btnConnect.doClick();
				}
	        });
		txtIpTracker.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e) && txtIpTracker.isEnabled()) {
            		enabledDisabledMenuItems("txtIpTracker");
            		cutpasteMenu.show(e.getComponent(), e.getX(), e.getY());
            	}
            }
        });
		
		lblStatus.setBounds(12, 12, 685, 15);
		panel.add(lblStatus);
		
		final JButton btnDownload = new JButton(" Download ");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				if (firstConnect) {
					try {
						pc = new PeerClient(txtIpTracker.getText());
						pc.setDebug(debug);
						pc.startPollingWorker();
					} catch (UnknownHostException e) {
						if (debug) {
							System.out.println("Impossibile inizializzare l'oggetto PeerClient: "+e.getMessage());
							e.printStackTrace();
						}
						return;
					}
					firstConnect = false;
				} else
					assert pc != null : "L'oggetto PeerClient non e' inizializzato ma la GUI e' gia' connessa";

				tr = pc.getTracker(server);
				
				if(tr == null && btnConnect.getText().equals("Disconnect")) {
					JOptionPane.showMessageDialog(null, "Unable to disconnect from Tracker, it is down", "Error", JOptionPane.ERROR_MESSAGE);
					btnImport.setEnabled(false);
					btnConnect.setEnabled(false);
					btnDelete.setEnabled(false);
					lblStatus.setText("Status: Online, but tracker is down!");
					
					return;
				}
				
				if(tr == null) {
					JOptionPane.showMessageDialog(null, "Unable to connect to Tracker", "Error", JOptionPane.ERROR_MESSAGE);
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnDownload.setEnabled(false);
					table.clearSelection();
					table.setEnabled(false);
					btnImport.setEnabled(false);
					btnDelete.setEnabled(false);
					btnConnect.setText("    Connect   ");
					lblStatus.setText("Status: Offline");
					return;
				}
				
				// Quando si preme il pulsante Connect
				if (btnConnect.getText().equals("    Connect   ")) {
					txtIpTracker.setEnabled(false);
					txtInsertFileTo.setEnabled(true);
					btnDownload.setEnabled(true);
					table.setEnabled(true);
					btnImport.setEnabled(true);
					btnConnect.setText("Disconnect");
					lblStatus.setText("Status: Online");
					
					File resFolder = new File("resources/");
					File[] list = resFolder.listFiles();
					connect = true;
					boolean wasEnabled = btnImport.isEnabled();
					btnImport.setEnabled(true);
					for (int i=0 ; i<list.length ; ++i) {
						if(list[i].getName().equals(".gitignore"))
							continue;
						tmpFile = list[i];
						if (debug)
							System.out.println("Sto riregistrando la risorsa "+list[i].getName());
						btnImport.doClick();
						try {
							if (debug) {
								System.out.println("Ho finito di reimportare la risorsa "+list[i].getName()+", stampo la sua tabella");
								pc.myPS.getTable().get(list[i].getName()).print();
							}
						} catch (RemoteException e) {
							if (debug)
								e.printStackTrace();
						}
					}
					btnImport.setEnabled(wasEnabled);
					connect = false;
				} else { // Quando si preme il pulsante Disconnect
					if (debug)
						System.out.println("Disconnessione in corso...");
					close();
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnDownload.setEnabled(false);
					table.clearSelection();
					table.setEnabled(false);
					btnImport.setEnabled(false);
					btnDelete.setEnabled(false);
					btnConnect.setText("    Connect   ");
					lblStatus.setText("Status: Offline");
				}
				pc.trackerIp = txtIpTracker.getText();
			}
		});
		panel_4.add(btnConnect);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		txtInsertFileTo = new JTextField();
		txtInsertFileTo.setText("insert file to download...");
		txtInsertFileTo.setEnabled(false);
		panel_2.add(txtInsertFileTo);
		txtInsertFileTo.setColumns(10);
		txtInsertFileTo.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
            	if (txtInsertFileTo.isEnabled()) {
            		table.clearSelection();
            		btnDelete.setEnabled(false);
            	}
            	if (txtInsertFileTo.getText().equals("insert file to download...") &&
            		SwingUtilities.isLeftMouseButton(e) && txtInsertFileTo.isEnabled())
            		txtInsertFileTo.setText("");
            }
        });
		txtInsertFileTo.addKeyListener
		(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER)
					btnDownload.doClick();
			}
        });
		txtInsertFileTo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e) && txtInsertFileTo.isEnabled()) {
            		enabledDisabledMenuItems("txtInsertFileTo");
            		cutpasteMenu.show(e.getComponent(), e.getX(), e.getY());
            	}
            }
        });
		
		btnDownload.setEnabled(false);
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pc == null) {	
					JOptionPane.showMessageDialog(null, "PeerClient object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				tr = pc.getTracker(server);
				if (debug)
					System.out.println("Il Tracker e': " + tr);
				if(tr == null) {
					btnImport.setEnabled(false);
					btnConnect.setEnabled(false);
					btnDelete.setEnabled(false);
					lblStatus.setText("Status: Online, but tracker is down!");
				}
				
				String resName = txtInsertFileTo.getText();
				
				if(alreadyExists(resName)) {
					JOptionPane.showMessageDialog(null, "You already have that resource dude!", "Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(debug)
					System.out.println("Client in modalita' request");
				
				String prevC = null;
				if (tr != null)
					prevC = pc.simpleResourceRequest(tr, resName);
				SuperPeer c=null;
				String coord = null;
				if(tr == null || (prevC != null && prevC.equals("exception"))) {
					PeerTable pt = null;
					String p = "";
					Enumeration<String> en = null;
					
					/* Recuperiamo l'IP di un coordinatore di risorsa */
					try {
						en =  pc.myPS.getTable().keys();
					} catch (RemoteException e2) {}
					while(en.hasMoreElements() && p.equals("")) {
						if (debug)
							System.out.println("Sono nel ciclo di controllo dei coordinatori, ne cerco uno valido");
						String key = en.nextElement();
						try {
							pt = pc.myPS.getTable().get(key);
						} catch (RemoteException e1) {
							if (debug)
								e1.printStackTrace();
						}
						/* Utilizziamolo per richiedere la risorsa */
						if (pt != null) {
							if(pt.getCoord() == null || (p = pt.getCoord().peer).equals(""))
								continue;
							String s = "rmi://"+p+"/SuperPeer"+p;
							SuperPeer sp = pc.getCoord(s);
							prevC = pc.simpleResourceRequest(sp, resName);
							if (debug)
								System.out.println("Sono nel ciclo, ho trovato come coordinatore "+prevC);
							if (prevC == null|| prevC.equals("")) {
								p = "";
								continue;
							}
							
							coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;
							try {
								c = pc.getCoord(coord);
							
								c.ping();
							} catch (Exception exc) {
								if (debug)
									System.out.println("Il coordinatore "+prevC+" non e' valido, continuo");
								p = "";
								continue;
							}
						} else {
							JOptionPane.showMessageDialog(null, "Lost connection to the network!", "Error",JOptionPane.ERROR_MESSAGE);
							txtIpTracker.setEnabled(true);
							txtInsertFileTo.setEnabled(false);
							btnDownload.setEnabled(false);
							table.clearSelection();
							table.setEnabled(false);
							btnImport.setEnabled(false);
							btnDelete.setEnabled(false);
							btnConnect.setText("    Connect   ");
							lblStatus.setText("Status: Offline");
							return;
						}
					}
				}
				
				coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;
				
				c = pc.getCoord(coord);
				
				if(c == null) {
					try {
						if(tr == null) {
							JOptionPane.showMessageDialog(null, "Unable to disconnect from Tracker, it is down", "Error", JOptionPane.ERROR_MESSAGE);
							btnImport.setEnabled(false);
							btnConnect.setEnabled(false);
							btnDelete.setEnabled(false);
							lblStatus.setText("Status: Online, but tracker is down!");
							return;
						}
						tr.cambioCoordinatore("",resName);
					} catch (RemoteException e1) {
						if (debug)
							e1.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "Coordinator not found, he probably left the network unpolitely!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Vector<String> ipList = pc.getList(c, resName);
				int count = 0;
				while (ipList == null) {
					count++;
					if (count == 3) {
						try {
							tr.cambioCoordinatore("",resName);
						} catch (RemoteException e1) {
							if (debug)
								e1.printStackTrace();
						}
						JOptionPane.showMessageDialog(null, "Coordinator not found, he probably left the network unpolitely!", "Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (debug)
						System.out.println("Il coordinatore non sta rispondendo");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
						if (debug) {
							System.out.println("Eccezione mentre dormo: " + ex.getMessage());
							ex.printStackTrace();
						}
					}
					
					prevC = pc.advancedResourceRequest(tr, resName, prevC);
					coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;				
					SuperPeer c1 = pc.getCoord(coord);
					
					assert c1 != null : "L'oggetto SuperPeer non e' definito!";
					
					ipList = pc.getList(c1, resName);
				}
				
				PeerTable pt = new PeerTable();
				pt.setDebug(debug);
				for (int i=0 ; i<ipList.size() ; ++i) {
					
					String peer = ipList.get(i);
					
					if(peer.equals("")) {
						JOptionPane.showMessageDialog(null, "Resource "+resName+" not found in the network!", "Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					peer = "rmi://"+peer+"/"+"Peer"+peer;
					Peer p = pc.getPeer(peer);
					
					if(p == null)
						continue;
					
					PeerTableData pd = new PeerTableData(ipList.get(i), pc.discovery(p),
														 false, ipList.get(i).equals(prevC)?true:false);
					pt.add(pd);
				}
				
				if (debug)
					pt.print();
				try {
					pc.myPS.addToTable(resName, pt);
				} catch (RemoteException e2) {
					if (debug) {
						System.out.println("Problema ad aggiungere una entry alla tabella delle risorse:" + e2.getMessage());
						e2.printStackTrace();
					}
				}
				
				try {
					pt = pc.myPS.getTable().get(resName);
				} catch (RemoteException e1) {
					if (debug) {
						System.out.println("Problema nel recuperare la tabella delle risorse: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
				
				try {
					pc.myPS.setAvgDist(pt.getAvgDist(InetAddress.getLocalHost().getHostAddress()));
				} catch (Exception e1) {
					if (debug) {
						System.out.println("Problema nel settare la distanza media: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
				
				String closestPeer = pt.getMinDistPeer();
				String closestPeerServer = "rmi://"+closestPeer+"/"+"Peer"+closestPeer;
				boolean fail = false;
				
				for(int k=0 ; k<ipList.size() ; ++k) {
					if(fail) {
						if(ipList.get(k).equals(closestPeer))
							continue;
						closestPeerServer = "rmi://"+ipList.get(k)+"/"+"Peer"+ipList.get(k);
						fail = false;
					}
					
					Peer p = pc.getPeer(closestPeerServer);
					
					if(p == null) {
						fail = true;
						continue;
					}
					
					//richiedi la risorsa..
					if (pc.getResource(p, resName)) {
						DefaultTableModel model = (DefaultTableModel) table.getModel();
						model.addRow(new Object[]{resName});
						//aggiorna la distanza media dato che a sto punto saro' stato aggiunto nella tabella..
						try {
							pc.myPS.setAvgDist(pc.myPS.getTable().get(resName).getAvgDist(pc.myIp));
							if (debug)
								System.out.println("Nuova avgDist: "+pc.myPS.getAvgDist());
						} catch (RemoteException e1) {
							if (debug) {
								System.out.println("Impossibile settare il nuovo avgDist: " + e1.getMessage());
								e1.printStackTrace();
							}
						}
						
						//comunico agli altri peer (tranne chi me l'ha data) che la ho anche io
						for(int i=0 ; i<ipList.size() ; ++i) {
							String peer = ipList.get(i);
							if(!peer.equals(closestPeer)) {
								peer = "rmi://"+peer+"/"+"Peer"+peer;
								
								Peer pe = pc.getPeer(peer);
								if(pe!= null) {
									try {
										pe.addNewPeer(resName, pc.myIp);
									} catch (RemoteException e1) {
										if (debug)
											e1.printStackTrace();
									}
								}
								if(debug) {
									System.out.println("Notificando il mio arrivo al peer "+ipList.get(i));
								}
							}
						}
						break;
					} else {
						JOptionPane.showMessageDialog(null, "Transferring of the resource failed", "Warning!", JOptionPane.WARNING_MESSAGE);
						fail = true;
					}
				}
			}
		});
		panel_2.add(btnDownload);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(null);
		
		JLabel lblFileSacricati = new JLabel("Resources List");
		lblFileSacricati.setBounds(12, 25, 149, 15);
		panel_3.add(lblFileSacricati);
		
		table = new JTable(new DefaultTableModel(new Object[]{"Resource Name"},0) {
			
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
			    //all cells false
			    return false;
			}
		});
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBackground(Color.WHITE);
		table.setBounds(0, 52, 550, 321);
		table.setEnabled(false);
		table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e)) {
            		if (table.rowAtPoint(e.getPoint()) >= 0) {
            			if(!pc.trackerIsDown) {
            				// get the row index that contains that coordinate
                			int rowNumber = table.rowAtPoint(e.getPoint());
                 
                			// Get the ListSelectionModel of the JTable
                			ListSelectionModel model = table.getSelectionModel();
                 
                			// set the selected interval of rows. Using the "rowNumber"
                			// variable for the beginning and end selects only that one row.
                			model.setSelectionInterval(rowNumber, rowNumber);
                			
                			btnDelete.setEnabled(true);
            			} else {
            				btnDelete.setEnabled(false);
            			}
            			enabledDisabledMenuItems();
            			runFileMenu.show(e.getComponent(), e.getX(), e.getY());
            		} else {
            			table.clearSelection();
            			btnDelete.setEnabled(false);
            		}
            	} else if (SwingUtilities.isLeftMouseButton(e)) {
            		if (table.rowAtPoint(e.getPoint()) >= 0) {
            			if(!pc.trackerIsDown) {
            				btnDelete.setEnabled(true);
            			} else {
            				btnDelete.setEnabled(false);
            			}
            		} else {
            			table.clearSelection();
            			btnDelete.setEnabled(false);
            		}
            	}
            }
        });
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0)
					runMenuItem.doClick();  
			}
		});
		panel_3.add(table);
		
		runMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int[] selectedRows = table.getSelectedRows();
				try {
					if (debug)
						System.out.println("Sto eseguendo il file "+model.getValueAt(selectedRows[0], 0).toString());
					Process pr = Runtime.getRuntime().exec("gnome-open resources/"+model.getValueAt(selectedRows[0], 0).toString());
					pr.waitFor();
				} catch (InterruptedException ie) {
					if (debug)
						System.out.println("Esecuzione del sottoprocesso interrotta (CTRL+C?)");
				} catch (IOException ioe) {
					if (debug)
						System.out.println("Impossibile eseguire questo file");
				}
			}
		});

		btnImport.setEnabled(false);
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pc == null) {	
					JOptionPane.showMessageDialog(null, "PeerClient object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String server = "rmi://"+pc.trackerIp+"/Tracker";
				tr = pc.getTracker(server);
				
				if(tr == null) {
					JOptionPane.showMessageDialog(null, "Tracker is unreachable, resource import not available!", "Error", JOptionPane.ERROR_MESSAGE);
					btnImport.setEnabled(false);
					btnConnect.setEnabled(false);
					btnDelete.setEnabled(false);
					lblStatus.setText("Status: Online, but tracker is down!");
					return;
				}
				int selezione = 0;
				File f = null;
				JFileChooser fc = null;
				if (!connect) {
					fc = new JFileChooser("~");
					fc.setMultiSelectionEnabled(true);
					selezione = fc.showDialog(null, "Seleziona il file da aprire");
				} else
					selezione = JFileChooser.APPROVE_OPTION;
				
                if (selezione == JFileChooser.APPROVE_OPTION) {
                	if (connect)
                		f = tmpFile;
                	else
                		f = fc.getSelectedFile();
                	if (alreadyExists(f.getName()) && !connect) {
    					JOptionPane.showMessageDialog(null, "You already have that resource dude!", "Warning",JOptionPane.WARNING_MESSAGE);
    					return;
    				}
                	boolean copyOk = false;
                	if (debug)
                		System.out.println(f.getAbsolutePath());
	                if(!connect)
	                	copyOk = copyFile(f.getAbsolutePath(),"resources/"+f.getName());
	                else
	                	copyOk = true;
	                if(!copyOk) {
	                	JOptionPane.showMessageDialog(null, "Failed to copy the resource! Do you have read permissions..? Or maybe the resource simply doesn't exists..", "Error",JOptionPane.ERROR_MESSAGE);
	                	return;
	                }
	                
	                String resName = f.getName();
	                
	                if (debug)
	    				System.out.println("Client in modalita' registrazione");
	    			Vector<String> resNames = new Vector<String>();
	    			resNames.add(resName);
	    			
	    			//register new resources
	    			Vector<String> coords = pc.registerResources(tr, resNames);
	    			
	    			assert coords.size() == resNames.size() : "le dimensioni di coords e resNames non matchano!";
	    			
	    			//add coordinators in the hashtable
	    			for (int i=0 ; i<coords.size() ; ++i) {
	    				try {
	    					PeerTable temp = new PeerTable(new PeerTableData(coords.get(i),0, false, true));
	    					temp.setDebug(debug);
							pc.myPS.addToTable(resNames.get(i), temp);	
						} catch (RemoteException e2) {
							if (debug) {
								System.out.println("Impossibile aggiungere un elemento alla resourceTable");
								e2.printStackTrace();
							}
						}
	    				
	    				if (debug)
	    					System.out.println(coords.get(i) +"    "+ pc.myIp);
	    				
	    				if (!coords.get(i).equals(pc.myIp)) {
	    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
	    					SuperPeer c = pc.getCoord(coord);
	    					
	    					assert c != null : "L'oggetto SuperPeer non e' definito!";
	    					if (debug)
	    						System.out.println("Sto registrando una risorsa gia' presente nella rete: "+resNames.get(i)+", lo notifico al coordinatore, che e': "+coord);
	    					pc.registerResources(c, resNames);
	    					try {
		    					PeerTable pt = pc.myPS.getTable().get(resNames.get(i));
		    					pt.add(new PeerTableData(pc.myIp, 0,
										   false, false));
		    					pc.myPS.addToTable(resNames.get(i), pt);
	    					} catch(Exception e1) {
	    						if(debug)
	    							System.out.println("Impossibile aggiungere me stesso alla peerTable: "+e1.getMessage());
	    					}
	    					
	    				} else {
	    					if(debug)
	    						System.out.println("Sono io il nuovo coordinatore per la risorsa "+resNames.get(i));
	    					
		    				String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
		    				SuperPeer c = pc.getCoord(coord);
		    				try {
								c.register(pc.myIp, resNames);
							} catch (RemoteException e2) {
								if (debug) {
									System.out.println("Impossibile registrarmi come coordinatore nel mio SuperPeerServer");
									e2.printStackTrace();
								}
							}
		    				l.lock();
		    				if (!elected) {
		    					try {
									pc = new SuperPeerClient(pc, c, tr);
									if (debug)
										System.out.println("Istanziato riferimento al (Super)PeerClient: " + pc);
									pc.setDebug(debug);
									
								} catch (UnknownHostException e1) {
									if (debug) {
										System.out.println("Impossibile diventare il nuovo coordinatore: "+e1.getMessage());
										e1.printStackTrace();
									}
								}
		    					elected = true;
	    					}
		    				l.unlock();
	    				}
	    					
	    			}
	    			
	    			DefaultTableModel model = (DefaultTableModel) table.getModel();
	    			boolean found = false;
	    			for (int i=0 ; i<model.getRowCount() ; ++i) {
	    				if(model.getValueAt(i, 0).equals(resName)) {
	    					found = true;
	    					break;
	    				}	
	    			}
	    			if (!found) 
	    				model.addRow(new Object[]{resName});
                }
			}
		});
		btnImport.setBounds(573, 82, 117, 25);
		panel_3.add(btnImport);
		
		btnDelete = new JButton("Delete");
		
		btnDelete.setEnabled(false);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int[] selectedRows;
				if (debug) {
					System.out.println("Chiamata la delete con disconnect == "+ disconnect);
					System.out.println("Disconnesso riferimento al (Super)PeerClient: " + pc);
				}
				if(disconnect) {
					/* se non siamo superpeer le seguenti due istruzioni comunque non fanno danni */
					elected = false;
					if (debug)
						System.out.println("Eseguo la stopListRetriever sul (Super)PeerClient: " + pc);
					pc.stopListRetriever();
					selectedRows = new int[model.getRowCount()];
					for(int i=0 ; i<model.getRowCount() ; ++i)
						selectedRows[i] = i;
				} else
					selectedRows = table.getSelectedRows();
				
				for (int i=selectedRows.length-1 ; i>=0 && selectedRows.length>0 ; --i) {
					if (debug)
						System.out.println("Riga selezionata: "+selectedRows[i]);
					PeerTable pt = null;
					try {
						pt = pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0));
					} catch (RemoteException e1) {
						if (debug) {
							System.out.println("Impossibile recuperare la resourceTable dal mio server");
							e1.printStackTrace();
						}
					}
					if (debug) {
						System.out.println("Tabella prima crash");
						pt.print();
					}
					String coord = pt.getCoord().peer;
					String server = "rmi://"+coord+"/"+"SuperPeer"+coord;
					SuperPeer c = pc.getCoord(server);
					
					//se sono io il coordinatore faccio partire l'election
					if (debug)
						System.out.println("Mio indirizzo ip " + pc.myIp + " coordinatore " + coord);
					if(pc.myIp.equals(coord))
						pc.startElection(model.getValueAt(selectedRows[i], 0).toString(),true,tr,null);					
					if (debug)
						System.out.println("Chiamata la goodbye sul superpeer "+coord);
					try {
						pc.goodbye(c, model.getValueAt(selectedRows[i], 0).toString());
					} catch (RemoteException e2) {
						if (debug)
							e2.printStackTrace();
					}
					
					if (debug) {
						try {
							pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0).toString()).print();
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
					}
					
					//rimuovo dalla tabella del peer..
					try {
						pc.myPS.removeFromTable(model.getValueAt(selectedRows[i], 0).toString());
						if (debug) {
							System.out.println("Rimuovo dalla tabella della risorsa "+model.getValueAt(selectedRows[i], 0).toString()+
											   "l'ip "+pc.myIp);
							try {
								pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0).toString()).print();
							} catch (RemoteException e1) {
								e1.printStackTrace();
							} catch (NullPointerException ne) {
								System.out.println("Provo a stampare la tabella dopo la delete ma fallisco");
							}
						}
						
						//pc.myPS.addToTable(pc.myIp, ptable);
					} catch (RemoteException e1) {
						if (debug)
							e1.printStackTrace();
					}
					
					if (!disconnect) {
						File f = new File("resources/"+model.getValueAt(selectedRows[i], 0).toString()); 
						if(!f.delete())
							JOptionPane.showMessageDialog(null, "Unable to delete file from filesystem!", "Error",JOptionPane.ERROR_MESSAGE);
						
						model.removeRow(selectedRows[i]);
					}	
				}
				disconnect = false;
				btnDelete.setEnabled(false);
			}
		});
		btnDelete.setBounds(573, 119, 117, 25);
		panel_3.add(btnDelete);
		
		deleteMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDelete.doClick();
			}
		});
		
		panel.setLayout(null);

		frmTestFrameworkGui.getContentPane().setLayout(groupLayout);
	}

}

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
	 * Launch the application.
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
            	System.out.println("File " + src + " Copied");
            return true;
        } catch(IOException e) {
        	e.printStackTrace();
        	return false;
        }
	}
	
	/**
	 * Create the application.
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
	
	final JButton btnImport = new JButton("Import...");
	final JLabel lblStatus = new JLabel("Status: Offline");

	/**
	 * Inizializza il contenuto di un frame.
	 */
	private void initialize() {
		frmTestFrameworkGui = new JFrame();
		frmTestFrameworkGui.setResizable(false);
		frmTestFrameworkGui.setTitle("test Framework GUI");
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
		                		// o no per settare la label giusta riguardo lo status
		                		if(!pc.trackerIsDown) {
		                			lblStatus.setText("Status: Online");
		                			btnImport.setEnabled(true);
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
												System.out.println("Thread di elezione: errore nella creazione di un SuperPeerClient");
												e1.printStackTrace();
											}
			                				if (debug)
			                					System.out.println("!*!*! Mi istanzio un nuovo oggetto SuperPeer! !*!*!");
											pc.setDebug(debug);
											elected = true;
											break;
			                			}
			                			
		                			}
		                		} catch (RemoteException e1) {
		                			e1.printStackTrace();
		                		}
								l.unlock();
			                    try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} 
			                }
		                }
		            });
		/*	fine thread */
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
		
		final JButton btnConnect = new JButton("    Connect   ");
		
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
		
		final JButton btnNewButton = new JButton(" Download ");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				if (firstConnect) {
					try {
						pc = new PeerClient(txtIpTracker.getText());
						pc.setDebug(debug);
						pc.startPollingWorker();
					} catch (UnknownHostException e) {
						System.out.println("Unable to initialize PeerClient object: "+e.getMessage());
						e.printStackTrace();
						return;
					}
					firstConnect = false;
				} else
					assert pc != null : "PeerClient object not initialized but GUI already connected";

				tr = pc.getTracker(server);
				
				if(tr == null) {
					JOptionPane.showMessageDialog(null, "Unable to connect to Tracker", "Error", JOptionPane.ERROR_MESSAGE);
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnNewButton.setEnabled(false);
					table.clearSelection();
					table.setEnabled(false);
					btnImport.setEnabled(false);
					btnDelete.setEnabled(false);
					btnConnect.setText("    Connect   ");
					lblStatus.setText("Status: Offline");
				}
				
				// Quando si preme il pulsante Connect
				if (btnConnect.getText().equals("    Connect   ")) {
					txtIpTracker.setEnabled(false);
					txtInsertFileTo.setEnabled(true);
					btnNewButton.setEnabled(true);
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
							if (debug)
								System.out.println("Ho finito di reimportare la risorsa "+list[i].getName()+", stampo la sua tabella");
							PeerTable pt = pc.myPS.getTable().get(list[i].getName());
							pt.add(new PeerTableData(pc.myIp, 0,
								   false, pt.get().isEmpty() ? true : false));
							pc.myPS.addToTable(list[i].getName(), pt);
							pc.myPS.getTable().get(list[i].getName()).print();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					btnImport.setEnabled(wasEnabled);
					connect = false;
				}
				
				// Quando si preme il pulsante Disconnect
				else {
					if (debug)
						System.out.println("#### Disconnessione in corso ####");
					close();
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnNewButton.setEnabled(false);
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
            	if (txtInsertFileTo.getText().equals("insert file to download...") && SwingUtilities.isLeftMouseButton(e) && txtInsertFileTo.isEnabled())
            		txtInsertFileTo.setText("");
            }
        });
		txtInsertFileTo.addKeyListener
		(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER)
					btnNewButton.doClick();
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
		
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			/*							LISTENER PULSANTE DI DOWNLOAD				*/
			public void actionPerformed(ActionEvent e) {
				
				if(pc == null) {	
					JOptionPane.showMessageDialog(null, "PeerClient object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				tr = pc.getTracker(server);
				if (debug)
					System.out.println("Tracker after getTracker(): " + tr);
				if(tr == null) {
					//txtIpTracker.setEnabled(true);
					//txtInsertFileTo.setEnabled(false);
					//btnNewButton.setEnabled(false);
					//table.clearSelection();
					//table.setEnabled(false);
					btnImport.setEnabled(false);
					//btnDelete.setEnabled(false);
					//btnConnect.setText("    Connect   ");
					lblStatus.setText("Status: Online, but tracker is down!");
					return;
				}
				
				String resName = txtInsertFileTo.getText();
				
				if(alreadyExists(resName)) {
					JOptionPane.showMessageDialog(null, "You already have that resource dude!", "Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				/******************************/
				if(debug)
					System.out.println("Client in modalita' request");
				
				String prevC = null;
				if (tr != null)
					prevC = pc.simpleResourceRequest(tr, resName);
				if(tr == null || (prevC != null && prevC.equals("exception"))) {
					PeerTable pt = null;
					/* Recuperiamo l'IP di un coordinatore di risorsa */
					try {
						pt = pc.myPS.getTable().entrySet().iterator().next().getValue();
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					/* Utilizziamolo per richiedere la risorsa */
					if (pt != null) {
						String s = "rmi://"+pt.getCoord().peer+"/SuperPeer"+pt.getCoord().peer;
						SuperPeer sp = pc.getCoord(s);
						prevC = pc.simpleResourceRequest(sp, resName);
					} else {
						JOptionPane.showMessageDialog(null, "Lost connection to the network!", "Error",JOptionPane.ERROR_MESSAGE);
						txtIpTracker.setEnabled(true);
						txtInsertFileTo.setEnabled(false);
						btnNewButton.setEnabled(false);
						table.clearSelection();
						table.setEnabled(false);
						btnImport.setEnabled(false);
						btnDelete.setEnabled(false);
						btnConnect.setText("    Connect   ");
						lblStatus.setText("Status: Offline");
						return;
					}
				}
				
				if (prevC == null) {
					JOptionPane.showMessageDialog(null, "Resource not found in the network!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;
				
				SuperPeer c = pc.getCoord(coord);
				
				assert c != null : "SuperPeer object is undefined!";
				
				Vector<String> ipList = pc.getList(c, resName);
				int count = 0;
				while (ipList == null) {
					count++;
					if (count == 3) {
						try {
							tr.cambioCoordinatore("",resName);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
						JOptionPane.showMessageDialog(null, "Coordinator not found, he probably left the network unpolitely!", "Error",JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (debug)
						System.out.println("Coordinator isn't responding..");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
						System.out.println("Exception while sleeping: " + ex.getMessage());
						ex.printStackTrace();
					}
					
					prevC = pc.advancedResourceRequest(tr, resName, prevC);
					coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;				
					SuperPeer c1 = pc.getCoord(coord);
					
					/*
					 * FIXME: ha senso questa assert? come gestisce rmi il non
					 * rispondere..? fa ritornare un null?
					 */
					assert c1 != null : "SuperPeer object is undefined!";
					
					ipList = pc.getList(c1, resName);
				}
				
				PeerTable pt = new PeerTable();
				pt.setDebug(debug);
				for (int i=0 ; i<ipList.size() ; ++i) {
					
					String peer = ipList.get(i);
					peer = "rmi://"+peer+"/"+"Peer"+peer;
					Peer p = pc.getPeer(peer);
					
					assert p != null : "Peer object is undefined!";
					
					PeerTableData pd = new PeerTableData(ipList.get(i), pc.discovery(p),
														 false, ipList.get(i).equals(prevC)?true:false);
					pt.add(pd);
				}
				
				if (debug)
					pt.print();
				try {
					pc.myPS.addToTable(resName, pt);
				} catch (RemoteException e2) {
					System.out.println("Problems while adding an entry to the resource table:" + e2.getMessage());
					e2.printStackTrace();
				}
				
				try {
					pt = pc.myPS.getTable().get(resName);
				} catch (RemoteException e1) {
					System.out.println("Problems while getting the resource table: " + e1.getMessage());
					e1.printStackTrace();
				}
				
				try {
					pc.myPS.setAvgDist(pt.getAvgDist(InetAddress.getLocalHost().getHostAddress()));
				} catch (Exception e1) {
					System.out.println("Problems while setting average distance: " + e1.getMessage());
					e1.printStackTrace();
				}
				
				String closestPeer = pt.getMinDistPeer();
				closestPeer = "rmi://"+closestPeer+"/"+"Peer"+closestPeer;
				Peer p = pc.getPeer(closestPeer);
				
				assert p != null : "Peer object is undefined!";
				
				//richiedi la risorsa..
				if (pc.getResource(p, resName)) {
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.addRow(new Object[]{resName});
					//aggiorna la distanza media dato che a sto punto saro' stato aggiunto nella tabella..
					try {
						pc.myPS.setAvgDist(pc.myPS.getTable().get(resName).getAvgDist(pc.myIp));
						System.out.println("Nuova avgDist: "+pc.myPS.getAvgDist());
					} catch (RemoteException e1) {
						System.out.println("Unable to set new avgDist: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Trasferimento della risorsa fallito..", "Warning!", JOptionPane.WARNING_MESSAGE);
	
			/************************/
				
			}
		});
		panel_2.add(btnNewButton);
		
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
            			// get the row index that contains that coordinate
            			int rowNumber = table.rowAtPoint(e.getPoint());
             
            			// Get the ListSelectionModel of the JTable
            			ListSelectionModel model = table.getSelectionModel();
             
            			// set the selected interval of rows. Using the "rowNumber"
            			// variable for the beginning and end selects only that one row.
            			model.setSelectionInterval(rowNumber, rowNumber);
            			
            			btnDelete.setEnabled(true);
            			
            			runFileMenu.show(e.getComponent(), e.getX(), e.getY());
            		} else {
            			table.clearSelection();
            			btnDelete.setEnabled(false);
            		}
            	} else if (SwingUtilities.isLeftMouseButton(e)) {
            		if (table.rowAtPoint(e.getPoint()) >= 0)
            			btnDelete.setEnabled(true);
            		else {
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
						System.out.println("Trying to execute the file "+model.getValueAt(selectedRows[0], 0).toString());
					Process pr = Runtime.getRuntime().exec("gnome-open resources/"+model.getValueAt(selectedRows[0], 0).toString());
					pr.waitFor();
				} catch (InterruptedException ie) {
					System.out.println("Subprocess execution interrupted (CTRL+C?)");
				} catch (IOException ioe) {
					System.out.println("Can't execute that file, sorry :(");
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
					JOptionPane.showMessageDialog(null, "Tracker is unreachable!", "Error",JOptionPane.ERROR_MESSAGE);
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnNewButton.setEnabled(false);
					table.clearSelection();
					table.setEnabled(false);
					btnImport.setEnabled(false);
					btnDelete.setEnabled(false);
					btnConnect.setText("    Connect   ");
					lblStatus.setText("Status: Offline");
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
	                if(!copyOk) {
	                	JOptionPane.showMessageDialog(null, "Failed to copy the resource! Do you have read permissions..? Or maybe the resource simply doesn't exists..", "Error",JOptionPane.ERROR_MESSAGE);
	                	return;
	                }
	                
	                String resName = f.getName();
	                /********************************/
	                if (debug)
	    				System.out.println("Client in modalita' registrazione");
	    			Vector<String> resNames = new Vector<String>();
	    			resNames.add(resName);
	    			
	    			//register new resources
	    			Vector<String> coords = pc.registerResources(tr, resNames);
	    			
	    			assert coords.size() == resNames.size() : "coords and resNames sizes doesn't match!";
	    			
	    			//add coordinators in the hashtable
	    			for (int i=0 ; i<coords.size() ; ++i) {
	    				try {
	    					PeerTable temp = new PeerTable(new PeerTableData(coords.get(i),0, false, true));
	    					temp.setDebug(debug);
							pc.myPS.addToTable(resNames.get(i), temp);	
						} catch (RemoteException e2) {
							System.out.println("Unable to add an element to resourceTable");
							e2.printStackTrace();
						}
	    				
	    				if (debug)
	    					System.out.println("AAAAAAAAAAAAA  "+coords.get(i) +"    "+ pc.myIp);
	    				
	    				if (!coords.get(i).equals(pc.myIp)) {
	    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
	    					SuperPeer c = pc.getCoord(coord);
	    					
	    					assert c != null : "SuperPeer object is undefined!";
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
	    							System.out.println("Unable to add myself to my peerTable: "+e1.getMessage());
	    					}
	    					
	    				} else {
	    					if(debug)
	    						System.out.println("Sono io il nuovo coordinatore per la risorsa "+resNames.get(i));
	    					
		    				String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
		    				SuperPeer c = pc.getCoord(coord);
		    				try {
								c.register(pc.myIp, resNames);
							} catch (RemoteException e2) {
								System.out.println("Unable to register myself as coordinator in my SuperPeerServer");
								e2.printStackTrace();
							}
		    				l.lock();
		    				if (!elected) {
		    					try {
									pc = new SuperPeerClient(pc, c, tr);
									if (debug)
										System.out.println("### ISTANZIATO Riferimento al (Super)PeerClient: " + pc + " ###");
									pc.setDebug(debug);
									
								} catch (UnknownHostException e1) {
									System.out.println("Unable to become the new coordinator: "+e1.getMessage());
									e1.printStackTrace();
								}
		    					elected = true;
	    					}
		    				l.unlock();
	    				}
	    					
	    			}
	    			/************************************/
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
					System.out.println();
					System.out.println("### Chiamata la delete con disconnect == "+ disconnect + " ####");
					System.out.println("### DISCONNESSO Riferimento al (Super)PeerClient: " + pc + " ###");
					System.out.println();
				}
				if(disconnect) {
					/* se non siamo superpeer le seguenti due istruzioni comunque non fanno danni */
					elected = false;
					if (debug)
						System.out.println("### STOP LIST RETRIEVER Riferimento al (Super)PeerClient: " + pc + " ###");
					pc.stopListRetriever();
					selectedRows = new int[model.getRowCount()];
					for(int i=0 ; i<model.getRowCount() ; ++i)
						selectedRows[i] = i;
				}
				else
					selectedRows = table.getSelectedRows();
				
				for (int i=selectedRows.length-1 ; i>=0 && selectedRows.length>0 ; --i) {
					if (debug)
						System.out.println("Riga selezionata: "+selectedRows[i]);
					PeerTable pt = null;
					try {
						pt = pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0));
					} catch (RemoteException e1) {
						System.out.println("Unable to get resourceTable from my server");
						e1.printStackTrace();
					}
					if (debug) {
						System.out.println("****************************tabella prima crash **********************");
						pt.print();
					}
					String coord = pt.getCoord().peer;
					String server = "rmi://"+coord+"/"+"SuperPeer"+coord;
					SuperPeer c = pc.getCoord(server);
					
					//se sono io il coordinatore faccio partire l'election
					if (debug)
						System.out.println("**** MY IP " + pc.myIp + " COORD " + coord + " *******");
					if(pc.myIp.equals(coord))
						pc.startElection(model.getValueAt(selectedRows[i], 0).toString(),true,tr,null);					
					if (debug)
						System.out.println("Chiamata la goodbye sul superpeer "+coord);
					try {
						pc.goodbye(c, model.getValueAt(selectedRows[i], 0).toString());
					} catch (RemoteException e2) {
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
						//PeerTable ptable = pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0).toString());
						//ptable.get().remove(ptable.getIP(pc.myIp));
						pc.myPS.removeFromTable(model.getValueAt(selectedRows[i], 0).toString());
						if (debug)
							System.out.println("Rimuovo dalla tabella della risorsa "+model.getValueAt(selectedRows[i], 0).toString()+
											   "l'ip "+pc.myIp);
						if (debug) {
							try {
								pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0).toString()).print();
							} catch (RemoteException e1) {
								e1.printStackTrace();
							} catch (NullPointerException ne) {
								System.out.println(" ******* provo a stampare la tabella dopo la delete ma fallisco");
							}
						}
						//pc.myPS.addToTable(pc.myIp, ptable);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					
					if (!disconnect) {
						File f = new File("resources/"+model.getValueAt(selectedRows[i], 0).toString()); //TODO: compatibilita' windows..? ma anche no
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
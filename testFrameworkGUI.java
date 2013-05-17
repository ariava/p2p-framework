import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Vector;

import  java.io.*;


public class testFrameworkGUI {

	private JFrame frmTestFrameworkGui;
	private JTextField txtInsertFileTo;
	private JTable table;
	private JTextField txtIpTracker;
	private PeerClient pc;
	private Tracker tr;
	private boolean debug = true;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testFrameworkGUI window = new testFrameworkGUI();
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
	 * Parametri:
	 * resName: il nome della risorsa da controllare
	 * */
	private static boolean alreadyExists(String resName) {
		
		File resFolder = new File("resources/");
		File[] list = resFolder.listFiles();
		System.out.println("Stampa dei file nella cartella resources:");
		for(int i=0;i<list.length;++i) {
			System.out.println(list[i].getName());
			if(list[i].getName().equals(resName))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Metodo privato che copia un file.
	 * 
	 * Parametri:
	 * src: percorso assoluto del file;
	 * dst: percorso assoluto della destinazione scelta.
	 */
	private void copyFile(String src, String dst) {
		
		InputStream inStream = null;
        OutputStream outStream = null;
        try{
        	File file1 =new File(src);
        	File file2 =new File(dst);
 
            inStream = new FileInputStream(file1);
            outStream = new FileOutputStream(file2); 
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0){
            	outStream.write(buffer, 0, length);
            }
 
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
 
            System.out.println("File " + src + " Copied");
        }catch(IOException e){
        	e.printStackTrace();
        }
	}
	
	/**
	 * Create the application.
	 */
	public testFrameworkGUI() {
		initialize();
	}
	
	/*
	 * Il client esce in modo pulito
	 */
	private void close() {
		String coord = "rmi://"+pc.myIp+"/"+"SuperPeer"+pc.myIp;
    	SuperPeer c = pc.getCoord(coord);
    	try {
			c.goodbye(pc.myIp);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTestFrameworkGui = new JFrame();
		frmTestFrameworkGui.setResizable(false);
		frmTestFrameworkGui.setTitle("test Framework GUI");
		frmTestFrameworkGui.setBounds(100, 100, 733, 493);
		//frmTestFrameworkGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
		
		final JButton btnConnect = new JButton("    Connect   ");
		
		txtIpTracker = new JTextField();
		txtIpTracker.setText("insert tracker ip");
		panel_4.add(txtIpTracker);
		txtIpTracker.setColumns(10);		
		txtIpTracker.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
            	txtIpTracker.setText("");
            }
        });
		txtIpTracker.addKeyListener
			(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						btnConnect.doClick();
					}
				}
	        });
		
		final JLabel lblStatus = new JLabel("Status: Offline");
		lblStatus.setBounds(12, 12, 685, 15);
		panel.add(lblStatus);
		
		
		final JButton btnNewButton = new JButton("Download...");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				
				try {
					pc = new PeerClient(txtIpTracker.getText());
				} catch (UnknownHostException e) {
					System.out.println("Unable to initialize PeerClient object: "+e.getMessage());
					e.printStackTrace();
				}

				tr = pc.getTracker(server);
				
				if(tr == null) {
					JOptionPane.showMessageDialog(null, "Unable to connect to Tracker", "Error",JOptionPane.ERROR_MESSAGE);
					lblStatus.setText("Status: Offline");
				}
				
				if (btnConnect.getText().equals("    Connect   ")) {
					txtIpTracker.setEnabled(false);
					txtInsertFileTo.setEnabled(true);
					btnNewButton.setEnabled(true);
					btnConnect.setText("Disconnect");
					lblStatus.setText("Status: Online");
				}
				else {
					close();
					txtIpTracker.setEnabled(true);
					txtInsertFileTo.setEnabled(false);
					btnNewButton.setEnabled(false);
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
            	txtInsertFileTo.setText("");
            }
        });
		txtInsertFileTo.addKeyListener
		(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					btnNewButton.doClick();
				}
			}
        });
		
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(pc == null) {	
					JOptionPane.showMessageDialog(null, "PeerClient object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(tr == null) {	
					JOptionPane.showMessageDialog(null, "Tracker object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String resName = txtInsertFileTo.getText();
				
				if(alreadyExists(resName)) {
					JOptionPane.showMessageDialog(null, "You already have that resource dude!", "Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				/******************************/
				if(debug) {
					System.out.println("Client in modalita' request");
				}
				
				String prevC = pc.simpleResourceRequest(tr, resName);
				
				if(prevC == null) {
					JOptionPane.showMessageDialog(null, "Resource not found in the network!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String coord = "rmi://"+prevC+"/"+"SuperPeer"+prevC;
				
				SuperPeer c = pc.getCoord(coord);
				
				assert c != null : "SuperPeer object is undefined!";
				
				Vector<String> ipList = pc.getList(c, resName);
				while(ipList == null) {
					System.out.println("Coordinator isn't responding..");
					try {
						Thread.sleep(5000);	//TODO: trovare un tempo di sleep realistico
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
					 **/
					assert c1 != null : "SuperPeer object is undefined!";
					
					ipList = pc.getList(c1, resName);
				}
				
				PeerTable pt = new PeerTable();
				for(int i=0;i<ipList.size();++i) {
					
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
				if(pc.getResource(p, resName)) {
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
					JOptionPane.showMessageDialog(null, "Trasferimento della risorsa fallito..","Warning!",JOptionPane.WARNING_MESSAGE);
	
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
		
		table = new JTable(new DefaultTableModel(new Object[]{"Resource Name"},0));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBackground(Color.WHITE);
		table.setBounds(0, 52, 550, 321);
		panel_3.add(table);
		
		JButton btnImport = new JButton("Import...");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(pc == null) {	
					JOptionPane.showMessageDialog(null, "PeerClient object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(tr == null) {	
					JOptionPane.showMessageDialog(null, "Tracker object is undefined!", "Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				JFileChooser fc = new JFileChooser("~");
                fc.setMultiSelectionEnabled(true);
                int selezione = fc.showDialog(null, "Seleziona il file da aprire");
                
                File f = null;
                
                if(selezione == JFileChooser.APPROVE_OPTION) {
                	f = fc.getSelectedFile();
                	if(alreadyExists(f.getName())) {
    					JOptionPane.showMessageDialog(null, "You already have that resource dude!", "Warning",JOptionPane.WARNING_MESSAGE);
    					return;
    				}
	                System.out.println(f.getAbsolutePath());
	                
	                copyFile(f.getAbsolutePath(),"resources/"+f.getName());
	                String resName = f.getName();
	                /********************************/
	                if(debug) {
	    				System.out.println("Client in modalita' registrazione");
	    			}
	    			Vector<String> resNames = new Vector<String>();
	    			resNames.add(resName);
	    			
	    			//register new resources
	    			Vector<String> coords = pc.registerResources(tr, resNames);
	    			
	    			assert coords.size() == resNames.size() : "coords and resNames size doesn't match!";
	    			
	    			//add coordinators in the hashtable
	    			for(int i=0;i<coords.size();++i) {
	    				try {
	    					// XXX (Arianna): avgDist settata a "-1"?
							pc.myPS.addToTable(resNames.get(i),
											   new PeerTable(new PeerTableData(coords.get(i),
											   -1, false, true)));
						} catch (RemoteException e2) {
							System.out.println("Unable to add an element to resourceTable");
							e2.printStackTrace();
						}
	    				
	    				if (debug)
	    					System.out.println("AAAAAAAAAAAAA  "+coords.get(i) +"    "+ pc.myIp);
	    				
	    				if(!coords.get(i).equals(pc.myIp)) {
	    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
	    					SuperPeer c = pc.getCoord(coord);
	    					
	    					assert c != null : "SuperPeer object is undefined!";
	    					
	    					pc.registerResources(c, resNames);
	    					/*try {
	    	    				pc.myPS.syncTable(pc.resourceTable);
	    	    			} catch (RemoteException ex) {
	    	    				System.out.println("Unable to sync table with my server! I'll die horribly");
	    	    				
	    	    				ex.printStackTrace();
	    	    				System.exit(1);
	    	    			}TODO:remove*/
	    				}
	    				else {
	    					if(debug) {
	    						System.out.println("Sono io il nuovo coordinatore per la risorsa "+resNames.get(i));
	    					}
	    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer"+coords.get(i);
	    					SuperPeer c = pc.getCoord(coord);
	    					try {
								pc = new SuperPeerClient(pc,c,tr);
								
							} catch (UnknownHostException e1) {
								System.out.println("Unable to become the new coordinator: "+e1.getMessage());
								e1.printStackTrace();
							}
	    					/*try {
								c.syncTable(pc.resourceTable);
							} catch (RemoteException e1) {
								System.out.println("Non va la sync col superpeer");
								e1.printStackTrace();
							}TODO:remove*/
	    				}
	    					
	    			}
	    			/*try {
	    				pc.myPS.syncTable(pc.resourceTable);
	    			} catch (RemoteException ex) {
	    				System.out.println("Unable to sync table with my server! I'll die horribly");
	    				
	    				ex.printStackTrace();
	    				System.exit(1);
	    			}*/
	    			/************************************/
	    			DefaultTableModel model = (DefaultTableModel) table.getModel();
	    			model.addRow(new Object[]{resName});
                }
			}
		});
		btnImport.setBounds(573, 82, 117, 25);
		panel_3.add(btnImport);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int[] selectedRows = table.getSelectedRows();
				
				for(int i=selectedRows.length-1;i>=0 && selectedRows.length>0;--i) {
					System.out.println("Riga selezionata: "+selectedRows[i]);
					PeerTable pt = null;
					try {
						pt = pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0));
					} catch (RemoteException e1) {
						System.out.println("Unable to get resourceTable from my server");
						e1.printStackTrace();
					}
					System.out.println("****************************tabella prima crash **********************");
					pt.print();
					String coord = pt.getCoord().peer;
					String server = "rmi://"+coord+"/"+"SuperPeer"+coord;
					SuperPeer c = pc.getCoord(server);
					
					
					
					//se sono io il coord faccio partire l'election
					if(pc.myIp.equals(coord)) {
						pc.startElection(model.getValueAt(selectedRows[i], 0).toString(),true,tr);					
					}
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
						PeerTable ptable = pc.myPS.getTable().get(model.getValueAt(selectedRows[i], 0).toString());
						ptable.get().remove(ptable.getIP(pc.myIp));
						System.out.println("Rimuovo dalla tabella della risorsa "+model.getValueAt(selectedRows[i], 0).toString()+
								"l'ip "+pc.myIp);
						pc.myPS.addToTable(pc.myIp, ptable);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					
					File f = new File("resources/"+model.getValueAt(selectedRows[i], 0).toString()); //TODO: compatibilita' windows..? ma anche no
					if(!f.delete())
						JOptionPane.showMessageDialog(null, "Unable to delete file from filesystem!", "Error",JOptionPane.ERROR_MESSAGE);
					
					model.removeRow(selectedRows[i]);
					
					
					
				}
			}
		});
		btnDelete.setBounds(573, 119, 117, 25);
		panel_3.add(btnDelete);
		panel.setLayout(null);
		
		
		frmTestFrameworkGui.getContentPane().setLayout(groupLayout);
	}

}

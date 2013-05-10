import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.AbstractAction;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionListener;
import java.io.File;
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
		
		System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "5000");
		System.out.println(System.getProperty("sun.rmi.transport.tcp.handshakeTimeout"));
		
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
	 * TODO: commenta
	 * 
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
 
            if (inStream != null)inStream.close();
            if (outStream != null)outStream.close();
 
            System.out.println("File Copied..");
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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTestFrameworkGui = new JFrame();
		frmTestFrameworkGui.setResizable(false);
		frmTestFrameworkGui.setTitle("test Framework GUI");
		frmTestFrameworkGui.setBounds(100, 100, 733, 493);
		frmTestFrameworkGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
		
		txtIpTracker = new JTextField();
		txtIpTracker.setText("ip tracker");
		panel_4.add(txtIpTracker);
		txtIpTracker.setColumns(10);
		
		final JLabel lblStatus = new JLabel("Status: Offline");
		lblStatus.setBounds(12, 12, 685, 15);
		panel.add(lblStatus);
		
		JButton btnConnect = new JButton("   Connect    ");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String server = "rmi://"+txtIpTracker.getText()+"/"+"Tracker";
				
				try {
					pc = new PeerClient();
				} catch (UnknownHostException e) {
					System.out.println("Unable to initialize PeerClient object: "+e.getMessage());
					e.printStackTrace();
				}

				tr = pc.getTracker(server);
				
				if(tr == null) {
					JOptionPane.showMessageDialog(null, "Unable to connect to Tracker", "Error",JOptionPane.ERROR_MESSAGE);
					lblStatus.setText("Status: Offline");
				}
				
				lblStatus.setText("Status: Online");
				
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
		panel_2.add(txtInsertFileTo);
		txtInsertFileTo.setColumns(10);
		
		JButton btnNewButton = new JButton("Download...");
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
				/******************************/
				if(debug) {
					System.out.println("Client in modalita' request");
				}
				
				String prevC = pc.simpleResourceRequest(tr, resName);
				String coord = "rmi://"+prevC+"/"+"SuperPeer";
				
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
					coord = "rmi://"+prevC+"/"+"SuperPeer";				
					SuperPeer c1 = pc.getCoord(coord);
					
					assert c1 != null : "SuperPeer object is undefined!"; //FIXME: ha senso questa assert? come gestisce rmi il non
																		   //		 rispondere..? fa ritornare un null?
					
					ipList = pc.getList(c1, resName);
				}
				
				PeerTable pt = new PeerTable();
				for(int i=0;i<ipList.size();++i) {
					
					String peer = ipList.get(i);
					peer = "rmi://"+peer+"/"+"Peer"+peer;
					Peer p = pc.getPeer(peer);
					
					assert p != null : "Peer object is undefined!";
					
					PeerTableData pd = new PeerTableData(ipList.get(i),pc.discovery(p),false,ipList.get(i)==prevC?true:false);
					pt.add(pd);
				}
				pc.resourceTable.put(resName, pt);
				
				pt = pc.resourceTable.get(resName);
				
				pc.avgDist = pt.getAvgDist();
				
				String closestPeer = pt.getMinDistPeer();
				closestPeer = "rmi://"+closestPeer+"/"+"Peer"+closestPeer;
				Peer p = pc.getPeer(closestPeer);
				
				assert p != null : "Peer object is undefined!";
				
				//richiedi la risorsa..
				if(pc.getResource(p, resName)) {
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.addRow(new Object[]{resName}); 
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
                
                if(selezione == JFileChooser.APPROVE_OPTION) 
                	f = fc.getSelectedFile();
				
                System.out.println(f.getAbsolutePath());
                
                copyFile(f.getAbsolutePath(),"resources/"+f.getName());
                String resName = "resources/"+f.getName();
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
    				pc.resourceTable.put(resNames.get(i), new PeerTable(new PeerTableData(coords.get(i),-1,false,true)));
    				if(coords.get(i) != pc.myIp) {
    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer";
    					SuperPeer c = pc.getCoord(coord);
    					
    					assert c != null : "SuperPeer object is undefined!";
    					
    					pc.registerResources(c, resNames);
    				}
    				else {
    					if(debug) {
    						System.out.println("Sono io il nuovo coordinatore per la risorsa "+resNames.get(i));
    					}
    					String coord = "rmi://"+coords.get(i)+"/"+"SuperPeer";
    					SuperPeer c = pc.getCoord(coord);
    					try {
							pc = new SuperPeerClient(c,tr);
						} catch (UnknownHostException e1) {
							System.out.println("Unable to become the new coordinator: "+e1.getMessage());
							e1.printStackTrace();
						}
    				}
    					
    			}
    			try {
    				pc.myPS.syncTable(pc.resourceTable);
    			} catch (RemoteException ex) {
    				System.out.println("Unable to sync table with my server! I'll die horribly");
    				
    				ex.printStackTrace();
    				System.exit(1);
    			}
    			/************************************/
    			DefaultTableModel model = (DefaultTableModel) table.getModel();
    			model.addRow(new Object[]{resName});
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
					model.removeRow(selectedRows[i]);
					PeerTable pt = pc.resourceTable.get(model.getValueAt(i, 0));
					
					String coord = pt.getCoord().peer;
					String server = "rmi://"+coord+"/"+"SuperPeer";
					SuperPeer c = pc.getCoord(server);
					pc.goodbye(c, model.getValueAt(i, 0).toString());
				}
			}
		});
		btnDelete.setBounds(573, 119, 117, 25);
		panel_3.add(btnDelete);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnRefresh.setBounds(573, 156, 117, 25);
		panel_3.add(btnRefresh);
		panel.setLayout(null);
		
		
		frmTestFrameworkGui.getContentPane().setLayout(groupLayout);
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
			System.out.println("ciaoooooaaaaaaaaaaa");
		}
		public void actionPerformed(ActionEvent e) {
			System.out.println("ciaooooo");
		}
	}
}

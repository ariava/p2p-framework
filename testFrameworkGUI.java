import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import java.awt.CardLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.Color;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.JTextArea;
import javax.swing.JSeparator;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;


public class testFrameworkGUI {

	private JFrame frmTestFrameworkGui;
	private JTextField txtInsertFileTo;
	private JTable table;

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
		GroupLayout groupLayout = new GroupLayout(frmTestFrameworkGui.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addGap(33)
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
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
		panel_2.add(btnNewButton);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(null);
		
		JLabel lblFileSacricati = new JLabel("File scaricati");
		lblFileSacricati.setBounds(12, 25, 149, 15);
		panel_3.add(lblFileSacricati);
		
		JButton btnImport = new JButton("Import...");
		btnImport.setBounds(573, 82, 117, 25);
		panel_3.add(btnImport);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setBackground(Color.WHITE);
		table.setBounds(0, 52, 550, 321);
		panel_3.add(table);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setBounds(573, 119, 117, 25);
		panel_3.add(btnDelete);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBounds(573, 156, 117, 25);
		panel_3.add(btnRefresh);
		panel.setLayout(null);
		
		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setBounds(12, 12, 685, 15);
		panel.add(lblStatus);
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

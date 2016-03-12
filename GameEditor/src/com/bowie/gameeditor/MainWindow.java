package com.bowie.gameeditor;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.swing.JList;

import java.awt.GridLayout;

import javax.swing.JTabbedPane;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.MenuItem;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.JCheckBoxMenuItem;

import com.jogamp.nativewindow.util.Point;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.File;

public class MainWindow {

	private JFrame frmGameEditor;
	private Editor edt;
	private LogWindow logger;
	
	//list of toggles
	private JCheckBoxMenuItem toggleLogger = null;
	
	private List<Component> traverse(Container c) {
		List<Component> comps = new ArrayList<>();
		
		Component [] thisComps = c.getComponents();
		
		for (Component con : thisComps) {
			comps.add(con);
			if (con instanceof Container) {
				List<Component> myComps = traverse((Container) con);
				comps.addAll(myComps);
			}
		}
		
		return comps;
	}
	
	private Component queryComponentByName(String name) {
		List<Component> comps = traverse(frmGameEditor);
		
		for (int i=0; i<comps.size(); i++) {
			if (comps.get(i).getName() == name)
				return comps.get(i);
		}
		return null;
	}
	
	private void setupApp() {
		//next we create logger instance
		logger = new LogWindow();
		logger.setLocationRelativeTo(frmGameEditor);
		logger.setSize(frmGameEditor.getWidth(), logger.getHeight());
		logger.setLocation(frmGameEditor.getLocation().x, frmGameEditor.getLocation().y+frmGameEditor.getHeight());
		logger.setVisible(true);
		
		//spawn editor and attach logger
		edt = new Editor(logger);
		logger.addCommandListener(edt);
		
		//attach gl context to preserved canvas panel
		Component canvasPane = queryComponentByName("canvasPane");
		if (canvasPane != null) {
			if (canvasPane instanceof JPanel) {
				edt.attachToPane((JPanel) canvasPane);
				logger.log("editor attached to canvas panel");
			} else {
				logger.log("canvas panel is not a JPanel");
			}
		} else {
			logger.log("canvas panel not found");
		}
		
		//set logger listener (for closing and opening event)		
		logger.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				logger.log("Shown");
				if (toggleLogger != null) {
					toggleLogger.setState(true);
				}
			}
			
			@Override
			public void componentResized(ComponentEvent arg0) {}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				logger.log("Hidden");
				if (toggleLogger != null) {
					toggleLogger.setState(false);
				}
			}
		});
		
		//toggle state
		if (toggleLogger != null) {
			toggleLogger.setState(logger.isVisible());
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmGameEditor.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
		setupApp();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {			
		//the real game editor window
		frmGameEditor = new JFrame();
		frmGameEditor.setTitle("Game Editor");
		frmGameEditor.setBackground(Color.LIGHT_GRAY);
		frmGameEditor.setBounds(100, 100, 741, 517);
		frmGameEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{175, 0, 0};
		gridBagLayout.rowHeights = new int[]{451, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		frmGameEditor.getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane paneToolTab = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_paneToolTab = new GridBagConstraints();
		gbc_paneToolTab.insets = new Insets(0, 0, 0, 5);
		gbc_paneToolTab.fill = GridBagConstraints.BOTH;
		gbc_paneToolTab.gridx = 0;
		gbc_paneToolTab.gridy = 0;
		frmGameEditor.getContentPane().add(paneToolTab, gbc_paneToolTab);
		
		JPanel paneMesh = new JPanel();
		paneToolTab.addTab("Mesh", null, paneMesh, null);
		paneMesh.setLayout(null);
		
		JLabel lblMeshList = new JLabel("Mesh list");
		lblMeshList.setBounds(12, 12, 55, 16);
		paneMesh.add(lblMeshList);
		
		JList listMesh = new JList();
		listMesh.setBorder(new LineBorder(new Color(0, 0, 0)));
		listMesh.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMesh.setModel(new AbstractListModel() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		
		listMesh.setBounds(10, 34, 143, 97);
		paneMesh.add(listMesh);
		
		
		JPanel paneMeshBtnGrp = new JPanel();
		paneMeshBtnGrp.setBounds(12, 143, 141, 88);
		paneMesh.add(paneMeshBtnGrp);
		paneMeshBtnGrp.setLayout(new GridLayout(0, 1, 0, 5));
		
		JButton btnLoadMesh = new JButton("Load Mesh");
		btnLoadMesh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//gotta pick file, load it, and log it and go to hell
				JFileChooser jfc = new JFileChooser();
				int ret = jfc.showOpenDialog(frmGameEditor);
				
				if (ret == JFileChooser.APPROVE_OPTION) {
					String filename = jfc.getSelectedFile().getAbsolutePath();					
					//logger.log(m.toString());
					edt.testMesh(filename);
				}
			}
		});
		paneMeshBtnGrp.add(btnLoadMesh);
		
		JButton btnDeleteMesh = new JButton("Delete Mesh");
		paneMeshBtnGrp.add(btnDeleteMesh);
		
		JButton btnViewMesh = new JButton("View Mesh");
		paneMeshBtnGrp.add(btnViewMesh);
		btnDeleteMesh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JPanel paneObject = new JPanel();
		paneToolTab.addTab("Object", null, paneObject, null);
		
		JPanel paneGame = new JPanel();
		paneToolTab.addTab("Game", null, paneGame, null);
		paneGame.setLayout(null);
		
		JPanel paneCanvas = new JPanel();
		paneCanvas.setName("canvasPane");
		paneCanvas.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_paneCanvas = new GridBagConstraints();
		gbc_paneCanvas.insets = new Insets(4, 0, 4, 4);
		gbc_paneCanvas.fill = GridBagConstraints.BOTH;
		gbc_paneCanvas.gridx = 1;
		gbc_paneCanvas.gridy = 0;
		frmGameEditor.getContentPane().add(paneCanvas, gbc_paneCanvas);
		paneCanvas.setLayout(new GridLayout(0, 1, 0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		frmGameEditor.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmLoadDataset = new JMenuItem("Load Dataset");
		mntmLoadDataset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser jfc = new JFileChooser();
				int ret = jfc.showOpenDialog(frmGameEditor);
				
				if (ret == JFileChooser.APPROVE_OPTION) {
					String filename = jfc.getSelectedFile().getAbsolutePath();
					
					edt.setDataFile(filename);
				}
			}
		});
		mnFile.add(mntmLoadDataset);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmGameEditor.dispatchEvent(new WindowEvent(frmGameEditor, WindowEvent.WINDOW_CLOSING));
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnWindow = new JMenu("Window");
		menuBar.add(mnWindow);
		
		JCheckBoxMenuItem chckbxmntmLogger = new JCheckBoxMenuItem("Logger");
		chckbxmntmLogger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logger.setVisible(!logger.isVisible());
			}
		});
		chckbxmntmLogger.setName("mntmWindowLogger");
		mnWindow.add(chckbxmntmLogger);
		
		toggleLogger = chckbxmntmLogger;
	}
}

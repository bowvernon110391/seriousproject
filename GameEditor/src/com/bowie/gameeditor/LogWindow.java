package com.bowie.gameeditor;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;

import javax.swing.JScrollPane;

import java.awt.BorderLayout;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.Color;

import javax.swing.border.EtchedBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LogWindow extends JDialog implements Logger {
	/**
	 * Create the dialog.
	 */
	private JTextArea textLogArea;
	private JTextField textCmd;
	
	//list of history of command
	private List<String> cmdHistory = new ArrayList<>();
	//list of listener
	private List<ScriptCmdListener> cmdListeners = new ArrayList<>();
	
	public LogWindow() {
		setAlwaysOnTop(true);
		setTitle("Console");
		setBounds(100, 100, 514, 112);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane);
		
		JTextArea textLog = new JTextArea();
		textLog.setEditable(false);
		textLog.setBackground(Color.LIGHT_GRAY);
		scrollPane.setViewportView(textLog);
		
		this.textLogArea = textLog;
		
		textCmd = new JTextField();
		textCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//get command string
				String cmdString = textCmd.getText();
				//add to history
				cmdHistory.add(cmdString);
				//perform cmd
				for (ScriptCmdListener l : cmdListeners) {
					l.commandEvent(cmdString);
				}
				//clear text
				textCmd.setText("");
			}
		});
		getContentPane().add(textCmd, BorderLayout.SOUTH);
		textCmd.setColumns(10);
	}
	
	public void addCommandListener(ScriptCmdListener l) {
		cmdListeners.add(l);
	}
	
	public JTextArea getTextLog() {
		return textLogArea;
	}
	
	@Override
	public void log(String txt) {
		textLogArea.append(txt+"\r\n");		
	}
	
	@Override
	public void writeToFile(String filename) {
		try {
			FileOutputStream o = new FileOutputStream(new File(filename));
			
			o.write( textLogArea.getText().getBytes(Charset.forName("UTF-8") ) );
						
			o.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

/*
 * @Description: 带自动补全的搜索列表（不是自己写的，出自《Swing Hacks》。
 * @Coder: Xinjie_Wong
 * @Date: 2014/09/22
 */
package wong.mp3.ui;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;
import java.util.regex.*;

public class PlayerJTextField extends JTextField implements ListSelectionListener{
	Completer completer;
	JList completionList;
	JList songList;
	java.util.List<File> playList;
	DefaultListModel completionListModel;
	JScrollPane listScroller;
	JWindow listWindow;
	
	public PlayerJTextField(int col){
		super(col);
		this.completer = new Completer();
		completionListModel = new DefaultListModel();/////////////////////////////////////
		completionList = new JList(completionListModel);
		completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		completionList.addListSelectionListener(this);
		listScroller = new JScrollPane(completionList, 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listWindow = new JWindow();
		listWindow.getContentPane().add(listScroller);
	}
	
	public void addCompletion(String s){
		completer.addCompletion(s);
	}
	
	public void removeCompletion(String s){
		completer.removeCompletion(s);
	}
	
	public void clearCompletion(){
		completer.clearCompletion();
	}
	
	public void setSongList(JList songList){
		this.songList = songList;
	}
	
	public void setPlayList(java.util.List<File> playList){
		this.playList = playList;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e){
		if(e.getValueIsAdjusting()) return;
		if(completionList.getModel().getSize() == 0) return;
		listWindow.setVisible(false);
		
		final String completionString = (String) completionList.getSelectedValue();
		
		Thread worker = new Thread(){
			@Override
			public void run(){
				setText(completionString);
			}
		};
		SwingUtilities.invokeLater(worker);
		
		for(int index=0; index < playList.size(); index++){
			File song = playList.get(index);
			if(song.getName().equals(completionString)){
				songList.setSelectedIndex(index);
			}
		}
	}
	
	
	class Completer implements DocumentListener{
		private Pattern pattern;
		private ArrayList<String> completions;
		
		public Completer(){
			completions = new ArrayList<String>();
			getDocument().addDocumentListener(this);
		}
		
		public void addCompletion(String s){
			completions.add(s);
			buildAndShowPopup();
		}
		
		public void removeCompletion(String s){
			completions.remove(s);
			buildAndShowPopup();
		}
		
		public void clearCompletion(){
			completions.clear();
			buildPopup();
			listWindow.setVisible(false);
		}
		
		private void buildPopup(){
			completionListModel.clear();
			Iterator iterator = completions.iterator();
			pattern = Pattern.compile(".*"+getText()+".+");
			while(iterator.hasNext()){
				String completion = (String) iterator.next();
				Matcher matcher = pattern.matcher(completion);
				if(matcher.matches()){
					completionListModel.add(completionListModel.getSize(), completion);
				}
			}
		}
		
		private void showPopup(){
			if(completionListModel.getSize() == 0){
				listWindow.setVisible(false);
				return;
			}
			
			java.awt.Point los = getLocationOnScreen();
			int popX = los.x;
			int popY = los.y+getHeight();
			listWindow.setLocation(popX, popY);
			listWindow.pack();
			listWindow.setVisible(true);
		}
		
		private void buildAndShowPopup(){
			if(getText().length() < 1) return;
			buildPopup();
			showPopup();
		}
		
		public void insertUpdate(DocumentEvent e){
			buildAndShowPopup();
		}
		
		public void removeUpdate(DocumentEvent e){
			buildAndShowPopup();
		}
		
		public void changedUpdate(DocumentEvent e){
			buildAndShowPopup();
		}
	}
}


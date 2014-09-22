package wong.mp3.player;

import java.io.*;
import javax.swing.*;
import wong.mp3.ui.*;

public class Mp3Player {
	  public static void main(String[] args){
	        SwingUtilities.invokeLater(
	                new Runnable() {
	            public void run() {
	                PlayerUI mp3Player = new PlayerUI();
	                mp3Player.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                mp3Player.setVisible(true);
	            }
	        });

	    }
}

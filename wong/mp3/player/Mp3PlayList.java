package wong.mp3.player;

import java.io.*;
import java.util.*;

public class Mp3PlayList implements Serializable {

	/**
	 * 
	 */
	
	private List<File> playList;
	
	public Mp3PlayList(){
		this.playList = new ArrayList<File>();
	}
	
	public Mp3PlayList(List<File> playList){
		this.playList = playList;
	}
	
	public List<File> getPlayList(){
		return playList;
	}
	
	public void setPlayList(List<File> playList){
		this.playList = playList;
	}
	
	public void addSong(File song){
		if( !playList.contains(song)){
			playList.add(song);
		}
	}
	
	public void addSong(int index, File song){
		playList.add(index, song);
	}
	
	public File removeSong(int index){
		File song = playList.remove(index);
		return song;
	}
	
	public void removeSong(File song){
		playList.remove(song);
	}
	
	public void removeAllSongs(){
		playList.clear();
	}

}

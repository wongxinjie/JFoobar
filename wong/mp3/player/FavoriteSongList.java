/**
 * @Description: 记录用户的播放习惯的类
 * @author: Xinjie_Wong
 * @Date: 2014/09/22
 */
package wong.mp3.player;

import java.io.*;
import java.util.*;

public class FavoriteSongList implements Serializable {
	
	
	private static final long serialVersionUID = 4998474785570318743L;
	private Map<File, Integer> favoriteSongList;
	
	public FavoriteSongList(){
		favoriteSongList = new HashMap<File, Integer>();
	}
	
	public void initialize(List<File> songList){
		int size = songList.size();
		for(int i=0; i < size; i++){
			favoriteSongList.put(songList.get(i), 0);
		}
	}
	
	public Map<File, Integer> getFavoriteSongList(){
		return favoriteSongList;
	}
	
	public void update(File song){
		favoriteSongList.put(song, favoriteSongList.get(song)+1);
	}
	
	public void removeSong(File song){
		favoriteSongList.remove(song);
	}
	
	public void addSong(File song){
		if( !favoriteSongList.containsKey(song)){
			favoriteSongList.put(song, 0);
		}
	}
	
	public void clear(){
		favoriteSongList.clear();
	}
	
	/**
	 * Java中Map中根据值进行排序。
	 * 根据用户的播放次数，对列表进行递减排序。
	 */
	public List<Map.Entry<File, Integer>> getSortedList(){
		List<Map.Entry<File, Integer>> sortedList = new ArrayList<Map.Entry<File, Integer>>(favoriteSongList.entrySet());
		Collections.sort(sortedList, new Comparator<Map.Entry<File, Integer>>(){
			@Override
			public int compare(Map.Entry<File, Integer> objA, Map.Entry<File, Integer> objB){
				return (objB.getValue() - objA.getValue());
			}
		});
		return sortedList;
	}
}

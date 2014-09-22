/**
 * @Description: �����ļ��־û��࣬�������б�ȳ־û���Ӳ��
 * @author: Xinjie_Wong
 * @Date: 2014/09/22
 */
package wong.mp3.file;
import java.io.*;

public class Mp3FilePersistent {
	private String entryDirectory;	
	private File entryFile;
	/*
	 * �жϲ����������е�ƽ̨��
	 */
	private Mp3FilePersistent(String fileName){
		String osName = System.getProperties().getProperty("os.name");
		if(osName.startsWith("win")|| osName.startsWith("Win")){
			entryDirectory = "C:\\Documents and Settings\\All Users\\".concat(fileName);
		}else if(osName.contains("linux")||osName.contains("Linux")){
			entryDirectory = "/tmp/".concat(fileName);
		}
		entryFile = new File(entryDirectory);
	}
	
	public static Mp3FilePersistent create(String fileName){
		return new Mp3FilePersistent(fileName);
	}
	
	public void writeObject(Object object){
		try{
			FileOutputStream outputStream = new FileOutputStream(entryFile);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
		}catch(IOException ex){}	
	}
	
	public Object readObject(){
		Object obj=null;
		try{
			FileInputStream inputStream = new FileInputStream(entryFile);
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			obj = objectInputStream.readObject();
			objectInputStream.close();
		}catch(ClassNotFoundException ex){}
		catch(IOException e){}
		return obj;
	}
	
	
}

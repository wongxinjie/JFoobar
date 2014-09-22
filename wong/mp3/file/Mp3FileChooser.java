/**
 * @Description:�ļ�ѡ���࣬����ѡ��mp3�ļ�
 * @author: Xinjie_Wong
 * @Date: 2014/09/20
 */
package wong.mp3.file;

import javax.swing.*;
import java.io.File;

public class Mp3FileChooser extends JFileChooser
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4118160276966384029L;

	public Mp3FileChooser()
    {
        fileChooser = new JFileChooser();
    }
    
    public Mp3FileChooser(String direction){
        fileChooser = new JFileChooser(direction);
    }

    /*
     * ��ͼ�ν����е�ʵ�ִ��ļ��Ĺ��ܺ�����
     */
    public String[] OpenFile()
    {
            fileChooser.setAcceptAllFileFilterUsed(false);
            //����ļ����������ļ�����ʵ����SortFileFilter����ʵ�֡�
            fileChooser.addChoosableFileFilter(new Mp3FileFilter(".mp3", "music"));
            fileChooser.addChoosableFileFilter(new Mp3FileFilter(".wmv","music"));
            //���ö�ѡΪ�档
            fileChooser.setMultiSelectionEnabled(true);
            int returnVal = fileChooser.showOpenDialog(null);

            File[] files = null;
            String[] filePaths = null;
            if( returnVal == JFileChooser.APPROVE_OPTION){
                files = fileChooser.getSelectedFiles();
                filePaths = new String[files.length];
                
                for(int i=0; i < files.length; i++){
                    filePaths[i] = files[i].getAbsolutePath();
                }
            }

            return filePaths;

       }

    /*
     * �ļ����湦�ܺ�����
     */
    public void SaveFile(String str)
    {
        int returnVal = fileChooser.showSaveDialog(null);

        if ( returnVal == JFileChooser.APPROVE_OPTION){
            
        }

    }

    /*
     * ������غ����������в������
     * ��ɾ��
     */
    @Override
    public void approveSelection()
    {
        File file = this.getSelectedFile();

        if( file.exists()){
            int copy = JOptionPane.showConfirmDialog(null,
                    "�Ƿ�Ҫ���ǵ�ǰ�ļ���", "����", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (copy == JOptionPane.YES_OPTION)
            super.approveSelection();
        }
        else
            super.approveSelection();
    }

    private JFileChooser fileChooser;

}

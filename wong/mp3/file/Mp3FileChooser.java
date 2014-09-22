/**
 * @Description:文件选择类，弹框选择mp3文件
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
     * 在图形界面中的实现打开文件的功能函数。
     */
    public String[] OpenFile()
    {
            fileChooser.setAcceptAllFileFilterUsed(false);
            //添加文件过滤器。文件过滤实现在SortFileFilter类中实现。
            fileChooser.addChoosableFileFilter(new Mp3FileFilter(".mp3", "music"));
            fileChooser.addChoosableFileFilter(new Mp3FileFilter(".wmv","music"));
            //设置多选为真。
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
     * 文件保存功能函数。
     */
    public void SaveFile(String str)
    {
        int returnVal = fileChooser.showSaveDialog(null);

        if ( returnVal == JFileChooser.APPROVE_OPTION){
            
        }

    }

    /*
     * 这个重载函数的作用尚不清楚。
     * 可删。
     */
    @Override
    public void approveSelection()
    {
        File file = this.getSelectedFile();

        if( file.exists()){
            int copy = JOptionPane.showConfirmDialog(null,
                    "是否要覆盖当前文件？", "保存", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (copy == JOptionPane.YES_OPTION)
            super.approveSelection();
        }
        else
            super.approveSelection();
    }

    private JFileChooser fileChooser;

}

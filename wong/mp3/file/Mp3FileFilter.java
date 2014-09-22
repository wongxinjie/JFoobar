/**
 * @Description:�ļ���׺�����࣬Ҫ��FileChooser���ʹ��
 * @author: Xinjie_Wong
 * @Date: 2014/09/21
 */
package wong.mp3.file;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class Mp3FileFilter extends FileFilter {

    public Mp3FileFilter(String extensions, String description)
    {
        this.description = description;//�ļ���������
        this.extensions = extensions;//�ļ���׺����
    }

    @Override
    public boolean accept(File f)
    {
        if( f.isDirectory())
            return true;
        return f.getName().endsWith(extensions);
    }

    @Override
    public String getDescription()
    {
        return description + String.format("*%s", extensions);
    }
    
    private String extensions;
    private String description;

}

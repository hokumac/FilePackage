package studygis.common.file;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author study_gis@126.com
 * @date 2020/2/2
 * @Decription 文件结构对象定义，包含文件合并和拆分的实现,以对象序列化的方式便于文件对象的管理
 */

public class FileInfo implements Serializable {
    private String name;
    private int type;
    private long size;
    private ArrayList<FileInfo> subFiles;
    private long startposition;
    private long endposition;
    private transient String LocalPath;
    private transient int writebuffer = 2048;


    public String getLocalPath() {
        return LocalPath;
    }

    public void setLocalPath(String localPath) {
        LocalPath = localPath;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ArrayList getSubFiles() {
        return subFiles;
    }

    public void setSubFiles(ArrayList subFiles) {
        this.subFiles = subFiles;
    }

    public long getStartposition() {
        return startposition;
    }

    public void setStartposition(long startposition) {
        this.startposition = startposition;
    }

    public long getEndposition() {
        return endposition;
    }

    public void setEndposition(long endposition) {
        this.endposition = endposition;
    }

    //文件打包，将当前的文件对象打包到指定的文件中
    public boolean PackageFile(String tagPath) throws IOException {
        if (type == 1) {
            //处理文件夹
            if (subFiles != null) {
                for (int i = 0; i < subFiles.size(); i++) {
                    if (!subFiles.get(i).PackageFile(tagPath))
                        return false;
                }
            }
        } else {
            //处理文件
            if (!appendFile(tagPath))
                return false;
        }
        return true;
    }

    //文件打包的具体执行，以流的方式将文件输入到同一个指定文件中
    private boolean appendFile(String tagPath) throws IOException {
        if(startposition==endposition)
            return true;
        // 定义源文件
        File file = new File(LocalPath);
        InputStream fis = new FileInputStream(file);

        OutputStream fos = new FileOutputStream(tagPath,true);

        // 定义字节数组，接收读取到的源文件字节内容
        byte[] bytes = new byte[writebuffer];

        //需要改进写入效率
        int readBytecount=-1;
        while ((readBytecount=fis.read(bytes)) != -1) {
            fos.write(bytes,0,readBytecount);
        }

        fos.flush();
        fis.close();
        fos.close();
        return true;
    }

    //解压数据包，从指定文件中读取数据
    public  boolean UnPackageFile(String packageFile,String tagFolder,long offset) throws IOException {
        String pathName=tagFolder+"\\"+this.name;
        if (type == 1) {
            File dic = new File(pathName);
            if(!dic.exists())
                dic.mkdirs();
            //处理文件夹
            if (subFiles != null) {
                for (int i = 0; i < subFiles.size(); i++) {
                    if (!subFiles.get(i).UnPackageFile(packageFile,pathName,offset))
                        return false;
                }
            }
        } else {
            //处理文件
            File tagFile=new File(pathName);
            tagFile.createNewFile();

            File sourceFile=new File(packageFile);
            copyFileUsingFileStreams(sourceFile,tagFile,offset+startposition,endposition-startposition);
        }
        return true;
    }

    //解压的具体实现，从文件流中还原文件
    private  void copyFileUsingFileStreams(File source, File dest,long offset,long length)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead=-1;

            ///存在效率问题，待优化
            //大文件存在内存溢出问题
            long lset = input.skip(offset);
            while ((bytesRead = input.read(buf)) > 0&&length>0) {
                if(length<bytesRead) {
                    bytesRead= (int) length;
                }
                output.write(buf, 0, bytesRead);
                length=length-bytesRead;
            }
        } finally {
            input.close();
            output.close();
        }
    }

    //文件还原实现，利用FileChannel进行效率尝试
    private  void copyFileUsingFileChannels(File source, File dest,long offset,long length) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    //文件还原实现
    private boolean CreateFile(String packageFile,String tagPath,long offset) throws IOException {
         if(startposition==endposition)
         {
             File file = new File(tagPath+"\\"+name);
             file.createNewFile();
             return  true;
         }
        // 定义源文件
        File file = new File(packageFile);
        InputStream fis = new FileInputStream(file);
        fis.skip(offset);
        OutputStream fos = new FileOutputStream(tagPath);

        // 定义字节数组，接收读取到的源文件字节内容
        int nCount = (int) ((endposition-startposition)/writebuffer);

        if(nCount==0)
        {
            int nLength =(int)(endposition-startposition);
            byte[] bytes = new byte[nLength];
            while (fis.read(bytes) != -1) {
                fos.write(bytes);
            }
        } else {

            byte[] bytes = new byte[writebuffer];
            while (nCount>0) {
                fos.write(bytes);
                nCount--;
            }
        }

        fos.flush();
        fis.close();
        fos.close();
        return true;
    }
}

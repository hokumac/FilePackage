package studygis.common.file;

import studygis.common.encrypt.IFileEncrypt;
import studygis.common.event.IFileEventListener;
import studygis.common.event.fileEventMsgObj;
import studygis.common.event.fileEventObject;
import studygis.common.event.fileEventProcessObj;

import javax.swing.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
    private transient int writebuffer = 1024*2014*5;
    private IFileEncrypt fileEncrypt;

    private Vector eventlist=new Vector();

    public void setFileEncrypt(IFileEncrypt encrypt) {
        fileEncrypt = encrypt;
    }

    private  void setExtendMethod(FileInfo fileInfo)
    {
        Iterator it=eventlist.iterator();
        while(it.hasNext())
        {
            fileInfo.addMyEventListener((IFileEventListener) it.next());
        }

    }

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
                    subFiles.get(i).setFileEncrypt(fileEncrypt);
                    setExtendMethod(subFiles.get(i));
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
        if (startposition == endposition)
            return true;
        // 定义源文件
        File file = new File(LocalPath);
        onMsgFire("----------开始打包文件:"+this.name);
        InputStream fis = new FileInputStream(file);

        OutputStream fos = new FileOutputStream(tagPath, true);

        // 定义字节数组，接收读取到的源文件字节内容
        byte[] bytes = new byte[writebuffer];

        //需要改进写入效率
        int readBytecount = -1;
        long currentLength=0;
        while ((readBytecount = fis.read(bytes)) != -1) {
            if (fileEncrypt != null) {
                byte [] enBytes=fileEncrypt.EncryptBytes(bytes, readBytecount);
                fos.write(enBytes, 0, enBytes.length);

                currentLength+=enBytes.length;
                onProcessFire(file.length(),currentLength);
            } else {
                fos.write(bytes, 0, readBytecount);

                currentLength+=readBytecount;
                onProcessFire(file.length(),currentLength);
            }
        }
        onMsgFire("----------文件:"+this.name+"打包完成！");
        fos.flush();
        fis.close();
        fos.close();
        return true;
    }

    //解压数据包，从指定文件中读取数据
    public boolean UnPackageFile(String packageFile, String tagFolder, long offset) throws IOException {
        String pathName = tagFolder + "\\" + this.name;
        if (type == 1) {
            File dic = new File(pathName);
            if (!dic.exists())
                dic.mkdirs();
            //处理文件夹
            if (subFiles != null) {
                for (int i = 0; i < subFiles.size(); i++) {
                    subFiles.get(i).setFileEncrypt(fileEncrypt);
                    setExtendMethod(subFiles.get(i));
                    if (!subFiles.get(i).UnPackageFile(packageFile, pathName, offset))
                        return false;
                }
            }
        } else {
            //处理文件
            onMsgFire("----------开始解压文件:"+this.name);
            File tagFile = new File(pathName);
            tagFile.createNewFile();

            File sourceFile = new File(packageFile);
            copyFileUsingFileStreams(sourceFile, tagFile, offset + startposition,
                    endposition - startposition);
            onMsgFire("----------文件:"+this.name+"解压完成！");
        }
        return true;
    }

    //解压的具体实现，从文件流中还原文件
    private void copyFileUsingFileStreams(File source, File dest, long offset, long length)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead = -1;

            ///存在效率问题，待优化
            //大文件存在内存溢出问题
            long lset = input.skip(offset);
            long currentLength=0;
            long max=length;
            while ((bytesRead = input.read(buf)) > 0 && length > 0) {
                if (length < bytesRead) {
                    bytesRead = (int) length;
                }

                if (fileEncrypt != null) {
                    byte [] enBytes =fileEncrypt.DecryptBytes(buf, bytesRead);
                    output.write(enBytes, 0, enBytes.length);

                    currentLength+=enBytes.length;
                    onProcessFire(max,currentLength);
                } else {
                    output.write(buf, 0, bytesRead);

                    currentLength+=bytesRead;
                    onProcessFire(max,currentLength);
                }
                length = length - bytesRead;
            }
        } finally {
            input.close();
            output.close();
        }
    }

    //文件还原实现，利用FileChannel进行效率尝试
    private void copyFileUsingFileChannels(File source, File dest, long offset, long length) throws IOException {
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
    private boolean CreateFile(String packageFile, String tagPath, long offset) throws IOException {
        if (startposition == endposition) {
            File file = new File(tagPath + "\\" + name);
            file.createNewFile();
            return true;
        }
        // 定义源文件
        File file = new File(packageFile);
        InputStream fis = new FileInputStream(file);
        fis.skip(offset);
        OutputStream fos = new FileOutputStream(tagPath);

        // 定义字节数组，接收读取到的源文件字节内容
        int nCount = (int) ((endposition - startposition) / writebuffer);

        if (nCount == 0) {
            int nLength = (int) (endposition - startposition);
            byte[] bytes = new byte[nLength];
            while (fis.read(bytes) != -1) {
                fos.write(bytes);
            }
        } else {

            byte[] bytes = new byte[writebuffer];
            while (nCount > 0) {
                fos.write(bytes);
                nCount--;
            }
        }

        fos.flush();
        fis.close();
        fos.close();
        return true;
    }

    /*
     *@功能描述  发起消息事件
     * @参数 msg
     * @返回值 void
     */
    private  void onMsgFire(String msg)
    {
        fileEventMsgObj eventMsgObj=new fileEventMsgObj(this,msg);
        notifyMyEvent(eventMsgObj);
    }

    private void  onProcessFire(long max,long current){
        fileEventProcessObj eventMsgObj=new fileEventProcessObj(this,current,max);
        notifyMyEvent(eventMsgObj);
    }

    public void addMyEventListener(IFileEventListener me)
    {
        if(!eventlist.contains(me))
        eventlist.add(me);
    }

    public void deleteMyEventListener(IFileEventListener me)
    {
        eventlist.remove(me);
    }

    public void notifyMyEvent(fileEventObject me)
    {
        Iterator it=eventlist.iterator();
        while(it.hasNext())
        {
            //在类中实例化自定义的监听器对象,并调用监听器方法
            ((IFileEventListener) it.next()).handleEvent(me);
        }
    }
}

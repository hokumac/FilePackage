package studygis.common.file;

import studygis.common.encrypt.EncryptFactory;
import studygis.common.encrypt.EncryptMethod;
import studygis.common.encrypt.IFileEncrypt;
import studygis.common.event.IFileEventListener;
import studygis.common.event.fileEventMsgObj;
import studygis.common.event.fileEventObject;
import studygis.common.event.fileEventProcessObj;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author study_gis@126.com
 * @date 2020/2/2
 * @Decription 文件打包处理类，外部调用的打包解压处理对象
 */
public class FilePackage {

    ///分割关键字
    private String HeadEnd = "HeadEnd";
    private String ChartsetName = "ISO-8859-1";
    private String FileChartsetName = "UTF-8";
    private int readBufferLength=2048;


    private EncryptMethod encryptMethod=EncryptMethod.None;
    private String encryptPassWord;
    private EncryptFactory encryptFactory;


    private Vector eventlist=new Vector();

    public FilePackage(String chartsetName, EncryptMethod method,String pwd) {
        ChartsetName = chartsetName;
        encryptMethod=method;
        encryptPassWord=pwd;
        encryptFactory=new EncryptFactory();
    }

    public FilePackage(String chartsetName) {
        ChartsetName = chartsetName;
    }

    private FileInfo getAllFileName(String path) {
        return getPackageFileInfo(path, 0);
    }

    private FileInfo getPackageFileInfo(String path, long startPosition) {
        File file = new File(path);
        File[] files = file.listFiles();
        FileInfo info = new FileInfo();
        info.setName(file.getName());
        ArrayList subFile = new ArrayList();
        info.setType(1);
        info.setSubFiles(subFile);

        assert files != null;
        for (File a : files) {
            if (a.isFile()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(a.getName());
                fileInfo.setType(0);
                fileInfo.setSize(a.length());
                fileInfo.setStartposition(startPosition);
                fileInfo.setEndposition(startPosition + fileInfo.getSize());
                startPosition = fileInfo.getSize() + startPosition;
                fileInfo.setLocalPath(a.getAbsolutePath());
                info.getSubFiles().add(fileInfo);
            }
        }

        for (File a : files) {
            if (a.isDirectory()) {//如果文件夹下有子文件夹，获取子文件夹下的所有文件全路径。
                FileInfo subDic = getPackageFileInfo(a.getAbsolutePath() + "\\", startPosition);
                info.getSubFiles().add(subDic);
            }
        }
        return info;
    }

    public boolean Package(String path, String tagPath) throws IOException {
        onMsgFire("----------"+tagPath+"开始打包...");
        FileInfo fileInfo = getAllFileName(path);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(fileInfo);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        onProcessFire(10,5);
        //写入文件对象信息
        if (writeFileinfo(tagPath, bytes)) {

            setExtendMethod(fileInfo);
            //打包文件
            if (fileInfo.PackageFile(tagPath)) {
                onMsgFire("----------"+tagPath+"打包成功！");
                return true;
            }
        }
        onMsgFire("----------"+tagPath+"打包失败！");
        return false;
    }

    /*
     *@功能描述  为文件对象设置加密管理对象
      * @参数 fileInfo
     * @返回值 void
    */
    private  void setExtendMethod(FileInfo fileInfo)
    {
        if(encryptMethod!=EncryptMethod.None)
        {
            IFileEncrypt fileEncrypt= encryptFactory.getFileEncrypt(encryptMethod,encryptPassWord);
            fileInfo.setFileEncrypt(fileEncrypt);
        }

        Iterator it=eventlist.iterator();
        while(it.hasNext())
        {
            fileInfo.addMyEventListener((IFileEventListener) it.next());
        }

    }

    private boolean writeFileinfo(String tagPath, byte[] fileInfos) {
        InputStream in = null;

        try {

            //得到输出流
            File file = new File(tagPath);
            if (!file.exists()) {
                file.createNewFile();
            } else
                file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileInfos);
            fos.flush();


            OutputStreamWriter osw = new OutputStreamWriter(fos, FileChartsetName);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.newLine();
            bw.write(HeadEnd);
           //bw.newLine();
            bw.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    public FileInfo getFileInfo(String path) throws IOException, ClassNotFoundException {
        String strFileInfo = "";
        File f = new File(path);
        FileInputStream fos = new FileInputStream(f);

        //存在问题待优化
        byte[] bytes = new byte[readBufferLength];
        int readByte = fos.read(bytes);

        int nLenth = readBufferLength;

        ArrayList<Byte> list = new ArrayList<Byte>();
        ArrayList<Byte> headBytes = new ArrayList<Byte>();
        byte [] fixheadBytes=HeadEnd.getBytes();
        boolean findHeadEnd=false;
        while (readByte != -1) {
            for (int i = 0; i < bytes.length; i++) {
                if(bytes[i]=='H'){
                    headBytes.add(bytes[i]);
                }else{
                    if(headBytes.size()>0){
                          headBytes.add(bytes[i]);
                          if(bytes[i]==fixheadBytes[headBytes.size()-1]){
                              if(headBytes.size()==HeadEnd.length())
                              {
                                  findHeadEnd=true;
                                  list.add(bytes[i]);
                                  headBytes.clear();
                                  break;
                              }
                          }
                          else
                          {
                              headBytes.clear();
                          }
                    }
                }

                list.add(bytes[i]);
            }
            if(!findHeadEnd) {
                readByte = fos.read(bytes);
            }
            else
            {
                break;
            }
        }
        if(!findHeadEnd)
            return  null;
        nLenth = list.size();
        byte[] Allbytes = new byte[nLenth];
        for (int i = 0; i < nLenth; i++) {
            Allbytes[i] = list.get(i);
        }
        FileInfo pFileInfo = (FileInfo) SerializeUtils.serializebyToObject(Allbytes, ChartsetName);
        //这里的偏移量为读取的数据长度+隔离关键字长度+换行符
        pFileInfo.setStartposition(nLenth);
        return pFileInfo;
    }

    public boolean UnPackage(String filepath, String tagPath) throws IOException, ClassNotFoundException {
        onMsgFire("----------开始解压数据："+filepath);
        FileInfo pFileInfo = getFileInfo(filepath);
        if(pFileInfo!=null)
        {
            setExtendMethod(pFileInfo);
           return pFileInfo.UnPackageFile(filepath,tagPath,pFileInfo.getStartposition());
        }
        onMsgFire("----------解压完成！");
        return false;
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

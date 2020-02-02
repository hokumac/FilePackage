package studygis.common.file;

import java.io.*;
import java.util.ArrayList;

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
        FileInfo fileInfo = getAllFileName(path);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(fileInfo);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();

        //写入文件对象信息
        if (writeFileinfo(tagPath, bytes)) {
            //打包文件
            if (fileInfo.PackageFile(tagPath))
                return true;
        }
        return false;
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
            readByte = fos.read(bytes);
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
        FileInfo pFileInfo = getFileInfo(filepath);
        if(pFileInfo!=null)
        {
           return pFileInfo.UnPackageFile(filepath,tagPath,pFileInfo.getStartposition());
        }
        return false;
    }
}

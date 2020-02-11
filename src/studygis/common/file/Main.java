package studygis.common.file;

import studygis.common.event.fileMsgListener;
import studygis.common.event.fileProcessListener;
import studygis.common.timer.Timer;
import studygis.common.timer.TimerUtil;

import java.io.IOException;

/**
 * @author study_gis@126.com
 * @date 2020/2/2
 * @Decription 程序测试入口，后续需要完善的内容
 *  * 1.增加ui界面进行操作
 *  * 2.增加文件打包和解压的进度
 *  * 3.文件数据加密
 *  * 4.文件读写效率
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TimerUtil tu = new TimerUtil();
        tu.getTime();
    }

    @Timer
    public void Package() throws IOException {
        String ChartsetName="ISO-8859-1";
        FilePackage pk = new FilePackage(ChartsetName);
        addlistener(pk);

        String tagPath="C:\\Users\\Administrator\\Desktop\\调度远程办公\\package.file";
        String sourcePath="C:\\Users\\Administrator\\Desktop\\调度远程办公\\test";

        pk.Package(sourcePath,tagPath);
    }

    @Timer
    public void UnPackage() throws IOException, ClassNotFoundException {
        String ChartsetName="ISO-8859-1";
        FilePackage pk = new FilePackage(ChartsetName);
        addlistener(pk);

        String tagPath="C:\\Users\\Administrator\\Desktop\\调度远程办公\\package.file";
        String sourcePath="C:\\Users\\Administrator\\Desktop\\调度远程办公\\test";

        pk.UnPackage(tagPath,sourcePath+"1");
    }

    private  void addlistener(FilePackage pk)
    {
        pk.addMyEventListener(new fileMsgListener());
        pk.addMyEventListener(new fileProcessListener());
    }








}

package studygis.common.file;

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
        try {
            String ChartsetName="ISO-8859-1";
            FilePackage pk = new FilePackage(ChartsetName);

            String tagPath="C:\\Users\\Administrator\\Desktop\\package.file";
            String sourcePath="C:\\Users\\Administrator\\Desktop\\test";

            pk.Package(sourcePath,tagPath);
            pk.UnPackage(tagPath,sourcePath+"1");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

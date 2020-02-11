package studygis.common.event;

import java.util.EventObject;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 文件进度监听
 */
public class fileProcessListener implements IFileEventListener {

    private  long increatment;
    @Override
    public void handleEvent(EventObject eventObject) {
        fileEventObject fileobj=(fileEventObject)eventObject ;
        if(fileobj.getEventType()!=eventType.Process)
            return;;

        fileEventProcessObj obj =(fileEventProcessObj)eventObject;
        increatment+=obj.getCurrentProcess();
        if(increatment>1024) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("---------");
            stringBuilder.append(obj.getCurrentProcess());
            stringBuilder.append("---------");
            stringBuilder.append(obj.getMaxProcess());
            stringBuilder.append("---------");
            System.out.println(stringBuilder.toString());
            increatment=0;
        }
    }
}

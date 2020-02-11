package studygis.common.event;

import java.util.EventObject;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 文件传输消息监听
 */
public class fileMsgListener implements IFileEventListener {
    @Override
    public void handleEvent(EventObject eventObject) {
        fileEventObject fileobj=(fileEventObject)eventObject ;
        if(fileobj.getEventType()!=eventType.Msg)
            return;
         fileEventMsgObj obj =(fileEventMsgObj)eventObject ;
        System.out.println(obj.getEventMsg());
    }
}

package studygis.common.event;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 文件消息事件对象
 */
public class fileEventMsgObj extends fileEventObject {

    public fileEventMsgObj(Object source,String msg) {
        super(source);
        fileEventType=eventType.Msg;
        eventMsg=msg;
    }

    private  String eventMsg;

    public String getEventMsg() {
        return eventMsg;
    }

    public void setEventMsg(String eventMsg) {
        this.eventMsg = eventMsg;
    }
}

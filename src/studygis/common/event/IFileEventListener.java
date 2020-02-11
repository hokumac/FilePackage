package studygis.common.event;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述
 */
public interface IFileEventListener extends EventListener {

    void handleEvent(EventObject eventObject) ;
}

package studygis.common.event;

import java.awt.*;
import java.util.EventObject;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述
 */
public class fileEventObject extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public fileEventObject(Object source) {
        super(source);
    }

    public studygis.common.event.eventType getEventType() {
        return fileEventType;
    }

    public void setEventType(studygis.common.event.eventType eventType) {
        this.fileEventType = eventType;
    }

    protected eventType fileEventType;

}

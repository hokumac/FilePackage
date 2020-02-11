package studygis.common.event;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 文件进度事件
 */
public class fileEventProcessObj extends fileEventObject {
     

    public fileEventProcessObj(Object source,long current,long max) {
        super(source);
        currentProcess=current;
        maxProcess=max;
        fileEventType=eventType.Process;
    }

    private long maxProcess;

    public long getMaxProcess() {
        return maxProcess;
    }

    public void setMaxProcess(long maxProcess) {
        this.maxProcess = maxProcess;
    }

    public long getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(long currentProcess) {
        this.currentProcess = currentProcess;
    }

    private long currentProcess;
}

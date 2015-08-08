package dragon.model.job;

/**
 * Created by lin.cheng on 8/7/15.
 */
public class Schedule {

    Long id;
    Boolean active;
    String type;
    Long gid;
    String cron;
    String param;
    Long modified;

    public Schedule(String type, Long gid, String cron) {
        this.type = type;
        this.gid = gid;
        this.cron = cron;
    }

    public Schedule(Long id, Boolean active, String type, Long gid, String cron, String param, Long modified) {
        this.id = id;
        this.active = active;
        this.type = type;
        this.gid = gid;
        this.cron = cron;
        this.param = param;
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        if (active == null) {
            return true;
        }
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}

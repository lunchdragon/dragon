package dragon.model.food;

/**
 * Created by lin.cheng on 6/19/15.
 */
public class Record {
    Long id;
    Long resid;
    Long goTime;
    Boolean veto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResid() {
        return resid;
    }

    public void setResid(Long resid) {
        this.resid = resid;
    }

    public Long getGoTime() {
        return goTime;
    }

    public void setGoTime(Long goTime) {
        this.goTime = goTime;
    }

    public Boolean getVeto() {
        return veto;
    }

    public void setVeto(Boolean veto) {
        this.veto = veto;
    }
}

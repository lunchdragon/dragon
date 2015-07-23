package dragon.model.food;

/**
 * Created by lin.cheng on 7/20/15.
 */
public class Group {

    Long id;
    String name;
    String alias;
    String preference;
    Boolean active;
    Boolean noApprove;

    public Group(String name, String preference) {
        this.name = name;
        this.preference = preference;
    }

    public Group(Long id, String name, String alias, String preference, Boolean active, Boolean noApprove) {
        this.id = id;
        this.name = name;
        this.alias = alias;
        this.preference = preference;
        this.active = active;
        this.noApprove = noApprove;
    }

    public Group() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        if(alias == null){
            return name;
        }
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public Boolean getActive() {
        if(active == null){
            return true;
        }
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getNoApprove() {
        if(noApprove == null){
            return true;
        }
        return noApprove;
    }

    public void setNoApprove(Boolean noApprove) {
        this.noApprove = noApprove;
    }
}

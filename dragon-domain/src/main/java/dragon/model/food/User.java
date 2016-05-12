 package dragon.model.food;

/**
 * Created by lin.cheng on 6/18/15.
 */
public class User {
    Long id;
    String name;
    String email;

    public User(String email) {
        this.email = email;
        this.name = email.split("@")[0];
    }

    public User() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

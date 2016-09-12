package dragon.service.sec;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Identity implements Serializable,Cloneable {

    private String fullName;
    private String subject;
    private long id;
    private String clientAddr;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    @Override
    protected Identity clone() {
        try {
            Identity si = (Identity) super.clone();
            si.setFullName(fullName);
            si.setId(id);
            si.setSubject(subject);
            si.setClientAddr(clientAddr);
            return si;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

}

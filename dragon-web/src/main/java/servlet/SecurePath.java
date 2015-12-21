package servlet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlRootElement
public class SecurePath implements java.io.Serializable {
    private static final long serialVersionUID = 261890134423698873L;
    private String path;
    private AuthType authType;
    private transient Pattern pattern = null;

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    @XmlAttribute
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SecurePath other = (SecurePath) obj;
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return authType + ": " + path;
    }

    public boolean match(String uri) {
        if (pattern == null) {
            String pp = path.replaceAll("\\.", "\\\\.");
            pp  = pp.replace("*", ".*");
            pattern = Pattern.compile(pp);
        }
        Matcher matcher = pattern.matcher(uri);
        return matcher.matches();
    }
}

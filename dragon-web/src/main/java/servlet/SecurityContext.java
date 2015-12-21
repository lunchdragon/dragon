package servlet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement
public class SecurityContext implements java.io.Serializable {
    private static final long serialVersionUID = -4085275941090925756L;
    private String loginPage;
    private String loginPageMobile;
    private String twoFactorPage;
    private Set<SecurePath> securePaths;

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public String getLoginPageMobile() {
        return loginPageMobile;
    }

    public void setLoginPageMobile(String loginPageMobile) {
        this.loginPageMobile = loginPageMobile;
    }

    public String getTwoFactorPage() {
        return twoFactorPage;
    }

    public void setTwoFactorPage(String twoFactorPage) {
        this.twoFactorPage = twoFactorPage;
    }

    @XmlElementWrapper(name="securePaths")
    @XmlElement(name="securePath")
    public Set<SecurePath> getSecurePaths() {
        return securePaths;
    }

    public void setSecurePaths(Set<SecurePath> securePaths) {
        this.securePaths = securePaths;
    }
}

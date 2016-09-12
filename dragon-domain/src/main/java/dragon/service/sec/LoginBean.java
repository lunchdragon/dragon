package dragon.service.sec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.Serializable;

public class LoginBean implements Serializable {

    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";
    public static final String ORI_URL = "originalURL";
    static Log logger = LogFactory.getLog(LoginBean.class);

    private String username;
    private String password;
    private boolean twofactor;
    private String duoHost;
    private String duoSign;
    private String originalURL = "";

    public String getDuoSign() {
        return duoSign;
    }

    public void setDuoSign(String duoSign) {
        this.duoSign = duoSign;
    }

    public String getDuoHost() {
        return duoHost;
    }

    public void setDuoHost(String duoHost) {
        this.duoHost = duoHost;
    }

    public boolean isTwofactor() {
        return twofactor;
    }

    public void setTwofactor(boolean twofactor) {
        this.twofactor = twofactor;
    }

    @PostConstruct
    public void initialize() {

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        if(externalContext.getSessionMap().containsKey(ORI_URL)) {
            originalURL = (String)externalContext.getSessionMap().get(ORI_URL);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String login() {
        try {
            if (SecureContexts.beginSession(username, password, !twofactor)) {
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                if (StringUtils.isNotBlank(originalURL)){
                    externalContext.redirect(originalURL);
                    originalURL = "";
                }
                return SUCCESS;
            }
        } catch (Exception ex) {
            logger.error("", ex);
            if(FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("System is temporarily out of service"));
            }
        }
        return FAIL;
    }
}

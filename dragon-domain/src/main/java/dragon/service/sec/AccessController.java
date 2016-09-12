package dragon.service.sec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Identity implementation that supports permission checking against db
 * configuration
 *
 */
public class AccessController {

    private static final long serialVersionUID = 8499179434718202443L;
    public final static long ANONYMOUS_CUST_ID = SecureContexts.BG_PROC_CUST_ID;
    public final static long SYSTEM_CUST_ID = SecureContexts.SYSTEM_CUST_ID;
    public final static long SUPER_CUST_ID = SecureContexts.SUPER_CUST_ID;
    public final static long SUPER_GLOBAL_ID = SecureContexts.SUPER_GLOBAL_ID;
    public final static String ORIGIN_AO = "System";
    public final static String ORIGIN_USER = "User";
    private static final Log logger = LogFactory.getLog(AccessController.class);
    private Map<String, PermissionBits> permissionMap = new HashMap<String, PermissionBits>();

    public static boolean isSysDomain(long domainId) {
        return domainId == SecureContexts.SYSTEM_CUST_ID
                || domainId == SecureContexts.SERVICE_CUST_ID
                || domainId == SecureContexts.BG_PROC_CUST_ID;
    }

    public static boolean isValidCustomerId(long domainId) {
        return domainId != SecureContexts.SYSTEM_CUST_ID
                && domainId != SecureContexts.SERVICE_CUST_ID
                && domainId != SecureContexts.BG_PROC_CUST_ID
                && domainId != SecureContexts.SUPER_CUST_ID
                && domainId != SecureContexts.SUPER_GLOBAL_ID;
    }

    public static Collection<Long> getSystemDomains() {
        return SecureContexts.getSystemDomains();
    }

    public static boolean isLoggedIn() {
        if (SecureContexts.isContextActive()) {
            RequestContext ctx = SecureContexts.getSessionContext().get();
            return ctx != null && ctx.containsKey(SecureContexts.LOGIN_IDENTITY);
        }

        return false;
    }

    public static AccessController instance() {
        if (SecureContexts.isContextActive()) {
            return (AccessController) SecureContexts.getSessionContext().get().get(SecureContexts.ACL);
        } else {
            throw new RuntimeException("Invalid state");
        }
    }

    public static Identity get2faIdentity() {
        return SecureContexts.get2faIdentity();
    }

    public static void exitSu() {
        SecureContexts.exitSu();
    }

    public static void setThreadIdentity(Identity ident) {
        SecureContexts.setThreadIdentity(ident);
    }

    public static void invalidateThreadIdentity() {
        SecureContexts.invalidateThreadIdentity();
    }

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        if (this.domain != domain && (this.domain == null || !this.domain.equals(domain))) {
            this.domain = domain;
            //if (Events.exists()) Events.instance().raiseEvent(EVENT_CREDENTIALS_UPDATED);
        }
    }

    public boolean hasPermission(String name, String action, Object... arg) {
        boolean pass = false;
        PermissionBits perm = permissionMap.get(name);
        if (perm != null) {
            pass = perm.contains(Permission.valueOf(action)); // check if has right on the resource category
            if (pass && arg != null) {  // check if has right on the object.
//                for (Object obj : arg) {
//                }
            }
        }
        return pass;
    }
    public static Identity getIdentity() {
        Identity user = SecureContexts.getThreadIdentity();
        if (user == null) {
            user = SecureContexts.getLoginIdentity();
        }

        return user;
    }

    public static String getCurrentUserName() {
        Identity user = getIdentity();
        if (user == null) {
            return null;
        }

        return user.getSubject();
    }

    public static Long getCurrentUserId() {
        Identity user = getIdentity();
        if (user == null) {
            return null;
        }

        return user.getId();
    }
}

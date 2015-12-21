package dragon.service.sec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class SecureContexts {
    public final static long BG_PROC_CUST_ID = 9L;
    public final static long SYSTEM_CUST_ID = 0L;
    public final static long SUPER_CUST_ID = 1L;
    public final static long SERVICE_CUST_ID = 2L;
    public final static long SUPER_GLOBAL_ID = 3L;

    public static final String SYSTEM_DOMAIN_NAME = "system";
    public static final String SERVICE_DOMAIN_NAME = "service";
    public static final String SUPER_DOMAIN_NAME = "Super";
    
    public static final String ACL = "__acl__";
    public static final String LOGIN_IDENTITY = "__login_identity__";
    public static final String TWOFA_IDENTITY = "__2FA_identity__";
    public static final String SU_IDENTITY = "__su_identity__";
    public static final String LOGGED_FLAG = "__LOGOUT__";
    public static final String SYS_INIT = "__phoenix_sys_init__";

    private static final Log logger = LogFactory.getLog(SecureContexts.class);
    
    static final ThreadLocal<Deque<Identity>> threadContext = new ThreadLocal<Deque<Identity>>();
    static final ThreadLocal<RequestContext> sessionContext = new ThreadLocal<RequestContext>();

    private static boolean initialized = false;

    static Collection<Long> getSystemDomains() {
		Set<Long> systemDomains = new HashSet<Long>();
		systemDomains.add(SecureContexts.SYSTEM_CUST_ID);
		systemDomains.add(SecureContexts.SERVICE_CUST_ID);
		systemDomains.add(SecureContexts.BG_PROC_CUST_ID);
		systemDomains.add(SecureContexts.SUPER_GLOBAL_ID);
    	return systemDomains;
	}
    
    public static void beginRequest(HttpServletRequest request) {
        RequestContext ctx = new RequestContext(request);
        if (!ctx.containsKey(ACL)) {
            ctx.put(ACL, new AccessController());
        }
        sessionContext.set(ctx);
    }

    public static void beginRequest(HttpSession session) {
        RequestContext ctx = new RequestContext(session);
        if (!ctx.containsKey(ACL)) {
            ctx.put(ACL, new AccessController());
        }
        sessionContext.set(ctx);
    }

    public static void endRequest() {
        threadContext.set(null);
        sessionContext.set(null);
    }

    public static boolean isContextActive() {
        return sessionContext.get() != null;
    }

    public static Identity getThreadIdentity() {
        Deque<Identity> deque = SecureContexts.threadContext.get();
        Identity user = null;
        if (deque != null && !deque.isEmpty()) {
            user = deque.peek().clone();
        }
        return user;
    }

    public static void setThreadIdentity(Identity ident) {
        Deque<Identity> deque = SecureContexts.threadContext.get();
        if (deque == null) {
            deque = new LinkedList<Identity>();
            SecureContexts.threadContext.set(deque);
        }
        deque.push(ident);
    }

    public static void invalidateThreadIdentity() {
        Deque<Identity> deque = SecureContexts.threadContext.get();
        if (deque != null) {
            deque.pop();
        }
    }

    public static boolean beginSession(String domain, String subject, String password, String userDomain) throws NamingException {
        return beginSession(domain, subject, password, userDomain, false);
    }

    public static boolean beginSession(String domain, String subject, String password, String userDomain, boolean setTwoFactor) throws NamingException {
        if (StringUtils.isEmpty(domain)) {
            domain = SERVICE_DOMAIN_NAME;
        }
        return true;
    }

    public static Identity get2faIdentity() {
        Identity u = null;
        if (SecureContexts.isContextActive()) {
            u = (Identity) sessionContext.get().get(SecureContexts.TWOFA_IDENTITY);
        }
        if (u == null) {
            return null;
        }

        return u.clone();
    }

    public static Identity getSuIdentity() {
        Identity u = null;
        if (SecureContexts.isContextActive()) {
            u = (Identity) sessionContext.get().get(SecureContexts.SU_IDENTITY);
        }
        if (u == null) {
            return null;
        }

        return u.clone();
    }

    private static final int DEFAULT_SVC_TIME_OUT = 120;

    public static String getRemoteAddr() {
        RequestContext webCtx = sessionContext.get();
        if (webCtx != null) {
            return webCtx.getRemoteAddr();
        } else {
            return null;
        }
    }

    public static String getSessionId() {
        RequestContext webCtx = sessionContext.get();
        if (webCtx != null) {
            return webCtx.getSessionId();
        } else {
            return null;
        }
    }

    public static ThreadLocal<RequestContext> getSessionContext() {
        return sessionContext;
    }

    public static ThreadLocal<Deque<Identity>> getThreadContext() {
        return threadContext;
    }

    public static boolean isSued(){
        RequestContext context = sessionContext.get();
        if(context != null && context.get(SU_IDENTITY) != null){
            return true;
        }
        return false;
    }

    public static void exitSu() {
        if (SecureContexts.isContextActive()) {
            sessionContext.get().remove(SU_IDENTITY);
        }
    }

    private static String keyValue = null;
    public static boolean isInitialized() {
        return initialized;
    }

    private static byte[] mapbytes(byte[] data, int start, int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i ++) {
            bytes[i] = data[ (i + start) % data.length];
        }

        return bytes;
    }

    public static boolean isSysInitRequest() {
        RequestContext ctx = sessionContext.get();
        if (ctx != null) {
            String value = ctx.getRequestHeader(SYS_INIT);
            return value != null && value.equalsIgnoreCase("true");
        }

        return false;
    }

}


package dragon.service.sec;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class RequestContext implements java.io.Serializable {

    private static final long serialVersionUID = -1577091480833140023L;
    private HttpServletRequest request;
    private HttpSession session;

    public RequestContext(HttpServletRequest request) {
        this.request = request;
        this.session = request.getSession(false);
    }

    public RequestContext(HttpSession session) {
        this.session = session;
    }

    public ServletContext getServletContext() {
        if(this.session != null){
            return this.session.getServletContext();
        }else{
            return null;
        }
    }

    public void createSession() {
        HttpSession hs = request.getSession(false);
        if (hs != null) hs.invalidate();
        this.session = request.getSession(true);
    }

    public void setSessionTimeout(int interval) {
        if (this.session != null) {
            this.session.setMaxInactiveInterval(interval);
        }
    }
    
    public boolean containsKey(Object key) {
        return session == null ? false : session.getAttribute((String) key) != null;
    }

    public Object get(Object key) {
        return session == null ? null : session.getAttribute((String) key);
    }

    public Set<String> keySet() {
        Enumeration<String> names = null;
        if (session == null) {
            names = request.getAttributeNames();
        } else {
            names = session.getAttributeNames();
        }
        Set<String> keys = new HashSet<String>();
        while (names.hasMoreElements()) {
            keys.add(names.nextElement());
        }
        return keys;
    }

    public Object put(String key, Object value) {
        if (session != null) {
            Object result = session.getAttribute(key);
            session.setAttribute(key, value);
            return result;
        } else {
            Object result = request.getAttribute(key);
            request.setAttribute(key, value);
            return result;
        }
    }

    public Object remove(String key) {
        if (session == null) {
            Object result = request.getAttribute(key);
            request.removeAttribute(key);
            return result;
        } else {
            Object result = session.getAttribute(key);
            session.removeAttribute(key);
            return result;
        }
    }

    public String getSessionId() {
        if (session != null) {
            return session.getId();
        } else {
            return null;
        }
    }

    public void invalidate() {
        if (session != null) {
            session.invalidate();
        }
    }

    public String getRemoteAddr() {
        if (request != null) {
            String remoteAddr = request.getHeader("X-Forwarded-For");
            if (remoteAddr == null) {
                remoteAddr = request.getRemoteAddr();
            } else if (remoteAddr.indexOf(',') > 0) {
                remoteAddr = remoteAddr.substring(0, remoteAddr.indexOf(','));
            }

            return remoteAddr;
        }

        return null;
    }

    public String getRequestHeader(String key) {
        if (request != null) {
            return request.getHeader(key);
        } else {
            return null;
        }
    }
}

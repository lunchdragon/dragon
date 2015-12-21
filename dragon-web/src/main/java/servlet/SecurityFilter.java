package servlet;

import dragon.comm.crypto.CryptoUtils;
import dragon.service.sec.AccessController;
import dragon.service.sec.SecureContexts;
import dragon.utils.XMLHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lin.cheng on 12/21/15.
 */
public class SecurityFilter implements Filter {
    private static final Log logger = LogFactory.getLog(SecurityFilter.class);
    private FilterConfig filterConfig = null;
    private static final String realm = "dragon";
    private static final ThreadLocal<AuthType> authRequired = new ThreadLocal<AuthType>();
    private SecurityContext securityContext;

    public SecurityFilter() {
    }

    private void doBeforeProcessing(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String contextRoot = request.getContextPath();
        String uri = request.getRequestURI();
        uri = uri.substring(contextRoot.length());
        if (!AccessController.isLoggedIn()) {
            if (securityContext != null) {
                for (SecurePath sp : securityContext.getSecurePaths()) {
                    if (sp.match(uri)) {
                        AuthType candidate = sp.getAuthType();
                        if(uri.contains("rest/h5/")){
                            if(uri.contains("h5/sec/isLoggedIn") || uri.contains("/h5/sec/login")){
                                return;//go ahead to do login
                            } else {
//                                candidate = AuthType.HTML;
                            }
                        }
                        authRequired.set(candidate);

                        break;
                    }
                }
            } else {
                authRequired.set(AuthType.Basic);
            }
            if (authRequired.get() == AuthType.Basic) {
                processBasicAuth(request, response);
            }
        } else {
            //OK
        }
    }

    private void processBasicAuth(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Basic ")) {
            String s = header.substring(6);
            String token = CryptoUtils.base64Decode(s);

            String domain = null;
            String username = "";
            String password = "";
            String ldapDomain = "Empty";
            int delim = token.indexOf(":");

            if (delim != -1) {
                username = token.substring(0, delim);
                password = token.substring(delim + 1);
            }
            delim = username.indexOf('/');
            if (delim > 0) {
                domain = username.substring(0, delim);
                username = username.substring(delim + 1);
                if (StringUtils.isNotBlank(username)) {
                    String[] userDomains = username.split("/");
                    if (userDomains != null) {
                        if (userDomains.length == 2) {
                            if (StringUtils.isNotBlank(userDomains[0].trim())) {
                                username = userDomains[0].trim();
                            }
                            if (StringUtils.isNotBlank(userDomains[1].trim())) {
                                ldapDomain = userDomains[1].trim();
                            }
                        }
                    }
                }
            }

            // Only reauthenticate if username doesn't match Identity.username and user isn't authenticated
            if (!AccessController.isLoggedIn()) {
                try {
                    SecureContexts.beginSession(domain, username, password, ldapDomain);
                } catch (NamingException ex) {
                    Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (AccessController.isLoggedIn()) {
            authRequired.set(null);
        }
    }

    private void doAfterProcessing(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        AuthType authType = authRequired.get();
        if (authType != null) {
            if  (authType == AuthType.Forbidden) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account has been disabled or closed");
            } else if (authType == AuthType.Basic) {
                response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");
            } else  { //Form or Mobile
                String contextRoot = request.getContextPath();
                    String loginPage = contextRoot;
                if (securityContext != null && securityContext.getLoginPage() != null) {
                    loginPage += "/" + securityContext.getLoginPage();
                }
                    response.sendRedirect(loginPage);
            }
        } else {
            String noSession = request.getHeader(HttpConst.NO_SESSION);
            if (noSession != null && noSession.equalsIgnoreCase("true")) {
            }
        }
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        authRequired.set(null);
        SecureContexts.beginRequest((HttpServletRequest) request);
        Throwable problem = null;
        try {
            doBeforeProcessing(request, response);

            if (authRequired.get() == null) {
                try {
                    chain.doFilter(request, response);
                } catch (Throwable t) {
                    // If an exception is thrown somewhere down the filter chain,
                    // we still want to execute our after processing, and then
                    // rethrow the problem after that.
                    problem = t;
                }
            }
            doAfterProcessing(request, response);
        } finally {
            SecureContexts.endRequest();
        }

        authRequired.set(null);

        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        String configLocation = filterConfig.getInitParameter("configLocation");
        InputStream in = filterConfig.getServletContext().getResourceAsStream(configLocation);
        try {
            String xml = IOUtils.toString(in);
            Document doc = XMLHelper.createDocument(xml, false);
            Element root = doc.getDocumentElement();
            securityContext = (SecurityContext) XMLHelper.unmarshall(root, SecurityContext.class, SecurePath.class);
        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("SecurityFilter()");
        }
        StringBuffer sb = new StringBuffer("SecurityFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }
}

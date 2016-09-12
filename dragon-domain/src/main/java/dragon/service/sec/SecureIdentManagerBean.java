package dragon.service.sec;

import dragon.comm.ApplicationException;
import dragon.comm.crypto.CryptoALG;
import dragon.comm.crypto.CryptoUtils;
import dragon.model.food.User;
import dragon.service.BizIntf;
import dragon.service.GroupIntf;
import dragon.utils.DbHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class SecureIdentManagerBean implements SecureIdentManager {

    static Log logger = LogFactory.getLog(SecureIdentManagerBean.class);
    @PersistenceContext
    private EntityManager em;
    @EJB
    private BizIntf eb;
    @EJB
    private GroupIntf gb;

    public void createOrUpdate(User user, final String password, final boolean strong) throws Exception {
        final String username = user.getName();
        createOrUpdate(username, password, strong);
    }

    public SecureIdent createOrUpdate(final String username, final String password, final boolean strong) throws Exception {
        SecureIdent ident = getIdent(username);
        if (ident == null) {
            ident = new SecureIdent();
            ident.setLoginId(username);
        }
        try {
            CryptoALG alg = strong ? CryptoALG.SHA_1 : CryptoALG.MD5;
            String salt = strong ? CryptoUtils.CreateRandomSalt() : null;
            String encoded = CryptoUtils.encrypt(alg, password, salt);
            ident.setPasscode(encoded);
            ident.setSalt(salt);
            ident.setAlg(alg);
        } catch (Exception ex) {
            logger.error("", ex);
            ident.setPasscode(password);
            ident.setAlg(CryptoALG.None);
        }

        saveIdent(ident);
        logger.info(username + " identity changed.");
        return ident;
    }

    public SecureIdent getIdent(String uid) {
        Connection conn = null;
        SecureIdent ret = null;

        try {
            conn = DbHelper.getConn();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from dragon_ident where login_id = '" + uid + "'");

            if (rs.next()) {
                ret = new SecureIdent();
                ret.setLoginId(uid);
                ret.setAlg(CryptoALG.valueOf(rs.getString("alg")));
                ret.setSalt(rs.getString("salt"));
                ret.setPasscode(rs.getString("passcode"));
            }

            return ret;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            DbHelper.closeConn(conn);
        }
    }

    public int saveIdent(SecureIdent u) throws Exception {

        String key = u.getLoginId();
        int cnt = DbHelper.runUpdate2(null, "update dragon_ident set passcode=?,alg=?,salt=? where login_id=?",
                u.getPasscode(), u.getAlg().toString(), u.getSalt(), key);

        if (cnt > 0) {
            return cnt;
        }

        cnt = DbHelper.runUpdate2(null, "insert into dragon_ident (login_id,passcode,alg,salt) VALUES(?,?,?,?)",
                key, u.getPasscode(), u.getAlg().toString(), u.getSalt());

        return cnt;
    }

    public int delete(String uid) throws Exception {
        logger.info("Removing ident: " + uid);

        int cnt = DbHelper.runUpdate2(null, "delete from dragon_ident where login_id=?", uid);
        return cnt;
    }
    
    public boolean validatePassword(final String username, final String password) throws ApplicationException {
        boolean isLocal = true;

        if (isLocal) {
            SecureIdent ident = getIdent(username);
            if (ident != null) {
                if (checkAccountLock(username)) {
                    throw new ApplicationException("Account locked due to consecutive login failures.");
                }
                try {
                    String encoded = ident.getPasscode();
                    String encpwd = CryptoUtils.encrypt(ident.getAlg(), password, ident.getSalt());
                    if (encoded.equals(encpwd)) {
                        return true;
                    } else {
                        trackAndLock(username, true);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(SecureIdentManagerBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return false;
    }

//    public void resetAccountLock(final long userId) {
//        final long custId = AccessController.getEffectiveCustId();
//        new RunAsGuard() {
//
//            @Override
//            protected Object execute() throws ApplicationException {
//                resetAccountLock(custId, userId);
//                return null;
//            }
//        }.runAs(AccessController.SYSTEM_CUST_ID);
//    }
//
//    public void resetAccountLock(long domainId, long userId) {
//        long custId = AccessController.getEffectiveCustId();
//        if (custId != AccessController.SUPER_CUST_ID && custId != AccessController.SYSTEM_CUST_ID) {
//            throw new AccessControlException("No sufficient privileges!");
//        }
//
//        SecureQueryExecutor sqe = getQueryExecutor();
//        sqe.remove(LoginTrail.class, new QueryCriteria("domainId=?1 and userId=?2", domainId, userId));
//        sqe.remove(LockedClient.class, new QueryCriteria("domainId=?1 and user.id=?2", domainId, userId));
//    }

    private static final int LOGIN_TRACK_TIME = 300000; // 5 mins
    private static final int LOCK_EXP_TIME = 600000; // 10 mins
    private static final int MAX_LOGIN_FAILED_TIMES = 10;

    private void trackAndLock(String userId, boolean loginFailed) {
//        String clientIp = SecureContexts.getRemoteAddr();
//        Ehcache cache = EhCacheManager.getEhcache(EhCacheManager.LOCAL_ADDRESSES_CACHE);
//        if (cache != null) {
//            net.sf.ehcache.Element elem = cache.get(clientIp);
//            if (elem != null) {
//                return;
//            }
//        } else {
//            if (NetUtils.isLocalHostAddress(clientIp)) {
//                return;
//            }
//        }
//
//        SecureQueryExecutor sqe = getQueryExecutor();
//        LoginTrail loginTrail = new LoginTrail();
//        loginTrail.setDomainId(domainId);
//        loginTrail.setUserId(userId);
//        loginTrail.setAttemptFailed(loginFailed);
//        loginTrail.setRemoteAddr(clientIp);
//        sqe.saveNew(loginTrail);
//        if (loginFailed) {
//            int loginTrackTime = LOGIN_TRACK_TIME;
//            SystemConfig lttSc = custConfMgr.get(ConfigCategory.Appserver, "Login_Track_Time");
//            if (lttSc != null) {
//                loginTrackTime = lttSc.getIntValue(LOGIN_TRACK_TIME);
//            }
//            List<LoginTrail> list = sqe.retrieveDataList(LoginTrail.class,
//                    new QueryCriteria("userId=?1 and creationTime>?2",
//                    loginTrail.getUserId(), System.currentTimeMillis() - loginTrackTime).setSortFields("creationTime DESC"));
//            int count = 0;
//            for (LoginTrail lt : list) {
//                if (lt.isAttemptFailed()) {
//                    count++;
//                } else {
//                    break;
//                }
//            }
//            int maxLoginFailedCount = MAX_LOGIN_FAILED_TIMES;
//            SystemConfig sc = custConfMgr.get(ConfigCategory.Appserver, "Max_Login_Failure_Count");
//            if (sc != null) {
//                maxLoginFailedCount = sc.getIntValue(MAX_LOGIN_FAILED_TIMES);
//            }
//            if (count == maxLoginFailedCount) {
//                User user = sqe.findById(User.class, userId);
//                Domain domain = sqe.findByCriteria(Domain.class, new QueryCriteria("domainId=?1", domainId));
//                LockedClient lockedClient = new LockedClient(loginTrail.getRemoteAddr(), user, loginTrail.getDomainId(), domain.getName());
//                sqe.saveNew(lockedClient);
//                PhAudit.audit(LogMessage.PH_AUDIT_ACCOUNT_LOCKED,
//                        new Pair("phCustId", String.valueOf(domainId)),
//                        new Pair("customer", domain.getName()),
//                        new Pair("targetUser", user.getName()),
//                        new Pair("srcIpAddr", String.valueOf(clientIp)));
//            }
//        }
    }

    private boolean checkAccountLock(String userId) {
//        SecureQueryExecutor sqe = getQueryExecutor();
//        LockedClient lockedClient = sqe.findByCriteria(LockedClient.class,
//                new QueryCriteria("domainId=?1 and user.id=?2", domainId, userId));
//        User u = sqe.findByCriteria(User.class,
//                new QueryCriteria("id=?1", userId));
//        Long userSpecificLockOutDuration = u.getLockoutDuration(); //60,000 milliseconds per
//        if (lockedClient != null) {
//            long lockExpiredTime =  (userSpecificLockOutDuration == null) ? LOCK_EXP_TIME : (userSpecificLockOutDuration * 60000);
////            Commented as we are getting per user lockout time from the database (i.e. User.lockoutDuration)
////            SystemConfig sc = custConfMgr.get(ConfigCategory.Appserver, "Lock_Expire_Time");
////            if (sc != null) {
////                lockExpiredTime = sc.getIntValue(LOCK_EXP_TIME);
////            }
//            if (lockedClient.getCreationTime() > System.currentTimeMillis() - lockExpiredTime) {
//                return true;
//            } else {
//                sqe.remove(LockedClient.class, lockedClient.getId());
//            }
//        }

        return false;
    }
}

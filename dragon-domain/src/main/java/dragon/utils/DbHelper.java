package dragon.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by lin.cheng on 6/16/15.
 */
public class DbHelper {

    static Log logger = LogFactory.getLog(DbHelper.class);

    private static ComboPooledDataSource ds = null;
    private static Connection sc = null;
    private static final Object lock = new Object();
    private static final Object lock2 = new Object();

    public static ComboPooledDataSource getDs(){
        if(ds == null){
            synchronized(lock){
                if(ds == null){
                    String server = ConfigHelper.instance().getConfig("server");
                    String db = ConfigHelper.instance().getConfig("db");
                    String dbuser = ConfigHelper.instance().getConfig("dbuser");
                    String dbpwd = ConfigHelper.instance().getConfig("dbpwd");

                    ds = new ComboPooledDataSource();
                    try {
                        ds.setDriverClass("org.postgresql.Driver");
                    } catch (PropertyVetoException e) {
                        logger.error("", e);
                        return null;
                    }
                    ds.setJdbcUrl("jdbc:postgresql://" + server + "/" + db);
                    ds.setUser(dbuser);
                    ds.setPassword(dbpwd);
                }
            }
        }

        return ds;
    }

    public static Connection getConn() throws SQLException {
        Connection conn = getDs().getConnection();
        logger.debug("Created :" + conn);
        return conn;
    }

    public static Connection getSharedConn() throws SQLException {
        if(sc == null || sc.isClosed()){
            synchronized(lock2){
                if(sc == null || sc.isClosed()){
                    sc = null;
                    sc = getDs().getConnection();
                }
            }
        }

        return sc;
    }

    public static void closeConn(Connection conn) {
        if(conn != null){
            try {
                logger.debug("Closed :" + conn);
                conn.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    public static <T> T runWithSingleResult(String sql, Connection conn){
        boolean reuse = conn != null;//Remember to close connection outside
        try {
            if(!reuse){
                conn = DbHelper.getConn();
            }
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if(rs.next()){
                return (T) rs.getObject(1);
            }
        } catch (Exception e){
            logger.error("", e);
        } finally {
            if(!reuse){
                closeConn(conn);
            }
        }
        return null;
    }

    public static int runUpdate(Connection conn, String sqlFmt, Object ... params){
        boolean reuse = conn != null;//Remember to close connection outside
        try {
            if(!reuse){
                conn = DbHelper.getConn();
            }
            Statement st = conn.createStatement();
            String sql = String.format(sqlFmt, params);
            int cnt = st.executeUpdate(sql);
            return cnt;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if(!reuse){
                closeConn(conn);
            }
        }
    }
}

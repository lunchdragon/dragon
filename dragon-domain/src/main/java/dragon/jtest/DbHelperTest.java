package dragon.jtest;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dragon.db.DbHelper;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.omg.DynamicAny.DynAnyHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Created by lin.cheng on 6/16/15.
 */
public class DbHelperTest {

    @Test
    public void testUpdate() throws Exception {
        ComboPooledDataSource ds = DbHelper.getDs();
        int id = 0;
        Connection conn = null;
        try{
            conn = ds.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select nextval ('dragon_id_sec')");
            if(rs.next()){
                id = rs.getInt(1);
            }

        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        DbHelper.runUpdate(null, "insert into dragon_restaurant VALUES('test3','',9,0,5,%s)", id);
    }
}
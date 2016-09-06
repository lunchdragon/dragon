package rest;

import dragon.comm.JSONHelper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Created by Lin on 2016/9/5.
 */
public class BaseRest {

    static Log logger = LogFactory.getLog(BaseRest.class);

    public static String createErrorResponse(Throwable ex) {

        try {
            logger.error(ex);
            return JSONHelper.toJson(new Error(ExceptionUtils.getRootCauseMessage(ex)));
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static class Error implements Serializable {
        int code;//TODO
        String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public Error(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Error(String msg) {
            this.code = -1;
            this.msg = msg;
        }
    }
}

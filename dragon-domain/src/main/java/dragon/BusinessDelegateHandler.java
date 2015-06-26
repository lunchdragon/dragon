package dragon;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author lin.cheng
 */
public class BusinessDelegateHandler implements InvocationHandler {
    private Object obj;

    public BusinessDelegateHandler(Object obj) {
        this.obj = obj;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(obj, args);
        } catch (Throwable t) {
            Throwable root = ExceptionUtils.getRootCause(t);
            if (root == null) {
                root = t;
            }
            throw root;
        }
    }

}

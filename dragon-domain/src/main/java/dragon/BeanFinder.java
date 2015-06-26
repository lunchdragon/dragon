package dragon;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author lin.cheng
 */
public class BeanFinder {
    private static final BeanFinder instance = new BeanFinder();

    private Context ctx = null;
    private Map<Class, Object> cache = null;

    public static BeanFinder getInstance() {
        return instance;
    }

    private BeanFinder() {
        try {
            ctx = new InitialContext();
            cache = Collections.synchronizedMap(new HashMap<Class, Object>());
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * get EJB local session bean
     * @param beanImplClass session bean implementation class
     * @return
     */
    public <T> T getLocalSessionBean(Class<T> beanImplClass) {
        T obj = (T) cache.get(beanImplClass);
        if (obj == null) {
            try {
                Object beanRef = ctx.lookup("java:comp/env/dragon/" + beanImplClass.getSimpleName() + "/local");
                obj = (T) Proxy.newProxyInstance(beanImplClass.getClassLoader(), beanImplClass.getInterfaces(), new BusinessDelegateHandler(beanRef));
                cache.put(beanImplClass, obj);
            } catch (NamingException ex) {
                throw new RuntimeException(ex);
            }
        }

        return obj;
    }

}

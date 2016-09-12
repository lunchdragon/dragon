package dragon.service.sec;

import dragon.comm.ApplicationException;
import dragon.model.food.User;

import javax.ejb.Local;

@Local
public interface SecureIdentManager {

    void createOrUpdate(User user, final String password, final boolean strong) throws Exception;

    SecureIdent createOrUpdate(final String username, final String password, boolean strong)throws Exception;

    int delete(String userId)throws Exception;

    boolean validatePassword(final String username, final String password) throws ApplicationException;
}

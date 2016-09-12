package dragon.service.sec;

import javax.ejb.Local;

@Local
public interface Authenticator
{
    Identity authenticate(String username, String password) throws RuntimeException;
    Identity authenticate(String username, String password, boolean setTwoFactor) throws RuntimeException;
}

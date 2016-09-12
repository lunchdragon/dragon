package dragon.service.sec;

import dragon.comm.crypto.CryptoALG;

public class SecureIdent {
    String loginId;
    String passcode;
    CryptoALG alg;
    String salt;

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public CryptoALG getAlg() {
        return alg;
    }

    public void setAlg(CryptoALG alg) {
        this.alg = alg;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}

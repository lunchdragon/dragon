package dragon.comm.crypto;

public enum CryptoALG {
    None("none"),              // 0
    MD5("MD5"),                // 1
    SHA_1("SHA-1"),            // 2
    SHA_256("SHA-256"),        // 3
    AES("AES");                // 4, reversible

    private String specName;

    private CryptoALG(String name) {
        this.specName = name;
    }

    public String getSpecName() {
        return specName;
    }

    public static CryptoALG specToALG(String specName) {
        for (CryptoALG alg : CryptoALG.values()) {
            if (alg.getSpecName().equalsIgnoreCase(specName)) {
                return alg;
            }
        }
        return null;
    }
}

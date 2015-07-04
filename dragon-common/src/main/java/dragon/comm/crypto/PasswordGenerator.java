package dragon.comm.crypto;

import java.util.Random;

public class PasswordGenerator {

    private final String LETTERS = "qwertyuiopzxcvbnmasdfghjklAZERTYUIOPMLKJHGFDSQWXCVBN";
    private final String NUMBERS = "1357924680";
    private final String SPECHARS = "%^+!*(#"; //"~!@#$%^*()?;=+-";

    private final Random random;

    public PasswordGenerator() {
        random = new Random(System.currentTimeMillis());
    }

    public String next(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length has to be bigger zero");
        }

        StringBuilder genPwd = new StringBuilder();

        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasSpeChar = false;
        
        for (int i = 0; i < length; i++) {
            int type_selector = random.nextInt(3);
            char c = '1';
            switch (type_selector % 3) {
                case 0:
                    c = LETTERS.charAt(random.nextInt(LETTERS.length()));
                    hasLetter = true;
                    break;
                case 1:
                    c = NUMBERS.charAt(random.nextInt(NUMBERS.length()));
                    hasNumber = true;
                    break;
                case 2:
                    c = SPECHARS.charAt(random.nextInt(SPECHARS.length()));
                    hasSpeChar = true;
                    break;
            }
            genPwd.append(c);
        }

        if (!hasLetter) {
            genPwd.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        if (!hasNumber) {
            genPwd.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (!hasSpeChar) {
            genPwd.append(SPECHARS.charAt(random.nextInt(SPECHARS.length())));
        }


        return genPwd.toString();
    }
}

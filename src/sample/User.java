package sample;

import java.security.Key;

public class User {
    private String userName;
    private Key publicKey;
    private Key privateKey;
    public static final int lenSign = 512;
    public static final String algo = "RSA";

    public User(String userName) {
        this.userName = userName;
    }

    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(Key privateKey) {
        this.privateKey = privateKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

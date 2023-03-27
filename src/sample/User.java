package sample;

import java.security.Key;

public class User {
    private String userName;
    private Key publicKey;
    private Key privateKey;
    private Key importedPublicKey;
    private Key importedPrivateKey;
    public static final int lenSign = 1024;
    public static final String algo = "RSA";

    public Key getImportedPublicKey() {
        return importedPublicKey;
    }

    public void setImportedPublicKey(Key importedPublicKey) {
        this.importedPublicKey = importedPublicKey;
    }

    public Key getImportedPrivateKey() {
        return importedPrivateKey;
    }

    public void setImportedPrivateKey(Key importedPrivateKey) {
        this.importedPrivateKey = importedPrivateKey;
    }

    public User(String userName) {
        this.userName = userName;
    }

    public void setPublicKey(Key publicKey) {this.publicKey = publicKey;}

    public Key getPublicKey() {return publicKey;}

    public Key getPrivateKey() {return privateKey;}

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
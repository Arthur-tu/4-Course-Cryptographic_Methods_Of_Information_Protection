package sample;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class User {
    private String userName;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Key importedPublicKey;

    public static final int lenSign = 1024;
    public static final String algo = "RSA";

    public Key getImportedPublicKey() {
        return importedPublicKey;
    }

    public void setImportedPublicKey(Key importedPublicKey) {
        this.importedPublicKey = importedPublicKey;
    }

    public User(String userName) {
        this.userName = userName;
    }

    public void setPublicKey(PublicKey publicKey) {this.publicKey = publicKey;}

    public PublicKey getPublicKey() {return publicKey;}

    public PrivateKey getPrivateKey() {return privateKey;}

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getUserName() {
        return userName;
    }
}

package sample.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Main;
import sample.User;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainController {
    @FXML
    private MenuItem createMenuItem;
    @FXML
    private MenuItem loadMenuItem;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private TextArea textAria;
    @FXML
    private Button saveDocButton;
    @FXML
    private Button loadDocButton;
    @FXML
    private Button chooseUserButton;
    @FXML
    private TextField nameField;
    @FXML
    private MenuItem exportOpenKeyItem;
    @FXML
    private MenuItem importOpenKeyItem;
    @FXML
    private MenuItem deleteKetPairItem;
    @FXML
    private MenuItem chooseCloseKeyItem;

    private static User currentUser;

    @FXML
    void initialize() {
        closeMenuItem.setOnAction(actionEvent -> closeProgram());

        aboutMenuItem.setOnAction(actionEvent -> openNewModalScene(
                "/sample/fxml/about.fxml", "О программе"));

        chooseUserButton.setOnAction(actionEvent -> chooseUser());

        saveDocButton.setOnAction(actionEvent -> saveDoc());

        createMenuItem.setOnAction(actionEvent -> createDoc());
    }

    private void createDoc() {
        textAria.clear();
        Main.stage.setTitle("Подписанный документ");
    }

    private void saveDoc() {
        String docText = textAria.getText();
        String lenUserName = String.valueOf(currentUser.getUserName().length());
        try {
             byte[] sign = getSign(docText);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private byte[] intToByteArray ( final int i ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(i);
        dos.flush();
        return bos.toByteArray();
    }

    private byte[] getSign(String docText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(User.algo);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(User.algo);
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(User.lenSign, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        currentUser.setPrivateKey(privateKey);
        currentUser.setPublicKey(publicKey);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] sign = cipher.doFinal(docText.getBytes(StandardCharsets.UTF_8));
        return sign;
    }

    private void chooseUser() {
        nameField.setDisable(false);
        String userName = nameField.getText();
        if (!userName.isEmpty()) {
            currentUser = new User(userName);
            nameField.setDisable(true);
        } else {
            System.out.println("Пользователь не задан");
        }
    }

    private void closeProgram() {
        System.exit(1);
    }

    private void openNewModalScene(String window, String title) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(window));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }
}

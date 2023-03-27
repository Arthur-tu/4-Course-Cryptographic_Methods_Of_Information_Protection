package sample.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Animations.Shake;
import sample.Main;
import sample.User;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class MainController {
    private static User currentUser;
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

    @FXML
    void initialize() {
        closeMenuItem.setOnAction(actionEvent -> closeProgram());

        aboutMenuItem.setOnAction(actionEvent -> openNewModalScene(
                "/sample/fxml/about.fxml", "О программе"));

        chooseUserButton.setOnAction(actionEvent -> chooseUser());

        saveDocButton.setOnAction(actionEvent -> saveDoc());

        saveMenuItem.setOnAction(actionEvent -> saveDoc());

        createMenuItem.setOnAction(actionEvent -> createDoc());

        loadDocButton.setOnAction(actionEvent -> loadDoc());

        loadMenuItem.setOnAction(actionEvent -> loadDoc());

        exportOpenKeyItem.setOnAction(actionEvent -> exportOpenKey());

        importOpenKeyItem.setOnAction(actionEvent -> importOpenKey());

        chooseCloseKeyItem.setOnAction(actionEvent -> chooseCloseKey());

        deleteKetPairItem.setOnAction(actionEvent -> deleteKeyPair());
    }

    private void deleteKeyPair() {
        try {
            File file2 = new File("src/PK/" + currentUser.getUserName() + ".pub");
            if (file2.delete()) {
                makeAlert(Alert.AlertType.INFORMATION, "Пара ключей удалена!", "Успех!");
                currentUser.setPublicKey(null);
                currentUser.setPrivateKey(null);
                chooseCloseKey();
            } else {
                makeAlert(Alert.AlertType.ERROR, "Пара ключей не найдена!", "Упс!");
            }
        } catch (NullPointerException npe) {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
    }

    private void chooseCloseKey() {
        nameField.setDisable(false);
        nameField.requestFocus();
        nameField.clear();
    }

    private void saveDoc() {
        if (currentUser != null && !currentUser.getUserName().isEmpty()) {
            String docText = textAria.getText();
            String path = choiceDir();
            if (!path.equals("Папка установки не выбрана")) {
                try (FileOutputStream fos = new FileOutputStream(path);
                     DataOutputStream dataOutputStream = new DataOutputStream(fos)) {
                    byte[] sign = getSignMD5withRSA(docText);
                    dataOutputStream.writeInt(currentUser.getUserName().length());
                    dataOutputStream.writeInt(sign.length);
                    dataOutputStream.writeUTF(currentUser.getUserName());
                    dataOutputStream.write(sign);
                    dataOutputStream.writeUTF(docText);
                } catch (Exception e) {
                    makeAlert(Alert.AlertType.ERROR, "Файл слишком большой!", "Упс!");
                }
                Main.stage.setTitle("Автор: " + currentUser.getUserName());
            } else {
                makeAlert(Alert.AlertType.ERROR, path, "Упс!");
            }
        } else {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
    }

    private void importOpenKey() {
        if (currentUser != null && !currentUser.getUserName().isEmpty()) {
            File path = loadImportKeyDir();
            if (path != null) {
                try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path))) {
                    int ulen = dataInputStream.readInt();
                    int blob_len = dataInputStream.readInt();
                    String uname = dataInputStream.readUTF();
                    byte[] openkey = dataInputStream.readNBytes(blob_len);
                    byte[] sign = getSignSHA512withRSA(openkey);

                    // Сохраним подписанный открытый ключ
                    File file2 = new File("src/PK/" + currentUser.getUserName() + ".pub");
                    if (file2.delete()) {
                        try (FileOutputStream fos = new FileOutputStream(file2.getPath());
                             DataOutputStream dataOutputStream = new DataOutputStream(fos)) {
                            dataOutputStream.writeInt(ulen);
                            dataOutputStream.writeInt(blob_len);
                            dataOutputStream.writeUTF(uname);
                            dataOutputStream.write(openkey);
                            dataOutputStream.write(sign);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                makeAlert(Alert.AlertType.ERROR, "Импортируемый публичный ключ не выбран!", "Упс!");
            }
        } else {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
    }

    private void exportOpenKey() {
        if (currentUser != null && !currentUser.getUserName().isEmpty()) {
            File file = loadExportKeyDir();
            if (file != null) {
                String path = file.getAbsolutePath() + "\\" + currentUser.getUserName() + ".pub";
                File file2 = new File("src/PK/" + currentUser.getUserName() + ".pub");
                try (FileOutputStream fos = new FileOutputStream(path); DataOutputStream dataOutputStream =
                        new DataOutputStream(fos); FileOutputStream fos2 = new FileOutputStream(file2.getPath());
                     DataOutputStream dataOutputStream2 = new DataOutputStream(fos2)) {

                    dataOutputStream.writeInt(currentUser.getUserName().length());
                    dataOutputStream.writeInt(currentUser.getPublicKey().getEncoded().length);
                    dataOutputStream.writeUTF(currentUser.getUserName());
                    dataOutputStream.write(currentUser.getPublicKey().getEncoded());

                    dataOutputStream2.writeInt(currentUser.getUserName().length());
                    dataOutputStream2.writeInt(currentUser.getPublicKey().getEncoded().length);
                    dataOutputStream2.writeUTF(currentUser.getUserName());
                    dataOutputStream2.write(currentUser.getPublicKey().getEncoded());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                makeAlert(Alert.AlertType.ERROR,
                        "Папка установки экспортированного ключа не выбрана!", "Упс!");
            }
        } else {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
    }

    private void loadDoc() {
        if (currentUser != null && !currentUser.getUserName().isEmpty()) {
            File path = loadDir();
            if (path != null) {
                try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path))) {
                    int userNameLength = dataInputStream.readInt();
                    System.out.println("userNameLength " + userNameLength);

                    int signLength = dataInputStream.readInt();
                    System.out.println("signLength " + signLength);

                    String userName = dataInputStream.readUTF();
                    System.out.println("username " + userName);

                    byte[] sign = dataInputStream.readNBytes(signLength);

                    String newdocText = dataInputStream.readUTF();
                    System.out.println("docText " + newdocText);

                    // Проверка подписи автора под документом:


                    if (findInPkDir(userName)) {
                        // Проверяем открытый ключ
                        File file = new File("src/PK/" + userName + ".pub");
                        try (DataInputStream dataInputStream1 =
                                     new DataInputStream(new FileInputStream(file.getPath()))) {
                            int openKeyUserNameLen = dataInputStream1.readInt();
                            System.out.println(openKeyUserNameLen);

                            int openKeyLen = dataInputStream1.readInt();
                            System.out.println(openKeyLen);

                            String openKeyUserName = dataInputStream1.readUTF();
                            System.out.println(openKeyUserName);

                            byte[] openKeyBlob = dataInputStream1.readNBytes(openKeyLen);
                            byte[] openKeySign = dataInputStream1.readAllBytes();

                            if (checkSignUnderOpenKey(openKeySign, openKeyBlob)) {
                                // Проверка подписи автора под документом:
                                try {
                                    Signature signature = Signature.getInstance("MD5withRSA");
                                    signature.initVerify((PublicKey) currentUser.getPublicKey());
                                    signature.update(newdocText.getBytes(StandardCharsets.UTF_8));
                                    boolean isgood = signature.verify(sign);

                                    if (isgood) {
                                        textAria.clear();
                                        textAria.appendText(newdocText);
                                        Main.stage.setTitle("Подписанный документ " + currentUser.getUserName());
                                    } else {
                                        makeAlert(Alert.AlertType.ERROR, "Проверка подписи автора под " +
                                                "документом не пройдена!", "Упс!");
                                    }
                                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                makeAlert(Alert.AlertType.ERROR, "Проверка подписи пользователя под открытым " +
                                    "ключом не пройдена!", "Упс!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        makeAlert(Alert.AlertType.ERROR, "Публичный ключ не экспортирован!", "Упс!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                makeAlert(Alert.AlertType.ERROR, "Окно выбора закрыто", "Упс!");
            }
        } else {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
    }

    private boolean checkSignUnderOpenKey(byte[] openKeySign, byte[] openKeyBlob){
        boolean isGood = false;
        try {
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify((PublicKey) currentUser.getImportedPublicKey());
            signature.update(openKeyBlob);
            isGood =  signature.verify(openKeySign);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return isGood;
    }

    private boolean findInPkDir(String userName) {
        File file = new File("src/PK/" + userName + ".pub");
        return file.exists();
    }

    private void createDoc() {
        textAria.clear();
        Main.stage.setTitle("Подписанный документ");
    }



    private String choiceDir() {
        String ans = "Папка установки не выбрана";
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Рекомендуемое расширение *.sd", "*.sd"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Выберите папку для установки");
        File dir = fileChooser.showSaveDialog(Main.stage);

        if (dir != null) {
            return dir.getAbsolutePath();
        } else {
            return ans;
        }
    }

    private File loadExportKeyDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setTitle("Выберите папку для экспорта открытого ключа");
        File dir = directoryChooser.showDialog(Main.stage);
        return dir;
    }

    private File loadDir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Файл с расширением *.sd", "*.sd"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Выберите папку для установки");
        File dir = fileChooser.showOpenDialog(Main.stage);
        return dir;
    }

    private File loadImportKeyDir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Файл с расширением *.pub", "*.pub"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Выберите файл для импорта открытого ключа");
        File dir = fileChooser.showOpenDialog(Main.stage);
        return dir;
    }

    private byte[] getSignMD5withRSA(String docText) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(User.algo);
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(User.lenSign, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        currentUser.setPrivateKey(privateKey);
        currentUser.setPublicKey(publicKey);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign((PrivateKey) privateKey);
        signature.update(docText.getBytes(StandardCharsets.UTF_8));
        return signature.sign();
    }

    private byte[] getSignSHA512withRSA(byte[] openkey) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(User.algo);
        SecureRandom random = new SecureRandom();
        keyPairGenerator.initialize(User.lenSign, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        currentUser.setImportedPrivateKey(privateKey);
        currentUser.setImportedPublicKey(publicKey);
        Signature signature = Signature.getInstance("SHA512withRSA");
        signature.initSign((PrivateKey) privateKey);
        signature.update(openkey);
        return signature.sign();
    }

    private void chooseUser() {
        nameField.setDisable(false);
        String userName = nameField.getText();
        if (!userName.isEmpty()) {
            currentUser = new User(userName);
            nameField.setDisable(true);
        } else {
            nameField.setText("Пользователь не задан!");
            Shake shake = new Shake(nameField);
            shake.playAnimation();
        }
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

    private void makeAlert(Alert.AlertType type, String headerText, String title) {
        Alert alert = new Alert(type);
        alert.setHeaderText(headerText);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void closeProgram() {
        System.exit(1);
    }
}
package sample.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Animations.Shake;
import sample.Dto;
import sample.Main;
import sample.User;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class MainController {
    private static User currentUser;
    private static Dto dto;
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

    private void saveDoc()  {
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
                    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                    X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(openkey);
                    KeyFactory keyFact = KeyFactory.getInstance(User.algo, "BC");
                    PublicKey pubKey = keyFact.generatePublic(x509Spec);
                    currentUser.setImportedPublicKey(pubKey);
                    byte[] sign = getSignSHA512withRSA(openkey);
                    dto = new Dto(ulen, blob_len, uname, openkey, sign);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /**
                 * Сохранение подписанного открытого ключа
                 */
                if (path.delete()) {
                    try (FileOutputStream fos = new FileOutputStream(path.getPath());
                         DataOutputStream dataOutputStream = new DataOutputStream(fos)) {
                        dataOutputStream.writeInt(dto.getUlen());
                        dataOutputStream.writeInt(dto.getBlob_len());
                        dataOutputStream.writeUTF(dto.getUname());
                        dataOutputStream.write(dto.getOpenkey());
                        dataOutputStream.write(dto.getSign());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                makeAlert(Alert.AlertType.INFORMATION, "Открытый ключ импортирован!", "Успех!");
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
            File file2 = new File("src/PK/" + currentUser.getUserName() + ".pub");
            try (FileOutputStream fos2 = new FileOutputStream(file2.getPath());
                 DataOutputStream dataOutputStream2 = new DataOutputStream(fos2)) {
                 dataOutputStream2.writeInt(currentUser.getUserName().length());
                 dataOutputStream2.writeInt(currentUser.getPublicKey().getEncoded().length);
                 dataOutputStream2.writeUTF(currentUser.getUserName());
                 dataOutputStream2.write(currentUser.getPublicKey().getEncoded());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            makeAlert(Alert.AlertType.INFORMATION, "Открытый ключ экспортирован", "Успех!");
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
                    int signLength = dataInputStream.readInt();
                    String userName = dataInputStream.readUTF();
                    byte[] sign = dataInputStream.readNBytes(signLength);
                    String newdocText = dataInputStream.readUTF();
                    /**
                     * Проверка на существование экспортированного ключа
                     */
                    if (findInPkDir(userName)) {
                        /**
                         * Считываем открытый ключ
                         */
                        File file = new File("src/PK/" + userName + ".pub");
                        try (DataInputStream dataInputStream1 =
                                     new DataInputStream(new FileInputStream(file.getPath()))) {
                            int openKeyUserNameLen = dataInputStream1.readInt();
                            int openKeyLen = dataInputStream1.readInt();
                            String openKeyUserName = dataInputStream1.readUTF();
                            byte[] openKeyBlob = dataInputStream1.readNBytes(openKeyLen);
                            byte[] openKeySign = dataInputStream1.readAllBytes();
                            /**
                             * Проверка подписи под открытым ключом
                             */
                            try {
                                Signature signature = Signature.getInstance("SHA512withRSA");
                                signature.initVerify(currentUser.getPublicKey());
                                signature.update(openKeyBlob);
                                boolean verified = signature.verify(openKeySign);
                                if (verified) {
                                    System.out.println("Ура");
                                    /**
                                     * Проверка подписи автора под документом
                                     */
//                                    Signature signature2 = Signature.getInstance("MD5withRSA");
//                                    signature2.initVerify((PublicKey) currentUser.getImportedPublicKey());
//                                    signature2.update(newdocText.getBytes(StandardCharsets.UTF_8));
//                                    boolean verified2 = signature.verify(sign);
//                                    if (verified2) {
//                                        textAria.clear();
//                                        textAria.appendText(newdocText);
//                                        Main.stage.setTitle("Подписанный документ " + currentUser.getUserName());
//                                    } else {
//                                        makeAlert(Alert.AlertType.ERROR, "Проверка подписи автора под " +
//                                                "документом не пройдена!", "Упс!");
//                                    }
                                } else {
                                    makeAlert(Alert.AlertType.ERROR, "Проверка подписи пользователя под открытым " +
                                            "ключом не пройдена!", "Упс!");
                                    return;
                                }
                            } catch (Exception e) {
                                makeAlert(Alert.AlertType.ERROR, "Ключ не импортирован или импортирован неподходящий", "Упс!");
                                e.printStackTrace();
                            }

                            // Проверка
                            try {
                                Signature signature = Signature.getInstance("MD5withRSA");
                                signature.initVerify((PublicKey) currentUser.getImportedPublicKey());
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

    private File loadDir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Файл с расширением *.sd", "*.sd"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Выберите папку для установки");
        return fileChooser.showOpenDialog(Main.stage);
    }

    private File loadImportKeyDir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Файл с расширением *.pub", "*.pub"));
        fileChooser.setInitialDirectory(new File("./src/PK"));
        fileChooser.setTitle("Выберите импортируемый открытый ключ");
        return fileChooser.showOpenDialog(Main.stage);
    }

    private byte[] getSignMD5withRSA(String docText) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException {
          Signature signature = Signature.getInstance("MD5withRSA");
          signature.initSign(currentUser.getPrivateKey());
          signature.update(docText.getBytes(StandardCharsets.UTF_8));
          return signature.sign();
    }

    private byte[] getSignSHA512withRSA(byte[] openkey) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException {
        byte[] message = openkey.clone();
        Signature signature = Signature.getInstance("SHA512withRSA");
        signature.initSign(currentUser.getPrivateKey());
        signature.update(message);
        return signature.sign();
    }

    private void chooseUser() {
        nameField.setDisable(false);
        String userName = nameField.getText();
        if (!userName.isEmpty()) {
            currentUser = new User(userName);
            nameField.setDisable(true);
            /**
             * Генерация пары ключей для документа
             */
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(User.algo, "BC");
                generator.initialize(User.lenSign, new SecureRandom());
                KeyPair keyPair = generator.generateKeyPair();
                PublicKey publicKey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();
                currentUser.setPublicKey(publicKey);
                currentUser.setPrivateKey(privateKey);
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                e.printStackTrace();
            }
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
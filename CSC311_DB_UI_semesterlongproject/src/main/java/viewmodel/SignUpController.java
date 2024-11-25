package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.util.prefs.Preferences;


public class SignUpController {

    @FXML
    private TextField usernameTF;
    @FXML
    private TextField pswdTF;

    @FXML
    private PasswordField pswdPF;
    @FXML
    private TextField conPswdTF;

    @FXML
    private PasswordField conPswdPF;
    @FXML
    private CheckBox showPwsdBox;

    @FXML
    protected void createNewAccount(ActionEvent actionEvent) {
        String username = usernameTF.getText();
        String password = pswdPF.getText();
        String confirmPassword = conPswdPF.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put(username, password);

        showAlert("Success", "Account created successfully.");
        clearForm();
    }
    @FXML
    protected  void showPassword(ActionEvent actionEvent){
        boolean showPswd = showPwsdBox.isSelected();
        pswdTF.setText(pswdPF.getText());
        pswdTF.setVisible(showPswd);
        pswdTF.setManaged(showPswd);
        pswdPF.setVisible(!showPswd);
        pswdPF.setManaged(!showPswd);
        conPswdTF.setText(conPswdPF.getText());
        conPswdTF.setVisible(showPswd);
        conPswdTF.setManaged(showPswd);
        conPswdPF.setVisible(!showPswd);
        conPswdPF.setManaged(!showPswd);
    }

    private void clearForm() {
        usernameTF.clear();
        pswdPF.clear();
        conPswdPF.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class DB_GUI_Controller implements Initializable {
   StorageUploader store = new StorageUploader();

    @FXML
    ProgressBar progressBar;
    @FXML
    private ComboBox<Major> major;

    @FXML
    Button addButtn,editButtn,deleteButtn,clearButtn;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TextField searchBar;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private static final String regexName = "^[a-zA-Z]{2,25}$";
    private static final String regexEmail = "^[a-zA-Z0-9._%+-]+@farmingdale.edu$";
    private static final String regexDepartment = "^[a-zA-Z\\s]{2,50}$";




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {

            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            major.getItems().setAll(Major.values());

            Buttontips();
            setEditable();
            searchFilter();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private boolean validateFields() {
        if (!first_name.getText().matches(regexName)) {
            showAlert("Invalid First Name Field", "First name must be 2-25 alphabetic characters.");
            return false;
        }
        if (!last_name.getText().matches(regexName)) {
            showAlert("Invalid Last Name Field", "Last name must be 2-25 alphabetic characters.");
            return false;
        }
        if (!department.getText().matches(regexDepartment)) {
            showAlert("Invalid Department Field", "Department must be 2-50 alphabetic characters.");
            return false;
        }
        if (!email.getText().matches(regexEmail)) {
            showAlert("Invalid Email Field", "Email must end with @farmingdale.edu.");
            return false;
        }
        if (major.getValue() == null) {
            showAlert("Invalid Major Field", "Please select a major.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static enum Major {Business, CSC, CPIS,CS}

    private void setEditable() {

        editButtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        deleteButtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

        addButtn.disableProperty().bind(
                first_name.textProperty().isEmpty()
                        .or(last_name.textProperty().isEmpty())
                        .or(department.textProperty().isEmpty())
                        .or(major.valueProperty().isNull())
                        .or(email.textProperty().isEmpty())
        );
    }

    @FXML
    protected void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                ObservableList<Person> importedData = FXCollections.observableArrayList();
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 5) { // Ensure there are enough values
                        Person person = new Person(
                                values[0], // First Name
                                values[1], // Last Name
                                values[2], // Department
                                values[3], // Major
                                values[4], // Email
                                values.length > 5 ? values[5] : "" // Image URL (optional)
                        );
                        importedData.add(person);
                    }
                }
                data.clear();
                data.addAll(importedData);
                tv.setItems(data);
                showAlert("Import Successful", "CSV file data imported");
            } catch (IOException e) {
                showAlert("Import Error", "An error occurred importing the CSV file.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("data.csv");
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Person person : data) {
                    writer.write(String.join(",",
                            person.getFirstName(),
                            person.getLastName(),
                            person.getDepartment(),
                            person.getMajor(),
                            person.getEmail(),
                            person.getImageURL()
                    ));
                    writer.newLine();
                }
                showAlert("Export Successful", "Data exported to CSV file.");
            } catch (IOException e) {
                showAlert("Export Error", "An error occurred exporting data to CSV file.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void addNewRecord() {

            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    major.getValue().toString(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();

    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.getSelectionModel().clearSelection();
        email.setText("");
        imageURL.setText("");
    }


    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {

        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(
                index + 1,
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                major.getValue().toString(),
                email.getText(),
                imageURL.getText()
        );
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }




    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }

        Task<Void> uploadTask = createUploadTask(file, progressBar);
        progressBar.progressProperty().bind(uploadTask.progressProperty());
        new Thread(uploadTask).start();
    }



    @FXML
    protected void addRecord() {
        showSomeone();
    }




    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            major.setValue(Major.valueOf(p.getMajor()));
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
        }
    }


    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }


    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                    }
                }

                return null;
            }
        };
    }

    private void Buttontips() {

        Tooltip clearTooltip = new Tooltip("Clear fields ");
        Tooltip addTooltip = new Tooltip(" Add a new record ");
        Tooltip editTooltip = new Tooltip(" Edit the selected record ");
        Tooltip deleteTooltip = new Tooltip(" Delete the selected record");
        clearTooltip.setShowDelay(javafx.util.Duration.seconds(2));
        addTooltip.setShowDelay(javafx.util.Duration.seconds(2));
        editTooltip.setShowDelay(javafx.util.Duration.seconds(2));
        deleteTooltip.setShowDelay(javafx.util.Duration.seconds(2));
        Tooltip.install(clearButtn,clearTooltip);
        Tooltip.install(addButtn, addTooltip);
        Tooltip.install(editButtn, editTooltip);
        Tooltip.install(deleteButtn, deleteTooltip);
    }

    private void searchFilter() {
        FilteredList<Person> filteredData = new FilteredList<>(data, p -> true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(person -> {if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return person.getFirstName().toLowerCase().contains(lowerCaseFilter)
                        || person.getLastName().toLowerCase().contains(lowerCaseFilter)
                        || person.getEmail().toLowerCase().contains(lowerCaseFilter)
                        || person.getDepartment().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Person> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tv.comparatorProperty());
        tv.setItems(sortedData);
    }

}
package porganizer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;


public class Controller implements Initializable {

    @FXML
    private TextField txtBaseDir;
    @FXML
    private TextField txtNewDir;
    @FXML
    private TextField txtPrefix;
    @FXML
    private TextField txtSuffix;
    @FXML
    private TextField txtStartNum;
    @FXML
    private TextArea txtLog;

    private File defaults, baseDir, newDir;
    private File[] newImages;

    private final DirectoryChooser dirChooser = new DirectoryChooser();

    private static final String[] imageFileExts = new String[]{".png", ".jpg", ".jpeg", ".tif", ".gif", ".bmp", ".nef"};

    @FXML
    private void selectBaseDir(ActionEvent event) {
        baseDir = dirChooser.showDialog(((Button) event.getSource()).getScene().getWindow());
        if (baseDir != null) {
            txtBaseDir.setText(baseDir.getAbsolutePath());
            setFields();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(defaults));
                writer.write(baseDir.getAbsolutePath());
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void selectNewDir(ActionEvent event) {
        newDir = dirChooser.showDialog(((Button) event.getSource()).getScene().getWindow());
        if (newDir != null) {
            txtNewDir.setText(newDir.getAbsolutePath());
            newImages = newDir.listFiles(getFileFilter());
            Arrays.sort(newImages, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });
            txtLog.appendText(newImages.length + " image files found.\n");
        }
    }


    @FXML
    private void clearLog(){
        txtLog.clear();
    }

    @FXML
    private void copyFiles() {
        if (baseDir != null && baseDir.canWrite()) {
            int copied = 0, failed = 0;
            for (File src : newImages) {
                try {
                    String ext = src.getAbsolutePath();
                    ext = ext.substring(ext.lastIndexOf("."));
                    Files.copy(src.toPath(), getTargetPath(ext), StandardCopyOption.REPLACE_EXISTING);
                    txtStartNum.setText(increment(txtStartNum.getText()));
                    copied++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    txtLog.appendText("Failed to copy file " + src.getName());
                    failed++;
                }
            }
            txtLog.appendText("Copied " + copied + " files. Failed to copy " + failed + " files.\n");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Files Copied...");
            alert.setHeaderText("Copy Completed");
            alert.setContentText("Copied " + copied + " files. Failed to copy " + failed + " files.");
            alert.showAndWait();
        } else {
            txtLog.appendText("Could not write to the target directory. Check your selection of Base Directory.");
        }
    }

    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        defaults = new File(System.getProperty("user.home") + "/.porganizer");
        if (defaults.exists() && defaults.canRead()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(defaults));
                String line = reader.readLine();
                if (line != null) {
                    baseDir = new File(line);
                    if (baseDir.exists()) {
                        txtBaseDir.setText(line);
                        setFields();
                    }
                }
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setFields() {
        String[] imageList = baseDir.list(getFileFilter());
        if (imageList.length > 0) {
            String str = imageList[0];
            if (str.lastIndexOf(".") > 0) {
                str = str.substring(0, str.lastIndexOf("."));
            }
            String[] sAry = str.split("\\d+");
            if (!sAry[0].equals("")) {
                txtPrefix.setText(sAry[0]);
            }

            if (sAry.length > 1 && !sAry[sAry.length - 1].equals("")) {
                txtSuffix.setText(sAry[sAry.length - 1]);
            }
            getNextNumber();
        }
    }

    private void getNextNumber() {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                String prefix = txtPrefix.getText();
                String suffix = txtSuffix.getText();

                if (name.startsWith(prefix) && name.endsWith(suffix))
                    return true;
                return false;
            }
        };
        String[] imageList = baseDir.list(getFileFilter());
        int startNum = 0;
        String startNumTxt = "00000";
        for (String str : imageList) {
            Scanner scanner = new Scanner(str).useDelimiter("\\D+");
            if (scanner.hasNextInt()) {
                String next = scanner.next();
                startNumTxt = startNum < Integer.parseInt(next) ? next : startNumTxt;
            }
        }
        txtStartNum.setText(increment(startNumTxt));
    }

    private static String increment(String base) {
        int baseInt = Integer.parseInt(base) + 1;
        String newStr = base.replaceAll("\\d", "0") + baseInt;
        return newStr.substring(newStr.length() - base.length());
    }

    private FilenameFilter getFileFilter() {
        FilenameFilter imageFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                for (String str : imageFileExts) {
                    if (name.endsWith(str)) {
                        return true;
                    }
                }
                return false;
            }
        };
        return imageFilter;
    }

    private Path getTargetPath(String ext) {
        File file = new File(txtBaseDir.getText() + "/" + txtPrefix.getText() + txtStartNum.getText() +
                txtSuffix.getText() + ext);
        return file.toPath();
    }
}


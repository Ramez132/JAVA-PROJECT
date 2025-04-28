package management.top;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import management.window.ManagementWindowController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;
import util.Constants;

import java.io.File;
import java.io.IOException;

public class ManagementTopController {

    private ManagementWindowController managementWindowController;
    @FXML private Button loadNewFileButton;
    @FXML private Label userNameLabel;
    private String userName;

    @FXML private Label messageOfRecentActionOutcomeLabel;

    public void setManagementWindowController(ManagementWindowController managementWindowController) {
        this.managementWindowController = managementWindowController;
    }

    public void setTextInUserNameLabel(String userName) {
        userNameLabel.setText(userName);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessageOfRecentActionOutcomeLabel (String messageOfRecentActionOutcome) {
        messageOfRecentActionOutcomeLabel.setText(messageOfRecentActionOutcome);
    }

    @FXML
    public void handleLoadNewFile() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        // Show the file chooser and wait for user selection
        File selectedFile = fileChooser.showOpenDialog(loadNewFileButton.getScene().getWindow());

        if (selectedFile != null) {
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart
                            ("file",
                            selectedFile.getName(),
                            RequestBody.create(selectedFile,
                                MediaType.parse("application/octet-stream")))
                    .build();

            String finalUrl = HttpUrl
                    .parse(Constants.UPLOAD_NEW_FILE)
                    .newBuilder()
                    .addQueryParameter("username", userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsyncWithPostAndBody(finalUrl, body,new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() ->
                            messageOfRecentActionOutcomeLabel.setText("Something went wrong: " + e.getMessage())
                    );
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() != 200) {
                        String responseBody = response.body().string();
                        Platform.runLater(() ->
                                messageOfRecentActionOutcomeLabel.setText(responseBody)
                        );
                    } else {
                        Platform.runLater(() -> {
                            messageOfRecentActionOutcomeLabel.setText("Recent file loaded successfully");
                            managementWindowController.addInfoOfUploadedSheetToTableView(response.body());
                        });
                    }
                }

            });
        }



//        // If a file is selected, display its name in the label
//        if (selectedFile != null) {
//            try {
//                Sheet sheet = sheetWindowController.getEngineManager().getSheetFromFile(selectedFile);
//                messageOfRecentActionOutcomeLabel.setText("Recent file loaded successfully");
//                filePathLabel.setText(selectedFile.getAbsoluteFile().toString());
//
//                deleteAllVersionNumbersInComboBoxFromPreviousSheet();
//                addNewVersionNumberToVersionComboBox(sheet.getVersion());
////                SheetWindowController.deleteAllRangesInRangeFactoryBeforeLoadingNewSheet();
//                //update UI with possible ranges from the new sheet
//                sheetWindowController.handleInitialRangesFromNewSheet(sheet);
//                sheetWindowController.displayNewSheetFromNewFile(sheet);
//            } catch (Exception e) {
//                messageOfRecentActionOutcomeLabel.setText(e.getMessage());
//            }
//        }
    }

    public void goToSheetWindow() {
        managementWindowController.goToSheetWindow();
    }
}

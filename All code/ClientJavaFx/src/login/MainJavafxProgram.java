package login;

//import engine.api.EngineManagerForServer;
//import engine.impl.EngineManagerForServerImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import management.window.ManagementWindowController;
import operating.window.SheetWindowController;
import util.http.HttpClientUtil;

import java.net.URL;

import static javafx.application.Application.launch;

public class MainJavafxProgram extends Application {

//    private EngineManagerForServer engineManager;
    private Scene loginScene;
    private Scene sheetWindowScene;
    private Scene managementWindowScene;
    ManagementWindowController managementWindowController;
    SheetWindowController sheetWindowController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) throws Exception {
//        engineManager = new EngineManagerForServerImpl();

        primaryStage.setTitle("Sheet-Cell");

        FXMLLoader fxmlLoader = new FXMLLoader();
        //URL url = getClass().getResource("main.fxml");
        URL url = getClass().getClassLoader().getResource("login/loginWindow.fxml");
        fxmlLoader.setLocation(url);
        Parent loginWindowRoot = fxmlLoader.load(url.openStream());
        LoginWindowController loginWindowController = fxmlLoader.getController();
        loginScene = new Scene(loginWindowRoot);

        FXMLLoader fxmlLoaderForManagementWindow = new FXMLLoader();
        URL url2 = getClass().getClassLoader().getResource("management/window/managementWindow.fxml");
//        URL url2 = getClass().getResource("../management/window/managementWindow.fxml");
        fxmlLoaderForManagementWindow.setLocation(url2);
        Parent ManagementWindowRoot = fxmlLoaderForManagementWindow.load(url2.openStream());
        managementWindowController  = fxmlLoaderForManagementWindow.getController();
        managementWindowScene = new Scene(ManagementWindowRoot);

//        url = getClass().getResource("../operating/table/tablePart.fxml");
//        FXMLLoader fxmlLoaderForTablePart = new FXMLLoader();
//        fxmlLoaderForTablePart.setLocation(url);
//        Parent tablePartRoot = fxmlLoaderForTablePart.load(url.openStream());
//        TablePartController tablePartController = fxmlLoaderForTablePart.getController();
//        Scene tablePartScene = new Scene(tablePartRoot);

        FXMLLoader fxmlLoaderForSheetWindow = new FXMLLoader();
        URL url3 = getClass().getClassLoader().getResource("operating/window/sheetWindow.fxml");
        fxmlLoaderForSheetWindow.setLocation(url3);
        Parent sheetWindowRoot = fxmlLoaderForSheetWindow.load(url3.openStream());
        sheetWindowController = fxmlLoaderForSheetWindow.getController();
        sheetWindowScene = new Scene(sheetWindowRoot);

        loginWindowController.setManagementWindowScene(managementWindowScene);
        managementWindowController.setSheetWindowScene(sheetWindowScene);
        sheetWindowController.setManagementWindowScene(managementWindowScene);

        loginWindowController.setPrimaryStage(primaryStage);
        managementWindowController.setPrimaryStage(primaryStage);
        sheetWindowController.setPrimaryStage(primaryStage);

        loginWindowController.setManagementWindowController(managementWindowController);
        managementWindowController.setSheetWindowController(sheetWindowController);
        sheetWindowController.setManagementWindowController(managementWindowController);

//        sheetWindowController.setEngineManager(engineManager);
//        SheetWindowController.setFxmlLoader(fxmlLoader);
//        Parent load = FXMLLoader.load(getClass().getResource("main.fxml"));
//        Scene scene = new Scene(root);
//        Scene scene = new Scene(root, 1262, 800);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }


    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        managementWindowController.close();
        sheetWindowController.close();
    }

}

//@Override
//public void stop() throws Exception {
//    HttpClientUtil.shutdown();
////    ManagementWindowController.close();
//}
//
//public static void main(String[] args) {
//    launch(args);
//}




//
//import javafx.application.Application;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
////video: 100188 - Java FX Hello World [JAD, JavaFX] | Powered by SpeaCode
//public class FirstTryJavaFXProgram extends Application {
//    private int clickCounter;
//    private Button btn;
//
//    @Override
//    public void start(Stage primaryStage) {
//        System.out.println("called on " + Thread.currentThread().getName());
//        btn = new Button();
//        btn.setText("Say 'Hello World'");
//        doSomethingWithButton();
//
//        StackPane root = new StackPane();
//        root.getChildren().add(btn);
//
//        Scene scene = new Scene(root, 300, 250);
//
//        primaryStage.setTitle("Hello World!");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        System.out.println("java fx is done");
//    }
//
//    private void doSomethingWithButton() {
//        EventHandler<ActionEvent> actionEventEventHandler =
//                event -> this.updateClickCounter();
//
//        btn.setOnAction(actionEventEventHandler);
//    }
//
//    private void updateClickCounter(){
//        ++clickCounter;
//        btn.setText("Clicked " + clickCounter + " Times");
//    }
//
//    public static void main(String[] args) {
//        new Thread(() -> System.out.println("blabla")).start();
//        launch(args);
//        System.out.println("main ended");
//    }
//
//}
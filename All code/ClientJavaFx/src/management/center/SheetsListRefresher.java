package management.center;

import dto.management.info.SheetBasicInfoDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import util.Constants;
import static util.Constants.GSON_INSTANCE;

public class SheetsListRefresher extends TimerTask {

    private final Consumer<List<SheetBasicInfoDto>> sheetsListConsumer;
//    private final BooleanProperty shouldUpdate;
    private final String userName;
//    private final String finalUrl;
    private int currentNumOfSheetsInClient;
//    private boolean wasThereAChangeInPermissions;


    public SheetsListRefresher(String userName, Consumer<List<SheetBasicInfoDto>> sheetsListConsumer) {
//        this.shouldUpdate = shouldUpdate;
        this.sheetsListConsumer = sheetsListConsumer;
        this.userName = userName;
        this.currentNumOfSheetsInClient = 0;
//        this.finalUrl = HttpUrl
//                .parse(Constants.GET_ALL_SHEETS_IN_SYSTEM)
//                .newBuilder()
//                .addQueryParameter(Constants.USERNAME, userName)
//                .build()
//                .toString();
    }

    @Override
    public void run() {

//        if (!shouldUpdate.get()) {
//            return;
//        }
        String finalUrl = HttpUrl
                .parse(Constants.GET_ALL_SHEETS_IN_SYSTEM)
                .newBuilder()
                .addQueryParameter(Constants.USERNAME, userName)
                .addQueryParameter(Constants.NUM_OF_SHEETS_IN_SERVER, String.valueOf(currentNumOfSheetsInClient))
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Sheets Request | Ended with failure...");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                Type listType = new TypeToken<List<String>>(){}.getType();
//                List<SheetBasicInfoDto> dataOfAllSheetsInSystem = GSON_INSTANCE.fromJson(response.body().string(), listType);
                int numOfSheetsInServer = Integer.parseInt(response.header(Constants.NUM_OF_SHEETS_IN_SERVER));
                boolean wasThereAChangeInPermissions = Boolean.parseBoolean(response.header(Constants.WAS_THERE_A_CHANGE_IN_PERMISSIONS));
                if (numOfSheetsInServer != currentNumOfSheetsInClient || wasThereAChangeInPermissions) {
                    currentNumOfSheetsInClient = numOfSheetsInServer;
                    String jsonArrayOfSheetBasicInfo = response.body().string();
                    SheetBasicInfoDto[] dataOfAllSheetsInSystem = GSON_INSTANCE.fromJson(jsonArrayOfSheetBasicInfo, SheetBasicInfoDto[].class);
                    sheetsListConsumer.accept(Arrays.asList(dataOfAllSheetsInSystem));
                    response.close();
                }
            }
        });
    }
}

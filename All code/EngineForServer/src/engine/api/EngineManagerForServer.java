package engine.api;

import dto.cell.CellDto;
import dto.management.info.SheetBasicInfoDto;
import dto.permission.PermissionRequestDto;
import dto.range.RangeDto;
import dto.sheet.SheetWithSortedOrFilteredRangeDto;
import dto.sheet.SheetDto;

import java.io.File;
import java.util.List;

public interface EngineManagerForServer {
    SheetBasicInfoDto tryToExtractSheetFromFileAndReturnBasicInfo(File file, String ownerUsername) throws Exception;

    List<PermissionRequestDto> getPendingAndDecidedPermissionRequestsDtoList(String sheetName);

    void addNewPendingPermissionRequest(String sheetName, String username, String permissionLevelRequested);

    void approveSelectedPermissionRequest(String sheetName, String username, String permissionLevelRequested, int numOfRequestForSheet);

    void rejectSelectedPermissionRequest(String sheetName, String username, String permissionLevelRequested, int numOfRequestForSheet);

    SheetDto getLastVersionSheetDto(String sheetName);
    CellDto getCellDtoFromRecentSelectedSheet(String sheetName, int rowNum, int columnNum);
    RangeDto getRangeDtoFromSelectedSheet(String selectedSheetName, String rangeName);
    List<String> getAllRangeNamesForSelectedSheet(String sheetName);
    SheetDto updateCellValueInSelectedSheet(String sheetName, int rowNumber, int columnNumber,
                                            String newOriginalValueStr, String username, int currentVersionNumInUpdatingClient);
    int getVersionNumOfRecentSelectedSheet(String selectedSheetName);
    SheetDto getSheetOfSpecificVersion(String selectedSheetName, int versionNum);
    void deleteRangeFromSelectedSheet(String selectedSheetName, String rangeName);
    boolean isSelectedRangeUsedInAnyCellWithRelevantFunction(String selectedSheetName, String rangeName);
    void addNewRangeToSelectedSheet(String selectedSheetName, String rangeName, String leftTopStartCoordinateStr, String rightBottomEndCoordinateStr);

    SheetWithSortedOrFilteredRangeDto getSheetDtoWithSortedArea(String selectedSheetName,
                                                                String newSortStartCoordinateStr,
                                                                String newSortEndCoordinateStr,
                                                                List<String> allColumnLettersToSortBy);

    List<String> getUniqueValuesForFilteringInSelectedColumnAndRelevantArea(String selectedSheetName, String filterAreaStartCoordinateStr, String filterAreaEndCoordinateStr, String stringWithLetterOfColumnToGetUniqueValuesToFilter);

    SheetWithSortedOrFilteredRangeDto getSheetDtoWithFilteredArea(String selectedSheetName, String filterAreaStartCoordinateStr, String filterAreaEndCoordinateStr, char columnLetterForFiltering, List<String> uniqueValuesInSelectedColumn);

    List<SheetBasicInfoDto> getDataOfAllSheetsInSystem(String username);

    int getNumOfPendingPermissionRequestsForSelectedSheet(String sheetName);

    boolean wasThereAChangeInSelectedUserPermissions(String username);

    String getPermissionLevelForSelectedUserAndSheet(String selectedSheetName, String userName);

    List<String> getAllRangesNamesFromSelectedSheet(String selectedSheetName);

    SheetDto getTempSheetForDynamicAnalysis(String selectedSheetName, int rowNumber, int columnNumber, String newOriginalValueStr, String username);


//    SheetBasicInfoDto extractSheetFromFileAndReturnBasicInfo(File file, String ownerUsername) throws Exception;
//    Sheet getSheetFromFile(File file) throws Exception;
//    Sheet getMostRecentSheetWithSelectedName(String sheetName);
////    Sheet getMostRecentSheet();
//
//    /**
//     * Returns a cell from the most recent sheet.
//     * if the cell is not present in activeCells in the sheet, it will be created as a new empty cell
//     * @param row    the row of the cell
//     * @param col the column of the cell
//     * @return the cell from the most recent sheet
//     */
//    Cell getCellFromMostRecentSheetWithSelectedName(String sheetName, int row, int col);
//    Sheet updateValueOfCellAndGetNewSheetWithSelectedName(String sheetName, int row, int col, String value);
//
//    /**
//     * @param version should be a number of requested version, starting from 1 (not 0)
//     */
//    Sheet getSheetOfSpecificVersion(int version);
//    int getLatestVersionNumber();
//    boolean isThereASheetLoadedToTheSystem();
//
//    Range addRangeToMostRecentSheet(String rangeName, String leftTopStartCoordinateStr, String rightBottomEndCoordinateStr);
//    Range getRangeFromMostRecentSheet(String rangeName);
//    void deleteRangeFromRangeFactory(String rangeName);
//    List<String> getAllRangeNamesInTheSystem();
//    boolean isThereAnyRangeInRangesFactory();
//    Range getRangeByItsName(String rangeName);
//    boolean isSelectedRangeUsedInAnyCellWithRelevantFunction(String rangeName);
//    boolean isFilteringOrSortingAreaValid(String newFilterStartCoordinateStr, String newFilterEndCoordinateStr);
//
//    boolean isColumnLetterInFilteringOrSortingArea
//            (String stringWithLetterOfSelectedColumn, String newAreaStartCoordinateStr, String newAreaEndCoordinateStr);
//
//    List<String> getUniqueValuesForFilteringInSelectedColumnAndRelevantArea
//            (char charLetterOfColumnToGetUniqueValuesToFilter, String newFilterStartCoordinateStr, String newFilterEndCoordinateStr);
//
//    /**
//    * method will be invoked only if the area and coordinates are valid
//     */
//    Range createRangeToSortOrFilter(String newFilterStartCoordinateStr, String newFilterEndCoordinateStr);
//
////    Sheet createCopyOfSheet();
//
//    Sheet createCopyOfRecentSheet();
//
//    RangeWithRowsInArea createFilteredRangeArea
//            (Range filteringRange, char letterOfColumnToGetUniqueValuesToFilter, List<String> uniqueValuesToFilter);
//
//    RangeWithRowsInArea createSortedRangeArea(Range newSortingRange, List<Character> listOfColumnLettersCharactersToSortBy);
}

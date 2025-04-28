package engine.impl;

import dto.cell.CellDto;
import dto.management.info.SheetBasicInfoDto;
import dto.permission.PermissionRequestDto;
import dto.range.RangeDto;
import dto.range.RangeWithEffectiveValuesDto;
import dto.sheet.SheetWithSortedOrFilteredRangeDto;
import dto.sheet.SheetDto;
import engine.api.EngineManagerForServer;
import engine.api.SingleSheetManager;
import engine.dto.factory.*;
import engine.permissions.PermissionLevel;
import engine.permissions.PermissionRequest;
import shticell.cell.api.Cell;
import shticell.jaxb.SheetFromFilesFactory;
import shticell.range.Range;
import shticell.row.RangeWithRowsInArea;
import shticell.sheet.api.Sheet;

import java.io.File;
import java.util.*;

public class EngineManagerForServerImpl implements EngineManagerForServer {

    private final Map<String, SingleSheetManager> mapSheetNameToSingleSheetManager;

    public EngineManagerForServerImpl() {
        this.mapSheetNameToSingleSheetManager = new HashMap<>();
    }

    public SheetBasicInfoDto tryToExtractSheetFromFileAndReturnBasicInfo(File file, String ownerUsername) throws Exception {
        try {
            return addNewSheetToNewManagerAndReturnBasicInfo(file, ownerUsername);
        }
        catch (Exception e) {
            throw e;
        }
    }

    private SheetBasicInfoDto addNewSheetToNewManagerAndReturnBasicInfo(File file, String ownerUsername) throws Exception {
        try {
            SheetBasicInfoDto sheetBasicInfoDto;
            Sheet possibleNewSheet = getSheetFromFile(file, ownerUsername);
            String newSheetName = possibleNewSheet.getNameOfSheet();

            if (mapSheetNameToSingleSheetManager.containsKey(newSheetName)) {
                throw new IllegalArgumentException("Sheet with the same name already exists in the system.");
            }
            SingleSheetManager singleSheetManager = new SingleSheetManagerImpl();
            sheetBasicInfoDto = singleSheetManager.addNewSheetToNewManagerAndReturnBasicInfo(possibleNewSheet, ownerUsername);
            mapSheetNameToSingleSheetManager.put(newSheetName, singleSheetManager);

            return sheetBasicInfoDto;
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<SheetBasicInfoDto> getDataOfAllSheetsInSystem(String username) {
        List<SheetBasicInfoDto> allSheetsInSystem = new ArrayList<>();
        synchronized (mapSheetNameToSingleSheetManager) {
            for (Map.Entry<String, SingleSheetManager> entry : mapSheetNameToSingleSheetManager.entrySet()) {
                SingleSheetManager singleSheetManager = entry.getValue();
                singleSheetManager.trySetPermissionLevelForSelectedUser(username);
                SheetBasicInfoDto sheetBasicInfoDto = singleSheetManager.getSheetBasicInfoDtoForSelectedUserWithItsPermissionLevel(username);
                allSheetsInSystem.add(sheetBasicInfoDto);
            }
        }

        return allSheetsInSystem;
    }

    @Override
    public int getNumOfPendingPermissionRequestsForSelectedSheet(String sheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        return singleSheetManager.getNumOfPendingPermissionRequests();
    }

    @Override
    public boolean wasThereAChangeInSelectedUserPermissions(String username) {
        for (Map.Entry<String, SingleSheetManager> entry : mapSheetNameToSingleSheetManager.entrySet()) {
            SingleSheetManager singleSheetManager = entry.getValue();
            if (singleSheetManager.wasThereAChangeInSelectedUserPermissions(username)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getPermissionLevelForSelectedUserAndSheet(String selectedSheetName, String userName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        return singleSheetManager.getPermissionLevelForSelectedUser(userName);
    }

    @Override
    public List<String> getAllRangesNamesFromSelectedSheet(String selectedSheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        return singleSheetManager.getAllRangeNamesRelatedToThisSheet();
    }

    @Override
    public SheetDto getTempSheetForDynamicAnalysis(String selectedSheetName, int rowNumber, int columnNumber, String newOriginalValueStr, String username) {

        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        Sheet sheet = singleSheetManager.getTempSheetForDynamicAnalysis(rowNumber, columnNumber, newOriginalValueStr, username);
        return SheetDtoFactory.createSheetDtoFromSheet(sheet);
    }

    @Override
    public List<PermissionRequestDto> getPendingAndDecidedPermissionRequestsDtoList(String sheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        List<PermissionRequest> permissionRequestsList = singleSheetManager.getPendingAndDecidedPermissionRequestsList();
        List<PermissionRequestDto> permissionRequestsDtoList = new ArrayList<>();
        for (PermissionRequest permissionRequest : permissionRequestsList) {
            PermissionRequestDto permissionRequestDto = PermissionRequestDtoFactory.createNewPermissionRequestDto(permissionRequest);
            permissionRequestsDtoList.add(permissionRequestDto);
        }

        return permissionRequestsDtoList;
    }

    @Override
    public void addNewPendingPermissionRequest(String sheetName, String username, String permissionLevelRequested) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        PermissionLevel permissionLevelRequestedAsEnum = PermissionLevel.valueOf(permissionLevelRequested.toUpperCase());
        singleSheetManager.addNewPendingPermissionRequest(username, permissionLevelRequestedAsEnum);
    }

    @Override
    public void approveSelectedPermissionRequest(String sheetName, String username, String permissionLevelRequested, int numOfRequestForSheet) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        PermissionLevel permissionLevelRequestedAsEnum = PermissionLevel.valueOf(permissionLevelRequested.toUpperCase());
        singleSheetManager.approvePermissionRequest(username, permissionLevelRequestedAsEnum, numOfRequestForSheet);
    }

    @Override
    public void rejectSelectedPermissionRequest(String sheetName, String username, String permissionLevelRequested, int numOfRequestForSheet) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        PermissionLevel permissionLevelRequestedAsEnum = PermissionLevel.valueOf(permissionLevelRequested.toUpperCase());
        singleSheetManager.rejectPermissionRequest(username, permissionLevelRequestedAsEnum, numOfRequestForSheet);
    }

    @Override
    public SheetDto getLastVersionSheetDto(String sheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        Sheet sheet = singleSheetManager.getMostRecentSheet();
        return SheetDtoFactory.createSheetDtoFromSheet(sheet);
    }

    @Override
    public CellDto getCellDtoFromRecentSelectedSheet(String sheetName, int rowNum, int columnNum) {

        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        Cell cell = singleSheetManager.getCellFromMostRecentSheet(rowNum, columnNum);
        return CellDtoFactory.createCellDto(cell);
    }

    @Override
    public RangeDto getRangeDtoFromSelectedSheet(String selectedSheetName, String rangeName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        Range range = singleSheetManager.getRangeFromMostRecentSheet(rangeName);
        return RangeDtoFactory.createRangeDtoFromRange(range);
    }

    @Override
    public List<String> getAllRangeNamesForSelectedSheet(String sheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
        return singleSheetManager.getAllRangeNamesRelatedToThisSheet();
    }

    @Override
    public SheetDto updateCellValueInSelectedSheet
            (String sheetName, int rowNumber, int columnNumber,
             String newOriginalValueStr, String username, int currentVersionNumInUpdatingClient) {
        if (!mapSheetNameToSingleSheetManager.containsKey(sheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(sheetName);
//        Sheet sheet = singleSheetManager.updateValueOfCellAndGetNewSheet(rowNumber, columnNumber, newOriginalValueStr, username);
        Sheet sheet = singleSheetManager.updateValueOfCellAndGetNewSheet
                                            (rowNumber, columnNumber,newOriginalValueStr,
                                                username, currentVersionNumInUpdatingClient);
        return SheetDtoFactory.createSheetDtoFromSheet(sheet);
    }

    @Override
    public int getVersionNumOfRecentSelectedSheet(String selectedSheetName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        return singleSheetManager.getLatestVersionNumber();
    }

    @Override
    public SheetDto getSheetOfSpecificVersion(String selectedSheetName, int versionNum) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        Sheet sheet = singleSheetManager.getSheetOfSpecificVersion(versionNum);
        return SheetDtoFactory.createSheetDtoFromSheet(sheet);
    }

    @Override
    public void deleteRangeFromSelectedSheet(String selectedSheetName, String rangeName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        singleSheetManager.deleteRangeRelatedToThisSheet(rangeName);
    }

    @Override
    public boolean isSelectedRangeUsedInAnyCellWithRelevantFunction(String selectedSheetName, String rangeName) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        return singleSheetManager.isSelectedRangeUsedInAnyCellWithRelevantFunction(rangeName);
    }

    @Override
    public void addNewRangeToSelectedSheet(String selectedSheetName, String rangeName,
                                           String leftTopStartCoordinateStr, String rightBottomEndCoordinateStr) {
        if (!mapSheetNameToSingleSheetManager.containsKey(selectedSheetName)) {
            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
        }
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        singleSheetManager.addRangeToMostRecentSheet(rangeName, leftTopStartCoordinateStr, rightBottomEndCoordinateStr);
    }

    @Override
    public SheetWithSortedOrFilteredRangeDto getSheetDtoWithSortedArea
            (String selectedSheetName, String newSortStartCoordinateStr,
             String newSortEndCoordinateStr, List<String> allColumnLettersToSortByAsStrings) {
        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);

        boolean isSortingAreaValid = singleSheetManager.isFilteringOrSortingAreaValid(newSortStartCoordinateStr, newSortEndCoordinateStr);
        if (!isSortingAreaValid) {
            throw new IllegalArgumentException
                    ("The sorting area " + newSortStartCoordinateStr + " to" + newSortEndCoordinateStr +
                    " is not valid. Please enter valid area for sorting and column letters in this area.");
        }

        Range newSortingRange = singleSheetManager.createRangeToSortOrFilter(newSortStartCoordinateStr, newSortEndCoordinateStr);
        boolean areAllColumnLetterInSortingArea = checkIfAllColumnLettersInSortingArea(singleSheetManager, allColumnLettersToSortByAsStrings, newSortStartCoordinateStr, newSortEndCoordinateStr);
        if (!areAllColumnLetterInSortingArea) {
            throw new IllegalArgumentException
            ("One or more column letters is not in selected sorting area - please enter valid area and column letters in this area.");
        }

        List<Character> listOfColumnLettersCharactersToSortBy = convertArrayOfColumnLettersStringsToArrayOfCharacters(allColumnLettersToSortByAsStrings);
        RangeWithRowsInArea sortedRangeArea = singleSheetManager.createSortedRangeArea(newSortingRange, listOfColumnLettersCharactersToSortBy);
        Sheet sheet = singleSheetManager.createCopyOfRecentSheet();
        SheetDto sheetDto = SheetDtoFactory.createSheetDtoFromSheet(sheet);
        RangeWithEffectiveValuesDto rangeWithEffectiveValuesDto = RangeWithEffectiveValuesDtoFactory.createRangeWithEffectiveValuesDto(sortedRangeArea);

        return new SheetWithSortedOrFilteredRangeDto(sheetDto, rangeWithEffectiveValuesDto);
    }

    @Override
    public List<String> getUniqueValuesForFilteringInSelectedColumnAndRelevantArea
            (String selectedSheetName, String filterAreaStartCoordinateStr,
             String filterAreaEndCoordinateStr, String stringWithLetterOfColumnToGetUniqueValuesToFilter) {

        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        boolean isSortingAreaValid = singleSheetManager.isFilteringOrSortingAreaValid(filterAreaStartCoordinateStr, filterAreaEndCoordinateStr);
        if (!isSortingAreaValid) {
            throw new IllegalArgumentException
                    ("The filtering area " + filterAreaStartCoordinateStr + " to" + filterAreaEndCoordinateStr +
                            " is not valid. Please enter valid area for filtering and column letter in this area.");
        }

        boolean isColumnLetterInFilteringArea = singleSheetManager.isColumnLetterInFilteringOrSortingArea
                (stringWithLetterOfColumnToGetUniqueValuesToFilter, filterAreaStartCoordinateStr, filterAreaEndCoordinateStr);
        if (!isColumnLetterInFilteringArea) {
            throw new IllegalArgumentException
            ("The column letter is not in selected filtering area - please enter valid area for filtering and column letter in this area.");
        }

        char charLetterOfColumnToGetUniqueValuesToFilter = stringWithLetterOfColumnToGetUniqueValuesToFilter.charAt(0);

        List<String> uniqueValuesInSelectedColumn = singleSheetManager.getUniqueValuesForFilteringInSelectedColumnAndRelevantArea(charLetterOfColumnToGetUniqueValuesToFilter, filterAreaStartCoordinateStr, filterAreaEndCoordinateStr);
        if (uniqueValuesInSelectedColumn.isEmpty()) {
            throw new IllegalArgumentException
                    ("No unique values found in selected column '" + charLetterOfColumnToGetUniqueValuesToFilter + "'.");
        }

        return uniqueValuesInSelectedColumn;
    }

    @Override
    public SheetWithSortedOrFilteredRangeDto getSheetDtoWithFilteredArea
            (String selectedSheetName, String filterAreaStartCoordinateStr,
             String filterAreaEndCoordinateStr, char columnLetterForFiltering, List<String> uniqueValuesInSelectedColumn) {

        SingleSheetManager singleSheetManager = mapSheetNameToSingleSheetManager.get(selectedSheetName);
        Range newFilteringRange = singleSheetManager.createRangeToSortOrFilter(filterAreaStartCoordinateStr, filterAreaEndCoordinateStr);
        RangeWithRowsInArea filteredRangeArea =
                singleSheetManager.createFilteredRangeArea(newFilteringRange, columnLetterForFiltering, uniqueValuesInSelectedColumn);
        Sheet sheet = singleSheetManager.createCopyOfRecentSheet();
        SheetDto sheetDto = SheetDtoFactory.createSheetDtoFromSheet(sheet);
        RangeWithEffectiveValuesDto rangeWithEffectiveValuesDto = RangeWithEffectiveValuesDtoFactory.createRangeWithEffectiveValuesDto(filteredRangeArea);

        return new SheetWithSortedOrFilteredRangeDto(sheetDto, rangeWithEffectiveValuesDto);
    }


//    private SheetDto displaySheetWithFilteredOrSortedRange(Sheet sheet, RangeWithRowsInArea filteredOrSortedRangeArea) {
////        SheetDto sheetDto = SheetDtoFactory.createSheetDtoFromSheet(sheet);
//        Map<CoordinateDto, CellDto> coordinateToCellDtoMap = new HashMap<>();
//
//        int numOfRows = sheet.getNumOfRows();
//        int numOfColumns = sheet.getNumOfColumns();
//        Range selectedRange = filteredOrSortedRangeArea.getRange();
//        int filteredRangeRowStart = selectedRange.getRowStart();
//        int filteredRangeRowEnd = selectedRange.getRowEnd();
//        int filteredRangeColumnStart = selectedRange.getColumnStart();
//        int filteredRangeColumnEnd = selectedRange.getColumnEnd();
//
//        for (int currentRowNum = 1; currentRowNum <= numOfRows; currentRowNum++) {
//            for (int currentColumnNum = 1; currentColumnNum <= numOfColumns; currentColumnNum++) {
//                Coordinate coordinate = CoordinateFactory.getCoordinate(currentRowNum, currentColumnNum);
//                String effectiveValueOfCellAsString;
//
//                //check if the cell is in the filtered range
//                if (currentRowNum >= filteredRangeRowStart && currentRowNum <= filteredRangeRowEnd
//                        && currentColumnNum >= filteredRangeColumnStart && currentColumnNum <= filteredRangeColumnEnd) {
//
//                    effectiveValueOfCellAsString = filteredOrSortedRangeArea.getEffectiveValueOfCellAsString(currentRowNum, currentColumnNum);
////                    effectiveValueOfCellAsString = addThousandsSeparator(effectiveValueOfCellAsString);
//                }
//                return sheetDto;
//            }
//        }
//    }

    private boolean checkIfAllColumnLettersInSortingArea(SingleSheetManager singleSheetManager,
            List<String> allColumnLettersToSortByAsStrings, String newSortStartCoordinateStr, String newSortEndCoordinateStr) {
        boolean allColumnLettersInSortingArea = true;
        for (String currentColumnLetterString : allColumnLettersToSortByAsStrings) {
            boolean currentColumnInSortingArea = singleSheetManager.isColumnLetterInFilteringOrSortingArea(currentColumnLetterString, newSortStartCoordinateStr, newSortEndCoordinateStr);
            if (!currentColumnInSortingArea) {
                allColumnLettersInSortingArea = false;
                break;
            }
        }

        return allColumnLettersInSortingArea;
    }

    private List<Character> convertArrayOfColumnLettersStringsToArrayOfCharacters(List<String> allColumnLettersToSortByAsString) {
        List<Character> allColumnLettersToSortByAsCharacters = new ArrayList<>();

        for (String currentString : allColumnLettersToSortByAsString) {
            allColumnLettersToSortByAsCharacters.add(currentString.charAt(0));
        }

        return allColumnLettersToSortByAsCharacters;
    }

//    @Override
//    public SheetBasicInfoDto extractSheetFromFileAndReturnBasicInfo(File file, String ownerUsername) throws Exception {
//        Sheet sheet = getSheetFromFile(file);
//        return SheetBasicInfoDtoFactory.createSheetBasicInfoDto(sheet.getNameOfSheet(), ownerUsername,
//                PermissionLevel.OWNER.getPermissionLevel(), sheet.getNumOfRows(), sheet.getNumOfColumns());
//    }
//

    private Sheet getSheetFromFile(File file, String ownerUsername) throws Exception {
        try {
            Sheet currentSheet;
            currentSheet = SheetFromFilesFactory.CreateSheetObjectFromXmlFile(file, ownerUsername);
            //if the code gets here, the file is valid and new Sheet object was created
            return currentSheet;
        }
        catch (Exception e) {
            throw e;
        }
    }


//
//    @Override
//    public Sheet getMostRecentSheetWithSelectedName(String sheetName) throws NoSuchElementException {
//        if (mapSheetNameToSheetVersionsArray.containsKey(sheetName)) {
//            List<Sheet> sheetVersionsArray = mapSheetNameToSheetVersionsArray.get(sheetName);
//            return sheetVersionsArray.getLast();
//        }
//        else {
//            throw new NoSuchElementException("There is no sheet with the requested name in the system.");
//        }
//
////
////        if (!sheetVersionsArray.isEmpty()) {
////            return sheetVersionsArray.getLast();
////        }
////        else {
////            throw new NoSuchElementException("There is no sheet in the system.");
////        }
//    }
//
//    @Override
//    public Cell getCellFromMostRecentSheetWithSelectedName(String sheetName, int row, int column) {
//        List<Sheet> sheetVersionsArray = mapSheetNameToSheetVersionsArray.get(sheetName);
//        return sheetVersionsArray.getLast().getCell(row, column); //is it the expected behavior?
//    }
//
//    @Override
//    public Sheet updateValueOfCellAndGetNewSheetWithSelectedName(String sheetName, int row, int col, String value) throws RuntimeException {
//        Sheet possibleNewSheet;
//        boolean isUpdatePartOfSheetInitialization = sheetVersionsArray.isEmpty();
//        try {
//            Sheet latestSheet = sheetVersionsArray.getLast();
//            possibleNewSheet = latestSheet.updateCellValueAndCalculate(row, col, value, isUpdatePartOfSheetInitialization);
//            if (possibleNewSheet != sheetVersionsArray.getLast()) { //the sheet has changed
//                sheetVersionsArray.add(possibleNewSheet); //add the new sheet to the list
//            }
//
//            return sheetVersionsArray.getLast();
//        }
//        catch (Exception e) { //should we catch a more specific Exception? could there be a few types?
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    /**
//     * @param version should be a number of requested version, starting from 1 (not 0)
//     */
//    @Override
//    public Sheet getSheetOfSpecificVersion(int version) throws NoSuchElementException, IndexOutOfBoundsException {
//        try {
//            if (!sheetVersionsArray.isEmpty()) {
//                return sheetVersionsArray.get(version - 1);
//            }
//            else {
//                throw new NoSuchElementException("There are no sheets in the system.");
//            }
//        }
//        catch(IndexOutOfBoundsException e){
//            throw new IndexOutOfBoundsException("The requested version is not available.");
//        }
//    }
//
//    /**
//     * Returns a counter that starts from 1 (not 0).
//     *
//     * @return the number of the latest version, starting from 1
//     */
//    @Override
//    public int getLatestVersionNumber() throws NoSuchElementException {
//        if (!sheetVersionsArray.isEmpty()) {
//            return sheetVersionsArray.size();
//        }
//        else {
//            throw new NoSuchElementException("There is no sheet loaded to the system.");
//        }
//    }
//
//    @Override
//    public boolean isThereASheetLoadedToTheSystem() {  //returns true if there is a sheet loaded to the system
//        if (sheetVersionsArray.isEmpty()) {
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    @Override
//    public Range addRangeToMostRecentSheet(String rangeName, String leftTopStartCoordinateStr, String rightBottomEndCoordinateStr) {
//        return RangesManager.createRangeFromTwoCoordinateStringsAndNameString(sheetVersionsArray.getLast(), rangeName, leftTopStartCoordinateStr, rightBottomEndCoordinateStr);
//    }
//
//    @Override
//    public Range getRangeFromMostRecentSheet(String rangeName) {
//        return RangesManager.getRangeByItsName(rangeName);
//    }
//
//    @Override
//    public boolean isSelectedRangeUsedInAnyCellWithRelevantFunction(String rangeName) {
//        return sheetVersionsArray.getLast().isSelectedRangeIsUsedInSheet(rangeName);
//    }
//
//    @Override
//    public boolean isFilteringOrSortingAreaValid(String newFilterStartCoordinateStr, String newFilterEndCoordinateStr) {
//        Sheet currentSheet = sheetVersionsArray.getLast();
//        Coordinate topLeftStartCoordinate, bottomRightEndCoordinate;
//        try {
//            topLeftStartCoordinate = CoordinateFactory.getCoordinateFromStr(newFilterStartCoordinateStr, currentSheet);
//            bottomRightEndCoordinate = CoordinateFactory.getCoordinateFromStr(newFilterEndCoordinateStr, currentSheet);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Error trying to create filtering area from coordinates provided: " + e.getMessage());
//        }
//        return RangesManager.isCoordinatesCreateValidRange(topLeftStartCoordinate, bottomRightEndCoordinate);
//    }
//
//    @Override
//    public Range createRangeToSortOrFilter(String newFilterStartCoordinateStr, String newFilterEndCoordinateStr) {
//        // method will be invoked only if the area and coordinates are valid
//        Sheet currentSheet = sheetVersionsArray.getLast();
//        Coordinate topLeftStartCoordinate = CoordinateFactory.getCoordinateFromStr(newFilterStartCoordinateStr, currentSheet);
//        Coordinate bottomRightEndCoordinate = CoordinateFactory.getCoordinateFromStr(newFilterEndCoordinateStr, currentSheet);
//
//        return new RangeImpl("CurrentRangeToFilterOrSort", topLeftStartCoordinate, bottomRightEndCoordinate);
//    }
//
//    @Override
//    public Sheet createCopyOfRecentSheet() {
//        return sheetVersionsArray.getLast().createCopyOfSheet();
//    }
//
//    @Override
//    public RangeWithRowsInArea createFilteredRangeArea(Range rangeToFilter, char letterOfColumnToGetUniqueValuesToFilter, List<String> uniqueValuesToFilter) {
//        try {
//            if (rangeToFilter == null) {
//                throw new IllegalArgumentException("Filtering range is null.");
//            }
//            if (uniqueValuesToFilter == null) {
//                throw new IllegalArgumentException("List of unique values to filter is null.");
//            }
//            if (uniqueValuesToFilter.isEmpty()) {
//                throw new IllegalArgumentException("List of unique values to filter is empty.");
//            }
//        } catch (Exception e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//        try {
//            Sheet copyOfRecentSheetWithAreaToFilter = sheetVersionsArray.getLast().createCopyOfSheet();
//            RangeWithRowsInArea rangeWithRowsInArea = new RangeWithRowsInArea(copyOfRecentSheetWithAreaToFilter, rangeToFilter);
//            rangeWithRowsInArea.buildRangeFromAllRelevantCellsInRange();
//            int columnNumFromChar = CoordinateFactory.getColumnNumberFromChar(letterOfColumnToGetUniqueValuesToFilter, copyOfRecentSheetWithAreaToFilter);
//            int numOfIterationsLeftToPreform = rangeToFilter.getRowEnd() - rangeToFilter.getRowStart() + 1;
//            int currentRowToCheck = rangeWithRowsInArea.getFirstRowNumToCheck();
//            int lastRowToCheck = rangeWithRowsInArea.getLastRowNumToCheck();
////
////            int rowEnd = rangeToFilter.getRowEnd();
//            int numOfRowsToCheck = rangeWithRowsInArea.getCurrentNumOfRows();
//            int numOfRowsAlreadyChecked = 0;
//
//
//            while (currentRowToCheck <= lastRowToCheck && numOfRowsAlreadyChecked < numOfRowsToCheck) {
//                String currentEffectiveValueOfCellAsString = rangeWithRowsInArea.getEffectiveValueOfCellAsString(currentRowToCheck, columnNumFromChar);
//                if (!uniqueValuesToFilter.contains(currentEffectiveValueOfCellAsString)) {
//                    rangeWithRowsInArea.removeCurrentRowAndMoveAllRowsOneRowUp(currentRowToCheck);
//                    currentRowToCheck--; //to check the next row that moved up
//                }
//                currentRowToCheck++; //even if the row was removed, the next row moved up ?
//                numOfRowsAlreadyChecked++;
//            }
//
//            return rangeWithRowsInArea;
//        }
//        catch (Exception e) {
//            throw new IllegalArgumentException("Error trying to create a copy of the sheet with filtered area: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public RangeWithRowsInArea createSortedRangeArea(Range newSortingRange, List<Character> listOfColumnLettersCharactersToSortBy) {
//        Sheet copyOfRecentSheetWithAreaToSort = sheetVersionsArray.getLast().createCopyOfSheet();
//        RangeSorter rangeToSort = new RangeSorter(copyOfRecentSheetWithAreaToSort, newSortingRange,listOfColumnLettersCharactersToSortBy);
//        rangeToSort.sortRange();
//
//        return rangeToSort.getSortedRangeWithRows();
//    }
//
//
//    @Override
//    public boolean isColumnLetterInFilteringOrSortingArea(String stringWithLetterOfSelectedColumn, String newAreaStartCoordinateStr, String newAreaEndCoordinateStr) {
//        Sheet currentSheet = sheetVersionsArray.getLast();
//        int columnNumFromChar;
//
//        //getting here only if isFilteringOrSortingAreaValid() returned true,
//        //so the coordinates are valid and will not throw an exception
//        Coordinate topLeftStartCoordinate = CoordinateFactory.getCoordinateFromStr(newAreaStartCoordinateStr, currentSheet);
//        Coordinate bottomRightEndCoordinate = CoordinateFactory.getCoordinateFromStr(newAreaEndCoordinateStr, currentSheet);
//
//        if (stringWithLetterOfSelectedColumn.length() != 1) {
//            throw new IllegalArgumentException("Column letter must be exactly one character long.");
//        }
//        try {
//            columnNumFromChar = CoordinateFactory.getColumnNumberFromChar(stringWithLetterOfSelectedColumn.charAt(0), currentSheet);
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Error trying to reach requested column " + stringWithLetterOfSelectedColumn +
//                    " in selected filtering area" + e.getMessage());
//        }
//
//        return columnNumFromChar >= topLeftStartCoordinate.getColumn() && columnNumFromChar <= bottomRightEndCoordinate.getColumn();
//    }
//
//    @Override
//    public List<String> getUniqueValuesForFilteringInSelectedColumnAndRelevantArea
//            (char charLetterOfColumnToGetUniqueValuesToFilter,
//             String newFilterStartCoordinateStr,
//             String newFilterEndCoordinateStr) {
//        return sheetVersionsArray.getLast().getUniqueValuesForFilteringInSelectedColumnAndRelevantArea(charLetterOfColumnToGetUniqueValuesToFilter, newFilterStartCoordinateStr, newFilterEndCoordinateStr);
//    }
//
//    @Override
//    public void deleteRangeFromRangeFactory(String rangeName) {
//        try {
//            RangesManager.deleteRangeFromRangesFactory(rangeName);
//        } catch (Exception e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//    }
//
//    @Override
//    public List<String> getAllRangeNamesInTheSystem() {
//        return RangesManager.getAllRangesNames();
//    }
//
//    @Override
//    public Range getRangeByItsName(String rangeName) { return RangesManager.getRangeByItsName(rangeName); }
//
//    @Override
//    public boolean isThereAnyRangeInRangesFactory() {
//        return RangesManager.isThereAnyRangeInRangesFactory();
//    }


}

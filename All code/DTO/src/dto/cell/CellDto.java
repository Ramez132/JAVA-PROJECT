package dto.cell;

import dto.coordinate.CoordinateDto;

import java.util.Set;

public record CellDto(CoordinateDto coordinateDto,
                      String originalValueStr,
                      String effectiveValueStr,
                      int lastVersionInWhichCellHasChanged,
                      String userNameOfLastChange,
                      Set<CoordinateDto> dependsOnCoordinatesSet,
                      Set<CoordinateDto> influencesOnCoordinatesSet) {

//    public static CellDto createCellDto(Cell cell) {
//        CoordinateDto coordinateDto = new CoordinateDto(cell.getCoordinate().getRow(), cell.getCoordinate().getColumn());
//        String originalValueStr = cell.getOriginalValueStr();
//        String effectiveValueStr = getEffectiveValueAsString(cell);
//        int lastVersionInWhichCellHasChanged = cell.getLastVersionInWhichCellHasChanged();
//
//        Set<Coordinate> dependsOnCoordinatesSet = cell.getDependsOnMap().keySet();
//        Set<CoordinateDto> dependsOnCoordinatesDtoSet = CoordinateDto.createCoordinateDtoSet(dependsOnCoordinatesSet);
//        Set<Coordinate> influencesOnCoordinatesSet = cell.getInfluencingOnMap().keySet();
//        Set<CoordinateDto> influencesOnCoordinatesDtoSet = CoordinateDto.createCoordinateDtoSet(influencesOnCoordinatesSet);
//
//        return new CellDto(coordinateDto, originalValueStr, effectiveValueStr,
//                lastVersionInWhichCellHasChanged, dependsOnCoordinatesDtoSet, influencesOnCoordinatesDtoSet);
//    }
//
//    private static String getEffectiveValueAsString(Cell cell) {
//        String effectiveValueOfCellAsString;
//
//        EffectiveValue effectiveValueOfCell = cell.getCurrentEffectiveValue();
//        if (effectiveValueOfCell == null) {
//            effectiveValueOfCellAsString = addThousandsSeparator(" ");
//        } else {
//            effectiveValueOfCellAsString = addThousandsSeparator(effectiveValueOfCell.getValue().toString());
//        }
//
//        return effectiveValueOfCellAsString;
//    }
//
//    private static String addThousandsSeparator(String number) throws NumberFormatException {
//        try {
//            return NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(number));
//        }
//        catch (NumberFormatException e) {
//            return number;
//        }
//    }
}

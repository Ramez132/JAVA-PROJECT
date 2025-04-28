package dto.sheet;

import dto.cell.CellDto;
import dto.coordinate.CoordinateDto;

import java.util.Map;

public record SheetDto( String sheetName,
                        Map<CoordinateDto, CellDto> coordinateToCellDtoMap,
                        int numOfRows, int numOfColumns,
                        int rowHeight, int columnWidth,
                        int thisSheetVersion ) {

//    public static SheetDto createSheetDtoFromSheet(Sheet sheet) {
//        String sheetName = sheet.getNameOfSheet();
//        Map<CoordinateDto, CellDto> coordinateToCellDtoMap = sheet.getActiveCells().entrySet().stream()
//                .collect(
//                        HashMap::new,
//                        (map, entry) -> map.put(new CoordinateDto(entry.getKey().getRow(), entry.getKey().getColumn()),
//                                CellDto.createCellDto(entry.getValue())),
//                        Map::putAll
//                );
//        return new SheetDto(sheet.getNameOfSheet(),
//                coordinateToCellDtoMap,
//                sheet.getNumOfRows(), sheet.getNumOfColumns(),
//                sheet.getRowHeight(), sheet.getColumnWidth(),
//                sheet.getVersion());
//    }

//    public static SheetDto createSheetDto(Map<Coordinate, Cell> coordinateCellMap) {
//        return new SheetDto(coordinateCellMap.entrySet().stream()
//                .collect(
//                        Map::of,
//                        (map, entry) -> map.put(new CoordinateDto(entry.getKey().getRow(), entry.getKey().getColumn()),
//                                CellDto.createCellDto(entry.getValue())),
//                        Map::putAll
//                )
//        );
//    }
}

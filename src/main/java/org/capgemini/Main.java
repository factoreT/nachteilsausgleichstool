package org.capgemini;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.capgemini.data.OutputEntry;
import org.capgemini.data.Person;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RESET = "\u001B[0m";
    private final static String SINGLE_MAP_FILENAME = "filename";
    public static String ausgleichsDokumentOrdnerPath = "";
    public static String berechnungsWorkbookPath = "";
    public static String outputWorkbookPath = "";
    private static Map<String, String> sheetDataSingle = new HashMap<>();
    private static Map<String, Person> sheetDataBerechnung = new HashMap<>();
    private static List<OutputEntry> outputEntryList = new ArrayList<>();
    private static PrintWriter writer;

    public static void main(String[] args) throws IOException {

        TerminalCommunicator.initSession();

        Workbook berchnungsWorkbook;

        FileInputStream outputWorkbookInputStream = new FileInputStream(outputWorkbookPath);
        Workbook outputWorkbook = new XSSFWorkbook(outputWorkbookInputStream);
        berchnungsWorkbook = new XSSFWorkbook(new FileInputStream(berechnungsWorkbookPath));

        writer = new PrintWriter(ausgleichsDokumentOrdnerPath + File.separator + "output.txt", "UTF-8");
        writeLineToOutFile("Bearbeite Dateien:");

        Sheet berechnungSheet = berchnungsWorkbook.getSheetAt(0);
        readAdressenBerechnungssheet(berechnungSheet);
        File[] files = new File(ausgleichsDokumentOrdnerPath).listFiles();
        int index = 0;
        for (File ausgleichsDokument : Objects.requireNonNull(files)) {
            index++;

            if (ausgleichsDokument.getName().equalsIgnoreCase("output.txt")) {
                writeLineToConsole(
                        ANSI_GREEN + "(o) " + ANSI_RESET + index + " von " + files.length + " (" + ausgleichsDokument.getName() + ") ");
                continue;
            }


            sheetDataSingle = new HashMap<>();
            FileInputStream ausgleichsDokumentStream = new FileInputStream(ausgleichsDokument);
            Workbook workbook = new XSSFWorkbook(ausgleichsDokumentStream);
            sheetDataSingle.put(SINGLE_MAP_FILENAME, ausgleichsDokument.getName());
            readSingleSheet(workbook.getSheetAt(0));

            int returnCode = checkPermission();
            Person checkPerson = sheetDataBerechnung.get(sheetDataSingle.get("email"));

            if (returnCode == 0) {
                writeLineToConsole(
                        ANSI_GREEN + "(o) " + ANSI_RESET + index + " von " + files.length + " (" + ausgleichsDokument.getName() + ") ");
                outputEntryList.add(new OutputEntry(sheetDataSingle.get("ggid"), sheetDataSingle.get("name"),
                                                    sheetDataSingle.get("vorname"),
                                                    Double.parseDouble(sheetDataSingle.get("sum")),
                                                    sheetDataSingle.get("submitDate"),
                                                    sheetDataSingle.get(SINGLE_MAP_FILENAME)));
            } else if (returnCode == 1) {

                writeLineToConsole(
                        ANSI_YELLOW + "(?) " + ANSI_RESET + index + " von " + files.length + " (" + ausgleichsDokument.getName() + ") ");
                writeLineToOutFile(
                        "\"" + checkPerson.getVorname() + " " + checkPerson.getName() + "\" hat Wochenend-Tage eingetragen. Bitte manuell nachprüfen. Auszahlungseintrag wurde trotzdem erstellt! (Datei \"" + sheetDataSingle.get(
                                SINGLE_MAP_FILENAME) + "\")");
            } else {
                writeLineToConsole(
                        ANSI_RED + "(!) " + ANSI_RESET + index + " von " + files.length + " (" + ausgleichsDokument.getName() + ") ");
                switch (returnCode) {
                    case 2 -> writeLineToOutFile(
                            "\"" + checkPerson.getVorname() + " " + checkPerson.getName() + "\" stimmt nicht mit der Mail Adresse überein. Bitte manuell nachprüfen. (Datei \"" + sheetDataSingle.get(
                                    SINGLE_MAP_FILENAME) + "\")");
                    case 3 -> writeLineToOutFile(
                            "\"" + checkPerson.getVorname() + " " + checkPerson.getName() + "\" ist berechtigt, aber die angegebenen Flags stimmen nicht überein. Bitte manuell nachprüfen. (Datei \"" + sheetDataSingle.get(
                                    SINGLE_MAP_FILENAME) + "\")");
                    case 4 -> writeLineToOutFile(
                            "\"" + checkPerson.getVorname() + " " + checkPerson.getName() + "\" ist nicht für einen Ausgleich berechtigt. Ggf. manuell nachprüfen. (Datei \"" + sheetDataSingle.get(
                                    SINGLE_MAP_FILENAME) + "\")");
                    default -> throw new IllegalStateException("Unexpected value: " + returnCode);
                }
            }
        }
        writeLineToConsole("Alle Dateien bearbeitet.");

        writeLineToConsole("Beginne Eintragung in Output Excel...");
        writeLineToOutFile("\n\nErfolgreich in Auszahlungssheet eingetragen:");


        Sheet sheet;
        if (outputWorkbook.getSheetIndex(LocalDate.now().toString() + "_generated") < 0) {
            sheet = outputWorkbook.cloneSheet(outputWorkbook.getSheetIndex("Template"));
            outputWorkbook.setSheetName(outputWorkbook.getNumberOfSheets() - 1,
                                        LocalDate.now().toString() + "_generated");
        } else {
            sheet = outputWorkbook.getSheetAt(outputWorkbook.getSheetIndex(LocalDate.now().toString() + "_generated"));
        }

        for (int i = 0; i < outputEntryList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            OutputEntry entry = outputEntryList.get(i);

            Cell c = row.createCell(0);
            c.setCellValue(entry.getId());
            c = row.createCell(1);
            c.setCellValue(entry.getName());
            c = row.createCell(2);
            c.setCellValue(entry.getVorname());
            c = row.createCell(3);
            c.setCellValue(entry.getSum());
            c = row.createCell(4);
            c.setCellValue(entry.getSubmitDate());
            c = row.createCell(5);
            c.setCellValue(entry.getFilename());

            writeLineToOutFile("\"" + entry.getVorname() + " " + entry.getName() + "\" ist berechtigt.");
        }

        FileOutputStream outputStream = new FileOutputStream(outputWorkbookPath);
        outputWorkbook.write(outputStream);

        outputStream.close();
        outputWorkbook.close();
        writeLineToConsole("Output Excel erfolgreich geschrieben!");
        writer.close();
        System.out.println(
                "\nDas Log wurde in der Datei " + ausgleichsDokumentOrdnerPath + File.separator + "output.txt" + " gespeichert.\n");
        TerminalCommunicator.endLoop();

    }

    public static void readCellToDataMap(Sheet inputSheet, String key, int rowNumber, int colNumber) {
        Cell cell = inputSheet.getRow(rowNumber).getCell(colNumber);
        String value = "";
        if (cell.getCellType() == CellType.FORMULA) {
            value = switch (cell.getCachedFormulaResultType()) {
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                case STRING -> cell.getRichStringCellValue().toString();
                default -> value;
            };
        } else if (Objects.requireNonNull(cell.getCellType()) == CellType.NUMERIC) {
            if (!DateUtil.isCellDateFormatted(cell)) {
                value = String.valueOf((int) (cell.getNumericCellValue()));
            } else {
                value = parseDateToString(cell);
            }
        } else {
            value = cell.toString();
        }

        if (value.equalsIgnoreCase("ja")) {
            value = "true";
        }
        if (value.equalsIgnoreCase("nein")) {
            value = "false";
        }
        sheetDataSingle.put(key, value);
    }

    public static void readSingleSheet(Sheet sheet) {
        readCellToDataMap(sheet, "name", 3, 1);
        readCellToDataMap(sheet, "vorname", 4, 1);
        readCellToDataMap(sheet, "ggid", 5, 1);
        readCellToDataMap(sheet, "email", 6, 1);
        readCellToDataMap(sheet, "emailPdl", 7, 1);

        readCellToDataMap(sheet, "flagPkwWay", 11, 3);
        readCellToDataMap(sheet, "flagPkwTime", 11, 4);
        readCellToDataMap(sheet, "flagPTTime", 11, 5);

        readCellToDataMap(sheet, "submitDate", 14, 1);
        readCellToDataMap(sheet, "sum", sheet.getLastRowNum(), 2);

        sheetDataSingle.put("hasWeekends", String.valueOf(sheetIncludesWeekendDates(sheet)));
    }

    public static void readAdressenBerechnungssheet(Sheet inputSheet) {
        for (int i = 1; i <= inputSheet.getLastRowNum(); i++) {
            Row row = inputSheet.getRow(i);
            sheetDataBerechnung.put(row.getCell(5).toString().toLowerCase(), new Person(
                    row.getCell(4).toString(),
                    row.getCell(3).toString(),
                    row.getCell(5).toString(),
                    mapBoolean(row.getCell(64).getRichStringCellValue().toString()),
                    mapBoolean(row.getCell(65).getRichStringCellValue().toString()),
                    mapBoolean(row.getCell(66).getRichStringCellValue().toString())

            ));
        }
    }

    public static int checkPermission() {
        Person checkPerson = sheetDataBerechnung.get(sheetDataSingle.get("email"));

        if (!checkPerson.getName().equalsIgnoreCase(sheetDataSingle.get("name")) || !checkPerson.getVorname()
                                                                                                .equalsIgnoreCase(
                                                                                                        sheetDataSingle.get(
                                                                                                                "vorname"))) {
            return 2;
        }


        if (checkPerson.isFlagPkwTime() || checkPerson.isFlagPkwWay() || checkPerson.isFlagPTTime()) {
            if (checkPerson.isFlagPTTime() == Boolean.parseBoolean(sheetDataSingle.get("flagPTTime"))
                    && checkPerson.isFlagPkwTime() == Boolean.parseBoolean(sheetDataSingle.get("flagPkwTime"))
                    && checkPerson.isFlagPkwWay() == Boolean.parseBoolean(sheetDataSingle.get("flagPkwWay"))) {
                if (Boolean.parseBoolean(sheetDataSingle.get("hasWeekends"))) {
                    return 1;
                }
                return 0;
            } else {
                return 3;
            }
        } else {
            return 4;
        }
    }

    public static boolean sheetIncludesWeekendDates(Sheet sheet) {
        int i = 18;
        while (i < 1000) {
            Cell c = sheet.getRow(i).getCell(0);
            if (c.getCellType() == CellType.STRING && c.getStringCellValue().equalsIgnoreCase("summe")) {
                break;
            }
            if (c.getCellType() != CellType.BLANK) {
                String dayOfWeek = "";
                try {
                    dayOfWeek = Objects.requireNonNull(parseDate(c))
                                       .format(DateTimeFormatter.ofPattern("E", Locale.ENGLISH));
                } catch (NullPointerException ignored) {
                    writeLineToOutFile("Fehler beim parsen des Datums. Bitte manuell prüfen.");
                }
                if (dayOfWeek.equalsIgnoreCase("sat") || dayOfWeek.equalsIgnoreCase("sun")) {
                    return true;
                }
            }
            i++;
        }
        return false;

    }

    public static LocalDate parseDate(Cell cell) {
        try {
            return DateUtil.getJavaDate(cell.getNumericCellValue()).toInstant().atZone(ZoneId.systemDefault())
                           .toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    public static String parseDateToString(Cell cell) {
        LocalDate date = parseDate(cell);
        return date == null ? "???" : date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static void writeLineToOutFile(String message) {
        writer.println(message);
        writeLineToConsole(message);
    }

    public static void writeLineToConsole(String message) {
        System.out.println(message);
    }

    public static boolean mapBoolean(String input) {
        return switch (input) {
            case "nein" -> false;
            case "ja" -> true;
            default -> throw new RuntimeException("Falscher Wert für boolean");
        };
    }
}
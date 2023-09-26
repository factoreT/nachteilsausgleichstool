package org.capgemini;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TerminalCommunicator {

    private static LineReader reader;

    public static void initSession() throws IOException {
        Terminal terminal = TerminalBuilder.terminal();
        reader = LineReaderBuilder.builder()
                                  .terminal(terminal)
                                  .parser(new DefaultParser())
                                  .build();

        JFileChooser chooser = new JFileChooser();

        String configFilePath = System.getProperty("user.home") + File.separator + "ausgleichstool.cfg";
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            Scanner scanner = new Scanner(configFile, StandardCharsets.UTF_8);
            try {
                Main.berechnungsWorkbookPath = scanner.nextLine();
                Main.ausgleichsDokumentOrdnerPath = scanner.nextLine();
                Main.outputWorkbookPath = scanner.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("Config File vollständig. Neue Daten werden gespeichert.");
            }
        }

        String chosenOption =
                reader.readLine("Berechnungstabelle liegt hier: \"" + Main.berechnungsWorkbookPath + "\"? [y/n]");

        if (!(chosenOption.equalsIgnoreCase("y") || chosenOption.equalsIgnoreCase("n"))) {
            while (!(chosenOption.equalsIgnoreCase("y") || chosenOption.equalsIgnoreCase("n"))) {
                chosenOption = reader.readLine("Ungültige Option, [y/n]?");
            }
        }

        if (chosenOption.equalsIgnoreCase("n")) {
            if (chooser.showDialog(null, "Bitte Ausgleichsberechnungstabelle wählen.") != 0) {
                System.exit(0);
            }
            Main.berechnungsWorkbookPath = chooser.getSelectedFile().getAbsolutePath();
        }

        chosenOption = reader.readLine(
                "Ordner mit Einzeldokumenten liegt hier: \"" + Main.ausgleichsDokumentOrdnerPath + "\"? [y/n]");
        while (!(chosenOption.equalsIgnoreCase("y") || chosenOption.equalsIgnoreCase("n"))) {
            chosenOption = reader.readLine("Ungültige Option, [y/n]?");
        }

        if (chosenOption.equalsIgnoreCase("n")) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showDialog(null, "Bitte Einzel-Ausgleichsdokument Ordner wählen.") != 0) {
                System.exit(0);
            }

            Main.ausgleichsDokumentOrdnerPath = chooser.getSelectedFile().getAbsolutePath();
        }

        chosenOption = reader.readLine("Ausgabedatei liegt hier: \"" + Main.outputWorkbookPath + "\"? [y/n]");
        while (!(chosenOption.equalsIgnoreCase("y") || chosenOption.equalsIgnoreCase("n"))) {
            chosenOption = reader.readLine("Ungültige Option, [y/n]?");
        }

        if (chosenOption.equalsIgnoreCase("n")) {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showDialog(null, "Bitte Ausgabedokument (Template) wählen.") != 0) {
                System.exit(0);
            }

            Main.outputWorkbookPath = chooser.getSelectedFile().getAbsolutePath();
        }

        BufferedWriter printWriter =
                new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(configFilePath), StandardCharsets.UTF_8));
        printWriter.write(Main.berechnungsWorkbookPath);
        printWriter.newLine();
        printWriter.write(Main.ausgleichsDokumentOrdnerPath);
        printWriter.newLine();
        printWriter.write(Main.outputWorkbookPath);
        printWriter.newLine();
        printWriter.close();
    }

    public static void endLoop() {
        reader.readLine("Zum Beenden beliebige Taste drücken...");
    }
}

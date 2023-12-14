package de.xmaptool;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Main {

    private final static String X_MAP_RESOURCES_PATH = "de/xmaptool/resources/";
    
    public static void main(String[] args) {

        // read X Universe files
        ArrayList<XuniverseMap> xmaps = new ArrayList<XuniverseMap>();
        File resources = new File(X_MAP_RESOURCES_PATH);
        FilenameFilter xmlFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return (lowercaseName.endsWith(".xml"));
            }
        };
        for (File file : resources.listFiles(xmlFilter)) {
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                xmaps.add(XMapReader.readInputXml(new String(encoded, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                // Input nicht lesbar - Abbruch
                System.out.println("Error occured while deserializing " + file.getName() + ": " + e);
            }
        }
        if (xmaps.isEmpty()) {
            System.out.println("No valid universe map found");
            System.exit(-1);
        }
        
        // create view for all read universes
        XuniverseFrame frame = new XuniverseFrame(xmaps);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}

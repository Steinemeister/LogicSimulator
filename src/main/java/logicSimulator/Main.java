package logicSimulator;

import logicSimulator.nodes.*;

public class Main {
    public static void main(String[] args) {
        testJunction();
    }

    public static void testJunction() {
        Graph workspace = new Graph();

        // Komponenten erstellen
        ButtonNode button = new ButtonNode("HauptSchalter");
        JunctionNode junction = new JunctionNode("Knotenpunkt");
        LedNode led1 = new LedNode("LED_Links");
        LedNode led2 = new LedNode("LED_Rechts");

        workspace.addNode(button);
        workspace.addNode(junction);
        workspace.addNode(led1);
        workspace.addNode(led2);

        // --- Verdrahtung ---
        // 1. Vom Button ZUM Knotenpunkt
        workspace.addEdge(new Edge(button, "Out", junction, "Point"));

        // 2. VOM Knotenpunkt zu den beiden LEDs (Signal-Verteilung)
        workspace.addEdge(new Edge(junction, "Point", led1, "In"));
        workspace.addEdge(new Edge(junction, "Point", led2, "In"));

        // Simulation aufwecken
        workspace.initializeSimulation();

        System.out.println("--- Test des neuen Knotenpunkt-Systems ---");
        System.out.println("Ausgangslage (Ausschalter):");
        System.out.println("  Button an? " + button.isPressed());
        System.out.println("  LED 1 an?  " + led1.isOn());
        System.out.println("  LED 2 an?  " + led2.isOn());

        System.out.println("\n--- Schalter wird umgelegt! ---");
        button.toggle(workspace);

        System.out.println("Ergebnis nach Signal-Verteilung durch die Junction:");
        System.out.println("  Button an? " + button.isPressed());
        System.out.println("  LED 1 an?  " + led1.isOn()); // MUSS TRUE SEIN
        System.out.println("  LED 2 an?  " + led2.isOn()); // MUSS TRUE SEIN

        System.out.println("\n--- Schalter wird wieder ausgeschaltet! ---");
        button.toggle(workspace);

        System.out.println("Ergebnis nach dem Ausschalten:");
        System.out.println("  Button an? " + button.isPressed());
        System.out.println("  LED 1 an?  " + led1.isOn()); // MUSS FALSE SEIN
        System.out.println("  LED 2 an?  " + led2.isOn()); // MUSS FALSE SEIN
    }

    public static void testNot() {
        Graph workspace = new Graph();

        ButtonNode button = new ButtonNode("MeinButton");
        NotNode inverter = new NotNode("Inverter");
        LedNode led = new LedNode("StatusLED");

        workspace.addNode(button);
        workspace.addNode(inverter);
        workspace.addNode(led);

        workspace.addEdge(new Edge(button, "Out", inverter, "In"));
        workspace.addEdge(new Edge(inverter, "Out", led, "In"));

        // JETZT WICHTIG: Das System einmalig aufwecken
        workspace.initializeSimulation();

        System.out.println("Startzustand (Button ungedrückt):");
        System.out.println("  Button gedrückt? " + button.isPressed());
        System.out.println("  LED leuchtet?    " + led.isOn()); // MUSS JETZT TRUE SEIN!

        System.out.println("\n--- Button wird gedrückt! ---");
        button.toggle(workspace);

        System.out.println("Zustand nach Klick:");
        System.out.println("  Button gedrückt? " + button.isPressed());
        System.out.println("  LED leuchtet?    " + led.isOn()); // MUSS JETZT FALSE SEIN!
    }
}

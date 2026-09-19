package logicSimulator;

import logicSimulator.nodes.*;

public class Main {
    public static void main(String[] args) {
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

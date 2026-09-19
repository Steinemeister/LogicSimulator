package logicSimulator;

import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.*;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.*;

public class Main {
    public static void main(String[] args) {
        testNativeGates();
    }

    public static void testNativeGates() {
        Graph workspace = new Graph();

        // Zwei Buttons als globale Eingänge
        ButtonNode btnA = new ButtonNode("BtnA");
        ButtonNode btnB = new ButtonNode("BtnB");

        // Unsere neuen nativen Hochleistungskomponenten
        AndNode andNode = new AndNode("AND");
        NandNode nandNode = new NandNode("NAND");
        XorNode xorNode = new XorNode("XOR");
        XnorNode xnorNode = new XnorNode("XNOR");

        workspace.addNode(btnA);
        workspace.addNode(btnB);
        workspace.addNode(andNode);
        workspace.addNode(nandNode);
        workspace.addNode(xorNode);
        workspace.addNode(xnorNode);

        // Alle Gatter mit den zwei Buttons verbinden
        workspace.addEdge(new Edge(btnA, "Out", andNode, "A"));
        workspace.addEdge(new Edge(btnB, "Out", andNode, "B"));

        workspace.addEdge(new Edge(btnA, "Out", nandNode, "A"));
        workspace.addEdge(new Edge(btnB, "Out", nandNode, "B"));

        workspace.addEdge(new Edge(btnA, "Out", xorNode, "A"));
        workspace.addEdge(new Edge(btnB, "Out", xorNode, "B"));

        workspace.addEdge(new Edge(btnA, "Out", xnorNode, "A"));
        workspace.addEdge(new Edge(btnB, "Out", xnorNode, "B"));

        // Kombinationen durchtesten
        boolean[] inputStates = {false, true};

        workspace.initializeSimulation();

        System.out.println("--- Teste native Logik-Knoten ---");
        System.out.println("A \t B \t| AND \t NAND \t XOR \t XNOR");
        System.out.println("----------------------------------------------");

        for (boolean a : inputStates) {
            for (boolean b : inputStates) {
                // Knöpfe setzen (Simuliert das Klicken)
                if (btnA.isPressed() != a) btnA.toggle(workspace);
                if (btnB.isPressed() != b) btnB.toggle(workspace);

                // Simulation einmal komplett durchlaufen lassen
                workspace.propagateSignals();

                // Zustände auslesen
                String andRes  = andNode.getOutputs().get("Out").getState().toString();
                String nandRes = nandNode.getOutputs().get("Out").getState().toString();
                String xorRes  = xorNode.getOutputs().get("Out").getState().toString();
                String xnorRes = xnorNode.getOutputs().get("Out").getState().toString();

                System.out.printf("%s \t %s \t| %s \t %s \t %s \t %s\n",
                        a ? "1" : "0", b ? "1" : "0", andRes, nandRes, xorRes, xnorRes);
            }
        }
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

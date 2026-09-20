package logicSimulator.graph.nodes.module;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

import java.util.ArrayList;
import java.util.List;

public class CustomModuleNode extends Node {
    // Das gekapselte innere Universum dieses Moduls
    private final Graph internalGraph = new Graph();

    // Interne Listen, um die Schnittstellen-Knoten blitzschnell im Zugriff zu haben
    private final List<ModuleInputNode> internalInputNodes = new ArrayList<>();
    private final List<ModuleOutputNode> internalOutputNodes = new ArrayList<>();

    public CustomModuleNode(String name) {
        super(name);
        // Das äußere Gehäuse wird erst berechnet, wenn Pins hinzugefügt werden
    }

    /**
     * Fügt dem Modul einen neuen Eingang hinzu.
     */
    public void addExternalInput(String pinName) {
        // 1. Äußeren Pin am Gehäuse anlegen
        inputs.add(new Pin(pinName, this));

        // 2. Passenden Schnittstellen-Knoten für das Innenleben erstellen
        ModuleInputNode internalInNode = new ModuleInputNode(pinName);
        internalGraph.addNode(internalInNode);
        internalInputNodes.add(internalInNode);

        // Layout der äußeren Pins aktualisieren
        calculateLayout(20f);
    }

    /**
     * Fügt dem Modul einen neuen Ausgang hinzu.
     */
    public void addExternalOutput(String pinName) {
        // 1. Äußeren Pin am Gehäuse anlegen
        outputs.add(new Pin(pinName, this));

        // 2. Passenden Schnittstellen-Knoten für das Innenleben erstellen
        ModuleOutputNode internalOutNode = new ModuleOutputNode(pinName);
        internalGraph.addNode(internalOutNode);
        internalOutputNodes.add(internalOutNode);

        // Layout der äußeren Pins aktualisieren
        calculateLayout(20f);
    }

    /**
     * Gibt Zugriff auf den internen Graphen, um das Innenleben zu verdrahten.
     */
    public Graph getInternalGraph() {
        return internalGraph;
    }

    public List<ModuleInputNode> getInternalInputNodes() { return internalInputNodes; }
    public List<ModuleOutputNode> getInternalOutputNodes() { return internalOutputNodes; }

    @Override
    public void update(Graph graph) {
        // 1. SCHRITT: Äußere Signale in das Innenleben drücken
        for (int i = 0; i < inputs.size(); i++) {
            Pin externalInput = inputs.get(i);
            ModuleInputNode internalInNode = internalInputNodes.get(i);

            // Der einzige Pin (Index 0) des inneren Input-Knotens erhält den Zustand von außen
            Pin internalPin = internalInNode.getOutputs().get(0);

            // Per Event ohne Verzögerung in den inneren Graphen einspeisen
            internalGraph.queueEvent(internalPin, externalInput.getState(), 0);
        }

        // 2. SCHRITT: Das Innenleben komplett durchsimulieren, bis alle Signale stabil sind
        internalGraph.propagateSignals();

        // 3. SCHRITT: Ergebnisse aus dem Innenleben an die äußeren Ausgänge übergeben
        for (int i = 0; i < outputs.size(); i++) {
            Pin externalOutput = outputs.get(i);
            ModuleOutputNode internalOutNode = internalOutputNodes.get(i);

            // Der einzige Pin (Index 0) des inneren Output-Knotens hält das berechnete Ergebnis
            Pin internalPin = internalOutNode.getInputs().get(0);

            // Zustand an das äußere Event-System übergeben (Verzögerung 1 für Gatter-Laufzeit nach außen)
            graph.queueEvent(externalOutput, internalPin.getState(), 1);
        }
    }
}
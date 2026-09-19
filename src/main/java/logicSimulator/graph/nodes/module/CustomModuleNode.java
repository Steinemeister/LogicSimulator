package logicSimulator.graph.nodes.module;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class CustomModuleNode extends Node {
    // Jedes Modul kapselt nun sauber ein eigenes Graph-Objekt
    private final Graph internalGraph = new Graph();

    public CustomModuleNode(String name) {
        super(name);
    }

    public void addExternalInput(String pinName) {
        inputs.put(pinName, new Pin(pinName, this));
        // Registriere den passenden Schnittstellen-Knoten im internen Graphen
        internalGraph.addNode(new ModuleInputNode(pinName));

        calculateLayout(24f);
    }

    public void addExternalOutput(String pinName) {
        outputs.put(pinName, new Pin(pinName, this));
        // Registriere den passenden Schnittstellen-Knoten im internen Graphen
        internalGraph.addNode(new ModuleOutputNode(pinName));

        calculateLayout(24f);
    }

    // Zugriff auf den internen Graphen, um von außen Gatter/Kabel hinzuzufügen
    public Graph getInternalGraph() {
        return internalGraph;
    }

    @Override
    public void update(Graph graph) {
        // 1. Äußere Inputs in das Innenleben einspeisen
        for (String pinName : inputs.keySet()) {
            Pin externalInput = inputs.get(pinName);
            ModuleInputNode internalInNode = findInternalInputNode(pinName);
            if (internalInNode != null) {
                internalInNode.getOutputs().get("Out").setState(externalInput.getState());
            }
        }

        // 2. Den internen Graphen einen Schritt simulieren lassen
        internalGraph.propagateSignals();

        // 3. Ergebnisse aus dem Innenleben an die äußeren Outputs übergeben
        for (String pinName : outputs.keySet()) {
            Pin externalOutput = outputs.get(pinName);
            ModuleOutputNode internalOutNode = findInternalOutputNode(pinName);
            if (internalOutNode != null) {
                externalOutput.setState(internalOutNode.getInputs().get("In").getState());
            }
        }
    }

    private ModuleInputNode findInternalInputNode(String name) {
        return internalGraph.getNodes().stream()
                .filter(n -> n instanceof ModuleInputNode && n.getName().equals(name))
                .map(n -> (ModuleInputNode) n)
                .findFirst().orElse(null);
    }

    private ModuleOutputNode findInternalOutputNode(String name) {
        return internalGraph.getNodes().stream()
                .filter(n -> n instanceof ModuleOutputNode && n.getName().equals(name))
                .map(n -> (ModuleOutputNode) n)
                .findFirst().orElse(null);
    }
}
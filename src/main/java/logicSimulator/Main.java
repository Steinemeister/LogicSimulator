package logicSimulator;

import logicSimulator.nodes.*;

public class Main {
    public static void main(String[] args) {
        // 1. Modul erstellen
        CustomModuleNode andModule = new CustomModuleNode("AND_Gate");
        andModule.addExternalInput("A");
        andModule.addExternalInput("B");
        andModule.addExternalOutput("Y");

        Graph innerGraph = andModule.getInternalGraph();

        // Gatter definieren
        NotNode notA = new NotNode("Not_A");
        NotNode notB = new NotNode("Not_B");
        NotNode notY = new NotNode("Not_Y");

        // JETZT NEU: Ein Zusammenführungs-Knoten mit 2 Eingängen
        JunctionNode orJunction = new JunctionNode("OR_Junction", 2);

        innerGraph.addNode(notA);
        innerGraph.addNode(notB);
        innerGraph.addNode(notY);
        innerGraph.addNode(orJunction);

        // Interne Schnittstellen holen
        ModuleInputNode internalInA = (ModuleInputNode) innerGraph.getNodes().get(0);
        ModuleInputNode internalInB = (ModuleInputNode) innerGraph.getNodes().get(1);
        ModuleOutputNode internalOutY = (ModuleOutputNode) innerGraph.getNodes().get(2);

        // --- Korrekte Verdrahtung ---
        // 1. Eingänge zu den ersten NOT-Gattern
        innerGraph.addEdge(new Edge(internalInA, "Out", notA, "In"));
        innerGraph.addEdge(new Edge(internalInB, "Out", notB, "In"));

        // 2. Ausgänge der NOT-Gatter in die Eingänge der Junction leiten
        innerGraph.addEdge(new Edge(notA, "Out", orJunction, "In_0"));
        innerGraph.addEdge(new Edge(notB, "Out", orJunction, "In_1"));

        // 3. Vom Ausgang der Junction in das finale NOT-Gatter
        innerGraph.addEdge(new Edge(orJunction, "Out", notY, "In"));

        // 4. Zum Modul-Ausgang
        innerGraph.addEdge(new Edge(notY, "Out", internalOutY, "In"));

        // --- TESTSCHLEIFE ---
        Pin.State[] states = {Pin.State.LOW, Pin.State.HIGH};

        System.out.println("--- Teste selbstgebautes AND-Modul ---");
        System.out.println("Input A | Input B | Output Y");
        System.out.println("----------------------------");

        for (Pin.State stateA : states) {
            for (Pin.State stateB : states) {

                andModule.getInputs().get("A").setState(stateA);
                andModule.getInputs().get("B").setState(stateB);

                // Graphen pulsieren lassen, bis Signale durchgelaufen sind
                for (int i = 0; i < 5; i++) {
                    andModule.update();
                }

                Pin.State resultY = andModule.getOutputs().get("Y").getState();
                System.out.printf("  %-5s |   %-5s |   %s\n", stateA, stateB, resultY);
            }
        }
    }
}

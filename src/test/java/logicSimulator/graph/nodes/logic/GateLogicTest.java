package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Pin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GateLogicTest {
    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @ParameterizedTest(name = "AND: {0} und {1} sollte {2} sein")
    @CsvSource({
            "LOW,  LOW,  LOW",
            "LOW,  HIGH, LOW",
            "HIGH, LOW,  LOW",
            "HIGH, HIGH, HIGH"
    })
    void testAndGate(Pin.State inputA, Pin.State inputB, Pin.State expected) {
        AndNode andNode = new AndNode("AND");
        graph.addNode(andNode);

        andNode.getInputs().get("A").setState(inputA);
        andNode.getInputs().get("B").setState(inputB);

        // Zwinge das Gatter zur Berechnung
        andNode.update(graph);
        graph.propagateSignals();

        assertEquals(expected, andNode.getOutputs().get("Out").getState());
    }

    @ParameterizedTest(name = "NAND: {0} und {1} sollte {2} sein")
    @CsvSource({
            "LOW,  LOW,  HIGH",
            "LOW,  HIGH, HIGH",
            "HIGH, LOW,  HIGH",
            "HIGH, HIGH, LOW"
    })
    void testNandGate(Pin.State inputA, Pin.State inputB, Pin.State expected) {
        NandNode nandNode = new NandNode("NAND");
        graph.addNode(nandNode);

        nandNode.getInputs().get("A").setState(inputA);
        nandNode.getInputs().get("B").setState(inputB);

        nandNode.update(graph);
        graph.propagateSignals();

        assertEquals(expected, nandNode.getOutputs().get("Out").getState());
    }

    @ParameterizedTest(name = "XOR: {0} und {1} sollte {2} sein")
    @CsvSource({
            "LOW,  LOW,  LOW",
            "LOW,  HIGH, HIGH",
            "HIGH, LOW,  HIGH",
            "HIGH, HIGH, LOW"
    })
    void testXorGate(Pin.State inputA, Pin.State inputB, Pin.State expected) {
        XorNode xorNode = new XorNode("XOR");
        graph.addNode(xorNode);

        xorNode.getInputs().get("A").setState(inputA);
        xorNode.getInputs().get("B").setState(inputB);

        xorNode.update(graph);
        graph.propagateSignals();

        assertEquals(expected, xorNode.getOutputs().get("Out").getState());
    }
}

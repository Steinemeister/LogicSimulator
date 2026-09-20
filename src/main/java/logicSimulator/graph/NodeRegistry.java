package logicSimulator.graph;

import logicSimulator.graph.nodes.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class NodeRegistry {
    // Speichert den Typnamen und eine Lambda-Funktion (Factory), die eine neue Instanz erzeugt
    private final Map<String, Supplier<Node>> registry = new LinkedHashMap<>();

    /**
     * Registriert einen neuen Gatter- oder Modultyp im System.
     */
    public void registerType(String typeName, Supplier<Node> factory) {
        registry.put(factory.get().getName(), factory);
    }

    /**
     * Erstellt eine frische Instanz des gewünschten Typs.
     */
    public Node createInstance(String typeName) {
        Supplier<Node> factory = registry.get(typeName);
        return factory != null ? factory.get() : null;
    }

    /**
     * Gibt alle aktuell registrierten Typnamen zurück (perfekt für die Sidebar).
     */
    public Set<String> getAvailableTypes() {
        return registry.keySet();
    }
}

package logicSimulator.graph;

import logicSimulator.graph.nodes.CornerNode;
import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.SimulationEvent;
import logicSimulator.graph.nodes.Node;

import java.util.*;

public class Graph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final PriorityQueue<SimulationEvent> eventQueue = new PriorityQueue<>();
    private long currentTick = 0;

    public void addNode(Node node) { nodes.add(node); }
    public void addEdge(Edge edge) { edges.add(edge); }

    public void removeEdge(Edge edge) {
        if (edge == null) return;

        // Merke dir die betroffenen Knoten, bevor das Kabel gelöscht wird
        Pin srcPin = findPinGlobally(edge.getSourcePinId());
        Pin destPin = findPinGlobally(edge.getDestPinId());

        Node srcNode = srcPin != null ? srcPin.getOwner() : null;
        Node destNode = destPin != null ? destPin.getOwner() : null;

        // 1. Das eigentliche Kabel löschen
        edges.remove(edge);

        // 2. Automatische Bereinigungskette starten
        if (srcNode != null) cleanupUselessConnectedNodes(srcNode);
        if (destNode != null) cleanupUselessConnectedNodes(destNode);
    }

    private void cleanupUselessConnectedNodes(Node node) {
        if (node == null) return;

        // Zähle, wie viele Kabel aktuell noch an diesem Knoten hängen
        List<Edge> connectedEdges = new ArrayList<>();
        for (Edge e : edges) {
            Pin src = findPinGlobally(e.getSourcePinId());
            Pin dest = findPinGlobally(e.getDestPinId());
            if ((src != null && src.getOwner() == node) || (dest != null && dest.getOwner() == node)) {
                connectedEdges.add(e);
            }
        }
        int connectedEdgesCount = connectedEdges.size();

        // --- FALL 1: CORNER-NODE IST REINE SPEICHERLEICHE GEWORDEN ---
        if (node instanceof CornerNode && connectedEdgesCount < 2) {
            nodes.remove(node);

            // Lösche die verbliebenen Reste dieser Corner und jage deren Nachbarn in die Kaskade
            for (Edge e : connectedEdges) {
                edges.remove(e);
                Pin src = findPinGlobally(e.getSourcePinId());
                Pin dest = findPinGlobally(e.getDestPinId());

                if (src != null && src.getOwner() != node) cleanupUselessConnectedNodes(src.getOwner());
                if (dest != null && dest.getOwner() != node) cleanupUselessConnectedNodes(dest.getOwner());
            }
            return;
        }

        // --- FALL 2: JUNCTION-NODE REINIGEN / VEREINFACHEN ---
        if (node instanceof JunctionNode && connectedEdgesCount <= 2) {
            // Unterfall 2A: Die Junction hat weniger als 2 Kabel -> Komplett nutzlos, weglöschen!
            if (connectedEdgesCount < 2) {
                nodes.remove(node);
                for (Edge e : connectedEdges) {
                    edges.remove(e);
                    Pin src = findPinGlobally(e.getSourcePinId());
                    Pin dest = findPinGlobally(e.getDestPinId());
                    if (src != null && src.getOwner() != node) cleanupUselessConnectedNodes(src.getOwner());
                    if (dest != null && dest.getOwner() != node) cleanupUselessConnectedNodes(dest.getOwner());
                }
            }
            // Unterfall 2B: Exakt 2 Kabel -> Zu einer Corner vereinfachen!
            else {
                float currentX = node.getX();
                float currentY = node.getY();

                Edge incomingEdge = null;
                Edge outgoingEdge = null;
                Pin junctionUniversalPin = node.getInputs().get(0);

                for (Edge e : connectedEdges) {
                    if (e.getDestPinId().equals(junctionUniversalPin.getId())) incomingEdge = e;
                    if (e.getSourcePinId().equals(junctionUniversalPin.getId())) outgoingEdge = e;
                }

                if (incomingEdge != null && outgoingEdge != null) {
                    Pin realSrc = findPinGlobally(incomingEdge.getSourcePinId());
                    Pin realDest = findPinGlobally(incomingEdge.getDestPinId()); // (Das ist der Pin der Junction selbst)
                    Pin realNextDest = findPinGlobally(outgoingEdge.getDestPinId());

                    if (realSrc != null && realNextDest != null) {
                        float srcX = realSrc.getAbsoluteX();
                        float srcY = realSrc.getAbsoluteY();
                        float destX = realNextDest.getAbsoluteX();
                        float destY = realNextDest.getAbsoluteY();

                        // =========================================================
                        // NEU: BEGRADIGUNGS-CHECK (Verschmelzen zu einer langen Edge)
                        // =========================================================
                        // Fall A: Beide Segmente verlaufen schnurgerade horizontal auf derselben Höhe
                        // Fall B: Beide Segmente verlaufen schnurgerade vertikal auf derselben Breite
                        boolean isStraightHorizontal = (srcY == currentY && currentY == destY);
                        boolean isStraightVertical   = (srcX == currentX && currentX == destX);

                        if (isStraightHorizontal || isStraightVertical) {
                            // Wir löschen die Junction und die beiden Teilstücke komplett
                            nodes.remove(node);
                            edges.remove(incomingEdge);
                            edges.remove(outgoingEdge);

                            // Wir ziehen EIN EINZIGES, langes, ununterbrochenes Kabel!
                            edges.add(new Edge(realSrc, realNextDest));

                            System.out.println("[Engine] Leitung erfolgreich begradigt. Knickpunkt entfernt.");

                            // Kaskade bei den neuen Endpartnern fortsetzen
                            cleanupUselessConnectedNodes(realSrc.getOwner());
                            cleanupUselessConnectedNodes(realNextDest.getOwner());
                            return; // Vorgang abgeschlossen!
                        }
                    }
                }

                // --- Standard-Verhalten (Wenn es ein echter 90-Grad-Knick ist): ---
                // Lösche die alte Junction lautlos
                nodes.remove(node);
                if (incomingEdge != null) edges.remove(incomingEdge);
                if (outgoingEdge != null) edges.remove(outgoingEdge);

                // Erstelle die normale CornerNode für den echten Knick
                CornerNode newCorner = new CornerNode("CornerGen_" + System.currentTimeMillis());
                newCorner.setPosition(currentX, currentY);
                nodes.add(newCorner);

                if (incomingEdge != null) {
                    Pin realSrc = findPinGlobally(incomingEdge.getSourcePinId());
                    if (realSrc != null) edges.add(new Edge(realSrc, newCorner.getInputs().get(0)));
                }
                if (outgoingEdge != null) {
                    Pin realDest = findPinGlobally(outgoingEdge.getDestPinId());
                    if (realDest != null) edges.add(new Edge(newCorner.getOutputs().get(0), realDest));
                }

                System.out.println("[Engine] Junction wurde automatisch zu einer Corner vereinfacht.");
                cleanupUselessConnectedNodes(newCorner);
            }
        }
    }

    public Pin findPinGlobally(UUID pinId) {
        for (Node node : nodes) {
            Pin p = node.findPinById(pinId);
            if (p != null) return p;
        }
        return null;
    }

    public void queueEvent(Pin pin, Pin.State newState, long delay) {
        eventQueue.add(new SimulationEvent(currentTick + delay, pin, newState));
    }

    public boolean step() {
        if (eventQueue.isEmpty()) return false;

        // Wenn das nächste Event in der Zukunft liegt, spulen wir die Zeit vor
        if (eventQueue.peek().getTick() > currentTick) {
            currentTick = eventQueue.peek().getTick();
        }

        // Sammle ALLE Events für den aktuellen Tick
        List<SimulationEvent> currentEvents = new ArrayList<>();
        while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= currentTick) {
            currentEvents.add(eventQueue.poll());
        }

        List<Node> nodesToUpdate = new ArrayList<>();

        // Verarbeite die Events
        for (SimulationEvent event : currentEvents) {
            Pin pin = event.getPin();
            if (pin.getState() != event.getNewState()) {
                triggerPinChange(pin, event.getNewState(), nodesToUpdate);
            }
        }

        // Berechne die Logik-Knoten (AND, NOT, CustomModule)
        for (Node node : nodesToUpdate) {
            node.update(this);
        }

        return !eventQueue.isEmpty();
    }

    private void triggerPinChange(Pin pin, Pin.State newState, List<Node> nodesToUpdate) {
        pin.setState(newState);

        // Wenn es ein Gatter-Eingang ist, muss das Gatter rechnen
        if (pin.getOwner() != null && !(pin.getOwner() instanceof JunctionNode)) {
            if (!nodesToUpdate.contains(pin.getOwner())) {
                nodesToUpdate.add(pin.getOwner());
            }
        }

        // Signal über Kabel weiterleiten
        for (Edge edge : edges) {
            if (edge.getSourcePinId().equals(pin.getId())) {
                Pin destPin = findPinGlobally(edge.getDestPinId());
                if (destPin != null) {
                    if (destPin.getOwner() instanceof JunctionNode || destPin.getOwner() instanceof CornerNode) {
                        // REKURSION: Sowohl Junctions als auch Corners schalten das Signal verzögerungsfrei durch!
                        if (destPin.getState() != newState) {
                            // Wenn es eine CornerNode ist, müssen wir das Signal intern vom Input-Pin auf den Output-Pin spiegeln
                            if (destPin.getOwner() instanceof CornerNode) {
                                destPin.setState(newState);
                                Pin cornerOutPin = destPin.getOwner().getOutputs().get(0);
                                triggerPinChange(cornerOutPin, newState, nodesToUpdate);
                            } else {
                                // Normales Junction-Verhalten
                                triggerPinChange(destPin, newState, nodesToUpdate);
                            }
                        }
                    } else {
                        // Normale Gatter erhalten ein Event für JETZT (delay = 0)
                        queueEvent(destPin, newState, 0);
                    }
                }
            }
        }
    }

    public void propagateSignals() {
        int maxSafetyLoops = 5000;
        // WICHTIG: Wir simulieren so lange, wie Events in der Queue existieren.
        // Erst wenn step() false zurückgibt UND die Queue wirklich leer ist, stoppen wir.
        while (!eventQueue.isEmpty() && maxSafetyLoops > 0) {
            step();
            // Erhöhe den Tick erst, wenn für den aktuellen Tick wirklich alle
            // Kettenreaktionen (0-Tick-Events) abgearbeitet wurden!
            if (!eventQueue.isEmpty() && eventQueue.peek().getTick() > currentTick) {
                currentTick++;
            }
            maxSafetyLoops--;
        }
    }

    public void initializeSimulation() {
        currentTick = 0;
        eventQueue.clear();
        for (Node node : nodes) {
            node.update(this);
        }
        propagateSignals();
    }

    public Node getNodeAt(float mx, float my) {
        // Rückwärts durchlaufen für die korrekte Tiefenreihenfolge (Vordergrund zuerst)
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);

            if (node instanceof JunctionNode) {
                // Junctions haben eine Größe von 0x0. Wir prüfen den Abstand zum Pin.
                Pin p = node.getInputs().get(0);
                float dx = p.getAbsoluteX() - mx;
                float dy = p.getAbsoluteY() - my;
                float clickRadius = 10f; // 10 Pixel Toleranz zum Greifen der Junction
                if ((dx * dx + dy * dy) <= (clickRadius * clickRadius)) {
                    return node;
                }
            } else {
                // Normale Gatter über die Box prüfen
                if (node.contains(mx, my)) {
                    return node;
                }
            }
        }
        return null;
    }

    public Edge getEdgeAt(float mx, float my, float tolerance) {
        for (Edge edge : edges) {
            if (edge.isPointNearLine(mx, my, tolerance, this)) {
                return edge;
            }
        }
        return null;
    }

    public Pin getAnyPinAt(float mx, float my, float radius) {
        for (Node node : nodes) {
            Pin pin = node.getPinAt(mx, my, radius);
            if (pin != null) {
                return pin;
            }
        }
        return null;
    }

    public void silentRemoveEdge(Edge edge) {
        if (edge != null) {
            edges.remove(edge);
        }
    }

    public JunctionNode splitEdgeWithJunction(Edge edgeToSplit, String junctionName, float mx, float my, float gridSize) {
        Pin originalSrc = findPinGlobally(edgeToSplit.getSourcePinId());
        Pin originalDest = findPinGlobally(edgeToSplit.getDestPinId());

        // Linien-Endpunkte holen
        float x1 = originalSrc.getAbsoluteX();
        float y1 = originalSrc.getAbsoluteY();
        float x2 = originalDest.getAbsoluteX();
        float y2 = originalDest.getAbsoluteY();

        // Mathematische Projektion der Maus (mx, my) auf die Strecke (x1,y1) -> (x2,y2)
        float l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        float t = 0f;
        if (l2 != 0) {
            t = ((mx - x1) * (x2 - x1) + (my - y1) * (y2 - y1)) / l2;
            t = Math.max(0f, Math.min(1f, t));
        }

        // Das ist der exakte Punkt AUF dem Kabel
        float lineX = x1 + t * (x2 - x1);
        float lineY = y1 + t * (y2 - y1);

        // Raster-Einrastung für diesen Linienpunkt berechnen
        float snappedX = Math.round(lineX / gridSize) * gridSize;
        float snappedY = Math.round(lineY / gridSize) * gridSize;

        // Altes Kabel entfernen
        silentRemoveEdge(edgeToSplit);

        // Neue Junction erstellen und exakt auf der Linie platzieren
        JunctionNode newJunction = new JunctionNode(junctionName);
        newJunction.setPosition(snappedX, snappedY);
        addNode(newJunction);

        Pin junctionPin = newJunction.getInputs().get(0);

        // Neue Kabelsegmente legen
        addEdge(new Edge(originalSrc, junctionPin));
        addEdge(new Edge(junctionPin, originalDest));

        // Live-Zustand injizieren
        List<Node> nodesToUpdate = new ArrayList<>();
        triggerPinChange(originalSrc, originalSrc.getState(), nodesToUpdate);
        for (Node n : nodesToUpdate) {
            n.update(this);
        }
        propagateSignals();

        return newJunction;
    }

    public void removeNode(Node node) {
        if (node == null) return;
        nodes.remove(node);

        // Sicherheits-Feature: Lösche alle Kabel, die an Pins dieses Knotens hingen
        edges.removeIf(edge -> {
            Pin src = findPinGlobally(edge.getSourcePinId());
            Pin dest = findPinGlobally(edge.getDestPinId());
            return (src != null && src.getOwner() == node) || (dest != null && dest.getOwner() == node);
        });
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
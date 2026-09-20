package logicSimulator.rendering;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.NodeRegistry;
import logicSimulator.graph.nodes.CornerNode;
import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;

public class EditorWindow extends Application {
    private final Graph logicGraph;

    private final NodeRegistry nodeRegistry;

    // Zustandsspeicher für das Verschieben und Verkabeln
    private Node draggingNode = null;
    private float mouseOffsetX = 0f;
    private float mouseOffsetY = 0f;
    private Pin sourcePinForNewEdge = null;

    private String selectedTypeName = null;

    // Globale Rastergröße für das Grid-Snapping
    private final float gridSize = 20f;
    private final float sidebarWidth = 200f;

    public EditorWindow(Graph logicGraph, NodeRegistry nodeRegistry) {
        this.logicGraph = logicGraph;
        this.nodeRegistry = nodeRegistry;
    }

    @Override
    protected void configure(Configuration config) {
        config.setTitle("Logic Simulator - Pure Grid Snapping Architecture");
        config.setWidth(1280);
        config.setHeight(720);
    }

    /**
     * Rastet eine Koordinate exakt auf die Rasterlinien ein (für Junctions und Pins).
     * Beispiel bei gridSize 20: 0, 20, 40, 60...
     */
    private float snap(float value) {
        return Math.round(value / gridSize) * gridSize;
    }

    private float snapWithYCorrection(float value) {
        return Math.round((value + (gridSize / 2f)) / gridSize) * gridSize - (gridSize / 2f);
    }

    @Override
    public void process() {
        float windowWidth = ImGui.getIO().getDisplaySizeX();
        float windowHeight = ImGui.getIO().getDisplaySizeY();
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        // Farbdefinitionen (ImGui-Format: 0xAABBGGRR)
        int colorNodeBg     = ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f);
        int colorNodeBorder = ImGui.getColorU32(0.40f, 0.40f, 0.40f, 1.0f);
        int colorText       = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);
        int colorPinLow     = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);
        int colorPinHigh    = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);
        int colorGridDot    = ImGui.getColorU32(0.25f, 0.25f, 0.25f, 0.5f);
        int colorTempCable  = ImGui.getColorU32(1.0f, 0.6f, 0.0f, 0.8f);
        int colorPreview    = ImGui.getColorU32(1.0f, 0.6f, 0.0f, 0.4f);

        // =================================================================
        // 1. SEITENLEISTE (LEFT SIDEBAR)
        // =================================================================
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(sidebarWidth, windowHeight);
        int sidebarFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar
                | imgui.flag.ImGuiWindowFlags.NoResize
                | imgui.flag.ImGuiWindowFlags.NoMove
                | imgui.flag.ImGuiWindowFlags.NoCollapse;

        ImGui.begin("SidebarWindow", sidebarFlags);
        ImGui.text("Werkzeuge:");
        ImGui.separator();
        ImGui.spacing();

        // Modus-Auswahl per Radio-Button
        if (ImGui.radioButton("Auswahl / Kabel", selectedTypeName == null)) {
            selectedTypeName = null;
        }
        ImGui.spacing();
        ImGui.text("Komponenten-Bibliothek:");
        ImGui.separator();

        // Iteriere komplett dynamisch über alle registrierten Typen aus der NodeRegistry
        for (String typeName : nodeRegistry.getAvailableTypes()) {
            if (ImGui.radioButton(typeName, typeName.equals(selectedTypeName))) {
                selectedTypeName = typeName;
            }
        }

        ImGui.spacing();
        ImGui.separator();
        if (selectedTypeName != null) {
            ImGui.textColored(1.0f, 0.6f, 0.0f, 1.0f, "Platzierungs-Modus");
            ImGui.textWrapped("LINKSKLICK platziert die Node.\nRECHTSKLICK bricht den Modus ab.");
        }

        ImGui.end();

        // =================================================================
        // 2. MAIN CANVAS (ARBEITSFLÄCHE)
        // =================================================================
        ImGui.setNextWindowPos(sidebarWidth, 0);
        ImGui.setNextWindowSize(windowWidth - sidebarWidth, windowHeight);
        int canvasFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar
                | imgui.flag.ImGuiWindowFlags.NoResize
                | imgui.flag.ImGuiWindowFlags.NoMove
                | imgui.flag.ImGuiWindowFlags.NoCollapse
                | imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("CanvasWindow", canvasFlags);
        ImDrawList drawList = ImGui.getWindowDrawList();

        // Grid punkte ab dem Canvas-Start (sidebarWidth) zeichnen
        for (float gx = sidebarWidth; gx < windowWidth; gx += gridSize) {
            for (float gy = 0; gy < windowHeight; gy += gridSize) {
                drawList.addCircleFilled(gx, gy, 1.0f, colorGridDot);
            }
        }

        boolean mouseOnCanvas = mouseX > sidebarWidth;

        // =================================================================
        // 3. MAUS-INTERAKTIONEN & ABBRÜCHE
        // =================================================================

        // --- RECHTE MAUSTASTE: Kabel-Abbruch, Spawn-Abbruch ODER Gatter greifen ---
        if (ImGui.isMouseClicked(1)) {
            if (sourcePinForNewEdge != null) {
                sourcePinForNewEdge = null; // Kabelziehen abbrechen
            } else if (selectedTypeName != null) {
                selectedTypeName = null; // Platzierungsmodus abbrechen
            } else if (mouseOnCanvas) {
                // Wenn nichts aktiv ist, normales Greifen per Rechtsklick aktivieren
                Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
                if (hitNode != null) {
                    draggingNode = hitNode;
                    mouseOffsetX = mouseX - draggingNode.getX();
                    mouseOffsetY = mouseY - draggingNode.getY();
                }
            }
        }

        // --- LINKE MAUSTASTE: Spawnen ODER (Verkabeln, Splitten, Schalten) ---
        if (ImGui.isMouseClicked(0) && mouseOnCanvas) {

            if (selectedTypeName != null) {
                // Normales Platzieren aus der Sidebar (Unverändert)
                Node newNode = nodeRegistry.createInstance(selectedTypeName);
                if (newNode != null) {
                    newNode.setPosition(snap(mouseX), snap(mouseY));
                    logicGraph.addNode(newNode);
                }
            } else {
                if (ImGui.getIO().getKeyShift()) {
                    // Shift + Linksklick -> Kabel splitten (Nutzt jetzt snapNode für das Linien-Zwischengrid)
                    Edge clickedEdge = logicGraph.getEdgeAt(mouseX, mouseY, 6f);
                    if (clickedEdge != null) {
                        logicGraph.splitEdgeWithJunction(clickedEdge, "Junc_" + System.currentTimeMillis(), mouseX, mouseY, gridSize);
                    }
                } else {
                    // Normaler Linksklick: Prüfe, ob ein Pin getroffen wurde
                    Pin clickedPin = logicGraph.getAnyPinAt(mouseX, mouseY, 6f);

                    if (clickedPin != null) {
                        if (sourcePinForNewEdge == null) {
                            sourcePinForNewEdge = clickedPin; // Kabelziehen starten
                        } else {
                            // Kabel final andocken
                            if (sourcePinForNewEdge != clickedPin) {
                                logicGraph.addEdge(new Edge(sourcePinForNewEdge, clickedPin));
                                logicGraph.initializeSimulation();
                            }
                            sourcePinForNewEdge = null;
                        }
                    }
                    // =========================================================
                    // NEU: KLICK AUF FREIE FLÄCHE WÄHREND DES KABELZIEHENS (CORNER SPAWN)
                    // =========================================================
                    else if (sourcePinForNewEdge != null) {
                        float startX = sourcePinForNewEdge.getAbsoluteX();
                        float startY = sourcePinForNewEdge.getAbsoluteY();

                        // Entscheide anhand des größeren Abstands, ob die Linie horizontal oder vertikal läuft
                        float targetX, targetY;
                        if (Math.abs(mouseX - startX) > Math.abs(mouseY - startY)) {
                            targetX = snap(mouseX);
                            targetY = snapWithYCorrection(startY); // Bleibe starr auf der horizontalen Start-Linie
                        } else {
                            targetX = snap(startX); // Bleibe starr auf der vertikalen Start-Linie
                            targetY = snapWithYCorrection(mouseY);
                        }

                        // Erzeuge die CornerNode mitten im Betrieb
                        CornerNode corner = new CornerNode("Corner_" + System.currentTimeMillis());
                        corner.setPosition(targetX, targetY);
                        logicGraph.addNode(corner);

                        // Verbinde das bisherige Segment vom Start zum Eingang der Corner
                        logicGraph.addEdge(new Edge(sourcePinForNewEdge, corner.getInputs().get(0)));

                        // NAHTLOS WEITERZIEHEN: Der Ausgang der Corner wird sofort der neue Kabel-Start!
                        sourcePinForNewEdge = corner.getOutputs().get(0);
                        logicGraph.initializeSimulation();
                    }
                    // Klick flach auf ein Gehäuse
                    else {
                        Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
                        if (hitNode instanceof ButtonNode) ((ButtonNode) hitNode).toggle(logicGraph);
                    }
                }
            }
        }

        // Kontinuierliches Verschieben per gedrückter rechter Maustaste (Drag Index 1)
        if (draggingNode != null && ImGui.isMouseDragging(1)) {
            float rawTargetX = mouseX - mouseOffsetX;
            float rawTargetY = mouseY - mouseOffsetY;
            if (draggingNode instanceof JunctionNode) {
                // KORREKTUR: Beim manuellen Verschieben bleibt die Junction im Zwischen-Grid sitzen!
                draggingNode.setPosition(snap(rawTargetX), snapWithYCorrection(rawTargetY));
            } else {
                // Gatter-Gehäuse rasten auf den exakten Linien ein
                draggingNode.setPosition(snap(rawTargetX), snap(rawTargetY));
            }
        }

        // Greifen beenden beim Loslassen von Rechts
        if (ImGui.isMouseReleased(1)) {
            draggingNode = null;
        }

        if (mouseOnCanvas && ImGui.isKeyPressed(imgui.flag.ImGuiKey.Delete)) {
            // 1. Schau, ob die Maus über einer Node steht
            Node nodeToHover = logicGraph.getNodeAt(mouseX, mouseY);
            if (nodeToHover != null) {
                logicGraph.removeNode(nodeToHover);
                logicGraph.initializeSimulation(); // Schaltung neu berechnen
            } else {
                // 2. Schau, ob die Maus stattdessen über einem Kabel steht
                Edge edgeToHover = logicGraph.getEdgeAt(mouseX, mouseY, 5f);
                if (edgeToHover != null) {
                    logicGraph.removeEdge(edgeToHover);
                    logicGraph.initializeSimulation();
                }
            }
        }

        // =================================================================
        // 4. ZEICHEN-LOGIK (Kabel, Vorschau, Gatter)
        // =================================================================

        for (Edge edge : logicGraph.getEdges()) {
            Pin src = logicGraph.findPinGlobally(edge.getSourcePinId());
            Pin dest = logicGraph.findPinGlobally(edge.getDestPinId());

            if (src != null && dest != null) {
                int cableColor = (src.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                // Einfache, schnurgerade Linie von Pin zu Pin
                drawList.addLine(src.getAbsoluteX(), src.getAbsoluteY(), dest.getAbsoluteX(), dest.getAbsoluteY(), cableColor, 2.0f);
            }
        }

        // B) Temporäres Kabel (Vorschau) mit Pin-Magnet-Effekt zeichnen
        if (sourcePinForNewEdge != null) {
            float x1 = sourcePinForNewEdge.getAbsoluteX();
            float y1 = sourcePinForNewEdge.getAbsoluteY();

            float x2, y2;
            // Bestimme live für die Vorschau, in welche Richtung der Draht einrastet
            if (Math.abs(mouseX - x1) > Math.abs(mouseY - y1)) {
                x2 = snap(mouseX);
                y2 = snapWithYCorrection(y1); // Starr horizontal
            } else {
                x2 = snap(x1); // Starr vertikal
                y2 = snapWithYCorrection(mouseY);
            }

            // Nur eine einzige gerade Vorschau-Linie zeichnen!
            drawList.addLine(x1, y1, x2, y2, colorTempCable, 1.5f);
        }
        // C) Visuelle Platzierungsvorschau (Ghost-Preview) des ausgewählten Typs
        if (selectedTypeName != null && mouseOnCanvas) {
            Node previewNode = nodeRegistry.createInstance(selectedTypeName);
            if (previewNode != null) {
                float previewX = snap(mouseX);
                float previewY = snap(mouseY);
                drawList.addRectFilled(previewX, previewY, previewX + previewNode.getWidth(), previewY + previewNode.getHeight(), colorPreview, 4.0f);
                drawList.addRect(previewX, previewY, previewX + previewNode.getWidth(), previewY + previewNode.getHeight(), colorNodeBorder, 4.0f, 0, 1.5f);
            }
        }

        // D) Gatter, Junctions und Pins aus dem Graphen rendern
        for (Node node : logicGraph.getNodes()) {
            if (node instanceof JunctionNode) {
                Pin p = node.getInputs().get(0);
                int junctionColor = (p.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(p.getAbsoluteX(), p.getAbsoluteY(), 5f, junctionColor);
                continue;
            }
            if (node instanceof CornerNode) {
                Pin p = node.getInputs().get(0);
                int cornerColor = (p.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                // Zeichne einen winzigen Richtungs-Punkt (Radius 3px)
                drawList.addCircleFilled(p.getAbsoluteX(), p.getAbsoluteY(), 3f, cornerColor);
                continue;
            }
            float x = node.getX();
            float y = node.getY();
            float w = node.getWidth();
            float h = node.getHeight();
            // Hintergrundfarbe dynamisch an Komponententyp und Zustand anpassen
            int currentBgColor = colorNodeBg;
            if (node instanceof LedNode) {
                currentBgColor = ((LedNode) node).isOn() ? ImGui.getColorU32(0.0f, 0.6f, 0.0f, 1.0f) : ImGui.getColorU32(0.2f, 0.0f, 0.0f, 1.0f);
            } else if (node instanceof ButtonNode && ((ButtonNode) node).isPressed()) {
                currentBgColor = ImGui.getColorU32(0.25f, 0.35f, 0.25f, 1.0f);
            }
            // Kasten und Text zeichnen (Nutzt node.getTypeName() dynamisch aus der Registry!)
            drawList.addRectFilled(x, y, x + w, y + h, currentBgColor, 4.0f);
            drawList.addRect(x, y, x + w, y + h, colorNodeBorder, 4.0f, 0, 1.5f);
            drawList.addText(x + 10f, y + (h / 2f) - 6f, colorText, node.getTypeName());

            // Eingangs-Pins zeichnen
            for (Pin pin : node.getInputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
            drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
            drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }
            // Ausgangs-Pins zeichnen
            for (Pin pin : node.getOutputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
                drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }
        }
        ImGui.end();
    }
}

package logicSimulator.rendering;

import imgui.*;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiWindowFlags;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;
import logicSimulator.graph.edgeWaypoints.WaypointNode;
import logicSimulator.graph.nodes.NotNode;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer extends Application {

    public Graph graph;

    public static final float GRID_SPACING = 32.0f;

    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float zoom = 1.0f;

    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 4.0f;

    private final List<Node> selectedNodes = new ArrayList<>();

    private boolean isDraggingNodes = false;
    private int gridDragStartX = 0;
    private int gridDragStartY = 0;
    private final Map<Node, int[]> nodeStartPositions = new HashMap<>();

    private boolean isSelecting = false;
    private float selectionBoxStartX = 0.0f;
    private float selectionBoxStartY = 0.0f;

    public Renderer(Graph graph) {
        this.graph = graph;
    }

    @Override
    protected void configure(Configuration config) {
        config.setTitle("Logic Simulator");
        config.setWidth(1600);
        config.setHeight(900);
    }

    @Override
    protected void initImGui(Configuration config) {
        super.initImGui(config);

        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        ImFontConfig fontConfig = new imgui.ImFontConfig();

        fontConfig.setOversampleH(3);
        fontConfig.setOversampleV(1);

        try {
            InputStream is = getClass().getResourceAsStream("/arial.ttf");
            if (is == null) {
                throw new RuntimeException("Schriftart arial.ttf konnte nicht im Resources-Ordner gefunden werden!");
            }

            byte[] fontBytes = is.readAllBytes();
            is.close();

            ByteBuffer fontBuffer = ByteBuffer.allocateDirect(fontBytes.length);
            fontBuffer.put(fontBytes);
            fontBuffer.flip();

            fontAtlas.addFontFromMemoryTTF(fontBytes, 64.0f, fontConfig);

        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Fonts aus den Ressourcen: " + e.getMessage());
            e.printStackTrace();
            fontAtlas.addFontDefault(fontConfig);
        }

        fontAtlas.build();
        fontConfig.destroy();
    }

    @Override
    public void process() {
        float windowWidth = ImGui.getIO().getDisplaySizeX();
        float windowHeight = ImGui.getIO().getDisplaySizeY();

        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(windowWidth, windowHeight);

        int windowFlags = ImGuiWindowFlags.NoTitleBar
                | ImGuiWindowFlags.NoResize
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoCollapse
                | ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("Zeichenbereich", windowFlags);

        if (ImGui.isWindowHovered()) {
            float mouseWheel = ImGui.getIO().getMouseWheel();
            if (mouseWheel != 0.0f) {
                float mouseX = ImGui.getIO().getMousePosX();
                float mouseY = ImGui.getIO().getMousePosY();

                float mouseInWorldX = (mouseX / zoom) - offsetX;
                float mouseInWorldY = (mouseY / zoom) - offsetY;

                float oldZoom = zoom;
                zoom += mouseWheel * 0.1f;
                zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

                offsetX = (mouseX / zoom) - mouseInWorldX;
                offsetY = (mouseY / zoom) - mouseInWorldY;
            }
        }

        if (ImGui.isWindowHovered() && ImGui.isMouseDragging(2)) {
            offsetX += ImGui.getIO().getMouseDeltaX() / zoom;
            offsetY += ImGui.getIO().getMouseDeltaY() / zoom;
        }

        ImDrawList drawList = ImGui.getWindowDrawList();

        float scaledSpacing = GRID_SPACING * zoom;

        float startX = (offsetX * zoom) % scaledSpacing;
        float startY = (offsetY * zoom) % scaledSpacing;
        if (startX < 0) startX += scaledSpacing;
        if (startY < 0) startY += scaledSpacing;

        int dotColor = ImGui.getColorU32(0.5f, 0.5f, 0.5f, 0.5f);

        float dotRadius = Math.max(1.0f, 1.5f * zoom);

        for (float x = startX; x < windowWidth; x += scaledSpacing) {
            for (float y = startY; y < windowHeight; y += scaledSpacing) {
                drawList.addCircleFilled(x, y, dotRadius, dotColor);
            }
        }

        float mouseX = ImGui.getIO().getMousePosX();
        float mouseY = ImGui.getIO().getMousePosY();
        float worldMouseX = (mouseX / zoom) - offsetX;
        float worldMouseY = (mouseY / zoom) - offsetY;

        int gridMouseX = (int) Math.floor(worldMouseX / GRID_SPACING);
        int gridMouseY = (int) Math.floor(worldMouseY / GRID_SPACING);

        // 2. KLICK-ERKENNUNG (Linksklick)
        if (ImGui.isMouseClicked(0) && ImGui.isWindowHovered()) {
            // Prüfen, ob ein Node an der Mausposition getroffen wurde
            List<Node> hitNodes = graph.getNodesInside(gridMouseX, gridMouseY, gridMouseX + 1, gridMouseY + 1);

            if (!hitNodes.isEmpty()) {
                Node clickedNode = hitNodes.get(0);

                // Wenn der Node noch nicht ausgewählt war und SHIFT nicht gedrückt ist: Auswahl zurücksetzen
                if (!selectedNodes.contains(clickedNode) && !ImGui.getIO().getKeyShift()) {
                    selectedNodes.clear();
                }

                if (!selectedNodes.contains(clickedNode)) {
                    selectedNodes.add(clickedNode);
                }

                // Drag-Modus für Nodes starten
                isDraggingNodes = true;
                gridDragStartX = gridMouseX;
                gridDragStartY = gridMouseY;

                // Startpositionen aller ausgewählten Nodes merken
                nodeStartPositions.clear();
                for (Node n : selectedNodes) {
                    nodeStartPositions.put(n, new int[]{n.getX(), n.getY()});
                }
            } else {
                // Klick ins Leere: Auswahlbox-Modus starten
                if (!ImGui.getIO().getKeyShift()) {
                    selectedNodes.clear(); // Bestehende Auswahl löschen, außer SHIFT hält sie
                }
                isSelecting = true;
                selectionBoxStartX = mouseX;
                selectionBoxStartY = mouseY;
            }
        }

        // 3. WÄHREND DES ZIEHENS (Mouse Dragging)
        if (ImGui.isMouseDragging(0)) {
            if (isDraggingNodes) {
                // MULTI-DRAG: Alle selektierten Nodes synchron verschieben
                int deltaX = gridMouseX - gridDragStartX;
                int deltaY = gridMouseY - gridDragStartY;

                for (Node n : selectedNodes) {
                    int[] startPos = nodeStartPositions.get(n);
                    if (startPos != null) {
                        n.setX(startPos[0] + deltaX);
                        n.setY(startPos[1] + deltaY);
                    }
                }
            } else if (isSelecting) {
                // SELECTION BOX RENDER: Zeichne die transluzente Auswahlbox auf dem Bildschirm
                int boxBgColor = ImGui.getColorU32(0.2f, 0.6f, 1.0f, 0.15f); // Hellblau gefüllt
                int boxBorderColor = ImGui.getColorU32(0.2f, 0.6f, 1.0f, 0.6f); // Blauer Rahmen

                drawList.addRectFilled(selectionBoxStartX, selectionBoxStartY, mouseX, mouseY, boxBgColor);
                drawList.addRect(selectionBoxStartX, selectionBoxStartY, mouseX, mouseY, boxBorderColor, 0.0f, 0, 1.5f);
            }
        }

        // 4. MAUSTASTE LOSLASSEN
        if (ImGui.isMouseReleased(0)) {
            if (isSelecting) {
                // Auswahlbox auswerten: Konvertiere Start- und Endpunkt der Box in Welt-Grid-Koordinaten
                float worldStartBoxX = (selectionBoxStartX / zoom) - offsetX;
                float worldStartBoxY = (selectionBoxStartY / zoom) - offsetY;

                int gridStartX = (int) Math.floor(Math.min(worldStartBoxX, worldMouseX) / GRID_SPACING);
                int gridStartY = (int) Math.floor(Math.min(worldStartBoxY, worldMouseY) / GRID_SPACING);
                int gridEndX = (int) Math.floor(Math.max(worldStartBoxX, worldMouseX) / GRID_SPACING) + 1;
                int gridEndY = (int) Math.floor(Math.max(worldStartBoxY, worldMouseY) / GRID_SPACING) + 1;

                // Alle Nodes in diesem Bereich finden und zur Auswahl hinzufügen
                List<Node> boxedNodes = graph.getNodesInside(gridStartX, gridStartY, gridEndX, gridEndY);
                for (Node n : boxedNodes) {
                    if (!selectedNodes.contains(n)) {
                        selectedNodes.add(n);
                    }
                }
            }

            // Zustände zurücksetzen
            isDraggingNodes = false;
            isSelecting = false;
        }

        graph.getNodes().values().forEach(node -> node.draw(drawList, offsetX, offsetY, zoom, selectedNodes.contains(node)));

        ImGui.end();
    }

    @Override
    protected void postProcess() {
        super.postProcess();
    }
}

package logicSimulator.rendering;

import imgui.*;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiWindowFlags;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.Pin;
import logicSimulator.graph.edgeWaypoints.WaypointNode;
import logicSimulator.graph.nodes.NotNode;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class Renderer extends Application {

    public Graph graph;

    public static final float GRID_SPACING = 32.0f;

    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float zoom = 1.0f;

    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 4.0f;

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

        graph.getNodes().values().forEach(node -> node.draw(drawList, offsetX, offsetY, zoom));

        ImGui.end();
    }

    @Override
    protected void postProcess() {
        super.postProcess();
    }
}

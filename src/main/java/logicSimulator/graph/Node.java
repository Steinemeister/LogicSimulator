package logicSimulator.graph;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import logicSimulator.rendering.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Node {
    private final UUID id;
    private final String nodeTypeName;
    private final List<Pin> inputPins;
    private final List<Pin> outputPins;

    private int x, y;
    private int width = 2, height = 1;

    public Node(String nodeTypeName) {
        this.id = UUID.randomUUID();
        this.nodeTypeName = nodeTypeName;
        this.inputPins = new ArrayList<>();
        this.outputPins = new ArrayList<>();
    }

    /*
    updates output pins based on input pins states
    returns list of pins that changed state
     */
    public abstract void update(Graph graph);

    public void draw(ImDrawList drawList, float offsetX, float offsetY, float zoom) {
        float worldX = this.x * Renderer.GRID_SPACING;
        float worldY = this.y * Renderer.GRID_SPACING;
        float worldWidth = this.width * Renderer.GRID_SPACING;
        float worldHeight = this.height * Renderer.GRID_SPACING;

        float screenX = (worldX + offsetX) * zoom;
        float screenY = (worldY + offsetY) * zoom;

        float scaledWidth = worldWidth * zoom;
        float scaledHeight = worldHeight * zoom;

        float pMinX = screenX;
        float pMinY = screenY;
        float pMaxX = screenX + scaledWidth;
        float pMaxY = screenY + scaledHeight;

        int fillColor = getColorFromType(nodeTypeName);
        int hash = this.nodeTypeName.hashCode();
        float hue = Math.abs(hash % 360) / 360.0f;
        float[] borderRgb = hslToRgb(hue, 0.65f, 0.35f);
        int borderColor = ImGui.getColorU32(borderRgb[0], borderRgb[1], borderRgb[2], 1.0f);

        float scaledRounding = 7.5f * zoom;
        float scaledThickness = 1.5f * zoom;

        drawList.addRectFilled(pMinX, pMinY, pMaxX, pMaxY, fillColor, scaledRounding, ImDrawFlags.None);
        drawList.addRect(pMinX, pMinY, pMaxX, pMaxY, borderColor, scaledRounding, ImDrawFlags.None, scaledThickness);

        imgui.ImFont currentFont = ImGui.getFont();

        float targetNodeFontSize = 20.0f;
        float scaledFontSize = targetNodeFontSize * zoom;

        ImVec2 textSize = new ImVec2();
        currentFont.calcTextSizeA(textSize, scaledFontSize, Float.MAX_VALUE, 0.0f, this.nodeTypeName);

        float centerX = pMinX + (scaledWidth / 2.0f);
        float centerY = pMinY + (scaledHeight / 2.0f);
        float textX = centerX - (textSize.x / 2.0f);
        float textY = centerY - (textSize.y / 2.0f);

        int textColor = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);

        drawList.addText(currentFont, (int) scaledFontSize, textX, textY, textColor, this.nodeTypeName, null);
    }

    private int getColorFromType(String typeName) {
        int hash = typeName.hashCode();

        float hue = Math.abs(hash % 360);

        float saturation = 0.65f;
        float lightness = 0.55f;

        float[] rgb = hslToRgb(hue / 360.0f, saturation, lightness);

        return ImGui.getColorU32(rgb[0], rgb[1], rgb[2], 1.0f);
    }

    private float[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1.0f + s) : l + s - l * s;
            float p = 2.0f * l - q;
            r = hueToRgb(p, q, h + 1.0f / 3.0f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0f / 3.0f);
        }

        return new float[]{r, g, b};
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0.0f) t += 1.0f;
        if (t > 1.0f) t -= 1.0f;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addPos(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public UUID getId() {
        return id;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public List<Pin> getInputPins() {
        return inputPins;
    }

    public List<Pin> getOutputPins() {
        return outputPins;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}

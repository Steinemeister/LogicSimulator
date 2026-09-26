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
    private int width = 4, height = 1;

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

    protected void setupPins(int inputsCount, int outputsCount) {
        for (int i = 0; i < inputsCount; i++) {
            inputPins.add(new Pin(this, Pin.PinType.INPUT, i));
        }
        for (int i = 0; i < outputsCount; i++) {
            outputPins.add(new Pin(this, Pin.PinType.OUTPUT, i));
        }

        // DYNAMISCHE HÖHENBERECHNUNG:
        // Jeder Pin braucht 1 Box Platz. Wir addieren 1 Box als Puffer für den unteren Rand.
        int maxPins = Math.max(inputsCount, outputsCount);
        this.height = Math.max(1, maxPins);
    }

    public void draw(ImDrawList drawList, float offsetX, float offsetY, float zoom, boolean selected) {
        float worldX = this.x * Renderer.GRID_SPACING;
        float worldY = this.y * Renderer.GRID_SPACING;
        float worldWidth = this.width * Renderer.GRID_SPACING;
        float worldHeight = this.height * Renderer.GRID_SPACING;

        float visualWorldY = worldY + (0.5f * Renderer.GRID_SPACING);

        float screenX = (worldX + offsetX) * zoom;
        float screenY = (visualWorldY + offsetY) * zoom;

        float scaledWidth = worldWidth * zoom;
        float scaledHeight = worldHeight * zoom;

        float pMinX = screenX;
        float pMinY = screenY;
        float pMaxX = screenX + scaledWidth;
        float pMaxY = screenY + scaledHeight;

        int hash = this.nodeTypeName.hashCode();
        float hue = Math.abs(hash % 360) / 360.0f;
        float saturation = 0.65f;

        // Basis-Helligkeit für normale Nodes, höhere Helligkeit für selektierte Nodes
        float fillLightness = selected ? 0.60f : 0.45f;
        float borderLightness = selected ? 0.40f : 0.25f;

        // HSL zu RGB konvertieren
        float[] fillRgb = hslToRgb(hue, saturation, fillLightness);
        float[] borderRgb = hslToRgb(hue, saturation, borderLightness);

        int fillColor = ImGui.getColorU32(fillRgb[0], fillRgb[1], fillRgb[2], 1.0f);
        int borderColor = ImGui.getColorU32(borderRgb[0], borderRgb[1], borderRgb[2], 1.0f);

        float baseThickness = selected ? 3.5f : 2.0f;
        float scaledRounding = 12.0f * zoom;
        float scaledThickness = baseThickness * zoom;

        // 4. Zeichnen des ausgefüllten Rechtecks samt Rahmen
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

        inputPins.forEach(pin -> pin.draw(drawList, offsetX, offsetY, zoom));
        outputPins.forEach(pin -> pin.draw(drawList, offsetX, offsetY, zoom));
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
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

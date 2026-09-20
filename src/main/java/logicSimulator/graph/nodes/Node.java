package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;

import java.util.*;

public abstract class Node {
    private final String name;

    // Pins werden jetzt in Listen statt fehleranfälligen Maps gespeichert
    protected final List<Pin> inputs = new ArrayList<>();
    protected final List<Pin> outputs = new ArrayList<>();

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Node(String name) {
        this.name = name;
        this.width = 80f;
    }

    public void calculateLayout(float gridSize) {
        int inputCount = inputs.size();
        int outputCount = outputs.size();
        int maxPins = Math.max(inputCount, outputCount);

        // Die Standardbreite bleibt ein Vielfaches des Grids (z.B. 80f)
        this.width = Math.round(80f / gridSize) * gridSize;

        if (maxPins == 0) {
            this.height = gridSize;
            return;
        }

        // Die Höhe entspricht exakt den benötigten Grid-Kacheln
        this.height = maxPins * gridSize;

        // Inputs links platzieren: Versatz um ein halbes Grid nach unten!
        for (int i = 0; i < inputs.size(); i++) {
            float relY = (i * gridSize) + (gridSize / 2f); // Versatz um gridSize / 2
            inputs.get(i).setRelativePosition(0f, relY);
        }

        // Outputs rechts platzieren: Versatz um ein halbes Grid nach unten!
        for (int j = 0; j < outputs.size(); j++) {
            float relY = (j * gridSize) + (gridSize / 2f);
            outputs.get(j).setRelativePosition(this.width, relY);
        }
    }

    /**
     * Sucht einen Pin in diesem Knoten anhand seiner UUID.
     */
    public Pin findPinById(UUID pinId) {
        for (Pin p : inputs)  if (p.getId().equals(pinId)) return p;
        for (Pin p : outputs) if (p.getId().equals(pinId)) return p;
        return null;
    }

    public Pin getPinAt(float mx, float my, float radius) {
        for (Pin pin : inputs) {
            float dx = pin.getAbsoluteX() - mx;
            float dy = pin.getAbsoluteY() - my;
            if ((dx * dx + dy * dy) <= (radius * radius)) return pin;
        }
        for (Pin pin : outputs) {
            float dx = pin.getAbsoluteX() - mx;
            float dy = pin.getAbsoluteY() - my;
            if ((dx * dx + dy * dy) <= (radius * radius)) return pin;
        }
        return null;
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean contains(float mx, float my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }
    public String getName() { return name; }
    public List<Pin> getInputs() { return inputs; }
    public List<Pin> getOutputs() { return outputs; }

    public abstract void update(Graph graph);
}

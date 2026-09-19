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

    public void calculateLayout(float pinDistance) {
        int maxPins = Math.max(inputs.size(), outputs.size());
        if (maxPins == 0) {
            this.height = pinDistance;
            return;
        }
        this.height = maxPins * pinDistance;

        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setRelativePosition(0f, (i * pinDistance) + (pinDistance / 2f));
        }
        for (int i = 0; i < outputs.size(); i++) {
            outputs.get(i).setRelativePosition(this.width, (i * pinDistance) + (pinDistance / 2f));
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

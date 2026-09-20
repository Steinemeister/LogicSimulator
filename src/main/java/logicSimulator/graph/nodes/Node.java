package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;

import java.util.*;

public abstract class Node {
    private final UUID instanceId; // Eindeutige ID der konkreten Instanz auf dem Feld
    private final String typeName; // Der Typ-Name (z.B. "AND", "OR", "CustomCPU")

    protected final List<Pin> inputs = new ArrayList<>();
    protected final List<Pin> outputs = new ArrayList<>();

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Node(String typeName) {
        this.instanceId = UUID.randomUUID();
        this.typeName = typeName;
        this.width = 80f;
    }

    public void calculateLayout(float gridSize) {
        int maxPins = Math.max(inputs.size(), outputs.size());
        if (maxPins == 0) {
            this.height = gridSize;
            return;
        }
        this.height = maxPins * gridSize;

        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setRelativePosition(0f, (i * gridSize) + (gridSize / 2f));
        }
        for (int i = 0; i < outputs.size(); i++) {
            outputs.get(i).setRelativePosition(this.width, (i * gridSize) + (gridSize / 2f));
        }
    }

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

    public UUID getInstanceId() { return instanceId; }
    public String getTypeName() { return typeName; } // NEU: Liefert den Typnamen für die Sidebar
    public List<Pin> getInputs() { return inputs; }
    public List<Pin> getOutputs() { return outputs; }

    public abstract void update(Graph graph);

    public String getName() {
        return typeName;
    }
}

package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Node {
    private final String name;
    protected final Map<String, Pin> inputs = new LinkedHashMap<>();
    protected final Map<String, Pin> outputs = new LinkedHashMap<>();

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Node(String name) {
        this.name = name;
        this.width = 80f;
    }

    public void calculateLayout(float pinDistance) {
        int inputCount = inputs.size();
        int outputCount = outputs.size();

        int maxPins = Math.max(inputCount, outputCount);

        if (maxPins == 0) {
            this.height = pinDistance;
            return;
        }

        this.height = maxPins * pinDistance;

        int i = 0;
        for (Pin pin : inputs.values()) {
            float relY = (i * pinDistance) + (pinDistance / 2f);
            pin.setRelativePosition(0f, relY);
            i++;
        }

        int j = 0;
        for (Pin pin : outputs.values()) {
            float relY = (j * pinDistance) + (pinDistance / 2f);
            pin.setRelativePosition(this.width, relY);
            j++;
        }
    }

    public Pin getPinAt(float mx, float my, float radius) {
        for (Pin pin : inputs.values()) {
            float dx = pin.getAbsoluteX() - mx;
            float dy = pin.getAbsoluteY() - my;
            if ((dx * dx + dy * dy) <= (radius * radius)) {
                return pin;
            }
        }

        for (Pin pin : outputs.values()) {
            float dx = pin.getAbsoluteX() - mx;
            float dy = pin.getAbsoluteY() - my;
            if ((dx * dx + dy * dy) <= (radius * radius)) {
                return pin;
            }
        }
        return null;
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public String getName() { return name; }
    public Map<String, Pin> getInputs() { return inputs; }
    public Map<String, Pin> getOutputs() { return outputs; }

    public abstract void update(Graph graph);
}

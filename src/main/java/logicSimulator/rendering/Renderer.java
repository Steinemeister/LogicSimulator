package logicSimulator.rendering;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.app.Application;
import imgui.app.Configuration;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Pin;
import logicSimulator.graph.edgeWaypoints.WaypointNode;

public class Renderer extends Application {

    @Override
    protected void configure(Configuration config) {
        super.configure(config);
    }

    @Override
    public void process() {

    }

    @Override
    protected void postProcess() {
        super.postProcess();
    }

    private static final int COLOR_ACTIVE = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);
    private static final int COLOR_INACTIVE = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);
    private static final float WIRE_THICKNESS = 2.0f; // Dicke des Kabels in Pixeln
}

package com.aft.api.agent.tool;

public interface ToolSpec {
    String name();

    String description();

    String inputSchema();

    default boolean writeEffect() {
        return false;
    }
}

package com.aft.api.agent.prompt;

public enum SystemPrompts {
    PLANNER("prompts/system-planner.st"),
    ANALYST("prompts/system-analyst.st");

    private final String location;

    SystemPrompts(String location) {
        this.location = location;
    }

    public String location() {
        return location;
    }
}

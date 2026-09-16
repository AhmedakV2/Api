package com.aft.api.agent.dto;

public record ToolSpecDto(String name, String description, String inputSchema, boolean approvalRequired) {
}

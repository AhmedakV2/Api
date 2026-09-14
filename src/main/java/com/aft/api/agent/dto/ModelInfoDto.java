package com.aft.api.agent.dto;

import java.util.List;

public record ModelInfoDto(String activeProvider, List<String> activeProviders, List<String> models,
                           String plannerModel, String fastModel) {
}

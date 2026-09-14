package com.aft.api.agent.dto;

import java.util.List;

public record SessionDetailDto(SessionDto session, List<MessageDto> messages) {
}

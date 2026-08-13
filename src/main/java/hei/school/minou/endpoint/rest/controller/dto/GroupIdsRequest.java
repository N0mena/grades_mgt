package hei.school.minou.endpoint.rest.controller.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupIdsRequest(List<UUID> groupIds) {}

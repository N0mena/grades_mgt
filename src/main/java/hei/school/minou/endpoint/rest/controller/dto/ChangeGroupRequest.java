package hei.school.minou.endpoint.rest.controller.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ChangeGroupRequest(UUID newGroupId) {}

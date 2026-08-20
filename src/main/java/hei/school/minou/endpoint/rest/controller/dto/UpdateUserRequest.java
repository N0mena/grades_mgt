package hei.school.minou.endpoint.rest.controller.dto;

import hei.school.minou.entity.enums.Role;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateUserRequest(
    String firstName, String lastName, Role role, String email, String password, UUID promotionId) {}

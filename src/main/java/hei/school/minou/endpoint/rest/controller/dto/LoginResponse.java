package hei.school.minou.endpoint.rest.controller.dto;

import hei.school.minou.entity.User;
import lombok.Builder;

@Builder
public record LoginResponse(String token, User user) {}

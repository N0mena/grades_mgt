package hei.school.minou.endpoint.rest.controller.dto;

import lombok.Builder;

@Builder
public record LoginRequest(String email, String password) {}

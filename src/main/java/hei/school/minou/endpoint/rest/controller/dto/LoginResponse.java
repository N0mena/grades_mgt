package hei.school.minou.endpoint.rest.controller.dto;

import hei.school.minou.entity.User;
import lombok.Builder;

@Builder
public record LoginResponse(boolean success, String token, String errorMessage, User user) {
  public static LoginResponse success(String token) {
    return new LoginResponse(true, token, null, null);
  }

  public static LoginResponse success(String token, User user) {
    return new LoginResponse(true, token, null, user);
  }

  public static LoginResponse failure(String errorMessage) {
    return new LoginResponse(false, null, errorMessage, null);
  }
}

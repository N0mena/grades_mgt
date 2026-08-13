package hei.school.minou.entity;

import hei.school.minou.entity.enums.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record User(UUID id, String firstName, String lastName, Role role, String email, String password) {
}

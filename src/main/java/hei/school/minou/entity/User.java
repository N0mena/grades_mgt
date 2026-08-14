package hei.school.minou.entity;

import hei.school.minou.entity.enums.Role;
import java.util.UUID;
import lombok.Builder;

@Builder
public record User(
    UUID id, String firstName, String lastName, Role role, String email, String password) {}

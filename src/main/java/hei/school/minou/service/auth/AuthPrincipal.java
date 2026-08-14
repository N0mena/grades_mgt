package hei.school.minou.service.auth;

import hei.school.minou.entity.enums.Role;
import java.util.UUID;

public record AuthPrincipal(UUID userId, Role role, String email) {}

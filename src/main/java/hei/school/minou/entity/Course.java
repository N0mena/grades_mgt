package hei.school.minou.entity;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Course(UUID id, String ref, String title, Integer credit, List<User> teachers) {}

package hei.school.minou.entity;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record Course(UUID id, String ref, String title, Integer credit, List<User> teachers) {
}

package hei.school.minou.entity;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Course(UUID id, String ref, String title, Integer credit) {}

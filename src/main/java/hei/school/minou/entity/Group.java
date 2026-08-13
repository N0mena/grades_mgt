package hei.school.minou.entity;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Group(UUID id, String ref) {}

package hei.school.minou.entity;

import lombok.Builder;

import java.util.UUID;

@Builder
public record Group(UUID id, String ref) {
}

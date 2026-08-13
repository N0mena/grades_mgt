package hei.school.minou.endpoint.event.consumer.model;

import hei.school.minou.PojaGenerated;
import hei.school.minou.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}

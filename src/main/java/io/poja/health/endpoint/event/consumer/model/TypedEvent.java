package io.poja.health.endpoint.event.consumer.model;

import io.poja.health.PojaGenerated;
import io.poja.health.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}

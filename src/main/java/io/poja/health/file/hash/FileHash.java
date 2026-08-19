package io.poja.health.file.hash;

import io.poja.health.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}

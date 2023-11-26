package com.imfs;

import java.nio.file.Path;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImfsChildren {
    private final Stream<Path> stream;
    private final int version;
    private final int size;
}

package com.imfs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ImfsRecord {
    @NonNull
    private final String materializedPath;
    private final boolean file;
    private final int blobId; // Used only by H2
    private final byte[] bytes; // Use only by TreeMap

    public static ImfsRecord ofDir(String materializedPath) {
        return builder().materializedPath(materializedPath).file(false).build();
    }
}

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
    private final byte[] bytes;

    public static ImfsRecord ofDir(String materializedPath) {
        return builder().materializedPath(materializedPath).build();
    }
}

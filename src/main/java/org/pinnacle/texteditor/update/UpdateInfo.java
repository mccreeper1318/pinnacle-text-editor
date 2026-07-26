package org.pinnacle.texteditor.update;

import java.net.URI;

public record UpdateInfo(
        String version,
        String assetName,
        URI downloadUri,
        String sha256
) {
}

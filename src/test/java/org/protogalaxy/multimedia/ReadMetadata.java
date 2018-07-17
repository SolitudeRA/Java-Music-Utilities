package org.protogalaxy.multimedia;

import org.junit.jupiter.api.Test;
import org.protogalaxy.multimedia.components.Metadata;

import java.nio.file.Paths;

class ReadMetadata {
    @Test
    void readMetadata() {
        MusicContainer container = new MusicContainer(Paths.get("W:\\Projects\\Java-Music-Utilities\\src\\main\\java\\org\\protogalaxy\\multimedia\\test.aiff"));
        Metadata metadata = new Metadata(container);
        metadata.readCover(container);
    }
}

package org.protogalaxy.multimedia;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;

import java.nio.file.Path;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;

public class MusicContainer {
    private Path path = null;
    private AVFormatContext avFormatContext = avformat_alloc_context();

    public MusicContainer(Path path) {
        this.path = path;
        av_register_all();
        avformat_open_input(avFormatContext, path.toString(), null, null);
        avformat_find_stream_info(avFormatContext, (PointerPointer) null);
    }

    public MusicContainer(byte[] stream) {
        BytePointer bytePointer = new BytePointer(stream);
        av_register_all();
        //TODO: stream reader from memory
    }

    public void destory() {
        avformat_close_input(avFormatContext);
    }

    public Path getPath() {
        return path;
    }

    public AVFormatContext getAvFormatContext() {
        return avFormatContext;
    }
}

package org.lds.media.isobmff;

import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class FullBox extends Box {
    private int version;
    private int flags;

    public FullBox(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
        int value = r.readIntValue();
        dataOffset += 4;
        version = (byte) (value >> 24);
        flags = value & 0xffffff;
    }

    public int getVersion() {
        return version;
    }

    public int getFlags() {
        return flags;
    }
}

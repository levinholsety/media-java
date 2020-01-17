package org.lds.media.isobmff;

import org.lds.io.AbstractBinaryReader;
import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class Box {

    public static Box read(AbstractSeekableBinaryReader r) throws IOException {
        long size = r.readIntValue() & 0xffff;
        String type = r.readString(4);
        if ("ftyp".equals(type)) {
            return new FileTypeBox(r, size, type);
        } else if ("meta".equals(type)) {
            return new MetaBox(r, size, type);
        } else if ("iinf".equals(type)) {
            return new ItemInfoBox(r, size, type);
        } else if ("infe".equals(type)) {
            return new ItemInfoEntry(r, size, type);
        } else if ("iloc".equals(type)) {
            return new ItemLocationBox(r, size, type);
        } else {
            return new Box(r, size, type);
        }
    }

    private final AbstractBinaryReader r;
    protected long dataOffset;
    private long size;
    private String type;
    private String userType;

    protected Box(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        this.r = r;
        dataOffset = 8;
        this.size = size;
        this.type = type;
        if (size == 1) {
            this.size = r.readLongValue();
            dataOffset += 8;
        } else if (size == 0) {
            this.size = r.getLength() - r.getPosition() + 8;
        }
        if ("uuid".equals(type)) {
            this.userType = r.readString(16);
            dataOffset += 16;
        }
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getUserType() {
        return userType;
    }

    public void skip() throws IOException {
        r.skip(getSize() - dataOffset);
    }
}

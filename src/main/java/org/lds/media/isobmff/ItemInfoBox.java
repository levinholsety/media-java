package org.lds.media.isobmff;

import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class ItemInfoBox extends FullBox {

    private int entryCount;

    public ItemInfoBox(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
        if (getVersion() == 0) {
            entryCount = r.readShortValue();
            dataOffset += 2;
        } else {
            entryCount = r.readIntValue();
            dataOffset += 4;
        }

    }

    public int getEntryCount() {
        return entryCount;
    }
}

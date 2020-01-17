package org.lds.media.isobmff;

import org.lds.Encoding;
import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class ItemInfoEntry extends FullBox {
    private int itemId;
    private String itemType;
    private String itemName;

    public ItemInfoEntry(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
        if (getVersion() >= 2) {
            if (getVersion() == 2) {
                itemId = r.readShortValue();
                dataOffset += 2;
            } else if (getVersion() == 3) {
                itemId = r.readIntValue();
                dataOffset += 4;
            }
            r.skip(2);
            dataOffset += 2;
            itemType = r.readString(4);
            dataOffset += 4;
            byte[] data = r.readByteArrayUntil((byte) 0);
            dataOffset += data.length + 1;
            itemName = Encoding.UTF_8.decode(data);
        }
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }
}

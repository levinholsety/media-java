package org.lds.media.isobmff;

import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class ItemLocationBox extends FullBox {
    public static class Item {
        private int itemId;
        private int extentCount;
        private Extent[] extents;

        public Item(ItemLocationBox box, AbstractSeekableBinaryReader r) throws IOException {
            if (box.getVersion() < 2) {
                itemId = r.readShortValue();
                box.dataOffset += 2;
            } else if (box.getVersion() == 2) {
                itemId = r.readIntValue();
                box.dataOffset += 4;
            }
            if (box.getVersion() == 1 || box.getVersion() == 2) {
                r.skip(2);
                box.dataOffset += 2;
            }
            r.skip(2);
            box.dataOffset += 2;
            r.skip(box.baseOffsetSize);
            box.dataOffset += box.baseOffsetSize;
            extentCount = r.readShortValue();
            box.dataOffset += 2;
            extents = new Extent[extentCount];
            for (int i = 0; i < extentCount; i++) {
                extents[i] = new Extent(box, r);
            }
        }

        public int getItemId() {
            return itemId;
        }

        public int getExtentCount() {
            return extentCount;
        }

        public Extent getExtent(int index) {
            return extents[index];
        }
    }

    public static class Extent {
        private long extentOffset;
        private long extentLength;

        public Extent(ItemLocationBox box, AbstractSeekableBinaryReader r) throws IOException {
            if ((box.getVersion() == 1 || box.getVersion() == 2) && box.indexSize > 0) {
                r.skip(box.indexSize);
                box.dataOffset += box.indexSize;
            }
            switch (box.offsetSize) {
                case 0:
                    extentOffset = 0;
                    break;
                case 4:
                    extentOffset = r.readUnsignedIntValue();
                    break;
                case 8:
                    extentOffset = r.readLongValue();
                    break;
                default:
                    throw new IOException("invalid length size");
            }
            box.dataOffset += box.offsetSize;
            switch (box.lengthSize) {
                case 0:
                    extentLength = 0;
                    break;
                case 4:
                    extentLength = r.readUnsignedIntValue();
                    break;
                case 8:
                    extentLength = r.readLongValue();
                    break;
                default:
                    throw new IOException("invalid length size");
            }
            box.dataOffset += box.lengthSize;
        }

        public long getExtentOffset() {
            return extentOffset;
        }

        public long getExtentLength() {
            return extentLength;
        }
    }


    private int offsetSize;
    private int lengthSize;
    private int baseOffsetSize;
    private int indexSize;
    private int itemCount;
    private Item[] items;

    public ItemLocationBox(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
        short sizes = r.readShortValue();
        dataOffset += 2;
        offsetSize = sizes >> 12;
        lengthSize = sizes >> 8 & 0xf;
        baseOffsetSize = sizes >> 4 & 0xf;
        if (getVersion() == 1 || getVersion() == 2) {
            indexSize = sizes & 0xf;
        }
        if (getVersion() < 2) {
            itemCount = r.readShortValue();
            dataOffset += 2;
        } else if (getVersion() == 2) {
            itemCount = r.readIntValue();
            dataOffset += 4;
        }
        items = new Item[itemCount];
        for (int i = 0; i < itemCount; i++) {
            items[i] = new Item(this, r);
        }
    }

    public int getItemCount() {
        return itemCount;
    }

    public Item getItem(int index) {
        return items[index];
    }

    public Item getItemById(int itemId) {
        for (int i = 0; i < itemCount; i++) {
            Item item = getItem(i);
            if (item.getItemId() == itemId) return item;
        }
        return null;
    }
}

package org.lds.media.tiff;

import java.io.IOException;

public class DE {

    public static final int TYPE_BYTE = 1;
    public static final int TYPE_ASCII = 2;
    public static final int TYPE_SHORT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_RATIONAL = 5;
    public static final int TYPE_SBYTE = 6;
    public static final int TYPE_UNDEFINED = 7;
    public static final int TYPE_SSHORT = 8;
    public static final int TYPE_SLONG = 9;
    public static final int TYPE_SRATIONAL = 10;
    public static final int TYPE_FLOAT = 11;
    public static final int TYPE_DOUBLE = 12;

    private final int tag;
    private final int type;
    private final int count;
    private final byte[] valueOffset;

    DE(int tag, int type, int count, byte[] valueOffset) {
        this.tag = tag;
        this.type = type;
        this.count = count;
        this.valueOffset = valueOffset;
    }

    public int getTag() {
        return tag;
    }

    public int getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public int getTypeLength() throws IOException {
        switch (type) {
            case TYPE_BYTE:
            case TYPE_ASCII:
            case TYPE_SBYTE:
            case TYPE_UNDEFINED:
                return 1;
            case TYPE_SHORT:
            case TYPE_SSHORT:
                return 2;
            case TYPE_LONG:
            case TYPE_SLONG:
            case TYPE_FLOAT:
                return 4;
            case TYPE_RATIONAL:
            case TYPE_SRATIONAL:
            case TYPE_DOUBLE:
                return 8;
            default:
                return -1;
        }
    }

    public byte[] getValueOffset() {
        return valueOffset;
    }

}

package org.lds.media.tiff;

import org.lds.ByteArrayUtil;
import org.lds.Util;
import org.lds.io.AbstractSeekableBinaryReader;
import org.lds.math.Fraction;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IFD {
    private final TIFF tiff;
    private final int entryCount;
    private final Map<Integer, DE> entries;
    private final long offsetOfNextIFD;

    IFD(TIFF tiff, int entryCount, Map<Integer, DE> entries, long offsetOfNextIFD) {
        this.tiff = tiff;
        this.entryCount = entryCount;
        this.entries = entries;
        this.offsetOfNextIFD = offsetOfNextIFD;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public Map<Integer, DE> getEntries() {
        return entries;
    }

    public long getOffsetOfNextIFD() {
        return offsetOfNextIFD;
    }

    public Long getValueOffset(int tag) {
        DE entry = entries.get(tag);
        if (entry != null) {
            return ByteArrayUtil.toUnsignedIntValue(entry.getValueOffset(), tiff.getOrder());
        }
        return null;
    }

    public Object getValue(int tag) throws IOException {
        DE entry = entries.get(tag);
        if (entry == null) {
            return null;
        }
        int length = entry.getTypeLength() * entry.getCount();
        if (length <= 0) {
            return null;
        }
        if (length <= 4) {
            byte[] value = new byte[length];
            System.arraycopy(entry.getValueOffset(), 0, value, 0, length);
            return value;
        }
        long offset = ByteArrayUtil.toUnsignedIntValue(entry.getValueOffset(), tiff.getOrder());
        AbstractSeekableBinaryReader reader = tiff.getReader();
        reader.seek(tiff.getOffset() + offset);
        switch (entry.getType()) {
            case DE.TYPE_BYTE:
            case DE.TYPE_SBYTE:
            case DE.TYPE_UNDEFINED: {
                byte[] value = new byte[length];
                reader.read(value);
                return value;
            }
            case DE.TYPE_ASCII: {
                List<String> list = new ArrayList<String>();
                byte[] buf = new byte[entry.getCount()];
                int off = -1;
                for (int i = 0; i < entry.getCount(); i++) {
                    buf[++off] = reader.read();
                    if (buf[off] == 0) {
                        list.add(new String(buf, 0, off, reader.getEncoding().getCharset()));
                        off = -1;
                    }
                }
                return list.toArray(new String[0]);
            }
            case DE.TYPE_SHORT: {
                int[] array = new int[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = reader.readUnsignedShortValue();
                }
                return array;
            }
            case DE.TYPE_LONG: {
                long[] array = new long[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = reader.readUnsignedIntValue();
                }
                return array;
            }
            case DE.TYPE_RATIONAL: {
                Fraction[] array = new Fraction[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    long numerator = reader.readUnsignedIntValue();
                    long denominator = reader.readUnsignedIntValue();
                    array[i] = new Fraction(numerator, denominator);
                }
                return array;
            }
            case DE.TYPE_SSHORT: {
                short[] array = new short[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = reader.readShortValue();
                }
                return array;
            }
            case DE.TYPE_SLONG: {
                int[] array = new int[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = reader.readIntValue();
                }
                return array;
            }
            case DE.TYPE_SRATIONAL: {
                Fraction[] array = new Fraction[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    int numerator = reader.readIntValue();
                    int denominator = reader.readIntValue();
                    array[i] = new Fraction(numerator, denominator);
                }
                return array;
            }
            case DE.TYPE_FLOAT: {
                float[] array = new float[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = Float.intBitsToFloat(reader.readIntValue());
                }
                return array;
            }
            case DE.TYPE_DOUBLE: {
                double[] array = new double[entry.getCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = Double.longBitsToDouble(reader.readLongValue());
                }
                return array;
            }
            default:
                return null;
        }
    }

    public String getString(int tag) throws IOException {
        Object value = getValue(tag);
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            if (array.length > 0) {
                return array[0];
            }
        }
        return null;
    }

    public Long getLong(int tag) throws IOException {
        Object value = getValue(tag);
        if (!Util.isNullOrEmpty(value)) {
            return ((Number) Array.get(value, 0)).longValue();
        }
        return null;
    }

    public Fraction getRational(int tag) throws IOException {
        Object value = getValue(tag);
        if (value instanceof Fraction[]) {
            Fraction[] array = (Fraction[]) value;
            if (array.length > 0) {
                return array[0];
            }
        }
        return null;
    }

}

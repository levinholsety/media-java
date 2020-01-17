package org.lds.media.exif;

import org.lds.ByteArrayUtil;
import org.lds.io.AbstractSeekableBinaryReader;
import org.lds.io.FileBinaryReader;
import org.lds.math.Fraction;
import org.lds.media.isobmff.*;
import org.lds.media.tiff.IFD;
import org.lds.media.tiff.TIFF;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Exif {
    private static final int SOI = 0xffd8;
    private static final int APP1 = 0xffe1;
    private static final int SOS = 0xffda;
    private static final String MAKE_CANON = "Canon";
    private static final String MAKE_NIKON = "NIKON CORPORATION";
    private static final byte[] EXIF_MARKER = new byte[]{0x45, 0x78, 0x69, 0x66, 0x00, 0x00};
    private static final byte[] NIKON_TYPE_1_MARKER = new byte[]{0x4e, 0x69, 0x6b, 0x6f, 0x6e, 0x00, 0x01, 0x00};
    private static final byte[] NIKON_TYPE_2_A_MARKER = new byte[]{0x4e, 0x69, 0x6b, 0x6f, 0x6e, 0x00, 0x02, 0x10, 0x00, 0x00};
    private static final byte[] NIKON_TYPE_2_B_MARKER = new byte[]{0x4e, 0x69, 0x6b, 0x6f, 0x6e, 0x00, 0x02, 0x00, 0x00, 0x00};

    public static Exif parse(File file) throws IOException {
        return parse(new FileBinaryReader(file));
    }

    private static Exif parse(AbstractSeekableBinaryReader reader) throws IOException {
        reader.setOrder(ByteOrder.BIG_ENDIAN);
        long offset = findTIFFInJPEG(reader);
        if (offset == 0) {
            offset = findTIFFInHEIC(reader);
        }
        TIFF tiff = TIFF.parse(reader, offset);
        return tiff == null ? null : new Exif(tiff);
    }

    private static long findTIFFInJPEG(AbstractSeekableBinaryReader reader) throws IOException {
        reader.seek(0);
        if ((reader.readUnsignedShortValue()) == SOI) {
            while (true) {
                int id = reader.readUnsignedShortValue();
                int size = reader.readUnsignedShortValue();
                if (id == APP1) {
                    byte[] exifMarker = reader.readByteArray(6);
                    if (Arrays.equals(exifMarker, EXIF_MARKER)) {
                        return reader.getPosition();
                    }
                    break;
                } else if (id == SOS) {
                    break;
                } else {
                    reader.skip(size - 2);
                }
            }
        }
        return 0;
    }

    private static long findTIFFInHEIC(AbstractSeekableBinaryReader reader) throws IOException {
        reader.seek(0);
        Box box = Box.read(reader);
        if (box instanceof FileTypeBox) {
            FileTypeBox ftyp = (FileTypeBox) box;
            if ("heic".equals(ftyp.getMajorBrand())) {
                box = Box.read(reader);
                if (box instanceof MetaBox) {
                    MetaBox meta = (MetaBox) box;
                    int itemId = 0;
                    for (long offset = meta.getDataOffset(); offset < meta.getSize(); offset += box.getSize()) {
                        box = Box.read(reader);
                        if (box instanceof ItemInfoBox) {
                            ItemInfoBox iinf = (ItemInfoBox) box;
                            for (int i = 0; i < iinf.getEntryCount(); i++) {
                                ItemInfoEntry infe = (ItemInfoEntry) Box.read(reader);
                                if ("Exif".equals(infe.getItemType())) {
                                    itemId = infe.getItemId();
                                }
                                infe.skip();
                            }
                        } else if (box instanceof ItemLocationBox) {
                            ItemLocationBox iloc = (ItemLocationBox) box;
                            ItemLocationBox.Item item = iloc.getItemById(itemId);
                            ItemLocationBox.Extent extent = item.getExtent(0);
                            return extent.getExtentOffset() + 10;
                        } else {
                            box.skip();
                        }
                    }
                }
            }
        }
        return 0;
    }

    private final TIFF tiff;
    private final IFD ifd0;
    private final IFD ifd1;
    private final IFD exifIFD;
    private final IFD gpsInfoIFD;
    private final IFD makerNoteIFD;

    public Exif(TIFF tiff) throws IOException {
        this.tiff = tiff;
        ifd0 = tiff.readIFD(tiff.getOffsetOfIFD());
        ifd1 = getIFD1();
        exifIFD = getExifIFD();
        gpsInfoIFD = getGPSInfoIFD();
        makerNoteIFD = getMakerNoteIFD();
    }

    public String getMake() throws IOException {
        if (ifd0 == null) {
            return null;
        }
        return ifd0.getString(0x10f);
    }

    public String getModel() throws IOException {
        if (ifd0 == null) {
            return null;
        }
        return ifd0.getString(0x110);
    }

    public String getDateTime() throws IOException {
        if (ifd0 == null) {
            return null;
        }
        return ifd0.getString(0x132);
    }

    public String getExposureTime() throws IOException {
        if (exifIFD == null) {
            return null;
        }
        Fraction value = exifIFD.getRational(0x829a);
        if (value == null) {
            return null;
        }
        if (value.getNumerator() > value.getDenominator()) {
            return new DecimalFormat().format(value.floatValue());
        }
        return String.format("1/%d", value.reciprocal().longValue());
    }

    public Float getFNumber() throws IOException {
        if (exifIFD == null) {
            return null;
        }
        Fraction value = exifIFD.getRational(0x829d);
        if (value == null) {
            return null;
        }
        return value.floatValue();
    }

    public Double getGPSLatitude() throws IOException {
        if (gpsInfoIFD == null) {
            return null;
        }
        Object value = gpsInfoIFD.getValue(0x2);
        if (value instanceof Fraction[]) {
            return dms((Fraction[]) value);
        }
        return null;
    }

    public Double getGPSLongitude() throws IOException {
        if (gpsInfoIFD == null) {
            return null;
        }
        Object value = gpsInfoIFD.getValue(0x4);
        if (value instanceof Fraction[]) {
            return dms((Fraction[]) value);
        }
        return null;
    }

    public String getLens() throws IOException {
        String make = getMake();
        if (MAKE_CANON.equals(make)) {
            if (makerNoteIFD != null) {
                return makerNoteIFD.getString(0x95);
            }
        } else if (MAKE_NIKON.equals(make)) {
            if (makerNoteIFD != null) {
                Object value = makerNoteIFD.getValue(0x84);
                if (value instanceof Fraction[]) {
                    Fraction[] array = (Fraction[]) value;
                    if (array.length == 4) {
                        int v1 = array[0].intValue();
                        int v2 = array[1].intValue();
                        float v3 = array[2].floatValue();
                        float v4 = array[3].floatValue();
                        DecimalFormat df = new DecimalFormat();
                        return String.format("%smm f/%s",
                                v1 == v2 ? Integer.toString(v1) : String.format("%d-%d", v1, v2),
                                v3 == v4 ? df.format(v3) : String.format("%s-%s", df.format(v3), df.format(v4)));
                    }
                }
            }
        }
        return null;
    }

    private IFD getIFD1() throws IOException {
        if (ifd0.getOffsetOfNextIFD() <= 0) {
            return null;
        }
        return tiff.readIFD(ifd0.getOffsetOfNextIFD());
    }

    private IFD getExifIFD() throws IOException {
        if (ifd0 == null) {
            return null;
        }
        Long exifIFDPointer = ifd0.getValueOffset(0x8769);
        if (exifIFDPointer == null || exifIFDPointer <= 0) {
            return null;
        }
        return tiff.readIFD(exifIFDPointer);
    }

    private IFD getGPSInfoIFD() throws IOException {
        if (ifd0 == null) {
            return null;
        }
        Long gpsInfoIFDPointer = ifd0.getValueOffset(0x8825);
        if (gpsInfoIFDPointer == null || gpsInfoIFDPointer <= 0) {
            return null;
        }
        return tiff.readIFD(gpsInfoIFDPointer);
    }

    private IFD getMakerNoteIFD() throws IOException {
        if (exifIFD == null) {
            return null;
        }
        Long makerNoteOffset = exifIFD.getValueOffset(0x927c);
        if (makerNoteOffset != null && makerNoteOffset > 0) {
            String make = getMake();
            if (MAKE_CANON.equals(make)) {
                return tiff.readIFD(makerNoteOffset);
            } else if (MAKE_NIKON.equals(make)) {
                tiff.getReader().seek(makerNoteOffset);
                byte[] buf = tiff.getReader().readByteArray(10);
                if (ByteArrayUtil.startsWith(buf, NIKON_TYPE_1_MARKER)) {
                    return tiff.readIFD(makerNoteOffset + 8);
                } else if (ByteArrayUtil.startsWith(buf, NIKON_TYPE_2_A_MARKER) ||
                        ByteArrayUtil.startsWith(buf, NIKON_TYPE_2_B_MARKER)) {
                    TIFF makerNoteTIFF = TIFF.parse(tiff.getReader(), tiff.getOffset() + makerNoteOffset + 10);
                    if (makerNoteTIFF != null) {
                        return makerNoteTIFF.readIFD(makerNoteTIFF.getOffsetOfIFD());
                    }
                } else {
                    return tiff.readIFD(makerNoteOffset);
                }
            }
        }
        return null;
    }

    private double dms(Fraction[] array) {
        return array[0].doubleValue() + array[1].doubleValue() / 60 + array[2].doubleValue() / 3600;
    }
}

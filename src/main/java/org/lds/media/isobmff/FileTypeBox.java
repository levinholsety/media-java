package org.lds.media.isobmff;

import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class FileTypeBox extends Box {
    private final String majorBrand;
    private final int minorVersion;
    private final String[] compatibleBrands;

    public FileTypeBox(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
        majorBrand = r.readString(4);
        dataOffset += 4;
        minorVersion = r.readIntValue();
        dataOffset += 4;
        compatibleBrands = new String[(int) (this.getSize() - dataOffset) / 4];
        for (int i = 0; i < compatibleBrands.length; i++) {
            compatibleBrands[i] = r.readString(4);
            dataOffset += 4;
        }
    }

    public String getMajorBrand() {
        return majorBrand;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String[] getCompatibleBrands() {
        return compatibleBrands;
    }
}

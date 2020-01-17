package org.lds.media.isobmff;

import org.lds.io.AbstractSeekableBinaryReader;

import java.io.IOException;

public class MetaBox extends FullBox {
    public MetaBox(AbstractSeekableBinaryReader r, long size, String type) throws IOException {
        super(r, size, type);
    }

}

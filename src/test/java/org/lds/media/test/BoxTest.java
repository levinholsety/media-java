package org.lds.media.test;

import org.junit.Assert;
import org.junit.Test;
import org.lds.io.FileBinaryReader;
import org.lds.media.isobmff.*;

import java.nio.ByteOrder;

public class BoxTest {

    @Test
    public void testBox() throws Exception {
        FileBinaryReader reader = null;
        try {
            reader = new FileBinaryReader("D:\\Temp\\20180513_185604_iPhone 8 Plus_451.heic");
            reader.setOrder(ByteOrder.BIG_ENDIAN);
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
                                Assert.assertEquals(18757, extent.getExtentOffset());
                                Assert.assertEquals(2052, extent.getExtentLength());
                                return;
                            } else {
                                box.skip();
                            }
                        }
                    }
                }
            }
            Assert.fail();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}

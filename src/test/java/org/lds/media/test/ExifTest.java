package org.lds.media.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.lds.media.exif.Exif;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class ExifTest {

    @Test
    public void testExif() throws IOException {
        File dir = new File("D:\\Temp");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                int index = name.lastIndexOf('.');
                if (index < 0) {
                    return false;
                }
                String ext = name.substring(index).toLowerCase();
                return ".jpg".equals(ext) || ".heic".equals(ext) || ".nef".equals(ext);
            }
        });
        if (files == null) {
            return;
        }
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            Exif exif = Exif.parse(file);
            if (exif == null) {
                continue;
            }
            JSONObject obj = new JSONObject(true);
            obj.put("make", exif.getMake());
            obj.put("model", exif.getModel());
            obj.put("dateTime", exif.getDateTime());
            obj.put("exposureTime", exif.getExposureTime());
            obj.put("fNumber", exif.getFNumber());
            obj.put("gpsLatitude", exif.getGPSLatitude());
            obj.put("gpsLongitude", exif.getGPSLongitude());
            obj.put("lens", exif.getLens());
            System.out.println(JSON.toJSONString(obj, true));
        }
    }
}

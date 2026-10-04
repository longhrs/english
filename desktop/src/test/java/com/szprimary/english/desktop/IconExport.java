package com.szprimary.english.desktop;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/** 开发用：生成 jpackage 所需的 packaging/icon.png 与 packaging/icon.ico（内嵌 PNG 的 ICO）。 */
public final class IconExport {

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "packaging");
        dir.mkdirs();
        ImageIO.write(IconFactory.render(256), "png", new File(dir, "icon.png"));
        int[] sizes = {16, 24, 32, 48, 64, 128, 256};
        List<byte[]> pngs = new ArrayList<byte[]>();
        for (int size : sizes) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ImageIO.write(IconFactory.render(size), "png", buf);
            pngs.add(buf.toByteArray());
        }
        DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(dir, "icon.ico")));
        try {
            out.write(le16(0));
            out.write(le16(1));
            out.write(le16(sizes.length));
            int offset = 6 + 16 * sizes.length;
            for (int i = 0; i < sizes.length; i++) {
                int s = sizes[i] >= 256 ? 0 : sizes[i];
                out.writeByte(s);
                out.writeByte(s);
                out.writeByte(0);
                out.writeByte(0);
                out.write(le16(1));
                out.write(le16(32));
                out.write(le32(pngs.get(i).length));
                out.write(le32(offset));
                offset += pngs.get(i).length;
            }
            for (byte[] png : pngs) {
                out.write(png);
            }
        } finally {
            out.close();
        }
    }

    private static byte[] le16(int v) {
        return new byte[]{(byte) v, (byte) (v >> 8)};
    }

    private static byte[] le32(int v) {
        return new byte[]{(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)};
    }
}

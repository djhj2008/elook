package com.example.demo.elook;

import java.io.OutputStream;

public class BMPWriter {

    private int width;
    private int height;

    public BMPWriter(int width ,int height){
        this.width = width;
        this.height = height;
    }



    public void savebmpTop(OutputStream ops) throws Exception {
        ops.write('B');
        ops.write('M');
        int size = 14 + 40 + height * width + 256 *4;
        // 位图文件的大小
        int offset = 14 + 40 + 256*4;
        writeInt(ops, size);
        writeShort(ops, (short) 0);
        writeShort(ops, (short) 0);
        writeInt(ops, 54+256*4);
    }

    /**
     * 保存BMP图片位图信息头部分的方法
     *
     * @param ops
     *            输出流
     * @throws Exception
     *             异常
     */
    public void savebmpInfo(OutputStream ops) throws Exception {
        writeInt(ops, 40);
        writeInt(ops, width);
        writeInt(ops, height);
        writeShort(ops, (short) 1);
        writeShort(ops, (short) 8);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
    }

    /**
     * 保存BMP图片位图数据部分的方法
     *
     * @param ops
     *            输出流
     * @throws Exception
     *             异常
     */
    public void savebmpDate(OutputStream ops, byte[] image,int offset,int len) throws Exception {
        byte[] panel = new byte[4];
        for(int i=0;i<256;i++) {
            panel[0]=(byte)i;
            panel[1]=(byte)i;
            panel[2]=(byte)i;
            panel[3]=(byte)0xff;
            ops.write(panel);
        }
        ops.write(image,offset,len);
    }

    /**
     * 由于写入的是字节，所以要将整型进行转换
     *
     * @param ops
     *            输出流
     * @param t
     *            整型值
     * @throws Exception
     *             异常
     */
    public void writeInt(OutputStream ops, int t) throws Exception {
        int a = (t >> 24) & 0xff;
        int b = (t >> 16) & 0xff;
        int c = (t >> 8) & 0xff;
        int d = t & 0xff;
        ops.write(d);
        ops.write(c);
        ops.write(b);
        ops.write(a);
    }

    /**
     * 由于写入的是字节，所以要将颜色值进行转换
     *
     * @param ops
     *            输出流
     * @param t
     *            整型的颜色值
     * @throws Exception
     *             异常
     */
    public void writeColor(OutputStream ops, int t) throws Exception {
        int b = (t >> 16) & 0xff;
        int c = (t >> 8) & 0xff;
        int d = t & 0xff;
        ops.write(d);
        ops.write(c);
        ops.write(b);
    }

    /**
     * 由于写入的是字节，所以要将短整型进行转换
     *
     * @param ops
     *            输出流
     * @param t
     *            短整形值
     * @throws Exception
     *             异常
     */
    public void writeShort(OutputStream ops, short t) throws Exception {
        int c = (t >> 8) & 0xff;
        int d = t & 0xff;
        ops.write(d);
        ops.write(c);
    }
}

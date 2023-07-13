package com.cnpc.epai.core.workscene.pojo;
/**
 * @Title: rfConverter.java
 * @Package com.cnpc.epai.common.seismicVolume.service;
 * @Description: 通用格式转换类类文件
 * @author 张伟强
 * @date 2018.5.23
 * {修改记录：修改人、修改时间、修改内容等}
 * 备注:
 */

/**
 * @Title rfConverter
 * @author 张伟强
 * @version 1.0.0
 * @Description: 通用专业处理算法
 */
public class rfConverter {
    /**
     * byte 转float
     *
     * @param b
     * @return float
     */
    public static float byteArrayToFloat(byte[] b) {
        // 4 bytes
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return Float.intBitsToFloat(accum);
    }

    /**
     * byte 转int
     *
     * @param b
     * @return float
     */
    public static int byteArrayToInt(byte[] b) {
        // 4 bytes
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return accum;
    }
    /**
     * byte 转double
     *
     * @param b
     * @return double
     */
    public static double byteArrayToDouble(byte[] b) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (b[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }
    /**
     * 将int转为低字节在前，高字节在后的byte数组
     * @param n int
     * @return byte[]
     */
    public static byte[] InttoLH(int n) {

        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
    /**
     * 将int转为低字节在前，高字节在后的byte数组
     * @param d  double
     * @return byte[]
     */
   public static byte[] DoubletoLH(double d) {
       byte[] b=new byte[8];

        long l=Double.doubleToLongBits(d);
       b[0] = (byte) (0xff);
       b[1] = (byte) (l>> 8 & 0xff);
       b[2] = (byte) (l >> 16 & 0xff);
       b[3] = (byte) (l >> 24 & 0xff);
       b[4] = (byte) (l>> 32 & 0xff);
       b[5] = (byte) (l >> 40 & 0xff);
       b[6] = (byte) (l >> 48 & 0xff);
       b[7] = (byte) (l>> 56 & 0xff);

       return b;

    }


}

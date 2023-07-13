package com.cnpc.epai.core.worktask.pojo;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 数据对象转换
 * @author Administrator
 */
public class DataStream {

//    public static List<SEISSeismicTrace> getSeisTraces(String filePath, SEISSeismic2DTrace data) {
//        List<SEISSeismicTrace> tarceL = new ArrayList<SEISSeismicTrace>();
//        FileInputStream rf = null;
//        try {//解析地震数据体
//             /*
//             * 二进制字节流结构
//             *  a) 第1个4个字节 代表 数据格式类型 Format
//             *  b) 第2个4个字节 代表 数据类型的长度  L
//             *  c) 然后后面的 4 个字节 代表 tracenumber   地震道数据量
//             *  d) 然后后面的 4 个字节 代表 sample num 样品点数量
//             *  e) 然后后面的 4 个字节代表第一道 traceid  道集号
//             *  f) 然后后面的 4*sample num 个字节 代表 第一道 的 tracedata 数据
//             *
//             *  按照   e f   的循环读取后续数据
//             */
//            rf = new FileInputStream(filePath);
//
//
//            byte buffer[] = new byte[4];
//
//            rf.read(buffer);
//            int formatT;
//            formatT = Format.byteArrayToInt(buffer);
//
//            rf.read(buffer);
//            int longLength = Format.byteArrayToInt(buffer);
//
//            int traceNumber = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            traceNumber = Format.byteArrayToInt(buffer);
//
//            int sampleNumber = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            sampleNumber = Format.byteArrayToInt(buffer);
//
//            for (int i = 0; i < traceNumber; i++) {
//
//                int trace = 0;
//                buffer = new byte[4];
//                rf.read(buffer);
//                trace = Format.byteArrayToInt(buffer);
//
//                float[] traceData = new float[sampleNumber];
//                for (int m = 0; m < sampleNumber; m++) {
//                    buffer = new byte[longLength];
//                    rf.read(buffer);
//                    if (formatT == 8) {
//                        int cvalue = buffer[0] & 0xFF;
//                        float value = (float) cvalue;
//                        traceData[m] = value;
//                    } else if (formatT == 16) {
//                        short svalue = Format.byteToShort(buffer);
//                        float value = (float) svalue;
//                        traceData[m] = value;
//                    } else if (formatT == 32) {
//                        long lvalue = Format.byteToLong(buffer);
//                        float value = (float) lvalue;
//                        traceData[m] = value;
//                    } else {
//                        float fvalue = Format.byteArrayToFloat(buffer);
//                        traceData[m] = fvalue;
//                    }
//                }
//
//                SEISSeismicTrace traceInfo = new SEISSeismicTrace();
//
//                traceInfo.setTraceID(i + 1);
//                traceInfo.setCDP(trace);
//
//
//                if (data != null) {
//                    if (trace == 2066) {
//                        String stop = "";
//                    }
//                    ShotPoint sp = getShotPoint(data.getMappings(), data.getShotPoints(), trace);
//                    if (sp != null) {
//                        if (sp.getCoordinate() != null) {
//                            traceInfo.setCdp_X(sp.getCoordinate().getXCoordinate());
//                            traceInfo.setCdp_Y(sp.getCoordinate().getYCoordinate());
//                            traceInfo.setSample_intervall(data.getSamplesInterval());
//                            traceInfo.setSamplesNum(data.getSampleNUM());
//                            traceInfo.setShortPoint(sp.getShotPoint());
//                        }
//                    }
//                }
//
//
//
//                UnitValueSeriesF tarceData = new UnitValueSeriesF();
//                tarceData.setValues(traceData);
//                traceInfo.setData(tarceData);
//
//                tarceL.add(traceInfo);
//
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException("解析地震数据文件错误 \r\n" + ex.getMessage());
//        } catch (IOException ex) {
//            Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException("解析地震数据文件错误 \r\n" + ex.getMessage());
//        } finally {
//            if (rf != null) {
//                try {
//                    rf.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//                    throw new RuntimeException("关闭地震数据文件错误 \r\n" + ex.getMessage());
//                }
//            }
//        }
//        return tarceL;
//    }
//
//    public static List<SEISSeismicTrace> getSeisTracesGE(String filePath) {
//        List<SEISSeismicTrace> tarceL = new ArrayList<SEISSeismicTrace>();
//        FileInputStream rf = null;
//        try {
//
//            //解析地震数据体
//             /*
//             * 二进制字节流结构
//             *  a) 第一个4个字节 代表 long类型的长度  L
//             *  b) 然后后面的 L 个字节 代表 tracenumber   地震道数据量
//             *  c) 然后后面的 L 个字节 代表 第一道 的 header 长度 H
//             *  d) 然后后面的 H*4 个字节代表第一道 的 header 数据(int 类型)
//             *  e) 然后后面的 L 个字节 代表 第一道 的 tracedata 长度 T
//             *  f) 然后后面的 T*4 个字节代表第一道 的 tracedata 数据(float 类型)
//             *  按照 c d e f 的循环读取后续数据
//             */
//            rf = new FileInputStream(filePath);
//
//            byte buffer[] = new byte[4];
//            rf.read(buffer);
//            int longLength = Format.byteArrayToInt(buffer);
//
//            int traceNumber = 0;
//            buffer = new byte[longLength];
//            rf.read(buffer);
//            long tN = Format.byteToLong(buffer);
//            traceNumber = (int) Format.byteToLong(buffer);
//            for (int i = 0; i < traceNumber; i++) {
//
//                int headerLength = 0;
//                buffer = new byte[longLength];
//                rf.read(buffer);
//                headerLength = (int) Format.byteToLong(buffer);
//
//                int[] traceHeaderO = new int[headerLength];
//                for (int m = 0; m < headerLength; m++) {
//                    buffer = new byte[4];
//                    rf.read(buffer);
//                    int headerD = Format.byteArrayToInt(buffer);
//                    traceHeaderO[m] = headerD;
//                }
//
//                SEISSeismicTrace tarce = new SEISSeismicTrace();
//                tarce.setTraceID(i);
//                tarce.setCDP(traceHeaderO[13]);
//                tarce.setCdp_X(traceHeaderO[34]);
//                tarce.setCdp_Y(traceHeaderO[35]);
//                tarce.setShortPoint(traceHeaderO[17]);
//                int dataLength = 0;
//                buffer = new byte[longLength];
//                rf.read(buffer);
//                dataLength = (int) Format.byteToLong(buffer);
//
//                float[] traceDataO = new float[dataLength];
//                for (int m = 0; m < dataLength; m++) {
//                    buffer = new byte[4];
//                    rf.read(buffer);
//                    float data = Format.byteArrayToFloat(buffer);
//                    traceDataO[m] = data;
//                }
//
//
//                UnitValueSeriesF tarceData = new UnitValueSeriesF();
//                tarceData.setValues(traceDataO);
//                tarce.setData(tarceData);
//
//                tarceL.add(tarce);
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return tarceL;
//
//    }
//
//    public static List<SEISSeismicTrace> getSeis3DTraces(String filePath) {
//        List<SEISSeismicTrace> tarceL = new ArrayList<SEISSeismicTrace>();
//        FileInputStream rf = null;
//        try {//解析地震数据体
//             /*
//             * 二进制字节流结构
//             *  a) 第1个4个字节 代表 数据类型 Format  int8  float8 等
//             *  b) 第2个4个字节 代表 数据类型的长度  longSize
//             *  c) 第3个4个字节 代表 最小 inline 值
//             *  d) 第4个4个字节 代表 最大 inline 值
//             *  e) 第5个4个字节 代表 最小 crossline 值
//             *  f) 第6个4个字节 代表 最大 crossline 值
//             *  g) 第7个4个字节 代表 inline num
//             *  h) 第8个4个字节 代表 crossline num
//             *  i) 第9个4个字节 代表 sample num 样品点数量
//             *  j) 然后后面的 4个字节代表第一道 数据 inline 值
//             *  k) 然后后面的 4个字节代表第一道 数据  crossline 值
//             *  l) 然后后面的 longSize*sample num 个字节 代表 第一道 的 tracedata 数据
//             *
//             *  按照   j k l  的循环读取后续数据
//             */
//            rf = new FileInputStream(filePath);
//
//
//            byte buffer[] = new byte[4];
//
//            rf.read(buffer);
//            int formatT;
//            formatT = Format.byteArrayToInt(buffer);
//
//            rf.read(buffer);
//            int longLength = Format.byteArrayToInt(buffer);
//
//            int ixMin = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            ixMin = Format.byteArrayToInt(buffer);
//
//            int ixMax = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            ixMax = Format.byteArrayToInt(buffer);
//
//            int iyMin = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            iyMin = Format.byteArrayToInt(buffer);
//
//            int iyMax = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            iyMax = Format.byteArrayToInt(buffer);
//
//            int inlineNumber = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            inlineNumber = Format.byteArrayToInt(buffer);
//
//            int crosslineNumber = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            crosslineNumber = Format.byteArrayToInt(buffer);
//
//            int sampleNumber = 0;
//            buffer = new byte[4];
//            rf.read(buffer);
//            sampleNumber = Format.byteArrayToInt(buffer);
//
//
//
//            int ix, iy;
//            int traceID = 0;
//            for (ix = ixMin; ix < ixMax + 1; ix++) {
//                for (iy = iyMin; iy < iyMax + 1; iy++) {
//
//                    buffer = new byte[4];
//                    rf.read(buffer);
//                    int inline = Format.byteArrayToInt(buffer);
//
//                    buffer = new byte[4];
//                    rf.read(buffer);
//                    int crossline = Format.byteArrayToInt(buffer);
//
//                    float[] traceData = new float[sampleNumber];
//                    for (int m = 0; m < sampleNumber; m++) {
//                        buffer = new byte[longLength];
//                        rf.read(buffer);
//                        if (formatT == 8) {
//                            int cvalue = buffer[0] & 0xFF;
//                            float value = (float) cvalue;
//                            traceData[m] = value;
//                        } else if (formatT == 16) {
//                            short svalue = Format.byteToShort(buffer);
//                            float value = (float) svalue;
//                            traceData[m] = value;
//                        } else if (formatT == 32) {
//                            long lvalue = Format.byteToLong(buffer);
//                            float value = (float) lvalue;
//                            traceData[m] = value;
//                        } else {
//                            float fvalue = Format.byteArrayToFloat(buffer);
//                            traceData[m] = fvalue;
//                        }
//                    }
//
//                    SEISSeismicTrace traceInfo = new SEISSeismicTrace();
//
//                    traceInfo.setTraceID(traceID);
//                    traceInfo.setCDP(inline);
//                    traceInfo.setIn_line_num(inline);
//                    traceInfo.setCross_line_num(crossline);
//
//                    UnitValueSeriesF tarceData = new UnitValueSeriesF();
//                    tarceData.setValues(traceData);
//                    traceInfo.setData(tarceData);
//
//                    tarceL.add(traceInfo);
//                    traceID++;
//                }
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException("解析地震数据文件错误 文件不存在 \r\n" + ex.getMessage());
//        } catch (IOException ex) {
//            Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException("解析地震数据文件错误 数据流读取错误 \r\n" + ex.getMessage());
//        } finally {
//            if (rf != null) {
//                try {
//                    rf.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(DataStream.class.getName()).log(Level.SEVERE, null, ex);
//                    throw new RuntimeException("关闭地震数据文件错误 \r\n" + ex.getMessage());
//                }
//            }
//        }
//        return tarceL;
//    }
//
//    public static ShotPoint getShotPoint(List<Mapping> mappings, List<ShotPoint> shotpoints, int trace) {
//        try {
//            if (trace == 2065) {
//                String stop = "";
//            }
//            ShotPoint sp = new ShotPoint();
//            float shotPoint = 0;
//            if (mappings != null && shotpoints != null) {
//                if (mappings.size() > 1) {
//                    float rat = (mappings.get(0).getShotPoint() - mappings.get(1).getShotPoint())
//                            / (mappings.get(0).getTraceID() - mappings.get(1).getTraceID());
//                    rat = Format.getFloat(rat, 1, 4);
//                    shotPoint = mappings.get(0).getShotPoint() - ((mappings.get(0).getTraceID() - trace) * rat);
//
//                } else {
//                    throw new RuntimeException("解析导航数据错误，导航数据中炮点数据与道集号关系少于2列");
//                }
//                shotPoint = Format.getFloat(shotPoint, 1, 4);
//                sp.setShotPoint(shotPoint);
//                double x = 0;
//                double y = 0;
//                if (shotpoints.size() > 1) {
//                    int spSize = shotpoints.size();
//                    if (shotpoints.get(0).getShotPoint() < shotpoints.get(1).getShotPoint()) {
//                        if (shotPoint == 2014) {
//                            String stop = "";
//                        } else if (shotPoint == 2024) {
//                            String stop = "";
//                        }
//                        if (shotPoint < shotpoints.get(0).getShotPoint()) {
//                            x = shotpoints.get(0).getCoordinate().getXCoordinate()
//                                    + ((shotPoint - shotpoints.get(0).getShotPoint())
//                                    * (shotpoints.get(0).getCoordinate().getXCoordinate()
//                                    - shotpoints.get(1).getCoordinate().getXCoordinate()))
//                                    / (shotpoints.get(0).getShotPoint() - shotpoints.get(1).getShotPoint());
//                            y = shotpoints.get(0).getCoordinate().getYCoordinate()
//                                    + ((shotPoint - shotpoints.get(0).getShotPoint())
//                                    * (shotpoints.get(0).getCoordinate().getYCoordinate()
//                                    - shotpoints.get(1).getCoordinate().getYCoordinate()))
//                                    / (shotpoints.get(0).getShotPoint() - shotpoints.get(1).getShotPoint());
//                            Coordinate coord = new Coordinate();
//                            coord.setXCoordinate(x);
//                            coord.setYCoordinate(y);
//                            sp.setCoordinate(coord);
//                        } else if (shotPoint == shotpoints.get(0).getShotPoint()) {
//                            sp.setCoordinate(shotpoints.get(0).getCoordinate());
//                        } else {
//                            if (shotPoint == shotpoints.get(spSize - 1).getShotPoint()) {
//                                sp.setCoordinate(shotpoints.get(spSize - 1).getCoordinate());
//                            } else if (shotPoint > shotpoints.get(spSize - 1).getShotPoint()) {
//                                x = shotpoints.get(spSize - 1).getCoordinate().getXCoordinate()
//                                        - ((shotpoints.get(spSize - 1).getShotPoint() - shotPoint)
//                                        * (shotpoints.get(spSize - 2).getCoordinate().getXCoordinate()
//                                        - shotpoints.get(spSize - 1).getCoordinate().getXCoordinate()))
//                                        / (shotpoints.get(spSize - 2).getShotPoint() - shotpoints.get(spSize - 1).getShotPoint());
//                                y = shotpoints.get(spSize - 1).getCoordinate().getYCoordinate()
//                                        - ((shotpoints.get(spSize - 1).getShotPoint() - shotPoint)
//                                        * (shotpoints.get(spSize - 2).getCoordinate().getYCoordinate()
//                                        - shotpoints.get(spSize - 1).getCoordinate().getYCoordinate()))
//                                        / (shotpoints.get(spSize - 2).getShotPoint() - shotpoints.get(spSize - 1).getShotPoint());
//                                Coordinate coord = new Coordinate();
//                                coord.setXCoordinate(x);
//                                coord.setYCoordinate(y);
//                                sp.setCoordinate(coord);
//                            } else {
//                                int startSP = 0;
//                                for (int i = 0; i < spSize - 1; i++) {
//                                    if (shotPoint == shotpoints.get(i).getShotPoint()) {
//                                        sp.setCoordinate(shotpoints.get(i).getCoordinate());
//                                        break;
//                                    } else {
//                                        if (shotPoint > shotpoints.get(i).getShotPoint()
//                                                && shotPoint < shotpoints.get(i + 1).getShotPoint()) {
//                                            x = shotpoints.get(i + 1).getCoordinate().getXCoordinate()
//                                                    + ((shotPoint - shotpoints.get(i + 1).getShotPoint())
//                                                    * (shotpoints.get(i).getCoordinate().getXCoordinate()
//                                                    - shotpoints.get(i + 1).getCoordinate().getXCoordinate()))
//                                                    / (shotpoints.get(i).getShotPoint() - shotpoints.get(i + 1).getShotPoint());
//                                            y = shotpoints.get(i + 1).getCoordinate().getYCoordinate()
//                                                    + ((shotPoint - shotpoints.get(i + 1).getShotPoint())
//                                                    * (shotpoints.get(i).getCoordinate().getYCoordinate()
//                                                    - shotpoints.get(i + 1).getCoordinate().getYCoordinate()))
//                                                    / (shotpoints.get(i).getShotPoint() - shotpoints.get(i + 1).getShotPoint());
//                                            Coordinate coord = new Coordinate();
//                                            coord.setXCoordinate(x);
//                                            coord.setYCoordinate(y);
//                                            sp.setCoordinate(coord);
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        if (shotPoint > shotpoints.get(0).getShotPoint()) {
//                            x = shotpoints.get(0).getCoordinate().getXCoordinate()
//                                    + ((shotPoint - shotpoints.get(0).getShotPoint())
//                                    * (shotpoints.get(0).getCoordinate().getXCoordinate()
//                                    - shotpoints.get(1).getCoordinate().getXCoordinate()))
//                                    / (shotpoints.get(0).getShotPoint() - shotpoints.get(1).getShotPoint());
//                            y = shotpoints.get(0).getCoordinate().getYCoordinate()
//                                    + ((shotPoint - shotpoints.get(0).getShotPoint())
//                                    * (shotpoints.get(0).getCoordinate().getYCoordinate()
//                                    - shotpoints.get(1).getCoordinate().getYCoordinate()))
//                                    / (shotpoints.get(0).getShotPoint() - shotpoints.get(1).getShotPoint());
//                            Coordinate coord = new Coordinate();
//                            coord.setXCoordinate(x);
//                            coord.setYCoordinate(y);
//                            sp.setCoordinate(coord);
//                        } else if (shotPoint == shotpoints.get(0).getShotPoint()) {
//                            sp.setCoordinate(shotpoints.get(0).getCoordinate());
//                        } else {
//                            if (shotPoint == shotpoints.get(spSize - 1).getShotPoint()) {
//                                sp.setCoordinate(shotpoints.get(spSize - 1).getCoordinate());
//                            } else if (shotPoint < shotpoints.get(spSize - 1).getShotPoint()) {
//                                x = shotpoints.get(spSize - 1).getCoordinate().getXCoordinate()
//                                        - ((shotpoints.get(spSize - 1).getShotPoint() - shotPoint)
//                                        * (shotpoints.get(spSize - 2).getCoordinate().getXCoordinate()
//                                        - shotpoints.get(spSize - 1).getCoordinate().getXCoordinate()))
//                                        / (shotpoints.get(spSize - 2).getShotPoint() - shotpoints.get(spSize - 1).getShotPoint());
//                                y = shotpoints.get(spSize - 1).getCoordinate().getYCoordinate()
//                                        - ((shotpoints.get(spSize - 1).getShotPoint() - shotPoint)
//                                        * (shotpoints.get(spSize - 2).getCoordinate().getYCoordinate()
//                                        - shotpoints.get(spSize - 1).getCoordinate().getYCoordinate()))
//                                        / (shotpoints.get(spSize - 2).getShotPoint() - shotpoints.get(spSize - 1).getShotPoint());
//                                Coordinate coord = new Coordinate();
//                                coord.setXCoordinate(x);
//                                coord.setYCoordinate(y);
//                                sp.setCoordinate(coord);
//                            } else {
//                                int startSP = 0;
//                                for (int i = 0; i < spSize - 2; i++) {
//                                    if (shotPoint == shotpoints.get(i).getShotPoint()) {
//                                        sp.setCoordinate(shotpoints.get(i).getCoordinate());
//                                        break;
//                                    } else {
//                                        if (shotPoint < shotpoints.get(i).getShotPoint()
//                                                && shotPoint > shotpoints.get(i + 1).getShotPoint()) {
//                                            x = shotpoints.get(i + 1).getCoordinate().getXCoordinate()
//                                                    + ((shotPoint - shotpoints.get(i + 1).getShotPoint())
//                                                    * (shotpoints.get(i).getCoordinate().getXCoordinate()
//                                                    - shotpoints.get(i + 1).getCoordinate().getXCoordinate()))
//                                                    / (shotpoints.get(i).getShotPoint() - shotpoints.get(i + 1).getShotPoint());
//                                            y = shotpoints.get(i + 1).getCoordinate().getYCoordinate()
//                                                    + ((shotPoint - shotpoints.get(i + 1).getShotPoint())
//                                                    * (shotpoints.get(i).getCoordinate().getYCoordinate()
//                                                    - shotpoints.get(i + 1).getCoordinate().getYCoordinate()))
//                                                    / (shotpoints.get(i).getShotPoint() - shotpoints.get(i + 1).getShotPoint());
//                                            Coordinate coord = new Coordinate();
//                                            coord.setXCoordinate(x);
//                                            coord.setYCoordinate(y);
//                                            sp.setCoordinate(coord);
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    throw new RuntimeException("解析导航数据错误，导航数据中炮点坐标数据少于2列");
//                }
//
//            } else {
//                throw new RuntimeException("解析导航数据错误，导航数据不存在");
//            }
//            return sp;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析导航数据错误:" + ex.getMessage());
//        }
//    }
//
//    public static void download(String urlString, String filename) throws Exception {
//        // 构造URL
//        URL url = new URL(urlString);
//        // 打开连接
//        URLConnection con = url.openConnection();
//        // 输入流
//        InputStream is = con.getInputStream();
//        // 1K的数据缓冲
//        byte[] bs = new byte[1024];
//        // 读取到的数据长度
//        int len;
//        // 输出的文件流
//        OutputStream os = new FileOutputStream(filename);
//        // 开始读取
//        while ((len = is.read(bs)) != -1) {
//            os.write(bs, 0, len);
//        }
//        // 完毕，关闭所有链接
//        os.close();
//        is.close();
//    }
//
//    public static void copy(File file, File toFile) throws Exception {
//        byte[] b = new byte[1024];
//        int a;
//        FileInputStream fis;
//        FileOutputStream fos;
//        if (file.isDirectory()) {
//            String filepath = file.getAbsolutePath();
//            filepath = filepath.replaceAll("\\\\", "/");
//            String toFilepath = toFile.getAbsolutePath();
//            toFilepath = toFilepath.replaceAll("\\\\", "/");
//            int lastIndexOf = filepath.lastIndexOf("/");
//            toFilepath = toFilepath + filepath.substring(lastIndexOf, filepath.length());
//            File copy = new File(toFilepath);
//            //复制文件夹
//            if (!copy.exists()) {
//                copy.mkdir();
//            }
//            //遍历文件夹
//            for (File f : file.listFiles()) {
//                copy(f, copy);
//            }
//        } else {
//            if (toFile.isDirectory()) {
//                String filepath = file.getAbsolutePath();
//                filepath = filepath.replaceAll("\\\\", "/");
//                String toFilepath = toFile.getAbsolutePath();
//                toFilepath = toFilepath.replaceAll("\\\\", "/");
//                int lastIndexOf = filepath.lastIndexOf("/");
//                toFilepath = toFilepath + filepath.substring(lastIndexOf, filepath.length());
//
//                //写文件
//                File newFile = new File(toFilepath);
//                fis = new FileInputStream(file);
//                fos = new FileOutputStream(newFile);
//                while ((a = fis.read(b)) != -1) {
//                    fos.write(b, 0, a);
//                }
//            } else {
//                //写文件
//                fis = new FileInputStream(file);
//                fos = new FileOutputStream(toFile);
//                while ((a = fis.read(b)) != -1) {
//                    fos.write(b, 0, a);
//                }
//            }
//
//        }
//    }
//
//    public static Coordinate getCoordinateFromBinSetGrid(Coordinate LL, Coordinate RU,
//            double RUInline, double LLInline, double RUTrace, double LLTrace, double inline, double trace) {
//        try {
//            Coordinate coordinate = new Coordinate();
//
//            double x = LL.getXCoordinate() + ((RU.getXCoordinate() - LL.getXCoordinate()) * (inline - LLInline)) / (RUInline - LLInline);
//            double y = RU.getYCoordinate() - ((RU.getYCoordinate() - LL.getYCoordinate()) * (RUTrace - trace)) / (RUTrace - LLTrace);
//            x = Format.getDouble(x, 1, 4);
//            y = Format.getDouble(y, 1, 4);
//            coordinate.setXCoordinate(x);
//            coordinate.setYCoordinate(y);
//
//            return coordinate;
//        } catch (Exception ex) {
//            throw new RuntimeException("根据网格获取点坐标错误" + ex.getMessage());
//        }
//    }
//
//    public static List<IntpPoint> getHorizon3DDataFromFile(String filename) {
//        try {
//            /* 读取三维解释层位二进制文件
//             * 第一个四个字节：longSize 代表 long类型长度，类型int
//             * 第二到第七个 longSize字节：分别代表 最小inline、最大inline、inline步长、最小trace、最大trace、trace步长，类型为long
//             * 循环读取到文件末尾
//             *  {
//             *      longSize字节：index,代表顺序编号，类型为long
//             *      longSize字节：inline，代表inline号，类型为long
//             *      longSize字节：trace ，代表traceid，类型为long
//             *      8个字节：x, 代表x坐标，类型为 double
//             *      8个字节：y，代表y坐标，类型为 double
//             *      4个字节：z，代表z值，类型为 float
//             *  }
//             *
//             */
//            List<IntpPoint> pointDatas = new ArrayList<IntpPoint>();
//            FileInputStream rf = null;
//            rf = new FileInputStream(filename);
//            byte buffer[] = new byte[4];
//            rf.read(buffer);
//            int longSize = Format.byteArrayToInt(buffer);
//            buffer = new byte[longSize * 6];
//            rf.read(buffer);
//            buffer = new byte[longSize];
//            int len = -1;
//            while ((len = rf.read(buffer)) != -1) {
//                long index = Format.byteToLong(buffer);
//                buffer = new byte[longSize];
//                rf.read(buffer);
//                long inline = Format.byteToLong(buffer);
//                buffer = new byte[longSize];
//                rf.read(buffer);
//                long trace = Format.byteToLong(buffer);
//                buffer = new byte[8];
//                rf.read(buffer);
//                double x = Format.bytesToDouble(buffer);
//                buffer = new byte[8];
//                rf.read(buffer);
//                double y = Format.bytesToDouble(buffer);
//                buffer = new byte[4];
//                rf.read(buffer);
//                float z = Format.byteArrayToFloat(buffer);
//                buffer = new byte[longSize];
//
//                IntpPoint point = new IntpPoint();
//                point.setOrder(index);
//                point.setInline(inline);
//                point.setCmp(trace);
//                Coordinate coordinate = new Coordinate();
//                coordinate.setXCoordinate(x);
//                coordinate.setYCoordinate(y);
//                point.setCoordinate(coordinate);
//                UnitValueF zF = new UnitValueF();
//                zF.setValue(z);
//                point.setZ(zF);
//                pointDatas.add(point);
//            }
//            rf.close();
//            return pointDatas;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析三维层位散点数据错误" + ex.getMessage());
//        }
//    }
//
//    public static void writeHorizon3DDataListToFile(List<IntpPoint> data, String fileName, long minInline, long maxInline,
//            long linInc, long minTrace, long maxTrace, long traceInc) {
//        try {
//            /*写三维解释层位二进制文件
//             * 第一个四个字节：longSize 代表 long类型长度，类型int
//             * 第二到第七个 longSize字节：分别代表 最小inline、最大inline、inline步长、最小trace、最大trace、trace步长，类型为long
//             * 循环读取到文件末尾
//             *  {
//             *      longSize字节：index,代表顺序标号，类型为long
//             *      longSize字节：inline，代表inline号，类型为long
//             *      longSize字节：trace ，代表traceid，类型为long
//             *      8个字节：x, 代表x坐标，类型为 double
//             *      8个字节：y，代表y坐标，类型为 double
//             *      4个字节：z，代表z值，类型为 float
//             *  }
//             *
//             */
//            if (data != null) {
//                DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
//                int longSize = Long.SIZE / 8;
//                int totalSamp = data.size();
//
//                out.write(Format.intToByte(longSize));
//                out.write(Format.longToBytes(minInline));
//                out.write(Format.longToBytes(maxInline));
//                out.write(Format.longToBytes(linInc));
//                out.write(Format.longToBytes(minTrace));
//                out.write(Format.longToBytes(maxTrace));
//                out.write(Format.longToBytes(traceInc));
//
//
//                for (int i = 0; i < data.size(); i++) {
//                    out.write(Format.longToBytes(data.get(i).getOrder()));
//                    out.write(Format.longToBytes(data.get(i).getInline()));
//                    out.write(Format.longToBytes(data.get(i).getCmp()));
//
//                    out.write(Format.doubleToBytes(data.get(i).getCoordinate().getXCoordinate()));
//                    out.write(Format.doubleToBytes(data.get(i).getCoordinate().getYCoordinate()));
//                    out.write(Format.floatToByte(data.get(i).getZ().getValue()));
//                }
//                out.close();
//            } else {
//                throw new RuntimeException("被写入的三维地震散点数据为空");
//            }
//        } catch (Exception ex) {
//            throw new RuntimeException("将三维地震解释散点数据写入文件失败：" + ex.getMessage());
//        }
//    }
//
//    public static List<IntpPoint> getHorizon2DDataListFromFile(String filename) {
//        try {
//            /* 读取二维解释层位二进制文件
//             * 第一个四个字节：pointNum 代表 层位点个数  int
//             * 循环 pointNum 次 到文件末尾
//             * {
//             *      4个字节： cmp  代表 cmp号 int
//             *      8个字节： x  代表x坐标   double
//             *      8个字节： y 代表y坐标 double
//             *      4个字节： zValue  代表 Z 值  float
//             * }
//             *
//             */
//            List<IntpPoint> pointDatas = new ArrayList<IntpPoint>();
//            FileInputStream rf = null;
//            rf = new FileInputStream(filename);
//            byte buffer[] = new byte[4];
//            int number = 0;
//            rf.read(buffer);
//            number = Format.byteArrayToInt(buffer);
//            for (int i = 0; i < number; i++) {
//                IntpPoint point = new IntpPoint();
//                buffer = new byte[4];
//                rf.read(buffer);
//                int cmp = Format.byteArrayToInt(buffer);
//                point.setCmp(cmp);
//
//                buffer = new byte[8];
//                rf.read(buffer);
//                double x = Format.bytesToDouble(buffer);
//                buffer = new byte[8];
//                rf.read(buffer);
//                double y = Format.bytesToDouble(buffer);
//                Coordinate coordinate = new Coordinate();
//                coordinate.setXCoordinate(x);
//                coordinate.setYCoordinate(y);
//                point.setCoordinate(coordinate);
//
//                buffer = new byte[4];
//                rf.read(buffer);
//                float zValue = Format.byteArrayToFloat(buffer);
//                UnitValueF zF = new UnitValueF();
//                zF.setValue(zValue);
//                point.setZ(zF);
//                pointDatas.add(point);
//            }
//            rf.close();
//            return pointDatas;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析二维层位散点数据错误" + ex.getMessage());
//        }
//    }
//
//    public static void writeHorizon2DDataListToFile(List<IntpPoint> data, String fileName) {
//        try {
//            /* 写维解释层位二进制文件
//             * 第一个四个字节：pointNum 代表 层位点个数  int
//             * 循环 pointNum 次 到文件末尾
//             * {
//             *      第一个 4个字节： cmp  代表 cmp号 int
//             *      第二个 4个字节： zValue  代表 Z 值  float
//             * }
//             *
//             */
//            if (data != null) {
//                DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
//                out.write(Format.intToByte(data.size()));
//                for (int i = 0; i < data.size(); i++) {
//                    out.write(Format.intToByte((int) data.get(i).getCmp()));
//                    double x = 0;
//                    double y = 0;
//                    if (data.get(i).getCoordinate() != null) {
//                        x = data.get(i).getCoordinate().getXCoordinate();
//                        y = data.get(i).getCoordinate().getYCoordinate();
//                    }
//                    out.write(Format.doubleToBytes(x));
//                    out.write(Format.doubleToBytes(y));
//                    out.write(Format.floatToByte(data.get(i).getZ().getValue()));
//                }
//                out.close();
//            } else {
//                throw new RuntimeException("被写入的二维地震散点数据为空");
//            }
//
//
//        } catch (Exception ex) {
//            throw new RuntimeException("将二维地震解释散点数据写入文件失败：" + ex.getMessage());
//        }
//    }
//
//    public static void writeFaultSegmentToFile(List<IntpPoint> points, String fileName) {
//        try {
//            /* 写断层段散点数据二进制文件
//             * 第一个四个字节：longSize 代表 long 长度  int
//             * 第二个四个自己：pointNum 代表 断层段点个数  int
//             * 循环 pointNum 次 到文件末尾
//             * {
//             *      第一个 4个字节： inline  代表 inline号 int
//             *      第二个 4个字节： cmp  代表 cmp号  int
//             *      第三个 8个字节： x 代表 x坐标  double
//             *      第四个 8个字节： y 代表 y坐标  double
//             *      第五个 4个字节： z 代表 z值 float
//             * }
//             *
//             */
//            if (points != null) {
//
//                DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
//                int longSize = Long.SIZE / 8;
//                out.write(Format.intToByte(longSize));
//                out.write(Format.intToByte(points.size()));
//                for (int i = 0; i < points.size(); i++) {
//                    out.write(Format.intToByte((int) points.get(i).getInline()));
//                    out.write(Format.intToByte((int) points.get(i).getCmp()));
//                    double x = 0;
//                    double y = 0;
//                    if (points.get(i).getCoordinate() != null) {
//                        x = points.get(i).getCoordinate().getXCoordinate();
//                        y = points.get(i).getCoordinate().getYCoordinate();
//                    }
//                    out.write(Format.doubleToBytes(x));
//                    out.write(Format.doubleToBytes(y));
//                    out.write(Format.floatToByte(points.get(i).getZ().getValue()));
//                }
//                out.close();
//            } else {
//                throw new RuntimeException("被写入的断层段点数据为空");
//            }
//
//
//        } catch (Exception ex) {
//            throw new RuntimeException("将断层段点数据写入文件失败：" + ex.getMessage());
//        }
//    }
//
//    public static List<SEISPolygon> getPolygonsDataFromFile(String filename) {
//        try {
//
//
//            /* 读取断层组合线二进制文件
//             * 第一个四个字节：longSize 代表 long 类型 长度，类型为int
//             * 第二个四个字节：polygonNum  代表组合线数量，类型为int
//             * 循环读取 polygonNum 次，读取每个组合线的数据
//             *      {
//             *      longSize 个字节：polygonOrder  代表组合线的顺序ID，类型为long
//             *      4个字节：pointNum 代表组合线内点个数，类型为 int
//             *      循环读取 pointNum此，读取每个点的数据
//             *              {
//             *              longSize 个字节：pointOrder 代表点顺序ID，类型为long
//             *              8个字节：x 代表点x坐标，类型为 double
//             *              8个字节：y 代表点y坐标，类型为 double
//             *              4个字节：z 代表点z值，类型为 float
//             *              }
//             *      }
//             *
//             */
//            List<SEISPolygon> polygons = new ArrayList<SEISPolygon>();
//            FileInputStream rf = null;
//            rf = new FileInputStream(filename);
//
//            byte buffer[] = new byte[4];
//            rf.read(buffer);
//            int longSize = Format.byteArrayToInt(buffer);
//
//            buffer = new byte[4];
//            rf.read(buffer);
//            int polygonNum = Format.byteArrayToInt(buffer);
//
//            for (int i = 0; i < polygonNum; i++) {
//                SEISPolygon polygon = new SEISPolygon();
//
//                buffer = new byte[longSize];
//                rf.read(buffer);
//                long polygonOrder = Format.byteToLong(buffer);
//                polygon.setPolygonSeqNo(polygonOrder);
//
//                buffer = new byte[4];
//                rf.read(buffer);
//                int pointNum = Format.byteArrayToInt(buffer);
//
//                List<IntpPoint> points = new ArrayList<IntpPoint>();
//
//                for (int k = 0; k < pointNum; k++) {
//                    IntpPoint point = new IntpPoint();
//
//                    buffer = new byte[longSize];
//                    rf.read(buffer);
//                    long pointOrder = Format.byteToLong(buffer);
//                    point.setOrder(pointOrder);
//
//                    buffer = new byte[8];
//                    rf.read(buffer);
//                    double x = Format.bytesToDouble(buffer);
//
//                    buffer = new byte[8];
//                    rf.read(buffer);
//                    double y = Format.bytesToDouble(buffer);
//
//                    Coordinate coordinate = new Coordinate();
//                    coordinate.setXCoordinate(x);
//                    coordinate.setYCoordinate(y);
//                    point.setCoordinate(coordinate);
//
//                    buffer = new byte[4];
//                    rf.read(buffer);
//                    float z = Format.byteArrayToFloat(buffer);
//
//                    UnitValueF zValue = new UnitValueF();
//                    zValue.setValue(z);
//                    point.setZ(zValue);
//
//                    points.add(point);
//                }
//                polygon.setPoints(points);
//                polygon.setNumPoints(pointNum);
//                polygons.add(polygon);
//            }
//
//            return polygons;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析多边形数据错误" + ex.getMessage());
//        }
//    }
//
//    public static void writePolygonsDataToFile(List<SEISPolygon> data, String fileName, int polygonNum) {
//        try {
//            /*写断层组合线二进制文件
//             * 第一个四个字节：longSize 代表 long类型长度，类型int
//             * 第二个四个字节：polygonNum  代表组合线数量，类型为int
//             * 循环写 polygonNum 次，写每个组合线的数据
//             *      {
//             *      longSize 个字节：polygonOrder  代表组合线的顺序ID，类型为long
//             *      4个字节：pointNum 代表组合线内点个数，类型为 int
//             *      循环写 pointNum次，写每个点的数据
//             *              {
//             *              longSize 个字节：pointOrder 代表点顺序ID，类型为long
//             *              8个字节：x 代表点x坐标，类型为 double
//             *              8个字节：y 代表点y坐标，类型为 double
//             *              4个字节：z 代表点z值，类型为 float
//             *              }
//             *      }
//             *
//             */
//            if (data != null) {
//                DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
//                int longSize = Long.SIZE / 8;
//                out.write(Format.intToByte(longSize));
//                out.write(Format.intToByte(polygonNum));
//                for (int i = 0; i < polygonNum; i++) {
//                    SEISPolygon polygon = data.get(i);
//                    out.write(Format.longToBytes(polygon.getPolygonSeqNo()));
//                    int pointNum = polygon.getNumPoints();
//                    out.write(Format.intToByte(pointNum));
//                    List<IntpPoint> intpPointList = polygon.getPoints();
//                    for (int j = 0; j < pointNum; j++) {
//                        out.write(Format.longToBytes(intpPointList.get(j).getOrder()));
//                        out.write(Format.doubleToBytes(intpPointList.get(j).getCoordinate().getXCoordinate()));
//                        out.write(Format.doubleToBytes(intpPointList.get(j).getCoordinate().getYCoordinate()));
//                        UnitValueF z = intpPointList.get(j).getZ();
//                        if (z != null) {
//                            out.write(Format.floatToByte(z.getValue()));
//                        } else {
//                            out.write(Format.floatToByte(0));
//                        }
//                    }
//                }
//                out.close();
//            } else {
//                throw new RuntimeException("被写入的断层组合线数据为空");
//            }
//        } catch (Exception ex) {
//            throw new RuntimeException("将断层组合线数据写入文件失败：" + ex.getMessage());
//        }
//    }
//
//    public static float[] getHorizon3DDataFromFile(String filename, int number) {
//        try {
//            FileInputStream rf = null;
//            rf = new FileInputStream(filename);
//            byte buffer[] = new byte[4];
//            float[] data = new float[number];
//            for (int i = 0; i < number; i++) {
//                buffer = new byte[4];
//                rf.read(buffer);
//                float formatS = Format.byteArrayToFloat(buffer);
//                data[i] = formatS;
//            }
//            rf.close();
//            return data;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析三维层位散点数据错误" + ex.getMessage());
//        }
//    }
//
//    public static float[] getHorizon2DDataFromFile(String filename) {
//        try {
       //     FileInputStream rf = null;
//            rf = new FileInputStream(filename);
//            byte buffer[] = new byte[4];
//            int number = 0;
//            rf.read(buffer);
//            number = Format.byteArrayToInt(buffer);
//            float[] data = new float[number];
//            for (int i = 0; i < number; i++) {
//                buffer = new byte[4];
//                rf.read(buffer);
//                buffer = new byte[8];
//                rf.read(buffer);
//                buffer = new byte[8];
//                rf.read(buffer);
//                buffer = new byte[4];
//                rf.read(buffer);
//                float formatS = Format.byteArrayToFloat(buffer);
//                data[i] = formatS;
//            }
//            rf.close();
//            return data;
//        } catch (Exception ex) {
//            throw new RuntimeException("解析二维层位散点数据错误" + ex.getMessage());
//        }
//    }
}

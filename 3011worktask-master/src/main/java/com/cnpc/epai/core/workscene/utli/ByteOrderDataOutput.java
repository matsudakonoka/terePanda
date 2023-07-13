/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnpc.epai.core.workscene.utli;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 *
 * @author baohong
 */
public class ByteOrderDataOutput implements DataOutput {

    private DataOutput dataOutput;
    private ByteOrder byteOrder;

    public ByteOrderDataOutput(DataOutput dataOutput, ByteOrder byteOrder) {
        this.dataOutput = dataOutput;
        this.byteOrder = byteOrder;
    }

    @Override
    public void writeShort(int v) throws IOException {
        dataOutput.writeShort(ByteOrder.BIG_ENDIAN == byteOrder ? v : Short.reverseBytes((short) v));
    }

    @Override
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        dataOutput.writeInt(ByteOrder.BIG_ENDIAN == byteOrder ? v : Integer.reverseBytes(v));
    }

    @Override
    public void writeLong(long v) throws IOException {
        dataOutput.writeLong(ByteOrder.BIG_ENDIAN == byteOrder ? v : Long.reverseBytes(v));
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void write(int b) throws IOException {
        dataOutput.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        dataOutput.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        dataOutput.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        dataOutput.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        dataOutput.writeByte(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        dataOutput.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        dataOutput.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        dataOutput.writeUTF(s);
    }
}

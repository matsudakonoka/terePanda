/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnpc.epai.core.worktask.pojo;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author baohong
 */
public class ByteOrderDataInput implements DataInput {

    private DataInput dataInput;
    private ByteOrder byteOrder;

    public ByteOrderDataInput(DataInput dataInput, ByteOrder byteOrder) {
        this.dataInput = dataInput;
        this.byteOrder = byteOrder;
    }

    private byte[] readBytesLen(int len) throws IOException {
        byte[] buffer = new byte[len];
        //for (int i = 0; i < len; i++) {
        //    buffer[i] = dataInput.readByte();
        //}
        dataInput.readFully(buffer);
        return buffer;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        //如果是Smb，有bug ，原因是smb调用read,两个地方都增加文件指针
        dataInput.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return dataInput.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataInput.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return dataInput.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataInput.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        short value = ByteBuffer.wrap(readBytesLen(2)).getShort();
        return ByteOrder.BIG_ENDIAN == byteOrder ? value : Short.reverseBytes(value);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        short shortValue = readShort();
        return (int) (shortValue & 0xFFFFL);
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws IOException {
        int value = ByteBuffer.wrap(readBytesLen(4)).getInt();
        return ByteOrder.BIG_ENDIAN == byteOrder ? value : Integer.reverseBytes(value);
    }

    @Override
    public long readLong() throws IOException {
        long value = ByteBuffer.wrap(readBytesLen(8)).getLong();
        return ByteOrder.BIG_ENDIAN == byteOrder ? value : Long.reverseBytes(value);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        return dataInput.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return dataInput.readUTF();
    }
}

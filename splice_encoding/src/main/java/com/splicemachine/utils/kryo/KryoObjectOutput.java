/*
 * Copyright 2012 - 2016 Splice Machine, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.splicemachine.utils.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * @author Scott Fines
 *         Created on: 8/15/13
 */
public class KryoObjectOutput implements ObjectOutput {
    private final Output output;
    private final Kryo kryo;

    public KryoObjectOutput(Output output, Kryo kryo) {
        this.output = output;
        this.kryo = kryo;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        kryo.writeClassAndObject(output,obj);
    }

    @Override
    public void write(int b) throws IOException {
        output.writeInt(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b,off,len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        output.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        output.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        output.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        output.writeChar((char)v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        output.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        output.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        output.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        output.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        output.writeString(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        output.writeString(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        output.writeString(s);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}

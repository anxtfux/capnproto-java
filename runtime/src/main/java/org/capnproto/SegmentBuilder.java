// Copyright (c) 2013-2014 Sandstorm Development Group, Inc. and contributors
// Licensed under the MIT License:
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.capnproto;

import java.nio.ByteBuffer;

public final class SegmentBuilder extends SegmentReader {
    static final int FAILED_ALLOCATION = -1;
    private final int capacity; // in words
    public final int id;
    private int pos = 0; // in words

    public SegmentBuilder(ByteBuffer buf, BuilderArena arena, int id) {
        super(buf, arena);
        capacity = buf.capacity() >>> 3;
        this.id = id;
    }

    // return how many words have already been allocated
    public final int currentSize() {
        return pos;
    }

    /*
       Allocate `amount` words.
     */
    public final int allocate(int amount) {
        assert amount >= 0 : "tried to allocate a negative number of words";
        if (amount > capacity - pos)
            return FAILED_ALLOCATION;
        int result = pos;
        pos += amount;
        return result;
    }

    public final BuilderArena getArena() {
        return (BuilderArena)this.arena;
    }

    final boolean isWritable() {
        return true;
    }

    public final void put(int index, long value) {
        buffer.putLong(index << 3, value);
    }
}

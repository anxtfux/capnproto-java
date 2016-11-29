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
import java.util.LinkedList;

public final class BuilderArena implements Arena {
    private final LinkedList<SegmentBuilder> segments = new LinkedList<>();
    private final Allocator allocator;

    public BuilderArena() {
        this(1024);
    }

    public BuilderArena(int firstSegmentSizeWords) {
        this(firstSegmentSizeWords, false);
    }

    public BuilderArena(int firstSegmentSizeWords, boolean grow) {
        this(grow ? Allocator.growHeuristically(firstSegmentSizeWords) : Allocator.fixedSize(firstSegmentSizeWords));
    }

    public BuilderArena(Allocator allocator) {
        this.allocator = allocator;
    }

    public int getSegmentCount() {
        return segments.size();
    }

    public final SegmentReader tryGetSegment(int id) {
        return getSegment(id);
    }

    public final SegmentBuilder getSegment(int id) {
        return id == 0 ? segments.getFirst() : segments.get(id);
    }

    public final void checkReadLimit(int numBytes) { }

    static final class AllocateResult {
        public final SegmentBuilder segment;

        // offset to the beginning the of allocated memory
        public final int offset;

        private AllocateResult(SegmentBuilder segment, int offset) {
            this.segment = segment;
            this.offset = offset;
        }
    }

    public AllocateResult allocate(int amount) {
        SegmentBuilder segment;
        int result;
        if (segments.size() == 0 || (result = (segment = segments.getLast()).allocate(amount)) == SegmentBuilder.FAILED_ALLOCATION) {
            segment = new SegmentBuilder(allocator.allocate(segments.size(), amount), this, segments.size());
            segments.add(segment);
            return new AllocateResult(segment, segment.allocate(amount));
        }
        return new AllocateResult(segment, result);
    }

    public final ByteBuffer[] getSegmentsAndHeaderForOutput() {
        ByteBuffer[] result = new ByteBuffer[segments.size() + 1];
        ByteBuffer header = allocator.allocate(-1, (segments.size() + 2) / 2);
        // set number of segments -1
        header.putInt(segments.size() - 1);
        result[0] = header;
        int pos = 1;
        for (SegmentBuilder segment : segments) {
            ByteBuffer b = segment.buffer.duplicate(); // the byte order does not matter when writing byte wise
            header.putInt(segment.currentSize());
            b.position(0); // needed due to unnecessary double buffering in Text and Data
            b.limit(segment.currentSize() << 3);
            result[pos++] = b;
        }
        // ensure 8 byte alignment
        if (segments.size() % 2 == 0)
            header.putInt(0);
        header.flip();
        return result;
    }

    public final ByteBuffer[] getSegmentsForOutput() {
        ByteBuffer[] result = new ByteBuffer[segments.size()];
        int pos = 0;
        for (SegmentBuilder segment : segments) {
            ByteBuffer b = segment.buffer.duplicate(); // the byte order does not matter when writing byte wise
            b.position(0); // needed due to unnecessary double buffering in Text and Data
            b.limit(segment.currentSize() << 3);
            result[pos++] = b;
        }
        return result;
    }
}

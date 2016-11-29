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
import java.util.ArrayList;

public final class ReaderArena implements Arena {
    private final ArrayList<SegmentReader> segments;
    private long limit;

    public ReaderArena(ByteBuffer[] segmentSlices, long traversalLimitInWords) {
        limit = traversalLimitInWords;
        segments = new ArrayList<>(segmentSlices.length);
        for (ByteBuffer b : segmentSlices)
            segments.add(new SegmentReader(b, this));
    }

    public SegmentReader tryGetSegment(int id) {
        return segments.get(id);
    }

    public final void checkReadLimit(int numBytes) {
        if (numBytes > limit)
            throw new DecodeException("Read limit exceeded.");
        limit -= numBytes;
    }
}

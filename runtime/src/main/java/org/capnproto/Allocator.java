package org.capnproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author amer.banet
 */
@FunctionalInterface
public interface Allocator {
    /**
     * Allocates a buffer for the given segment. The resulting buffer must have at least the given size but may be larger.
     * The allocator decides about direct or heap buffer usage as well a about pooling (reusing) the buffers.
     * In case of buffer pooling the buffer may already contain data. It is up to the allocation if zeroing the buffers is useful before re-issuing.
     *
     * @param segmentNumber      index of the segment -1 is used when allocating the segment header
     * @param minimalSizeInWords minimal size in words (8 bytes blocks)
     * @return a buffer which has at least minimalSizeInWords * 8 bytes remaining
     */
    ByteBuffer allocate(int segmentNumber, int minimalSizeInWords);

    static Allocator fixedSize(int segmentWords) {
        return (segmentNumber, minimalSizeInWords) -> ByteBuffer.allocateDirect(Math.max(segmentWords << 3, minimalSizeInWords << 3)).order(ByteOrder.LITTLE_ENDIAN);
    }

    static Allocator growHeuristically(int firstSegmentWords) {
        return (segmentNumber, minimalSizeInWords) -> ByteBuffer.allocateDirect(Math.max(firstSegmentWords << 3 + segmentNumber, minimalSizeInWords << 3)).order(ByteOrder.LITTLE_ENDIAN);
    }
}

package com.seaboat.net.reactor;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author seaboat
 * @date 2017-01-02
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>A buffer pool. This pool provide a buffer queue for the operation of offer and poll. </p>
 */
public final class BufferPool {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BufferPool.class);
	private final int chunkSize;
	private final ConcurrentLinkedQueue<ByteBuffer> items = new ConcurrentLinkedQueue<ByteBuffer>();
	private AtomicInteger newCreated = new AtomicInteger(0);
	private final int capactiy;
	private long totalBytes = 0;
	private long totalCounts = 0;

	public BufferPool(int bufferSize, int chunkSize) {
		this.chunkSize = chunkSize;
		int size = bufferSize / chunkSize;
		size = (bufferSize % chunkSize == 0) ? size : size + 1;
		this.capactiy = size;
		for (int i = 0; i < capactiy; i++) {
			items.offer(ByteBuffer.allocateDirect(chunkSize));
		}
	}

	public int size() {
		return this.items.size();
	}

	public int capacity() {
		return capactiy + newCreated.get();
	}

	public ByteBuffer allocate() {
		ByteBuffer node = null;
		node = items.poll();
		if (node == null) {
			newCreated.incrementAndGet();
			node = ByteBuffer.allocateDirect(chunkSize);
		}
		return node;
	}

	private boolean checkValidBuffer(ByteBuffer buffer) {
		if (buffer == null || !buffer.isDirect()) {
			return false;
		} else if (buffer.capacity() != chunkSize) {
			// we don't recycle the buffer that isn't allocated by BufferPool.
			return false;
		}
		totalCounts++;
		totalBytes += buffer.limit();
		buffer.clear();
		return true;
	}

	public void recycle(ByteBuffer buffer) {
		if (!checkValidBuffer(buffer)) {
			return;
		}
		items.offer(buffer);
	}

	public int getAvgBufSize() {
		if (this.totalBytes < 0) {
			totalBytes = 0;
			this.totalCounts = 0;
			return 0;
		} else {
			return (int) (totalBytes / totalCounts);
		}
	}

	public ByteBuffer allocate(int size) {
		if (size <= this.chunkSize) {
			return allocate();
		} else {
			LOGGER.warn("allocate buffer size large than chunksize:"
					+ this.chunkSize + ",the size is " + size
					+ ". we create a temp buffer that could not be recycled."
					+ size);
			return ByteBuffer.allocate(size);
		}
	}
}

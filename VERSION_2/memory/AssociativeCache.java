package memory;

import java.util.ArrayList;
import java.util.LinkedList;

import util.Utils;

class EntryAssociatif {
	int value; // the line's word (1 cache-line = 1 word)
	boolean isValid; // the validity bit
	boolean isDirty;
	// SI isDIRTY == FAUX => Donnée en mémoire centrale == Donnée en cache
	// SI isDIRTY == VRAI => Donnée en mémoire centrale != Donnée en cache
	int address; // the line's address
	int age; // age for LRU
}

public class AssociativeCache implements Memory {
	//
	// ATTRIBUTES
	//
	// private final ArrayList<Entry> entries;
	private final LinkedList<EntryAssociatif> entries;
	private final int accessTime;
	private final int queueSize;
	private final Memory memory;

	private final int indexWidth;
	private final int indexMask;

	private int operationTime;
	private final Stats stats;

	public AssociativeCache(int size, int accessTime, Memory memory) {
		if (size <= 0) {
			throw new IllegalArgumentException("size");
		}
		if (accessTime <= 0) {
			throw new IllegalArgumentException("accessTime");
		}
		if (memory == null) {
			throw new NullPointerException("memory");
		}
		indexWidth = Utils.log(size);
		if (indexWidth == -1) {
			throw new IllegalArgumentException("size");
		}
		this.indexMask = Utils.mask(indexWidth);
		this.entries = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			entries.add(new EntryAssociatif());
		}
		this.accessTime = accessTime;
		this.memory = memory;
		this.queueSize = size;
		this.stats = new Stats();
	}

	@Override
	public int read(int address) {
		return 0;
	}

	@Override
	public void write(int address, int value) {

	}

	private int readFIFO(int address) {
		return 0;
	}

	private void writeFIFO(int address, int value) {
		EntryAssociatif entry = entries.get(toIndex(address));
		// HIT
		if (entry.isValid && entry.address == address) {
			entry.value = value;
			entry.isDirty = true;
			stats.writes.add(true, accessTime);
		}
		// MISS
		else {
			if (entry.isDirty) {
				memory.write(address, entry.value);
				operationTime = memory.getOperationTime() + accessTime;
			}
			entry.value = value;
			entry.address = address;
			entry.isValid = true;
			entry.isDirty = true;
			stats.writes.add(false, operationTime);
		}
		

	}

	private int readLRU(int address) {
		return 0;
	}

	private void writeLRU(int address, int value) {

	}

	@Override
	public int getOperationTime() {
		return operationTime;
	}

	@Override
	public Stats getStats() {
		return stats;
	}

	//
	// UTILITIES
	//
	private int toIndex(int address) {
		return address & indexMask;
	}

	private void flush() {
		// A FAIRE ?
	}

	private boolean isFull() {
		return entries.size() == queueSize;
	}
}

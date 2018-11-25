package memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import util.Utils;

class EntryAssociatif {
	int value; // the line's word (1 cache-line = 1 word)
	boolean isValid; // the validity bit
	boolean isDirty;
	// SI isDIRTY == FAUX => Donnï¿½e en mï¿½moire centrale == Donnï¿½e en cache
	// SI isDIRTY == VRAI => Donnï¿½e en mï¿½moire centrale != Donnï¿½e en cache
	int address; // the line's address
	int age = 0; // age for LRU
}

public class AssociativeCache implements Memory {
	//
	// ATTRIBUTES
	//
	private final ArrayList<EntryAssociatif> entriesLRU;
	private final Queue<EntryAssociatif> entries;
	private final int accessTime;
	private final int queueSize;
	private final Memory memory;

	private final int indexWidth;
	private final int indexMask;

	private int operationTime;
	private final Stats stats;

	//
	// CONSTRUCTORS
	//
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
		this.entries = new LinkedList<EntryAssociatif>();
		for (int i = 0; i < size; i++) {
			entries.add(new EntryAssociatif());
		}
		this.entriesLRU = new ArrayList<EntryAssociatif>(size);
		for (int i = 0; i < size; i++) {
			entriesLRU.add(new EntryAssociatif());
		}
		this.accessTime = accessTime;
		this.memory = memory;
		this.queueSize = size;
		this.stats = new Stats();
	}

	@Override
	public int read(int address) {
		return readFIFO(address);
	}

	@Override
	public void write(int address, int value) {
		writeFIFO(address, value);
	}

	private int readFIFO(int address) {
		EntryAssociatif entry = null;
		for (EntryAssociatif e : entries) {
			if (e.address == address && e.isValid)
				entry = e;
		}

		// MISS
		if (entry == null) {
			entry = new EntryAssociatif();
			entry.value = memory.read(address);
			entry.address = address;
			entry.isValid = true;
			if (isFull()) {
				// IS DIRTY ??
				EntryAssociatif tmp = entries.poll();
				memory.write(tmp.address, tmp.value);
			}
			entries.add(entry);
			operationTime = memory.getOperationTime() + accessTime;
			stats.reads.add(false, operationTime);
		} else {
			operationTime = accessTime;
			stats.reads.add(true, operationTime);
		}

		return entry.value;
	}

	private void writeFIFO(int address, int value) {

		for (EntryAssociatif e : entries) {
			// HIT
			if (e.address == address && e.isValid) {
				e.value = value;
				e.isDirty = true;
				operationTime = accessTime;
				stats.writes.add(false, operationTime);
				return;
			}
		}
		// MISS
		operationTime = accessTime;
		EntryAssociatif entry = new EntryAssociatif();
		entry.value = value;
		entry.address = address;
		entry.isValid = true;
		entry.isDirty = true;
		if (isFull()) {
			EntryAssociatif tmp = entries.poll();
			memory.write(tmp.address, tmp.value);
			operationTime = memory.getOperationTime() + accessTime;
		}
		entries.add(entry);
		stats.writes.add(true, operationTime);
	}

	private int readLRU(int address) {
		EntryAssociatif entry = null;
		for (EntryAssociatif e : entriesLRU) {
			if (e.address == address && e.isValid)
				entry = e;
			else
				e.age++;
		}

		// MISS
		if (entry == null) {
			entry = new EntryAssociatif();
			entry.value = memory.read(address);
			entry.address = address;
			entry.isValid = true;
			// GETOLDESTENTRY ??????????????
			// Qui gagne entre une entrée non valide et la plus vieille valide ????
			EntryAssociatif tmp = getOldestEntry(); //entriesLRU.get(toIndex(getOldestEntry()));
			if (tmp.isValid) { // && tmp.isDirty)
				memory.write(address, tmp.value);
				tmp.value = memory.read(address);
				tmp.age = 0;
			}
		} else {
			operationTime = accessTime;
			stats.reads.add(true, operationTime);
		}

		return entry.value;
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

	/*
	 * private int getOldestEntry() { int maxAge = 0, addressO = -1;
	 * for(EntryAssociatif e : entriesLRU) { if(e.age > maxAge && e.isValid) {
	 * maxAge = e.age; addressO = e.address; } } return addressO; }
	 */

	private EntryAssociatif getOldestEntry() {
		int maxAge = 0;
		EntryAssociatif entry=null;
		for (EntryAssociatif e : entriesLRU) {
			if (!e.isValid) {
				entry = e;
				return e;
			}
			else if (e.age > maxAge && e.isValid) {
				maxAge = e.age;
				entry = e;
			}
		}
		return entry;
	}
}

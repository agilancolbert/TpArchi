package memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import sun.security.x509.IssuingDistributionPointExtension;
import util.Utils;

class EntryAssociatif {
	int value; // the line's word (1 cache-line = 1 word)
	boolean isValid; // the validity bit
	boolean isDirty;
	// SI isDIRTY == FAUX => Donn�e en m�moire centrale == Donn�e en cache
	// SI isDIRTY == VRAI => Donn�e en m�moire centrale != Donn�e en cache
	int address; // the line's address
	int age = 0; // age for LRU
}

public class AssociativeCache implements Memory {
	//
	// ATTRIBUTES
	//
	private final ArrayList<EntryAssociatif> entriesLRU;
	private final Queue<EntryAssociatif> entriesFIFO;
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
		this.entriesFIFO = new LinkedList<EntryAssociatif>();
		for (int i = 0; i < size; i++) {
			entriesFIFO.add(new EntryAssociatif());
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
		operationTime =  accessTime;
		EntryAssociatif entry = null;
		for (EntryAssociatif e : entriesFIFO) {
			if (e.address == address && e.isValid) {
				if(e.isDirty) {
					memory.write(e.address, e.value);
					operationTime += memory.getOperationTime();
				}
			}
				entry = e;
		}

		// MISS
		if (entry == null) {
			entry = new EntryAssociatif();
			entry.value = memory.read(address);
			entry.address = address;
			entry.isValid = true;
			if (isFull()) {
				EntryAssociatif tmp = entriesFIFO.poll();
				if (tmp.isValid && tmp.isDirty)
					memory.write(tmp.address, tmp.value);
			}
			entriesFIFO.add(entry);
			operationTime += memory.getOperationTime();
			stats.reads.add(true, operationTime);
		} else {
			stats.reads.add(false, operationTime);
		}

		return entry.value;
	}

	private void writeFIFO(int address, int value) {
		operationTime = accessTime;
		for (EntryAssociatif e : entriesFIFO) {
			// HIT
			if (e.address == address && e.isValid) {
				if(e.isDirty) {
					memory.write(e.address, e.value);
					operationTime+=memory.getOperationTime();
				}
				e.value = value;
				e.isDirty = true;
				stats.writes.add(false, operationTime);
				return;
			}
		}
		// MISS
		EntryAssociatif entry = new EntryAssociatif();
		entry.value = value;
		entry.address = address;
		entry.isValid = true;
		entry.isDirty = true;
		if (isFull()) {
			EntryAssociatif tmp = entriesFIFO.poll();
			memory.write(tmp.address, tmp.value);
			operationTime = memory.getOperationTime() + accessTime;
		}
		entriesFIFO.add(entry);
		stats.writes.add(true, operationTime);
	}

	private int readLRU(int address) {
		operationTime = accessTime;
		EntryAssociatif entry = null;
		for (EntryAssociatif e : entriesLRU) {
			if (e.address == address && e.isValid) {
				if(e.isDirty) {
					memory.write(e.address, e.value);
					operationTime += memory.getOperationTime();
				}
				entry = e;
			}
			else
				e.age++;
		}

		// MISS
		if (entry == null) {
			entry = new EntryAssociatif();
			entry.value = memory.read(address);
			entry.address = address;
			entry.isValid = true;
			EntryAssociatif tmp = getOldestEntry();
			if (tmp.isValid && tmp.isDirty) {
				memory.write(address, tmp.value);
			}
			tmp.value = memory.read(address);
			tmp.age = 0;
			operationTime += memory.getOperationTime();
			stats.reads.add(true, operationTime);
		}
		// HIT
		else {
			stats.reads.add(false, operationTime);
		}

		return entry.value;
	}

	private void writeLRU(int address, int value) {

		operationTime = accessTime;
		for (EntryAssociatif e : entriesLRU) {
			if (e.address == address && e.isValid) {
				if (e.isDirty) {
					memory.write(e.address, e.value);
					operationTime += memory.getOperationTime();
				}
				e.value = value;
				stats.reads.add(true, operationTime);
				return;
			} else
				e.age++;
		}
		// MISS
			EntryAssociatif entry = null;
			entry = new EntryAssociatif();
			entry.value = memory.read(address);
			entry.address = address;
			entry.isValid = true;
			EntryAssociatif tmp = getOldestEntry();
			if (tmp.isValid && tmp.isDirty) 
				memory.write(address, tmp.value);
			tmp.value = value;
			tmp.age = 0;
			operationTime += memory.getOperationTime();
			stats.reads.add(false, operationTime);
		
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

	public void flush() {
		for (EntryAssociatif e : entriesFIFO) {
			if (e.isDirty)
				memory.write(e.address, e.value);
		}
	}

	private boolean isFull() {
		return entriesFIFO.size() == queueSize;
	}

	/*
	 * private int getOldestEntry() { int maxAge = 0, addressO = -1;
	 * for(EntryAssociatif e : entriesLRU) { if(e.age > maxAge && e.isValid) {
	 * maxAge = e.age; addressO = e.address; } } return addressO; }
	 */

	private EntryAssociatif getOldestEntry() {
		int maxAge = 0;
		EntryAssociatif entry = null;
		for (EntryAssociatif e : entriesLRU) {
			if (!e.isValid)
				return e;
			else if (e.age > maxAge && e.isValid) {
				maxAge = e.age;
				entry = e;
			}
		}
		return entry;
	}
}

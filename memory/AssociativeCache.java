package memory;

import java.util.ArrayList;

import util.Utils;

class EntryAssociatif {
	  int value;        // the line's word (1 cache-line = 1 word)
	  boolean isValid;  // the validity bit
	  boolean isDirty;  
		// SI isDIRTY == FAUX => Donnée en mémoire centrale == Donnée en cache
		// SI isDIRTY == VRAI => Donnée en mémoire centrale != Donnée en cache
	  int adresse ;
	  int compteur ; // LRU
	}

public class AssociativeCache implements Memory{
	  private final ArrayList<Entry> entries;
	  private final int accessTime;
	  private final Memory memory;
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

		    this.entries = new ArrayList<>(size);
		    for (int i = 0; i < size; i++) {
		      entries.add(new Entry());
		    }
		    this.accessTime = accessTime;
		    this.memory = memory;

		    this.stats = new Stats();
	}
	@Override
	public int read(int address) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(int address, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getOperationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Stats getStats() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
}

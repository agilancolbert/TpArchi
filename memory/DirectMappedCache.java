package memory;

import java.util.ArrayList;

import util.Utils;

//
// CACHE ENTRY
//
class Entry {
  int value;        // the line's word (1 cache-line = 1 word)
  int tag;          // the line's tag
  boolean isValid;  // the validity bit
  boolean isDirty;  
	// SI isDIRTY == FAUX => Donn�e en m�moire centrale == Donn�e en cache
	// SI isDIRTY == VRAI => Donn�e en m�moire centrale != Donn�e en cache
}

public class DirectMappedCache implements Memory {

  //
  // ATTRIBUTES
  //
  private final ArrayList<Entry> entries;
  private final int accessTime;
  private final Memory memory;

  private final int indexWidth;
  private final int indexMask;

  private int operationTime;
  private final Stats stats;

  //
  // CONSTRUCTORS
  //
  public DirectMappedCache(int size, int accessTime, Memory memory) {
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

    this.entries = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      entries.add(new Entry());
    }
    this.accessTime = accessTime;
    this.memory = memory;

    this.stats = new Stats();
  }

  //
  // Memory INTERFACE
  //
  @Override
  public int read(int address) {
    Entry entry = entries.get(toIndex(address));
    if (entry.isValid && entry.tag == toTag(address)) {
      // hit
      operationTime = accessTime;
      stats.reads.add(true, operationTime);
    } else {
      // miss
      entry.value = memory.read(address);
      entry.tag = toTag(address);
      entry.isValid = true;
      operationTime = memory.getOperationTime() + accessTime;
      stats.reads.add(false, operationTime);
    }
    return entry.value;
  }

  @Override
  public void write(int address, int value) {
    writeAround(address, value);
  }

  private void writeAround(int address, int value) {
    Entry entry = entries.get(toIndex(address));
    if (entry.isValid && entry.tag == toTag(address)) {
      entry.isValid = false;
    }
    memory.write(address, value);
    operationTime = memory.getOperationTime() + accessTime;
    stats.writes.add(false, operationTime);
  }

  // Tu �cris en m�moire centrale et en cache en m�me temps
  private void writeThrough(int address, int value) {
	  
//	  if (entry.isValid && entry.tag != toTag(address)) {
//	      entry.isValid = false;
//	    }
	  
	  
	  // Chercher l'entr�e
	  Entry entry = entries.get(toIndex(address));
	  //cas si la donn�e n'est pas pr�sente en cache
	  if(entry.tag == toTag(address))  {
		  entry.value = value;
		  entry.isValid =true;
	  }
	  memory.write(address, value);
	  operationTime = memory.getOperationTime() + accessTime;
	  stats.writes.add(false, operationTime);
	 
  }

  // Ecriture seulement en cache
	// Ajout du mot en MC si seulement la cache est remplie
  private void writeBack(int address, int value) {
	  Entry entry = entries.get(toIndex(address));
	    if (/*entry.isValid && */entry.tag == toTag(address))
	    {
	    	// Ce que t'as dans le cache est plus r�cent que ce que t'as en m�moire
	    	if(entry.isDirty)
	    	{
	    		// ECRIRE EN MEMOIRE CENTRALE CE QUE T'AS
	    		memory.write(address, entry.value);
	    		// Replace par la new data dans le cache
	    		entry.value= value;
	    		operationTime = memory.getOperationTime() + accessTime;
	    		stats.writes.add(false, operationTime);
	    	}
	    	else
	    	{
	    		entry.value= value;
	    		entry.isDirty = true;
	    		operationTime = accessTime;
	    		stats.writes.add(false, operationTime);
	    	}
	    }
  }

  private void flush()
  {
	// Pour chacune des entr�es, on mettra isValid a faux, et isDirty a faux  
	  for (Entry e : entries)
	  {
		  e.isDirty = false;
		  e.isValid = false;
	  }
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

  private int toTag(int address) {
    return address >> indexWidth;
  }

  private int toAddress(int tag, int index) {
    return (tag << indexWidth) + index;
  }

}

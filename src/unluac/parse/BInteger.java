package unluac.parse;

import java.math.BigInteger;
import java.util.ArrayList;

public class BInteger extends BObject {
  
  private final BigInteger big;
  private final int n;
  
  private static BigInteger MAX_INT = null;
  private static BigInteger MIN_INT = null;
  
  public BInteger(BInteger b) {
    this.big = b.big;
    this.n = b.n;
  }
  
  public BInteger(int n) {
    this.big = null;
    this.n = n;
  }
  
  public BInteger(BigInteger big) {
    this.big = big;
    this.n = 0;
    if(MAX_INT == null) {
      MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
      MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    }
  }

  public int asInt() {
    if(big == null) {
      return n;
    } else if(big.compareTo(MAX_INT) > 0 || big.compareTo(MIN_INT) < 0) {
      throw new IllegalStateException("The size of an integer is outside the range that unluac can handle.");
    } else {
      return big.intValue();
    }
  }
  
  public boolean lessThan(int x) {
    if(big == null) {
      return n < x;
    } else {
      return big.compareTo(BigInteger.valueOf(x)) < 0;
    }
  }
  
  public byte[] littleEndianBytes(int size) {
    ArrayList<Byte> bytes = new ArrayList<Byte>();
    if(big == null) {
      if(size >= 1) bytes.add((byte)(n & 0xFF));
      if(size >= 2) bytes.add((byte)((n >>> 8) & 0xFF));
      if(size >= 3) bytes.add((byte)((n >>> 16) & 0xFF));
      if(size >= 4) bytes.add((byte)((n >>> 24) & 0xFF));
    } else {
      BigInteger n = big;
      boolean negate = false;
      if(n.signum() < 0) {
        n = n.negate();
        n = n.subtract(BigInteger.ONE);
        negate = true;
      }
      BigInteger b256 = BigInteger.valueOf(256);
      BigInteger b255 = BigInteger.valueOf(255);
      while(n.compareTo(b256) < 0 && size > 0) {
        int v = n.and(b255).intValue();
        if(negate) {
          v = ~v;
        }
        bytes.add((byte)v);
        n = n.divide(b256);
        size--;
      }
    }
    while(size > bytes.size()) bytes.add((byte)0);
    byte[] array = new byte[bytes.size()];
    for(int i = 0; i < bytes.size(); i++) {
      array[i] = bytes.get(i);
    }
    return array;
  }
  
  public void iterate(Runnable thunk) {
    if(big == null) {
      int i = n;
      while(i-- != 0) {
        thunk.run();
      }
    } else {
      BigInteger i = big;
      while(i.signum() > 0) {
        thunk.run();
        i = i.subtract(BigInteger.ONE);
      }
    }
  }

}

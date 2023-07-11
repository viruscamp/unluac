package unluac.parse;

import unluac.decompile.PrintFlag;

public abstract class LNumber extends LObject {

  public static LNumber makeInteger(int number) {
    return new LIntNumber(number);
  }
  
  public static LNumber makeDouble(double x) {
    return new LDoubleNumber(x, LNumberType.NumberMode.MODE_FLOAT);
  }
  
  @Override
  public abstract String toPrintString(int flags);
    
  //TODO: problem solution for this issue
  public abstract double value();
  
  public abstract boolean integralType();
  
  public abstract long bits();
}

class LFloatNumber extends LNumber {
  
  public static final int NAN_SHIFT_OFFSET = 52 - 23;
  
  public final float number;
  public final LNumberType.NumberMode mode;
  
  public LFloatNumber(float number, LNumberType.NumberMode mode) {
    this.number = number;
    this.mode = mode;
  }
  
  @Override
  public String toPrintString(int flags) {
    if(mode == LNumberType.NumberMode.MODE_NUMBER && number == (float) Math.round(number)) {
      if(Float.floatToRawIntBits(number) == Float.floatToRawIntBits(-0.0f)) {
        return "-0";
      } else {
        return Integer.toString((int) number);
      }
    } else {
      if(Float.isInfinite(number)) {
        return number > 0.0 ? "1e9999" : "-1e9999";
      } else if(Float.isNaN(number)) {
        if(PrintFlag.test(flags, PrintFlag.DISASSEMBLER)) {
          int bits = Float.floatToRawIntBits(number);
          int canonical = Float.floatToRawIntBits(Float.NaN);
          if(bits == canonical) {
            return "NaN";
          } else {
            String sign = "+";
            if(bits < 0) {
              bits ^= 0x80000000;
              sign = "-";
            }
            long lbits = bits ^ canonical;
            // shift by difference in number of bits between double-precision and single-precision
            return "NaN" + sign + Long.toHexString(lbits << NAN_SHIFT_OFFSET);
          }
        } else {
          return "(0/0)";
        }
      } else {
        return Float.toString(number);
      }
    }
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LFloatNumber) {
      return Float.floatToRawIntBits(number) == Float.floatToRawIntBits(((LFloatNumber) o).number);
    } else if(o instanceof LNumber) {
      return value() == ((LNumber) o).value();
    }
    return false;
  }
  
  @Override
  public double value() {
    return number;
  }
  
  @Override
  public boolean integralType() {
    return false;
  }
  
  @Override
  public long bits() {
    return Float.floatToRawIntBits(number);
  }
  
}

class LDoubleNumber extends LNumber {
  
  public final double number;
  public final LNumberType.NumberMode mode;
  
  public LDoubleNumber(double number, LNumberType.NumberMode mode) {
    this.number = number;
    this.mode = mode;
  }
  
  @Override
  public String toPrintString(int flags) {
    if(mode == LNumberType.NumberMode.MODE_NUMBER && number == (double) Math.round(number)) {
      if(Double.doubleToRawLongBits(number) == Double.doubleToRawLongBits(-0.0)) {
        return "-0";
      } else {
        return Long.toString((long) number);
      }
    } else {
      if(Double.isInfinite(number)) {
        return number > 0.0 ? "1e9999" : "-1e9999";
      } else if(Double.isNaN(number)) {
        if(PrintFlag.test(flags, PrintFlag.DISASSEMBLER)) {
          long bits = Double.doubleToRawLongBits(number);
          long canonical = Double.doubleToRawLongBits(Double.NaN);
          if(bits == canonical) {
            return "NaN";
          } else {
            String sign = "+";
            if(bits < 0) {
              bits ^= 0x8000000000000000L;
              sign = "-";
            }
            return "NaN" + sign + Long.toHexString(bits ^ canonical);
          }
        } else {
          // There is normally no way for Lua source code to have a NaN literal.
          // This is the convention of %q.
          // (It may not be the same NaN, but that's probably not achievable in a portable way.)
          return "(0/0)";
        }
      } else {
        return Double.toString(number);
      }
    }
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LDoubleNumber) {
      return Double.doubleToRawLongBits(number) == Double.doubleToRawLongBits(((LDoubleNumber) o).number);
    } else if(o instanceof LNumber) {
      return value() == ((LNumber) o).value();
    }
    return false;
  }
  
  @Override
  public double value() {
    return number;
  }
  
  @Override
  public boolean integralType() {
    return false;
  }
  
  @Override
  public long bits() {
    return Double.doubleToRawLongBits(number);
  }
  
}

class LIntNumber extends LNumber {
  
  public final int number;
  
  public LIntNumber(int number) {
    this.number = number;
  }
  
  @Override
  public String toPrintString(int flags) {    
    return Integer.toString(number);
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LIntNumber) {
      return number == ((LIntNumber) o).number;
    } else if(o instanceof LNumber) {
      return value() == ((LNumber) o).value();
    }
    return false;
  }
  
  @Override
  public double value() {
    return number;
  }
  
  @Override
  public boolean integralType() {
    return true;
  }
  
  @Override
  public long bits() {
    return number;
  }
  
}

class LLongNumber extends LNumber {
  
  public final long number;
  
  public LLongNumber(long number) {
    this.number = number;
  }
  
  @Override
  public String toPrintString(int flags) {    
    return Long.toString(number);
  }
  
  @Override
  public boolean equals(Object o) {
    if(o instanceof LLongNumber) {
      return number == ((LLongNumber) o).number;
    } else if(o instanceof LNumber) {
      return value() == ((LNumber) o).value();
    }
    return false;
  }
  
  @Override
  public double value() {
    return number;
  }
  
  @Override
  public boolean integralType() {
    return true;
  }
  
  @Override
  public long bits() {
    return number;
  }
  
}
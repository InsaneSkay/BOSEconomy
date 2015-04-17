package cosine.boseconomy;

import java.util.Comparator;
import java.util.Locale;

public abstract class Setting<T>
{
  protected static final String PERM_DENY_MESSAGE = "You do not have permission to change this setting.";
  public static final int VALUE_DENY = 0;
  public static final int VALUE_ALLOW = 1;
  public static final int VALUE_REQUEST = 2;
  private String message = "";
  private boolean parseSuccess = false;
  
  public int getSortValue()
  {
    return 0;
  }
  
  public abstract String getName();
  
  public String getStringValue()
  {
    return getDataStringValue();
  }
  
  public abstract String getDataStringValue();
  
  public abstract T getObjectValue();
  
  public abstract T getDefaultValue();
  
  public void setToDefaultValue()
  {
    setValue(getDefaultValue());
  }
  
  public abstract void setValue(T paramT);
  
  public final T parseString(String[] array, int offset)
  {
    return parseString(arrayToString(array, offset));
  }
  
  public abstract T parseString(String paramString);
  
  public final String getMessage()
  {
    return this.message;
  }
  
  protected final void setMessage(String message)
  {
    if (message == null) {
      this.message = "";
    } else {
      this.message = message;
    }
  }
  
  public final boolean parseSucceeded()
  {
    return this.parseSuccess;
  }
  
  public final boolean parseFailed()
  {
    return !this.parseSuccess;
  }
  
  protected final void setParseSuccess(boolean parseSuccess)
  {
    this.parseSuccess = parseSuccess;
  }
  
  protected static String arrayToString(String[] array, int offset)
  {
    if ((array == null) || (offset >= array.length)) {
      return null;
    }
    StringBuilder sb = new StringBuilder(array[offset]);
    for (int i = offset + 1; i < array.length; i++) {
      sb.append(" ").append(array[i]);
    }
    return sb.toString();
  }
  
  protected static boolean isBooleanTrueString(String s, boolean toLowerCase)
  {
    if (toLowerCase) {
      s = s.toLowerCase(Locale.ENGLISH).trim();
    } else {
      s = s.trim();
    }
    return (s.equals("true")) || (s.equals("yes"));
  }
  
  protected static boolean isBooleanFalseString(String s, boolean toLowerCase)
  {
    if (toLowerCase) {
      s = s.toLowerCase(Locale.ENGLISH).trim();
    } else {
      s = s.trim();
    }
    return (s.equals("false")) || (s.equals("no"));
  }
  
  public static class NameComparator
    implements Comparator<Setting<?>>
  {
    public int compare(Setting<?> s1, Setting<?> s2)
    {
      return s1.getName().compareTo(s2.getName());
    }
  }
  
  public static class SortValueComparator
    implements Comparator<Setting<?>>
  {
    public int compare(Setting<?> s1, Setting<?> s2)
    {
      return s1.getSortValue() - s2.getSortValue();
    }
  }
}

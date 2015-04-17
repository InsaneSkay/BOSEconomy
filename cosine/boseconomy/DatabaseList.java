package cosine.boseconomy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class DatabaseList
{
  private ArrayList<ValueEntry> values;
  private ArrayList<ListEntry> lists;
  
  public DatabaseList()
  {
    this.values = new ArrayList();
    this.lists = new ArrayList();
  }
  
  public int getInt(String key)
  {
    try
    {
      return Integer.parseInt(getString(key));
    }
    catch (Exception ex) {}
    return 0;
  }
  
  public long getLong(String key)
  {
    try
    {
      return Long.parseLong(getString(key));
    }
    catch (Exception ex) {}
    return 0L;
  }
  
  public float getFloat(String key)
  {
    try
    {
      return Float.parseFloat(getString(key));
    }
    catch (Exception ex) {}
    return 0.0F;
  }
  
  public double getDouble(String key)
  {
    try
    {
      return Double.parseDouble(getString(key));
    }
    catch (Exception ex) {}
    return 0.0D;
  }
  
  public boolean getBoolean(String key)
  {
    return Boolean.parseBoolean(getString(key));
  }
  
  public String getString(String key)
  {
    for (ValueEntry ve : this.values) {
      if (ve.key.equals(key)) {
        return ve.value;
      }
    }
    return null;
  }
  
  public DatabaseList getList(String key)
  {
    for (ListEntry le : this.lists) {
      if (le.key.equals(key)) {
        return le.list;
      }
    }
    return null;
  }
  
  public DatabaseList getOrCreateList(String key)
  {
    DatabaseList list = getList(key);
    if (list == null)
    {
      list = new DatabaseList();
      setList(key, list);
    }
    return list;
  }
  
  public void setInt(String key, int value)
  {
    setString(key, value);
  }
  
  public void setLong(String key, long value)
  {
    setString(key, value);
  }
  
  public void setFloat(String key, float value)
  {
    setString(key, value);
  }
  
  public void setDouble(String key, double value)
  {
    setString(key, value);
  }
  
  public void setBoolean(String key, boolean value)
  {
    setString(key, value);
  }
  
  public void setString(String key, String value)
  {
    if (key != null) {
      this.values.add(new ValueEntry(key, value == null ? "" : value));
    }
  }
  
  public void setList(String key, DatabaseList list)
  {
    if ((key != null) && (list != null)) {
      this.lists.add(new ListEntry(key, list));
    }
  }
  
  public void removeValueKey(String key)
  {
    if (key != null)
    {
      Iterator<ValueEntry> iterator = this.values.iterator();
      while (iterator.hasNext())
      {
        ValueEntry ve = (ValueEntry)iterator.next();
        if (ve.key.equals(key)) {
          iterator.remove();
        }
      }
    }
  }
  
  public void removeListKey(String key)
  {
    if (key != null)
    {
      Iterator<ListEntry> iterator = this.lists.iterator();
      while (iterator.hasNext())
      {
        ListEntry le = (ListEntry)iterator.next();
        if (le.key.equals(key)) {
          iterator.remove();
        }
      }
    }
  }
  
  public boolean containsValueKey(String key)
  {
    for (ValueEntry ve : this.values) {
      if (ve.key.equals(key)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean containsListKey(String key)
  {
    for (ListEntry le : this.lists) {
      if (le.key.equals(key)) {
        return true;
      }
    }
    return false;
  }
  
  public int getValueCount()
  {
    return this.values.size();
  }
  
  public int getListCount()
  {
    return this.lists.size();
  }
  
  public int getElementCount()
  {
    return getValueCount() + getListCount();
  }
  
  public List<ValueEntry> getValues()
  {
    return this.values;
  }
  
  public List<ListEntry> getLists()
  {
    return this.lists;
  }
  
  public Iterator<ValueEntry> getValueIterator()
  {
    return this.values.iterator();
  }
  
  public Iterator<ListEntry> getListIterator()
  {
    return this.lists.iterator();
  }
  
  public void dump()
  {
    for (ValueEntry entry : this.values) {
      System.out.println("\"" + entry.key + "\" \"" + entry.value + "\"");
    }
    for (ListEntry entry : this.lists)
    {
      System.out.println("\"" + entry.key + "\" {");
      entry.list.dump();
      System.out.println("}");
    }
  }
  
  public class ValueEntry
  {
    public final String key;
    public final String value;
    
    public ValueEntry(String key, String value)
    {
      this.key = key;
      this.value = value;
    }
  }
  
  public class ListEntry
  {
    public final String key;
    public final DatabaseList list;
    
    public ListEntry(String key, DatabaseList list)
    {
      this.key = key;
      this.list = list;
    }
  }
}

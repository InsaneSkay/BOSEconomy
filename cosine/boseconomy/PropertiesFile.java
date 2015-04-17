package cosine.boseconomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

class PropertiesFile
{
  private static final int DEFAULT_SIZE = 16;
  private ArrayList<Entry> list;
  private File file;
  
  public PropertiesFile(String filename)
  {
    this.file = new File(filename);
    this.list = new ArrayList(16);
  }
  
  public String getString(String key)
  {
    return getEntry(key).value;
  }
  
  public int getInt(String key)
  {
    try
    {
      return Integer.parseInt(getEntry(key).value);
    }
    catch (Exception e) {}
    return 0;
  }
  
  public double getDouble(String key)
  {
    try
    {
      return Double.parseDouble(getEntry(key).value);
    }
    catch (Exception e) {}
    return 0.0D;
  }
  
  public boolean getBoolean(String key)
  {
    return Boolean.parseBoolean(getEntry(key).value);
  }
  
  public void setString(String key, String value)
  {
    getEntry(key).setValue(value);
  }
  
  public void setInt(String key, int value)
  {
    getEntry(key).setValue(value);
  }
  
  public void setDouble(String key, double value)
  {
    getEntry(key).setValue(value);
  }
  
  public void setBoolean(String key, boolean value)
  {
    getEntry(key).setValue(value);
  }
  
  public void setStringIfAbsent(String key, String value)
  {
    addEntryIfAbsent(key, value);
  }
  
  public void setIntIfAbsent(String key, int value)
  {
    addEntryIfAbsent(key, value);
  }
  
  public void setDoubleIfAbsent(String key, double value)
  {
    addEntryIfAbsent(key, value);
  }
  
  public void setBooleanIfAbsent(String key, boolean value)
  {
    addEntryIfAbsent(key, value);
  }
  
  public boolean removeKey(String key)
  {
    for (int i = 0; i < this.list.size(); i++) {
      if (((Entry)this.list.get(i)).key.equalsIgnoreCase(key))
      {
        this.list.remove(i);
        return true;
      }
    }
    return false;
  }
  
  public boolean keyExists(String key)
  {
    for (int i = 0; i < this.list.size(); i++) {
      if (((Entry)this.list.get(i)).key.equalsIgnoreCase(key)) {
        return true;
      }
    }
    return false;
  }
  
  public Entry getEntry(String key)
  {
    for (int i = 0; i < this.list.size(); i++) {
      if (((Entry)this.list.get(i)).key.equalsIgnoreCase(key)) {
        return (Entry)this.list.get(i);
      }
    }
    return addEntry(key, null);
  }
  
  private Entry addEntry(String key, String value)
  {
    Entry entry = new Entry(key, value);
    this.list.add(entry);
    return entry;
  }
  
  private Entry addEntryIfAbsent(String key, String value)
  {
    for (int i = 0; i < this.list.size(); i++) {
      if (((Entry)this.list.get(i)).key.equalsIgnoreCase(key)) {
        return null;
      }
    }
    return addEntry(key, value);
  }
  
  public synchronized void save()
    throws IOException
  {
    Writer out = null;
    try
    {
      if ((!this.file.exists()) || (!this.file.isFile())) {
        this.file.createNewFile();
      }
      out = new OutputStreamWriter(new FileOutputStream(this.file));
      for (int i = 0; i < this.list.size(); i++) {
        out.write(((Entry)this.list.get(i)).key + "=" + ((Entry)this.list.get(i)).getValue() + 
          System.getProperty("line.separator"));
      }
    }
    finally
    {
      out.close();
    }
  }
  
  public void load()
    throws IOException
  {
    Scanner scan = null;
    this.list = new ArrayList(16);
    try
    {
      if (this.file.canRead())
      {
        scan = new Scanner(this.file);
        while (scan.hasNextLine()) {
          try
          {
            Scanner scan2 = new Scanner(scan.nextLine());
            scan2.useDelimiter("=");
            String key = scan2.next();
            String value = scan2.nextLine().substring(1);
            setString(key, value);
            scan2.close();
          }
          catch (Exception localException) {}
        }
      }
    }
    finally
    {
      if (scan != null) {
        scan.close();
      }
    }
  }
  
  public File getFile()
  {
    return this.file;
  }
  
  private class Entry
  {
    final String key;
    private String value;
    
    public Entry(String key, String value)
    {
      this.key = (key == null ? "" : key);
      setValue(value);
    }
    
    public String getValue()
    {
      return this.value;
    }
    
    public void setValue(String value)
    {
      this.value = (value == null ? "" : value);
    }
  }
}

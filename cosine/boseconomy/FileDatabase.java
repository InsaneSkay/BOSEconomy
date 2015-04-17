package cosine.boseconomy;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

class FileDatabase
  extends Database
{
  protected static final int TYPE_TEXT = 0;
  protected static final int TYPE_BINARY = 1;
  protected static final int ENTRY_NULL = 0;
  protected static final int ENTRY_VALUE = 1;
  protected static final int ENTRY_LIST = 2;
  protected static final int ENTRY_LISTEND = 3;
  private char[] cbuf;
  private int cbufIndex;
  private boolean wasBracket;
  
  public FileDatabase(BOSEconomy plugin, int managerCount)
  {
    super(plugin, managerCount);
  }
  
  public FileDatabase(BOSEconomy plugin)
  {
    super(plugin);
  }
  
  public void addDatabaseManager(DatabaseManager manager, String label)
  {
    addDatabaseManager(manager, label, null, 0);
  }
  
  public void addDatabaseManager(DatabaseManager manager, String label, File file, int type)
  {
    if (manager != null) {
      this.managers.add(new FileManagerData(manager, label, file, type));
    }
  }
  
  public void addDatabaseManager(DatabaseManager manager, String label, String file, int type)
  {
    addDatabaseManager(manager, label, new File(file), type);
  }
  
  public final synchronized void refresh()
  {
    this.root = new DatabaseList();
    for (Database.ManagerData managerData : this.managers) {
      if (((FileManagerData)managerData).file != null) {
        readFile((FileManagerData)managerData);
      }
    }
  }
  
  public final synchronized void commit()
  {
    for (Database.ManagerData managerData : this.managers) {
      if ((managerData.manager.getChanged()) && 
        (((FileManagerData)managerData).file != null))
      {
        writeFile((FileManagerData)managerData);
        managerData.manager.setChanged(false);
      }
    }
  }
  
  private void readFile(FileManagerData managerData)
    throws NullPointerException
  {
    if (managerData == null) {
      throw new NullPointerException(
        "Attempted to read null file into FileDatabase.");
    }
    if (this.plugin.debug) {
      this.plugin.sendConsoleMessage("[BOSEconomy Debug] Reading database file '" + 
        managerData.file.getPath() + "'.");
    }
    this.cbuf = null;
    this.cbufIndex = 0;
    FileReader reader = null;
    try
    {
      if (!managerData.file.canRead()) {
        return;
      }
      reader = new FileReader(managerData.file);
      this.cbuf = new char[(int)managerData.file.length()];
      if (reader.read(this.cbuf) != this.cbuf.length) {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Failed to load '" + 
          managerData.file.getPath() + "' into the buffer.");
      }
      reader.close();
      if (managerData.type == 0) {
        readTextFile(managerData);
      } else if (managerData.type == 1) {
        readBinaryFile(managerData);
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Failed to load '" + 
        managerData.file.getPath() + "'.");
      if (reader != null) {
        try
        {
          reader.close();
        }
        catch (IOException ex)
        {
          ex.printStackTrace();
        }
      }
    }
    finally
    {
      if (reader != null) {
        try
        {
          reader.close();
        }
        catch (IOException ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }
  
  private void readTextFile(FileManagerData managerData)
  {
    DatabaseList rootList = new DatabaseList();
    
    LinkedList<DatabaseList> listStack = new LinkedList();
    listStack.push(rootList);
    while (this.cbufIndex < this.cbuf.length)
    {
      if (skipNewlines()) {
        break;
      }
      skipWhitespace();
      String key = readToken(true);
      if ((key == null) && (this.wasBracket))
      {
        if (listStack.poll() == rootList)
        {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Encountered an unmatched closing list bracket in file '" + 
            managerData.file.getPath() + "'. (Character " + (this.cbufIndex - 1) + 
            ")");
          break;
        }
      }
      else
      {
        skipWhitespace();
        String value = readToken(false);
        if ((value == null) && (this.wasBracket))
        {
          DatabaseList newList = new DatabaseList();
          

          ((DatabaseList)listStack.getFirst()).setList(key == null ? "" : key, newList);
          listStack.push(newList);
        }
        else if (value == null)
        {
          ((DatabaseList)listStack.peek()).setString(key, "");
        }
        else
        {
          ((DatabaseList)listStack.getFirst()).setString(key == null ? "" : key, value);
        }
      }
    }
    getRoot().setList(managerData.listName, rootList);
  }
  
  private void readBinaryFile(FileManagerData managerData)
  {
    DatabaseList rootList = new DatabaseList();
    
    LinkedList<DatabaseList> listStack = new LinkedList();
    listStack.push(rootList);
    while (this.cbufIndex < this.cbuf.length)
    {
      int entryType = readByte();
      if (entryType == 0)
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Binary file '" + 
          managerData.file.getPath() + "' terminated unexpectedly.");
        break;
      }
      if (entryType == 1)
      {
        String key = readString();
        String value = readString();
        if ((key == null) || (value == null))
        {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Binary file '" + 
            managerData.file.getPath() + "' terminated unexpectedly.");
          break;
        }
        ((DatabaseList)listStack.getFirst()).setString(key, value);
      }
      else if (entryType == 2)
      {
        String key = readString();
        
        DatabaseList newList = new DatabaseList();
        ((DatabaseList)listStack.getFirst()).setList(key == null ? "" : key, newList);
        listStack.push(newList);
      }
      else if (entryType == 3)
      {
        if (listStack.poll() == rootList)
        {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Encountered an unexpected list ending tag in binary file '" + 
            managerData.file.getPath() + "'. (Character " + (this.cbufIndex - 1) + 
            ")");
          break;
        }
      }
      else
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Encountered unexpected entry type " + entryType + 
          " in binary file '" + managerData.file.getPath() + 
          "'. The file might be corrupt.");
        break;
      }
    }
    getRoot().setList(managerData.listName, rootList);
  }
  
  private void writeFile(FileManagerData managerData)
    throws NullPointerException
  {
    if (managerData == null) {
      throw new NullPointerException(
        "Attempted to write to null file from FileDatabase.");
    }
    try
    {
      File parent = managerData.file.getParentFile();
      if ((parent != null) && (!parent.isDirectory()) && (!parent.mkdirs()))
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Failed to create parent directories for file '" + 
          managerData.file.getPath() + "'.");
        return;
      }
      if (this.plugin.debug) {
        this.plugin.sendConsoleMessage("[BOSEconomy Debug] Writing database file '" + 
          managerData.file.getPath() + "'.");
      }
      DataOutputStream out = 
        new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
        managerData.file)));
      


      DatabaseList rootList = 
        getRoot().getOrCreateList(managerData.listName);
      



      LinkedList<DatabaseList> listStack = new LinkedList();
      LinkedList<Iterator<DatabaseList.ListEntry>> listIteratorStack = 
        new LinkedList();
      listStack.push(rootList);
      listIteratorStack.push(rootList.getListIterator());
      
      String newline = System.getProperty("line.separator");
      boolean listMode = false;
      for (;;)
      {
        if (!listMode)
        {
          Iterator<DatabaseList.ValueEntry> valueIterator = 
            ((DatabaseList)listStack.peek()).getValueIterator();
          while (valueIterator.hasNext())
          {
            DatabaseList.ValueEntry entry = (DatabaseList.ValueEntry)valueIterator.next();
            if (managerData.type == 0)
            {
              for (int i = 1; i < listStack.size(); i++) {
                out.writeBytes("\t");
              }
              if ((entry.key.contains(" ")) || (entry.key.contains("\t")) || 
                (entry.key.contains("{")) || (entry.key.contains("}"))) {
                out.writeBytes("\"" + entry.key.replaceAll("\"", "") + "\" ");
              } else {
                out.writeBytes(entry.key.replaceAll("\"", "") + " ");
              }
              if ((entry.value.contains("\n")) || 
                (entry.value.contains("\r")) || 
                (entry.value.contains("{")) || 
                (entry.value.contains("}")) || (
                (entry.value.length() > 0) && ((entry.value.charAt(0) == ' ') || 
                (entry.value.charAt(0) == '\t')))) {
                out.writeBytes("\"" + entry.value.replaceAll("\"", "") + "\"" + 
                  newline);
              } else {
                out.writeBytes(entry.value.replaceAll("\"", "") + newline);
              }
            }
            else if (managerData.type == 1)
            {
              out.writeByte(1);
              out.writeInt(entry.key.length());
              out.writeBytes(entry.key);
              out.writeInt(entry.value.length());
              out.writeBytes(entry.value);
            }
          }
          listMode = true;
        }
        else if (((Iterator)listIteratorStack.peek()).hasNext())
        {
          listMode = false;
          

          DatabaseList.ListEntry nextListEntry = 
            (DatabaseList.ListEntry)((Iterator)listIteratorStack.peek()).next();
          if (managerData.type == 0)
          {
            for (int i = 1; i < listStack.size(); i++) {
              out.writeBytes("\t");
            }
            if ((nextListEntry.key.contains(" ")) || 
              (nextListEntry.key.contains("\t")) || 
              (nextListEntry.key.contains("{")) || 
              (nextListEntry.key.contains("}"))) {
              out.writeBytes("\"" + nextListEntry.key.replaceAll("\"", "") + 
                "\"");
            } else {
              out.writeBytes(nextListEntry.key.replaceAll("\"", ""));
            }
            out.writeBytes(" {" + newline);
          }
          else if (managerData.type == 1)
          {
            out.writeByte(2);
            out.writeInt(nextListEntry.key.length());
            out.writeBytes(nextListEntry.key);
          }
          listStack.push(nextListEntry.list);
          listIteratorStack.push(nextListEntry.list.getListIterator());
        }
        else
        {
          if (listStack.poll() == rootList) {
            break;
          }
          listIteratorStack.poll();
          if (managerData.type == 0)
          {
            for (int i = 1; i < listStack.size(); i++) {
              out.writeBytes("\t");
            }
            out.writeBytes("}" + newline);
          }
          else if (managerData.type == 1)
          {
            out.writeByte(3);
          }
        }
      }
      out.close();
    }
    catch (IOException ex)
    {
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Failed to write file '" + 
        managerData.file.getPath() + "'.");
      ex.printStackTrace();
    }
  }
  
  private String readToken(boolean isKey)
  {
    this.wasBracket = false;
    if (this.cbufIndex < this.cbuf.length)
    {
      if (isKey)
      {
        if (this.cbuf[this.cbufIndex] == '}')
        {
          this.wasBracket = true;
          this.cbufIndex += 1;
          return null;
        }
      }
      else if (this.cbuf[this.cbufIndex] == '{')
      {
        this.wasBracket = true;
        this.cbufIndex += 1;
        return null;
      }
      int startIndex = this.cbufIndex;
      int trailingWhitespace = 0;
      boolean quoteMode = false;
      while (this.cbufIndex < this.cbuf.length)
      {
        if (quoteMode)
        {
          if (this.cbuf[this.cbufIndex] == '"') {
            quoteMode = false;
          }
        }
        else if (this.cbuf[this.cbufIndex] == '"')
        {
          quoteMode = true;
        }
        else if ((this.cbuf[this.cbufIndex] == ' ') || (this.cbuf[this.cbufIndex] == '\t'))
        {
          if (isKey) {
            break;
          }
          trailingWhitespace++;
        }
        else
        {
          if ((this.cbuf[this.cbufIndex] == '\n') || (this.cbuf[this.cbufIndex] == '\r') || 
            (this.cbuf[this.cbufIndex] == '{') || (this.cbuf[this.cbufIndex] == '}')) {
            break;
          }
          trailingWhitespace = 0;
        }
        this.cbufIndex += 1;
      }
      if (startIndex == this.cbufIndex) {
        return null;
      }
      return 
        String.valueOf(this.cbuf, startIndex, this.cbufIndex - startIndex - trailingWhitespace).replaceAll("\"", "");
    }
    return null;
  }
  
  private int readByte()
  {
    if (this.cbufIndex > this.cbuf.length - 1)
    {
      this.cbufIndex = this.cbuf.length;
      return 0;
    }
    int value = this.cbuf[this.cbufIndex];
    this.cbufIndex += 1;
    return value;
  }
  
  private int readInt()
  {
    if (this.cbufIndex > this.cbuf.length - 4)
    {
      this.cbufIndex = this.cbuf.length;
      return 0;
    }
    int value = 
      this.cbuf[(this.cbufIndex + 3)] | this.cbuf[(this.cbufIndex + 2)] >> '\b' | 
      this.cbuf[(this.cbufIndex + 1)] >> '\020' | this.cbuf[this.cbufIndex] >> '\030';
    this.cbufIndex += 4;
    return value;
  }
  
  private String readString()
  {
    int length = readInt();
    if (this.cbufIndex > this.cbuf.length - length)
    {
      this.cbufIndex = this.cbuf.length;
      return null;
    }
    if (length == 0) {
      return "";
    }
    String value = String.valueOf(this.cbuf, this.cbufIndex, length);
    this.cbufIndex += length;
    return value;
  }
  
  private void skipWhitespace()
  {
    while (this.cbufIndex < this.cbuf.length)
    {
      if ((this.cbuf[this.cbufIndex] != ' ') && (this.cbuf[this.cbufIndex] != '\t')) {
        break;
      }
      this.cbufIndex += 1;
    }
  }
  
  private boolean skipNewlines()
  {
    while (this.cbufIndex < this.cbuf.length)
    {
      if ((this.cbuf[this.cbufIndex] != '\n') && (this.cbuf[this.cbufIndex] != '\r')) {
        return false;
      }
      this.cbufIndex += 1;
    }
    return true;
  }
  
  public void generateAllFiles()
  {
    for (Database.ManagerData managerData : this.managers) {
      if ((managerData instanceof FileManagerData)) {
        try
        {
          ((FileManagerData)managerData).file.createNewFile();
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "A problem occurred while generating the blank file '" + 
            ((FileManagerData)managerData).file.getPath() + "'.");
        }
      }
    }
  }
  
  protected class FileManagerData
    extends Database.ManagerData
  {
    public final File file;
    public final int type;
    
    public FileManagerData(DatabaseManager manager, String listName, File file, int type)
    {
      super(manager, listName);
      this.file = file;
      this.type = type;
    }
  }
}

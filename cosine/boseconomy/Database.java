package cosine.boseconomy;

import java.util.ArrayList;

abstract class Database
{
  protected final BOSEconomy plugin;
  protected DatabaseList root;
  protected ArrayList<ManagerData> managers;
  
  public Database(BOSEconomy plugin, int managerCount)
  {
    this.plugin = plugin;
    this.root = new DatabaseList();
    this.managers = new ArrayList(managerCount);
  }
  
  public Database(BOSEconomy plugin)
  {
    this(plugin, 8);
  }
  
  public DatabaseList getRoot()
  {
    return this.root;
  }
  
  public void addDatabaseManager(DatabaseManager manager, String label)
  {
    if (manager != null) {
      this.managers.add(new ManagerData(manager, label));
    }
  }
  
  public final void refreshManagers()
  {
    for (ManagerData managerData : this.managers) {
      managerData.manager.refresh(this, managerData.listName);
    }
  }
  
  public final void commitManagers()
  {
    for (ManagerData managerData : this.managers) {
      if (managerData.manager.getChanged()) {
        managerData.manager.commit(this, managerData.listName);
      }
    }
  }
  
  public void setManagersChanged(boolean changed)
  {
    for (ManagerData managerData : this.managers) {
      managerData.manager.setChanged(changed);
    }
  }
  
  public ManagerData getManagerData(DatabaseManager manager)
  {
    for (ManagerData managerData : this.managers) {
      if (managerData.manager == manager) {
        return managerData;
      }
    }
    return null;
  }
  
  public abstract void refresh();
  
  public abstract void commit();
  
  public class ManagerData
  {
    public final DatabaseManager manager;
    public final String listName;
    
    public ManagerData(DatabaseManager manager, String listName)
    {
      this.manager = manager;
      this.listName = listName;
    }
  }
}

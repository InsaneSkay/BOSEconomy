package cosine.boseconomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SettingsGroup<T extends Setting<?>>
  implements Iterable<T>
{
  private SettingsGroup<T> parent = null;
  private final HashMap<String, T> settings;
  
  public SettingsGroup()
  {
    this(null);
  }
  
  public SettingsGroup(SettingsGroup<T> parent)
  {
    this.settings = new HashMap();
    setParent(parent);
  }
  
  public T getSetting(String name)
  {
    T s = (Setting)this.settings.get(name);
    if (s == null)
    {
      if (this.parent == null) {
        return null;
      }
      return this.parent.getSetting(name);
    }
    return s;
  }
  
  public boolean hasSetting(String name)
  {
    return getSetting(name) != null;
  }
  
  public void addSetting(T setting)
  {
    this.settings.put(setting.getName(), setting);
  }
  
  public T removeSetting(String name)
  {
    return (Setting)this.settings.remove(name);
  }
  
  public void setParent(SettingsGroup<T> parent)
  {
    this.parent = parent;
  }
  
  public SettingsGroup<T> getParent()
  {
    return this.parent;
  }
  
  public Iterator<T> iterator()
  {
    return this.settings.values().iterator();
  }
  
  public List<T> getSettingList()
  {
    return new ArrayList(this.settings.values());
  }
}

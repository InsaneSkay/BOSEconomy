package cosine.boseconomy;

import cosine.boseconomy.bracket.membertype.BracketMemberType;
import cosine.boseconomy.bracket.membertype.CanChangeValueSetting;
import cosine.boseconomy.bracket.membertype.IsAdminTypeSetting;
import cosine.boseconomy.bracket.setting.BracketExcludedSetting;
import cosine.boseconomy.bracket.setting.BracketSetting;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BracketManager
  extends DatabaseManager
{
  private final BOSEconomy plugin;
  private final PaymentManager paymentManager;
  private final HashMap<String, Bracket> brackets;
  private long lastPayday;
  private final List<String> permissionNodeList;
  private final BracketMemberType adminMemberType;
  
  public BracketManager(BOSEconomy plugin)
  {
    this.plugin = plugin;
    this.paymentManager = new PaymentManager();
    this.lastPayday = System.currentTimeMillis();
    this.permissionNodeList = new LinkedList();
    this.brackets = new HashMap();
    
    this.adminMemberType = new BracketMemberType();
    this.adminMemberType.getSettings().addSetting(new IsAdminTypeSetting(true));
    this.adminMemberType.getSettings().addSetting(new CanChangeValueSetting(true, null));
  }
  
  public Bracket getBracket(String name)
  {
    if (name == null) {
      return null;
    }
    return (Bracket)this.brackets.get(name.toLowerCase());
  }
  
  public boolean containsBracket(Bracket bracket)
  {
    return this.brackets.containsValue(bracket);
  }
  
  public void addBracket(Bracket bracket)
  {
    if (bracket != null)
    {
      String name = bracket.getName().toLowerCase();
      bracket.setManager(this);
      this.brackets.put(name, bracket);
      updatePermissionNodeList();
      for (Bracket.BracketMember bm : bracket.getMemberList()) {
        if ((bm.getAccount() instanceof PlayerAccount)) {
          ((PlayerAccount)bm.getAccount()).updatePermissionNodes();
        }
      }
      setChanged();
    }
  }
  
  public void removeBracket(String name)
  {
    if (name != null)
    {
      Bracket b = (Bracket)this.brackets.remove(name.toLowerCase());
      if (b != null)
      {
        updatePermissionNodeList();
        b.notifyRemoval();
        b.setManager(null);
        setChanged();
        if (this.plugin.getRequestHandler() != null) {
          this.plugin.getRequestHandler().checkRequestValidities();
        }
      }
    }
  }
  
  public void clearBrackets()
  {
    this.brackets.clear();
    this.plugin.getAccountManager().clearAccountBrackets();
    updatePermissionNodeList();
    this.plugin.getAccountManager().updatePlayerPermissionNodes();
    setChanged();
    if (this.plugin.getRequestHandler() != null) {
      this.plugin.getRequestHandler().checkRequestValidities();
    }
  }
  
  public void renameBracket(String oldName, String newName)
  {
    if ((oldName != null) && (newName != null) && (this.brackets.get(newName) == null) && 
      (!oldName.equalsIgnoreCase(newName)))
    {
      Bracket bracket = (Bracket)this.brackets.remove(oldName.toLowerCase());
      if (bracket != null)
      {
        bracket.setName(newName);
        this.brackets.put(bracket.getName().toLowerCase(), bracket);
      }
    }
  }
  
  public PaySession buildPaySession(long currentTime)
  {
    int paydayIntervalSeconds = 
      this.plugin.getSettingsManager().getPaydayInterval().getSeconds();
    PaySession paySession = new PaySession(this.plugin);
    Bracket b;
    if (paydayIntervalSeconds > 0)
    {
      int timeIntervalSeconds = (int)((currentTime - this.lastPayday) / 1000L);
      if (timeIntervalSeconds >= paydayIntervalSeconds)
      {
        this.lastPayday = (this.lastPayday + paydayIntervalSeconds * 1000L * (
          timeIntervalSeconds / paydayIntervalSeconds));
        for (Iterator localIterator = this.brackets.values().iterator(); localIterator.hasNext();)
        {
          b = (Bracket)localIterator.next();
          b.buildPaySession(this.lastPayday, paySession, true);
        }
      }
    }
    else
    {
      this.lastPayday = 0L;
      for (Bracket b : this.brackets.values()) {
        b.buildPaySession(currentTime, paySession, false);
      }
    }
    return paySession;
  }
  
  public void updateOnlineTimes(long currentTime)
  {
    for (Bracket b : this.brackets.values()) {
      b.updateOnlineTimes(currentTime);
    }
  }
  
  public Bracket getDefaultBracket()
  {
    if (this.plugin.getSettingsManager().getDefaultBracket() == null) {
      return null;
    }
    Bracket defaultBracket = 
      getBracket(this.plugin.getSettingsManager().getDefaultBracket());
    if (defaultBracket == null)
    {
      defaultBracket = 
        new WageBracket(this, 
        this.plugin.getSettingsManager().getDefaultBracket(), 0.0D);
      BracketExcludedSetting s = 
        (BracketExcludedSetting)defaultBracket.getSetting("excluded");
      if (s != null) {
        s.setValue(Boolean.valueOf(true));
      }
      addBracket(defaultBracket);
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
        "Automatically created the default bracket '" + 
        defaultBracket.getName() + "'.");
    }
    return defaultBracket;
  }
  
  public int getBracketCount()
  {
    return this.brackets.size();
  }
  
  public Iterator<Bracket> getIterator()
  {
    return this.brackets.values().iterator();
  }
  
  public List<Bracket> getBracketList()
  {
    return new LinkedList(this.brackets.values());
  }
  
  public void setLastPayday(long lastPayday)
  {
    this.lastPayday = lastPayday;
    this.plugin.getBracketManager().getPaymentManager().setChanged();
  }
  
  public long getLastPayday()
  {
    return this.lastPayday;
  }
  
  public List<String> getPermissionNodeList()
  {
    return this.permissionNodeList;
  }
  
  public void updatePermissionNodeList()
  {
    this.permissionNodeList.clear();
    Iterator localIterator2;
    for (Iterator localIterator1 = this.brackets.values().iterator(); localIterator1.hasNext(); localIterator2.hasNext())
    {
      Bracket b = (Bracket)localIterator1.next();
      localIterator2 = b.getPermissionNodes().iterator(); continue;String node = (String)localIterator2.next();
      if (!this.permissionNodeList.contains(node)) {
        this.permissionNodeList.add(node);
      }
    }
  }
  
  public void refresh(Database database, String listName)
  {
    if (this.plugin.debug) {
      this.plugin.sendConsoleMessage("[BOSEconomy Debug] Refreshing bracket data from the database.");
    }
    clearBrackets();
    
    DatabaseList bracketsList = database.getRoot().getList(listName);
    if (bracketsList == null)
    {
      getDefaultBracket();
      setChanged();
    }
    else
    {
      Iterator<DatabaseList.ListEntry> bracketIterator = 
        bracketsList.getListIterator();
      while (bracketIterator.hasNext())
      {
        DatabaseList.ListEntry entry = (DatabaseList.ListEntry)bracketIterator.next();
        Bracket b = null;
        String type = entry.list.getString("type");
        if (type == null)
        {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Encountered bracket with no specified type while refreshing bracket data.");
        }
        else
        {
          if (type.equalsIgnoreCase("wage"))
          {
            b = new WageBracket(this, entry.key);
          }
          else if (type.equalsIgnoreCase("sub"))
          {
            b = new SubBracket(this, entry.key);
          }
          else
          {
            this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
              "Encountered unknown bracket type '" + type + "' while refreshing bracket data.");
            continue;
          }
          Iterator<DatabaseList.ValueEntry> settingIterator = 
            entry.list.getOrCreateList("settings").getValueIterator();
          while (settingIterator.hasNext())
          {
            DatabaseList.ValueEntry settingEntry = (DatabaseList.ValueEntry)settingIterator.next();
            BracketSetting<?> s = b.getSetting(settingEntry.key);
            if (s == null) {
              this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
                "Encountered unrecognized setting '" + settingEntry.key + 
                "' while loading bracket '" + b.getName() + "'.");
            } else if (!s.setValue(null, b, settingEntry.value, true, false, false)) {
              this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Encountered bad value '" + 
                settingEntry.value + "' for setting '" + s.getName() + 
                "' while loading bracket '" + b.getName() + "': " + s.getMessage() + 
                " Using value '" + s.getDataStringValue() + "' instead.");
            }
          }
          Iterator<DatabaseList.ValueEntry> memberIterator = 
            entry.list.getOrCreateList("members").getValueIterator();
          while (memberIterator.hasNext())
          {
            DatabaseList.ValueEntry memberEntry = (DatabaseList.ValueEntry)memberIterator.next();
            double count = 1.0D;
            try
            {
              count = Double.parseDouble(memberEntry.value);
            }
            catch (Exception localException) {}
            Bracket.BracketMember member = 
              b.addMember(this.plugin.getAccountManager().getAccountByName(
              memberEntry.key));
            if (member != null) {
              member.setCount(count);
            }
          }
          addBracket(b);
        }
      }
      setChanged(false);
    }
  }
  
  public void commit(Database database, String listName)
  {
    DatabaseList bracketsList = new DatabaseList();
    for (Bracket b : this.brackets.values())
    {
      DatabaseList thisBracketList = new DatabaseList();
      if ((b instanceof WageBracket))
      {
        thisBracketList.setString("type", "wage");
      }
      else if ((b instanceof SubBracket))
      {
        thisBracketList.setString("type", "sub");
      }
      else
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Encountered unknown bracket class '" + 
          b.getClass().getSimpleName() + 
          "' while committing bracket data.");
        continue;
      }
      DatabaseList settingsList = new DatabaseList();
      Iterator<BracketSetting<?>> settingIterator = b.getSettingIterator();
      while (settingIterator.hasNext())
      {
        BracketSetting<?> s = (BracketSetting)settingIterator.next();
        settingsList.setString(s.getName(), s.getDataStringValue());
      }
      thisBracketList.setList("settings", settingsList);
      

      DatabaseList memberList = new DatabaseList();
      Iterator<Bracket.BracketMember> memberIterator = b.getMemberIterator();
      while (memberIterator.hasNext())
      {
        Bracket.BracketMember member = (Bracket.BracketMember)memberIterator.next();
        
        memberList.setDouble(member.getAccount().getDataName(), member.getCount());
      }
      thisBracketList.setList("members", memberList);
      

      bracketsList.setList(b.getName(), thisBracketList);
    }
    database.getRoot().removeListKey(listName);
    database.getRoot().setList(listName, bracketsList);
  }
  
  public BracketMemberType getAdminMemberType()
  {
    return this.adminMemberType;
  }
  
  public PaymentManager getPaymentManager()
  {
    return this.paymentManager;
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
  
  public class PaymentManager
    extends DatabaseManager
  {
    public PaymentManager() {}
    
    public void refresh(Database database, String listName)
    {
      if (BracketManager.this.plugin.debug) {
        BracketManager.this.plugin.sendConsoleMessage("[BOSEconomy Debug] Refreshing payment data from the database.");
      }
      DatabaseList paymentsList = database.getRoot().getOrCreateList(listName);
      

      BracketManager.this.setLastPayday(paymentsList.getLong("lastPayday"));
      

      Iterator<DatabaseList.ListEntry> paymentIterator = 
        paymentsList.getListIterator();
      while (paymentIterator.hasNext())
      {
        DatabaseList.ListEntry entry = (DatabaseList.ListEntry)paymentIterator.next();
        

        Bracket b = BracketManager.this.getBracket(entry.key);
        if (b == null)
        {
          BracketManager.this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Encountered payment data for a bracket named '" + entry.key + 
            "', but no such bracket could be found.");
        }
        else
        {
          b.setLastTime(entry.list.getLong("lastTime"));
          if (b.getOnlineMode())
          {
            Iterator<DatabaseList.ValueEntry> memberIterator = 
              entry.list.getOrCreateList("times").getValueIterator();
            while (memberIterator.hasNext())
            {
              DatabaseList.ValueEntry memberEntry = (DatabaseList.ValueEntry)memberIterator.next();
              int time = 0;
              try
              {
                time = Integer.parseInt(memberEntry.value);
              }
              catch (Exception localException) {}
              Bracket.BracketMember member = b.getMember(memberEntry.key);
              if (member == null) {
                BracketManager.this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
                  "Encountered time data for a member of the '" + b.getName() + 
                  "' bracket named '" + entry.key + 
                  "', but that account is not a member.");
              } else {
                member.setOnlineTime(time);
              }
            }
          }
        }
      }
      setChanged(false);
    }
    
    public void commit(Database database, String listName)
    {
      DatabaseList paymentsList = new DatabaseList();
      


      paymentsList.setLong("lastPayday", BracketManager.this.getLastPayday());
      for (Bracket b : BracketManager.this.brackets.values())
      {
        DatabaseList thisPaymentList = new DatabaseList();
        

        thisPaymentList.setLong("lastTime", b.getLastTime());
        if (b.getOnlineMode())
        {
          DatabaseList memberTimeList = new DatabaseList();
          Iterator<Bracket.BracketMember> memberIterator = b.getMemberIterator();
          while (memberIterator.hasNext())
          {
            Bracket.BracketMember member = (Bracket.BracketMember)memberIterator.next();
            
            memberTimeList.setInt(member.getAccount().getDataName(), member.getOnlineTime());
          }
          thisPaymentList.setList("times", memberTimeList);
        }
        paymentsList.setList(b.getName(), thisPaymentList);
      }
      database.getRoot().removeListKey(listName);
      database.getRoot().setList(listName, paymentsList);
    }
  }
}

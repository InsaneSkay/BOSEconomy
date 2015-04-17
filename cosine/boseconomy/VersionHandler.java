package cosine.boseconomy;

import cosine.boseconomy.bracket.setting.BracketExcludedSetting;
import cosine.boseconomy.bracket.setting.BracketOnlineModeSetting;
import cosine.boseconomy.bracket.setting.BracketValueSetting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

class VersionHandler
{
  private BOSEconomy plugin;
  private String version = null;
  private boolean needSettingsSave = false;
  private boolean needBracketSave = false;
  private boolean needDatabaseRefresh;
  private boolean useOnlineWages;
  
  public VersionHandler(BOSEconomy plugin)
  {
    this.plugin = plugin;
    loadVersion();
  }
  
  public boolean handleVersion()
  {
    this.needDatabaseRefresh = true;
    this.needSettingsSave = false;
    this.needBracketSave = false;
    boolean success = updateTo0_7_8_1();
    saveVersion();
    return success;
  }
  
  private boolean updateTo0_6_0()
  {
    if (!getVersion().equals("0.6.0"))
    {
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
        "Encountered unsupported version '" + this.version + "'!");
      return false;
    }
    return true;
  }
  
  private boolean updateTo0_6_1()
  {
    if (!getVersion().equals("0.6.1")) {
      if (updateTo0_6_0())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.6.1 format.");
        


        setVersion("0.6.1");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_6_2()
  {
    if (!getVersion().equals("0.6.2")) {
      if (updateTo0_6_1())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.6.2 format.");
        


        setVersion("0.6.2");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_0()
  {
    if (!getVersion().equals("0.7.0")) {
      if (updateTo0_6_2())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.0 format.");
        




        PropertiesFile propsFile = 
          new PropertiesFile("plugins/BOSEconomy/BOSEconomy.properties");
        if (propsFile.getFile().canRead()) {
          try
          {
            propsFile.load();
            

            SettingsManager settings = this.plugin.getSettingsManager();
            if (propsFile.keyExists("money-name")) {
              settings.setMoneyName(propsFile.getString("money-name"));
            }
            if (propsFile.keyExists("money-name-plural")) {
              settings.setMoneyNamePlural(propsFile
                .getString("money-name-plural"));
            }
            if (propsFile.keyExists("initial-money")) {
              settings.setInitialMoney(propsFile.getDouble("initial-money"));
            }
            if ((propsFile.keyExists("wage-interval")) && 
              (propsFile.keyExists("wage-interval-unit"))) {
              settings.setDefaultBracketInterval(new TimeInterval(propsFile
                .getInt("wage-interval"), propsFile
                .getString("wage-interval-unit")));
            }
            settings.setPaydayInterval(settings.getDefaultBracketInterval());
            if (propsFile.keyExists("default-bracket")) {
              settings.setDefaultBracket(propsFile.getString("default-bracket"));
            }
            if (propsFile.keyExists("debug-mode")) {
              this.plugin.debug = propsFile.getBoolean("debug-mode");
            }
            if (propsFile.keyExists("op-permissions")) {
              settings.setUseOpPermissions(propsFile
                .getBoolean("op-permissions"));
            }
            this.useOnlineWages = 
              ((propsFile.keyExists("online-wages")) && 
              (propsFile.getBoolean("online-wages")));
          }
          catch (IOException ex)
          {
            ex.printStackTrace();
            this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
              "Failed to load the properties file. Settings will be reverted to their default values.");
          }
        }
        File bracketsFile = 
          new File("plugins/BOSEconomy/brackets.txt");
        Scanner bracketsScan = null;
        try
        {
          if (bracketsFile.canRead())
          {
            bracketsScan = new Scanner(bracketsFile);
            while (bracketsScan.hasNextLine())
            {
              Scanner temp = new Scanner(bracketsScan.nextLine());
              
              String name = null;
              if (temp.hasNext())
              {
                name = temp.next();
                




                WageBracket bracket = 
                  new WageBracket(this.plugin.getBracketManager(), name);
                this.plugin.getBracketManager().addBracket(bracket);
                
                double value = 0.0D;
                try
                {
                  value = temp.nextDouble();
                }
                catch (Exception localException1) {}
                if ((value < 0.0D) || (Double.isInfinite(value)) || 
                  (Double.isNaN(value))) {
                  value = 0.0D;
                }
                BracketValueSetting valueSetting = 
                  (BracketValueSetting)bracket.getSetting("value");
                if (valueSetting != null) {
                  valueSetting.setValue(Double.valueOf(value));
                }
                boolean exclude = false;
                while (temp.hasNext())
                {
                  String flag = temp.next();
                  if (flag.equalsIgnoreCase("exclude")) {
                    exclude = true;
                  }
                }
                BracketExcludedSetting excludedSetting = 
                  (BracketExcludedSetting)bracket.getSetting("excluded");
                if (excludedSetting != null) {
                  excludedSetting.setValue(Boolean.valueOf(exclude));
                }
                BracketOnlineModeSetting onlineModeSetting = 
                  (BracketOnlineModeSetting)bracket.getSetting("online-mode");
                if (onlineModeSetting != null) {
                  onlineModeSetting.setValue(Boolean.valueOf(this.useOnlineWages));
                }
                temp.close();
              }
            }
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to load brackets file.");
        }
        finally
        {
          if (bracketsScan != null) {
            bracketsScan.close();
          }
        }
        File moneyFile = new File("plugins/BOSEconomy/money.txt");
        File usersFile = new File("plugins/BOSEconomy/users.txt");
        Scanner moneyScan = null;
        Scanner usersScan = null;
        try
        {
          if (moneyFile.canRead())
          {
            moneyScan = new Scanner(moneyFile);
            while (moneyScan.hasNextLine())
            {
              Account account = null;
              String name = null;
              Scanner temp = new Scanner(moneyScan.nextLine());
              if (temp.hasNext())
              {
                name = temp.next();
                if (name.length() != 0)
                {
                  boolean isBank = false;
                  if (name.charAt(0) == '$')
                  {
                    isBank = true;
                    name = name.substring(1);
                  }
                  if (isBank) {
                    account = new BankAccount(this.plugin.getAccountManager(), name);
                  } else {
                    account = new PlayerAccount(this.plugin.getAccountManager(), name);
                  }
                  this.plugin.getAccountManager().addAccount(account);
                  if (temp.hasNext())
                  {
                    double money = 0.0D;
                    try
                    {
                      money = temp.nextDouble();
                    }
                    catch (Exception localException3) {}
                    account.setMoney(money);
                  }
                  else
                  {
                    account.setMoney(0.0D);
                    continue;
                  }
                  temp.close();
                }
              }
            }
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to load the money file '" + "plugins/BOSEconomy/" + 
            "money.txt" + "'!");
        }
        finally
        {
          if (moneyScan != null) {
            moneyScan.close();
          }
        }
        try
        {
          if (usersFile.canRead())
          {
            usersScan = new Scanner(usersFile);
            while (usersScan.hasNextLine())
            {
              Scanner temp = new Scanner(usersScan.nextLine());
              temp.useDelimiter(":");
              

              String name = null;
              if (temp.hasNext())
              {
                name = temp.next();
                




                Account account = 
                  this.plugin.getAccountManager().getAccountByName(name);
                if (account == null)
                {
                  this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Found account '" + name + 
                    "' in the users file with no matching data from the money file.");
                }
                else
                {
                  if ((account instanceof BankAccount))
                  {
                    if (temp.hasNext())
                    {
                      Scanner temp2 = new Scanner(temp.next());
                      temp2.useDelimiter(",");
                      while (temp2.hasNext()) {
                        ((BankAccount)account).addOwner(temp2.next());
                      }
                      temp2.close();
                    }
                    if (temp.hasNext())
                    {
                      Scanner temp2 = new Scanner(temp.next());
                      temp2.useDelimiter(",");
                      while (temp2.hasNext()) {
                        ((BankAccount)account).addMember(temp2.next());
                      }
                      temp2.close();
                    }
                  }
                  else if ((account instanceof PlayerAccount))
                  {
                    if (temp.hasNext())
                    {
                      Scanner temp2 = new Scanner(temp.next());
                      temp2.useDelimiter(",");
                      while (temp2.hasNext())
                      {
                        Bracket bracket = 
                          this.plugin.getBracketManager().getBracket(temp2.next());
                        Bracket.BracketMember member = bracket.getMember(account);
                        if (member == null) {
                          bracket.addMember(account);
                        } else {
                          member.setCount(member.getCount() + 1.0D);
                        }
                      }
                      temp2.close();
                    }
                  }
                  temp.close();
                }
              }
            }
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to load the users file '" + "plugins/BOSEconomy/" + 
            "users.txt" + "'!");
        }
        finally
        {
          if (usersScan != null) {
            usersScan.close();
          }
        }
        if (!propsFile.getFile().delete()) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to delete file '" + propsFile.getFile().getPath() + 
            "'.");
        }
        if (!moneyFile.delete()) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to delete file '" + moneyFile.getPath() + "'.");
        }
        if (!usersFile.delete()) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to delete file '" + usersFile.getPath() + "'.");
        }
        if (!bracketsFile.delete()) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to delete file '" + bracketsFile.getPath() + "'.");
        }
        File lastPaymentFile = 
          new File("plugins/BOSEconomy/lastPayment.dat");
        if ((lastPaymentFile.exists()) && (!lastPaymentFile.delete())) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Failed to delete file '" + lastPaymentFile.getPath() + "'.");
        }
        this.plugin.getBOSEDatabase().setManagersChanged(true);
        this.plugin.getBOSEDatabase().commitManagers();
        





        this.needDatabaseRefresh = false;
        
        setVersion("0.7.0");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_1()
  {
    if (!getVersion().equals("0.7.1")) {
      if (updateTo0_7_0())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.1 format.");
        
        handleDatabaseRefresh();
        
        DatabaseList list = 
          this.plugin.getBOSEDatabase().getRoot().getList("settings");
        if (list != null)
        {
          String intervalString = list.getString("default-wage-interval");
          if (intervalString != null) {
            list.setString("default-bracket-interval", 
              intervalString);
          }
          intervalString = list.getString("save-interval");
          if (intervalString != null) {
            try
            {
              TimeInterval interval = new TimeInterval(intervalString);
              if (interval.getSeconds() == 0) {
                list.setString("save-interval", "1 second");
              }
            }
            catch (TimeInterval.TimeIntervalFormatException localTimeIntervalFormatException) {}
          }
          this.needSettingsSave = true;
        }
        setVersion("0.7.1");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_2()
  {
    if (!getVersion().equals("0.7.2")) {
      if (updateTo0_7_1())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.2 format.");
        
        handleDatabaseRefresh();
        
        DatabaseList list = 
          this.plugin.getBOSEDatabase().getRoot().getList("settings");
        if (list != null)
        {
          String bracketString = list.getString("default-wage-bracket");
          if (bracketString != null) {
            list.setString("default-bracket", bracketString);
          }
          this.needSettingsSave = true;
        }
        list = this.plugin.getBOSEDatabase().getRoot().getList("brackets");
        if (list != null)
        {
          for (DatabaseList.ListEntry bracket : list.getLists())
          {
            String paymentInterval = bracket.list.getString("paymentInterval");
            if (paymentInterval != null) {
              bracket.list.setString("bracketInterval", paymentInterval);
            }
          }
          this.needBracketSave = true;
        }
        setVersion("0.7.2");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_3()
  {
    if (!getVersion().equals("0.7.3")) {
      if (updateTo0_7_2())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.3 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.3");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_3_1()
  {
    if (!getVersion().equals("0.7.3.1")) {
      if (updateTo0_7_3())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.3.1 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.3.1");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_4()
  {
    if (!getVersion().equals("0.7.4")) {
      if (updateTo0_7_3_1())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.4 format.");
        
        handleDatabaseRefresh();
        

        DatabaseList list = 
          this.plugin.getBOSEDatabase().getRoot().getList("brackets");
        if (list != null)
        {
          for (DatabaseList.ListEntry bracket : list.getLists())
          {
            String value = "0.0";
            if (bracket.list.containsValueKey("value")) {
              value = bracket.list.getString("value");
            }
            String master = "";
            if (bracket.list.containsValueKey("master")) {
              master = bracket.list.getString("master");
            }
            String excluded = "false";
            if (bracket.list.containsValueKey("excluded")) {
              excluded = bracket.list.getString("excluded");
            }
            String disabled = "false";
            if (bracket.list.containsValueKey("disabled")) {
              disabled = bracket.list.getString("disabled");
            }
            boolean locked = true;
            if (bracket.list.containsValueKey("locked")) {
              locked = bracket.list.getBoolean("locked");
            }
            String onlineMode = "false";
            if (bracket.list.containsValueKey("onlineMode")) {
              onlineMode = bracket.list.getString("onlineMode");
            }
            String interval = "";
            if (bracket.list.containsValueKey("bracketInterval")) {
              interval = bracket.list.getString("bracketInterval");
            }
            DatabaseList settingsList = new DatabaseList();
            settingsList.setString("value", value);
            settingsList.setString("master", master);
            settingsList.setString("excluded", excluded);
            settingsList.setString("disabled", disabled);
            settingsList.setString("online-mode", onlineMode);
            settingsList.setString("interval", interval);
            if (locked)
            {
              settingsList.setString("add-member", "deny");
              settingsList.setString("remove-member", "deny");
              settingsList.setString("can-rename", "deny");
            }
            else
            {
              String type = bracket.list.getString("type");
              if (type.equalsIgnoreCase("sub")) {
                settingsList.setString("add-member", "request");
              } else {
                settingsList.setString("add-member", "allow");
              }
              settingsList.setString("remove-member", "allow");
              settingsList.setString("can-rename", "true");
            }
            bracket.list.setList("settings", settingsList);
          }
          this.needBracketSave = true;
        }
        setVersion("0.7.4");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_5()
  {
    if (!getVersion().equals("0.7.5")) {
      if (updateTo0_7_4())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.5 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.5");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_6_0()
  {
    if (!getVersion().equals("0.7.6.0")) {
      if (updateTo0_7_5())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.6.0 format.");
        
        handleDatabaseRefresh();
        

        DatabaseList list = 
          this.plugin.getBOSEDatabase().getRoot().getList("brackets");
        if (list != null)
        {
          for (DatabaseList.ListEntry bracket : list.getLists())
          {
            DatabaseList settings = bracket.list.getList("settings");
            if (settings != null)
            {
              String permissionNode = settings.getString("permission-node");
              if (permissionNode != null)
              {
                settings.removeValueKey("permission-node");
                settings.setString("permission-nodes", permissionNode);
              }
            }
          }
          this.needBracketSave = true;
        }
        setVersion("0.7.6.0");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_6_1()
  {
    if (!getVersion().equals("0.7.6.1")) {
      if (updateTo0_7_6_0())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.6.1 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.6.1");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_6_2()
  {
    if (!getVersion().equals("0.7.6.2")) {
      if (updateTo0_7_6_1())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.6.2 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.6.2");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_6_3()
  {
    if (!getVersion().equals("0.7.6.3")) {
      if (updateTo0_7_6_2())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.6.3 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.6.3");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_7_0()
  {
    if (!getVersion().equals("0.7.7.0")) {
      if (updateTo0_7_6_3())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.7.0 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.7.0");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_8_0()
  {
    if (!getVersion().equals("0.7.8.0")) {
      if (updateTo0_7_7_0())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.8.0 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.8.0");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  private boolean updateTo0_7_8_1()
  {
    if (!getVersion().equals("0.7.8.1")) {
      if (updateTo0_7_8_0())
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          "Updating plugin data to version 0.7.8.1 format.");
        
        handleDatabaseRefresh();
        
        setVersion("0.7.8.1");
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  public void setVersion(String version)
  {
    this.version = version;
  }
  
  public String getVersion()
  {
    return this.version;
  }
  
  public boolean isCurrentVersion()
  {
    return this.version.equals("0.7.8.1");
  }
  
  public void saveVersion()
  {
    File file = new File(this.plugin.getDataFolder().getPath() + "/" + "version.dat");
    OutputStreamWriter out = null;
    try
    {
      File parent = file.getParentFile();
      if ((parent != null) && (!parent.isDirectory()) && (!parent.mkdirs()))
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Failed to create parent directories for file '" + file.getPath() + 
          "'. Problems may occur the next time the plugin is started.");
        return;
      }
      out = new OutputStreamWriter(new FileOutputStream(file));
      out.write(getVersion());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
        "Failed to write the version file '" + file.getPath() + 
        "'. Problems may occur the next time the plugin is started.");
      if (out != null) {
        try
        {
          out.close();
        }
        catch (IOException localIOException1) {}
      }
    }
    finally
    {
      if (out != null) {
        try
        {
          out.close();
        }
        catch (IOException localIOException2) {}
      }
    }
  }
  
  public void loadVersion()
  {
    Scanner scan = null;
    try
    {
      File file = 
        new File(this.plugin.getDataFolder().getPath() + "/" + 
        "version.dat");
      if (file.canRead())
      {
        scan = new Scanner(file);
        if (scan.hasNext())
        {
          setVersion(scan.next());
          return;
        }
      }
      setVersion("0.7.8.1");
      saveVersion();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
        "A problem occurred while loading the version file! Data cannot be converted to the latest version!");
      
      setVersion("0.7.8.1");
    }
    finally
    {
      if (scan != null) {
        scan.close();
      }
    }
  }
  
  private void handleDatabaseRefresh()
  {
    if (this.needDatabaseRefresh)
    {
      this.plugin.getBOSEDatabase().refresh();
      this.needDatabaseRefresh = false;
    }
  }
  
  public boolean needSettingsSave()
  {
    return this.needSettingsSave;
  }
  
  public boolean needBracketSave()
  {
    return this.needBracketSave;
  }
}

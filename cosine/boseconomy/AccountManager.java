package cosine.boseconomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class AccountManager
  extends DatabaseManager
{
  private static final int DEFAULT_ACCOUNTS_SIZE = 64;
  private static final long COMPUTE_STATS_INTERVAL = 8000L;
  private final BOSEconomy plugin;
  private HashMap<String, Account> accounts;
  private long lastStatsCall = 0L;
  private double totalMoney = 0.0D;
  private int totalPlayers = 0;
  private double averageMoney = 0.0D;
  private long lastTop5Call = 0L;
  private PlayerAccount[] top5;
  
  public AccountManager(BOSEconomy plugin)
  {
    this.plugin = plugin;
    this.accounts = new HashMap(64);
  }
  
  public Account getAccountByName(String name)
  {
    if ((name == null) || (name.length() == 0)) {
      return null;
    }
    if (name.charAt(0) != '$') {
      name = '^' + name;
    }
    return (Account)this.accounts.get(name.toLowerCase());
  }
  
  public PlayerAccount getPlayerAccount(Player player)
  {
    return player == null ? null : getPlayerAccountByName(player.getName());
  }
  
  public PlayerAccount getPlayerAccountByName(String name)
  {
    return (PlayerAccount)this.accounts.get('^' + name.toLowerCase());
  }
  
  public BankAccount getBankAccountByName(String name)
  {
    return (BankAccount)this.accounts.get('$' + name.toLowerCase());
  }
  
  public PlayerAccount createNoobAccount(String name)
  {
    PlayerAccount account = getPlayerAccountByName(name);
    if (account != null) {
      return account;
    }
    account = 
      new PlayerAccount(this, name, this.plugin.getSettingsManager()
      .getInitialMoney());
    account.addToDefaultBracket();
    
    this.accounts.put(account.getHashName(), account);
    return account;
  }
  
  public boolean removeBankAccount(String name)
  {
    return this.accounts.remove('$' + name.toLowerCase()) != null;
  }
  
  public boolean addAccount(Account newAccount)
  {
    if ((newAccount == null) || (this.accounts.containsKey(newAccount.getHashName()))) {
      return false;
    }
    this.accounts.put(newAccount.getHashName(), newAccount);
    return true;
  }
  
  public boolean removeAccount(Account account)
  {
    return this.accounts.remove(account.getHashName()) != null;
  }
  
  public void renameAccount(String oldHash, String newHash)
  {
    Account a = (Account)this.accounts.remove(oldHash);
    if (a != null) {
      this.accounts.put(newHash, a);
    }
  }
  
  public void clearAccountBrackets()
  {
    for (Account a : this.accounts.values())
    {
      a.clearBrackets(true);
      a.clearBrackets(false);
    }
  }
  
  public void updatePlayerPermissionNodes()
  {
    for (Account a : this.accounts.values()) {
      if ((a instanceof PlayerAccount)) {
        ((PlayerAccount)a).updatePermissionNodes();
      }
    }
  }
  
  private void computeStats()
  {
    if (8000L < System.currentTimeMillis() - this.lastStatsCall)
    {
      this.totalMoney = 0.0D;
      this.totalPlayers = 0;
      this.averageMoney = 0.0D;
      for (Account account : this.accounts.values()) {
        if (((account instanceof PlayerAccount)) && 
          (!account.isExcluded()))
        {
          this.totalMoney += account.getMoney();
          
          this.totalPlayers += 1;
        }
      }
      if (this.totalPlayers > 0) {
        this.averageMoney = (this.totalMoney / this.totalPlayers);
      } else {
        this.averageMoney = 0.0D;
      }
      this.lastStatsCall = System.currentTimeMillis();
    }
  }
  
  public double getTotalMoney()
  {
    computeStats();
    return this.totalMoney;
  }
  
  public int getTotalPlayers()
  {
    computeStats();
    return this.totalPlayers;
  }
  
  public double getAverageMoney()
  {
    computeStats();
    return this.averageMoney;
  }
  
  private void computeTop5()
  {
    if (8000L < System.currentTimeMillis() - this.lastTop5Call)
    {
      this.top5 = new PlayerAccount[5];
      for (Account account : this.accounts.values()) {
        if (((account instanceof PlayerAccount)) && (!account.isExcluded())) {
          addToTop5((PlayerAccount)account);
        }
      }
      this.lastTop5Call = System.currentTimeMillis();
    }
  }
  
  private void addToTop5(PlayerAccount account)
  {
    for (int i = 0; i < this.top5.length; i++)
    {
      if (this.top5[i] == null)
      {
        this.top5[i] = account;
        return;
      }
      if (this.top5[i].getMoney() < account.getMoney())
      {
        for (int j = this.top5.length - 1; j > i; j--) {
          this.top5[j] = this.top5[(j - 1)];
        }
        this.top5[i] = account;
        return;
      }
    }
  }
  
  public PlayerAccount[] getTop5()
  {
    computeTop5();
    return this.top5;
  }
  
  public void loadOnlinePlayers()
  {
    for (Player player : this.plugin.getServer().getOnlinePlayers()) {
      handlePlayerJoin(player);
    }
  }
  
  public void releasePermAttachments()
  {
    for (Account a : this.accounts.values()) {
      if ((a instanceof PlayerAccount)) {
        ((PlayerAccount)a).releasePermAttachment();
      }
    }
  }
  
  public void handlePlayerJoin(Player player)
  {
    PlayerAccount account = getPlayerAccountByName(player.getName());
    if (account == null)
    {
      account = 
        new PlayerAccount(this, player.getName(), this.plugin.getSettingsManager()
        .getInitialMoney());
      addAccount(account);
      account.addToDefaultBracket();
      if (this.plugin.debug) {
        this.plugin.sendConsoleMessage("[BOSEconomy Debug] Creating new account for user '" + 
          player.getName() + "'.");
      }
    }
    account.setPlayer(player);
    

    account.updatePermissionNodes();
    if (player.hasPermission("BOSEconomy.common.request.list"))
    {
      List<Request> requests = 
        getPlugin().getRequestHandler().getReceivedRequests(account);
      if (requests.size() >= 1)
      {
        account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.INFO_COLOR + " You have " + requests.size() + 
          " received request" + (requests.size() == 1 ? "." : "s."));
        account.sendMessage(BOSEconomy.INFO_COLOR + "  " + 
          RequestHandler.getRequestViewMessage());
      }
    }
  }
  
  public void handlePlayerQuit(Player player)
  {
    PlayerAccount account = getPlayerAccountByName(player.getName());
    if (account != null) {
      account.setPlayer(null);
    }
  }
  
  public Iterator<Account> getAccountIterator()
  {
    return this.accounts.values().iterator();
  }
  
  public void refresh(Database database, String listName)
  {
    if (this.plugin.debug) {
      this.plugin.sendConsoleMessage("[BOSEconomy Debug] Refreshing account data from the database.");
    }
    this.lastStatsCall = 0L;
    this.totalMoney = 0.0D;
    this.totalPlayers = 0;
    this.averageMoney = 0.0D;
    
    this.lastTop5Call = 0L;
    this.top5 = new PlayerAccount[5];
    DatabaseList list = database.getRoot().getOrCreateList(listName);
    this.accounts = new HashMap();
    

    Iterator<DatabaseList.ListEntry> iterator = list.getListIterator();
    Account a;
    while (iterator.hasNext())
    {
      DatabaseList.ListEntry entry = (DatabaseList.ListEntry)iterator.next();
      a = null;
      String type = entry.list.getString("type");
      double money = entry.list.getDouble("money");
      if (type == null)
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Encountered missing account type for account '" + entry.key + 
          "' while refreshing account data.");
      }
      else
      {
        if (type.equalsIgnoreCase("player"))
        {
          a = new PlayerAccount(this, entry.key, money);
          ((PlayerAccount)a).updatePlayer();
        }
        else if (type.equalsIgnoreCase("bank"))
        {
          a = new BankAccount(this, entry.key, money);
          

          Iterator<DatabaseList.ValueEntry> memberIterator = 
            entry.list.getOrCreateList("members").getValueIterator();
          while (memberIterator.hasNext())
          {
            DatabaseList.ValueEntry memberEntry = (DatabaseList.ValueEntry)memberIterator.next();
            ((BankAccount)a).addMember(memberEntry.key);
          }
          Iterator<DatabaseList.ValueEntry> ownerIterator = 
            entry.list.getOrCreateList("owners").getValueIterator();
          while (ownerIterator.hasNext())
          {
            DatabaseList.ValueEntry ownerEntry = (DatabaseList.ValueEntry)ownerIterator.next();
            ((BankAccount)a).addOwner(ownerEntry.key);
          }
        }
        else
        {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
            "Encountered unknown account type '" + type + "' for account '" + 
            entry.key + "' while refreshing account data.");
          continue;
        }
        if (!addAccount(a)) {
          this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + "Encountered a duplicate " + 
            a.getClass().getSimpleName() + " '" + a.getName() + 
            "' while refreshing account data. Duplicate skipped.");
        } else if (this.plugin.debug) {
          this.plugin.sendConsoleMessage("[BOSEconomy Debug] Loaded " + 
            a.getClass().getSimpleName() + " '" + a.getName() + "'.");
        }
      }
    }
    for (Account a : this.accounts.values()) {
      if ((a instanceof BankAccount)) {
        ((BankAccount)a).resolveNames();
      }
    }
    setChanged(false);
  }
  
  public void commit(Database database, String listName)
  {
    List<Account> sortedAccounts = new ArrayList(this.accounts.values());
    Collections.sort(sortedAccounts, new Account.NameComparator());
    
    DatabaseList list = new DatabaseList();
    for (Account a : sortedAccounts)
    {
      DatabaseList accountList = new DatabaseList();
      if ((a instanceof PlayerAccount))
      {
        accountList.setString("type", "player");
      }
      else if ((a instanceof BankAccount))
      {
        DatabaseList ownerList = new DatabaseList();
        DatabaseList memberList = new DatabaseList();
        Iterator<BankAccount.BankMember> iterator = ((BankAccount)a).getIterator();
        while (iterator.hasNext())
        {
          BankAccount.BankMember bm = (BankAccount.BankMember)iterator.next();
          if (bm.isOwner()) {
            ownerList.setString(bm.getAccount().getDataName(), "");
          } else {
            memberList.setString(bm.getAccount().getDataName(), "");
          }
        }
        accountList.setString("type", "bank");
        accountList.setList("members", memberList);
        accountList.setList("owners", ownerList);
      }
      else
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Encountered unknown account class '" + a.getClass().getSimpleName() + 
          "' while committing account data.");
        continue;
      }
      accountList.setDouble("money", a.getMoney());
      list.setList(a.getName(), accountList);
    }
    database.getRoot().removeListKey(listName);
    database.getRoot().setList(listName, list);
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
}

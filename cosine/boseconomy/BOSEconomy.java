package cosine.boseconomy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.staticaccess.VanishNoPacket;

public class BOSEconomy
  extends JavaPlugin
{
  public static final String CURRENT_VERSION = "0.7.8.1";
  private Database database;
  private SettingsManager settingsManager;
  private AccountManager accountManager;
  private BracketManager bracketManager;
  private CommandHandler commandHandler;
  private RequestHandler requestHandler;
  private MoneyFormatter moneyFormatter;
  private BOSEconomyTask boseconomyTask;
  private int boseconomyTaskId;
  private boolean useVanishPlugin = false;
  public static final String MONEY_COLOR = ChatColor.AQUA.toString();
  public static final String GOOD_COLOR = ChatColor.GREEN.toString();
  public static final String NEUTRAL_COLOR = ChatColor.GRAY.toString();
  public static final String BAD_COLOR = ChatColor.RED.toString();
  public static final String INFO_COLOR = ChatColor.GOLD.toString();
  public static final String PLUGIN_COLOR = ChatColor.DARK_AQUA.toString();
  public static final String CONSOLE_COLOR = ChatColor.GRAY.toString();
  public static final String NAME = "BOSEconomy";
  public static final String TAG_BLANK = "[BOSEconomy] ";
  public static final String TAG_WARNING = "[BOSEconomy Warning] ";
  public static final String TAG_DEBUG = "[BOSEconomy Debug] ";
  public static final String TAG_BLANK_COLOR = PLUGIN_COLOR + "[" + "BOSEconomy" + "] ";
  public static final String TAG_PAYDAY_COLOR = PLUGIN_COLOR + "[" + "BOSEconomy" + " Payday] ";
  public static final String TAG_PAYMENTS_COLOR = PLUGIN_COLOR + "[" + "BOSEconomy" + " Payments] ";
  public static final String TAG_WARNING_COLOR = ChatColor.DARK_RED + "[" + "BOSEconomy" + " Warning] ";
  public static final String COMMAND_LABEL = "econ";
  public static final char PREFIX_SUBSET = '@';
  public static final char PREFIX_PLAYER = '^';
  public static final char PREFIX_BANK = '$';
  public static final char PREFIX_BRACKET = '#';
  public static final String FILE_SETTINGS = "settings.txt";
  public static final String FILE_ACCOUNTS = "accounts.txt";
  public static final String FILE_BRACKETS = "brackets.txt";
  public static final String FILE_PAYMENTS = "payments.dat";
  public static final String FILE_VERSION = "version.dat";
  public static final String FILE_PROPERTIES = "BOSEconomy.properties";
  public static final String FILE_USERS = "users.txt";
  public static final String FILE_MONEY = "money.txt";
  public static final String FILE_LASTPAYMENT = "lastPayment.dat";
  public static final String PLUGIN_DIR = "plugins/BOSEconomy/";
  public static final boolean PAY_DEBUG = false;
  public static final boolean BRACKET_ADDMEMBER_DEBUG = false;
  public static final boolean BRACKET_REMOVEMEMBER_DEBUG = false;
  public static final boolean ON_ENABLE_TIMER = false;
  public boolean debug = false;
  
  class BOSEconomyListener
    implements Listener
  {
    BOSEconomyListener() {}
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
      BOSEconomy.this.accountManager.handlePlayerJoin(event.getPlayer());
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
      BOSEconomy.this.accountManager.handlePlayerQuit(event.getPlayer());
    }
  }
  
  public void onEnable()
  {
    long startTime = 0L;
    





    this.requestHandler = new RequestHandler(this);
    


    this.database = new FileDatabase(this, 4);
    this.settingsManager = new SettingsManager(this);
    this.accountManager = new AccountManager(this);
    this.bracketManager = new BracketManager(this);
    ((FileDatabase)this.database).addDatabaseManager(this.settingsManager, "settings", 
      getDataFolder().getPath() + "/" + "settings.txt", 
      0);
    ((FileDatabase)this.database).addDatabaseManager(this.accountManager, "accounts", 
      getDataFolder().getPath() + "/" + "accounts.txt", 
      0);
    ((FileDatabase)this.database).addDatabaseManager(this.bracketManager, "brackets", 
      getDataFolder().getPath() + "/" + "brackets.txt", 
      0);
    ((FileDatabase)this.database).addDatabaseManager(
      this.bracketManager.getPaymentManager(), "payments", getDataFolder()
      .getPath() + "/" + "payments.dat", 1);
    

    this.moneyFormatter = new MoneyFormatter(this);
    

    this.boseconomyTask = new BOSEconomyTask(this);
    

    VersionHandler versionHandler = new VersionHandler(this);
    if (!versionHandler.isCurrentVersion())
    {
      if (!versionHandler.handleVersion()) {
        sendConsoleMessage(TAG_WARNING_COLOR + BAD_COLOR + 
          "Failed to update data to the latest version!");
      }
    }
    else {
      this.database.refresh();
    }
    this.database.refreshManagers();
    if (versionHandler.needSettingsSave()) {
      this.settingsManager.setChanged();
    }
    if (versionHandler.needBracketSave()) {
      this.bracketManager.setChanged();
    }
    this.database.commitManagers();
    this.database.commit();
    





    ((FileDatabase)this.database).generateAllFiles();
    





    Plugin vanishPlugin = getServer().getPluginManager().getPlugin("VanishNoPacket");
    if ((vanishPlugin instanceof VanishPlugin))
    {
      PluginDescriptionFile vanishPdf = vanishPlugin.getDescription();
      sendConsoleMessage(TAG_BLANK_COLOR + CONSOLE_COLOR + "Located " + 
        vanishPdf.getFullName() + 
        ". The setting '" + 
        "hide-vanished-players" + "' will be utilized.");
      this.useVanishPlugin = true;
    }
    else
    {
      if (this.settingsManager.getHideVanishedPlayers()) {
        sendConsoleMessage(TAG_BLANK_COLOR + CONSOLE_COLOR + 
          "Could not locate VanishNoPacket. The setting '" + 
          "hide-vanished-players" + "' will be ignored.");
      }
      this.useVanishPlugin = false;
    }
    this.commandHandler = new CommandHandler(this);
    

    getServer().getPluginManager().registerEvents(new BOSEconomyListener(), this);
    


    this.accountManager.loadOnlinePlayers();
    

    this.boseconomyTaskId = getServer().getScheduler()
      .scheduleSyncRepeatingTask(this, this.boseconomyTask, 20L, 20L);
    




    PluginDescriptionFile pdf = getDescription();
    sendConsoleMessage(PLUGIN_COLOR + pdf.getFullName() + CONSOLE_COLOR + " enabled.");
  }
  
  public void onDisable()
  {
    getServer().getScheduler().cancelTask(this.boseconomyTaskId);
    

    sendConsoleMessage(TAG_BLANK_COLOR + CONSOLE_COLOR + 
      "Saving plugin data.");
    this.database.commitManagers();
    this.database.commit();
    

    getAccountManager().releasePermAttachments();
    

    PluginDescriptionFile pdf = getDescription();
    sendConsoleMessage(PLUGIN_COLOR + pdf.getFullName() + CONSOLE_COLOR + " disabled.");
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
    if (this.commandHandler != null) {
      return this.commandHandler.tryCommands(sender, cmd, label, args);
    }
    return false;
  }
  
  @Deprecated
  public int getPlayerMoney(String name)
  {
    return (int)getPlayerMoneyDouble(name);
  }
  
  public double getPlayerMoneyDouble(String name)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null) {
      account = getAccountManager().createNoobAccount(name);
    }
    return account.getMoney();
  }
  
  @Deprecated
  public boolean setPlayerMoney(String name, int money, boolean mustBeOnline)
  {
    return setPlayerMoney(name, money, mustBeOnline);
  }
  
  public boolean setPlayerMoney(String name, double money, boolean mustBeOnline)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null)
    {
      if (getServer().getPlayer(name) != null)
      {
        account = getAccountManager().createNoobAccount(name);
        account.setMoney(money);
        return true;
      }
      if (mustBeOnline) {
        return false;
      }
      account = getAccountManager().createNoobAccount(name);
      account.setMoney(money);
      return true;
    }
    if (!(account instanceof PlayerAccount)) {
      return false;
    }
    if (mustBeOnline)
    {
      if (((PlayerAccount)account).hasPlayer())
      {
        account.setMoney(money);
        return true;
      }
      return false;
    }
    account.setMoney(money);
    return true;
  }
  
  @Deprecated
  public boolean addPlayerMoney(String name, int money, boolean mustBeOnline)
  {
    return addPlayerMoney(name, money, mustBeOnline);
  }
  
  public boolean addPlayerMoney(String name, double money, boolean mustBeOnline)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null)
    {
      if (getServer().getPlayer(name) != null)
      {
        account = getAccountManager().createNoobAccount(name);
        account.addMoney(money);
        return true;
      }
      if (mustBeOnline) {
        return false;
      }
      account = getAccountManager().createNoobAccount(name);
      account.addMoney(money);
      return true;
    }
    if (!(account instanceof PlayerAccount)) {
      return false;
    }
    if (mustBeOnline)
    {
      if (((PlayerAccount)account).hasPlayer())
      {
        account.addMoney(money);
        return true;
      }
      return false;
    }
    account.addMoney(money);
    return true;
  }
  
  public boolean playerRegistered(String name, boolean mustBeOnline)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null) {
      return false;
    }
    if ((account instanceof PlayerAccount))
    {
      if (mustBeOnline) {
        return ((PlayerAccount)account).hasPlayer();
      }
      return true;
    }
    return false;
  }
  
  public boolean registerPlayer(String name)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null)
    {
      account = getAccountManager().createNoobAccount(name);
      return true;
    }
    return false;
  }
  
  public void tellPlayerMoney(String name)
  {
    Account account = getAccountManager().getAccountByName(name);
    if (account == null) {
      account = getAccountManager().createNoobAccount(name);
    } else if (!(account instanceof PlayerAccount)) {
      return;
    }
    ((PlayerAccount)account).tellMoney();
  }
  
  @Deprecated
  public int getBankMoney(String name)
  {
    return (int)getBankMoneyDouble(name);
  }
  
  public double getBankMoneyDouble(String name)
  {
    BankAccount account = getAccountManager().getBankAccountByName(name);
    if (account == null)
    {
      account = new BankAccount(getAccountManager(), name);
      getAccountManager().addAccount(account);
    }
    return account.getMoney();
  }
  
  @Deprecated
  public boolean setBankMoney(String name, int money, boolean mustExist)
  {
    return setBankMoney(name, money, mustExist);
  }
  
  public boolean setBankMoney(String name, double money, boolean mustExist)
  {
    BankAccount account = getAccountManager().getBankAccountByName(name);
    if (account == null)
    {
      if (mustExist) {
        return false;
      }
      account = new BankAccount(getAccountManager(), name);
      getAccountManager().addAccount(account);
    }
    account.setMoney(money);
    return true;
  }
  
  @Deprecated
  public boolean addBankMoney(String name, int money, boolean mustExist)
  {
    return addBankMoney(name, money, mustExist);
  }
  
  public boolean addBankMoney(String name, double money, boolean mustExist)
  {
    BankAccount account = getAccountManager().getBankAccountByName(name);
    if (account == null)
    {
      if (mustExist) {
        return false;
      }
      account = new BankAccount(getAccountManager(), name);
      getAccountManager().addAccount(account);
    }
    account.addMoney(money);
    return true;
  }
  
  public boolean bankExists(String name)
  {
    BankAccount account = getAccountManager().getBankAccountByName(name);
    return account != null;
  }
  
  public boolean createBank(String name)
  {
    BankAccount account = getAccountManager().getBankAccountByName(name);
    if (account == null)
    {
      account = new BankAccount(getAccountManager(), name);
      getAccountManager().addAccount(account);
      return true;
    }
    return false;
  }
  
  public boolean removeBank(String name)
  {
    return getAccountManager().removeBankAccount(name);
  }
  
  public boolean addBankOwner(String bank, String owner, boolean mustExist)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null)
    {
      if (mustExist) {
        return false;
      }
      account = new BankAccount(getAccountManager(), bank);
      getAccountManager().addAccount(account);
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(owner);
    if (account.isOwner(p)) {
      return false;
    }
    account.addOwner(p);
    return true;
  }
  
  public boolean addBankMember(String bank, String member, boolean mustExist)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null)
    {
      if (mustExist) {
        return false;
      }
      account = new BankAccount(getAccountManager(), bank);
      getAccountManager().addAccount(account);
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(member);
    if (account.isMember(p)) {
      return false;
    }
    account.addMember(p);
    return true;
  }
  
  public boolean removeBankPlayer(String bank, String player)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null) {
      return false;
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(player);
    return account.removeMemberOrOwner(p);
  }
  
  public boolean isBankOwner(String bank, String owner)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null) {
      return false;
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(owner);
    return account.isOwner(p);
  }
  
  public boolean isBankMember(String bank, String member)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null) {
      return false;
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(member);
    return account.isMember(p);
  }
  
  public void tellBankMessage(String bank, String message, String sender)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account != null)
    {
      PlayerAccount p = getAccountManager().getPlayerAccountByName(sender);
      account.sendMessage(message, p);
    }
  }
  
  public void tellBankBalance(String bank)
  {
    BankAccount account = getAccountManager().getBankAccountByName(bank);
    if (account == null) {
      return;
    }
    account.tellBalanceToAll();
  }
  
  public void tellBankBalanceToPlayer(String bank, String player)
  {
    BankAccount b = getAccountManager().getBankAccountByName(bank);
    if (b == null) {
      return;
    }
    PlayerAccount p = getAccountManager().getPlayerAccountByName(player);
    b.tellBalance(p);
  }
  
  @Deprecated
  public int getInitialMoney()
  {
    return (int)this.settingsManager.getInitialMoney();
  }
  
  public double getInitialMoneyDouble()
  {
    return this.settingsManager.getInitialMoney();
  }
  
  @Deprecated
  public int getWageInterval()
  {
    return 0;
  }
  
  @Deprecated
  public String getWageIntervalUnit()
  {
    return null;
  }
  
  public String getMoneyName()
  {
    return this.settingsManager.getMoneyName();
  }
  
  public String getMoneyNamePlural()
  {
    return this.settingsManager.getMoneyNamePlural();
  }
  
  public String getMoneyNameCaps()
  {
    return this.settingsManager.getMoneyNameCaps();
  }
  
  public String getMoneyNamePluralCaps()
  {
    return this.settingsManager.getMoneyNamePluralCaps();
  }
  
  public int getFractionalDigits()
  {
    return this.settingsManager.getFractionalDigits();
  }
  
  public List<String> getPlayerList()
  {
    List<String> list = new ArrayList();
    Iterator<Account> iterator = this.accountManager.getAccountIterator();
    while (iterator.hasNext())
    {
      Account a = (Account)iterator.next();
      if ((a instanceof PlayerAccount)) {
        list.add(a.getName());
      }
    }
    Collections.sort(list);
    return list;
  }
  
  public List<String> getBankList()
  {
    List<String> list = new ArrayList();
    Iterator<Account> iterator = this.accountManager.getAccountIterator();
    while (iterator.hasNext())
    {
      Account a = (Account)iterator.next();
      if ((a instanceof BankAccount)) {
        list.add(a.getName());
      }
    }
    Collections.sort(list);
    return list;
  }
  
  public double getMoneyRounded(double money)
  {
    return this.moneyFormatter.roundMoney(money);
  }
  
  public double getMoneyFloored(double money)
  {
    return this.moneyFormatter.floorMoney(money);
  }
  
  public String getMoneyFormatted(double money)
  {
    return this.moneyFormatter.formatMoney(money);
  }
  
  public String getMoneyNameProper(double money)
  {
    return (money == 1.0D) || (money == -1.0D) ? getSettingsManager().getMoneyName() : 
      getSettingsManager().getMoneyNamePlural();
  }
  
  public String getMoneyNameCapsProper(double money)
  {
    return (money == 1.0D) || (money == -1.0D) ? getSettingsManager()
      .getMoneyNameCaps() : getSettingsManager().getMoneyNamePluralCaps();
  }
  
  public Database getBOSEDatabase()
  {
    return this.database;
  }
  
  public SettingsManager getSettingsManager()
  {
    return this.settingsManager;
  }
  
  public AccountManager getAccountManager()
  {
    return this.accountManager;
  }
  
  public BracketManager getBracketManager()
  {
    return this.bracketManager;
  }
  
  public CommandHandler getCommandHandler()
  {
    return this.commandHandler;
  }
  
  public RequestHandler getRequestHandler()
  {
    return this.requestHandler;
  }
  
  public MoneyFormatter getMoneyFormatter()
  {
    return this.moneyFormatter;
  }
  
  public BOSEconomyTask getBOSEconomyTask()
  {
    return this.boseconomyTask;
  }
  
  public boolean getUseVanishPlugin()
  {
    return this.useVanishPlugin;
  }
  
  public boolean getPlayerVanished(String name)
  {
    if (this.useVanishPlugin) {
      try
      {
        return VanishNoPacket.isVanished(name);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        sendConsoleMessage(TAG_WARNING_COLOR + BAD_COLOR + 
          "Exception thrown by VanishNoPacket. The setting '" + 
          "hide-vanished-players" + "' will be ignored.");
        return false;
      }
    }
    return false;
  }
  
  public boolean canSeeVanish(CommandHandler.BOSCommandSender sender)
  {
    if (sender.isPlayer()) {
      return sender.getSender().hasPermission("vanish.see");
    }
    return sender.isConsoleOrBlock();
  }
  
  public boolean canSeeVanish(Permissible p)
  {
    if (p == null) {
      return true;
    }
    return p.hasPermission("vanish.see");
  }
  
  public void sendConsoleMessage(String message)
  {
    if ((this.settingsManager == null) || 
      (!this.settingsManager.getShowConsoleColors())) {
      getServer().getConsoleSender().sendMessage(ChatColor.stripColor(message));
    } else {
      getServer().getConsoleSender().sendMessage(message);
    }
  }
  
  public static String getElapsedTime(long startTime)
  {
    long elapsedTime = System.nanoTime() - startTime;
    return String.format("%.6f", new Object[] { Double.valueOf(elapsedTime / 1000000000.0D) });
  }
  
  public void printElapsedTime(long startTime, String message)
  {
    sendConsoleMessage(TAG_BLANK_COLOR + ChatColor.WHITE + "(" + 
      getElapsedTime(startTime) + " seconds) " + message);
  }
  
  public static class Perms
  {
    public static final String ACCOUNTGROUP = "BOSEconomy.accountgroup";
    public static final String ACCOUNTGROUP_PLAYERS = "BOSEconomy.accountgroup.players";
    public static final String ACCOUNTGROUP_BANKS = "BOSEconomy.accountgroup.banks";
    public static final String ACCOUNTGROUP_BRACKETS = "BOSEconomy.accountgroup.brackets";
    public static final String ACCOUNTGROUP_GENERAL = "BOSEconomy.accountgroup.general";
    public static final String ACCOUNTGROUP_GENERAL_ONLINE = "BOSEconomy.accountgroup.general.online";
    public static final String ACCOUNTGROUP_GENERAL_OFFLINE = "BOSEconomy.accountgroup.general.offline";
    public static final String ACCOUNTGROUP_GENERAL_PLAYERS = "BOSEconomy.accountgroup.general.players";
    public static final String ACCOUNTGROUP_GENERAL_BANKS = "BOSEconomy.accountgroup.general.banks";
    public static final String COMMON = "BOSEconomy.common";
    public static final String COMMON_WALLET = "BOSEconomy.common.wallet";
    public static final String COMMON_INCOME = "BOSEconomy.common.income";
    public static final String COMMON_MASTERY = "BOSEconomy.common.mastery";
    public static final String COMMON_PAY = "BOSEconomy.common.pay";
    public static final String COMMON_STATS = "BOSEconomy.common.stats";
    public static final String COMMON_TOP5 = "BOSEconomy.common.top5";
    public static final String COMMON_BRACKET = "BOSEconomy.common.bracket";
    public static final String COMMON_BRACKET_RENAME = "BOSEconomy.common.bracket.rename";
    public static final String COMMON_BRACKET_INFO = "BOSEconomy.common.bracket.info";
    public static final String COMMON_BRACKET_SET = "BOSEconomy.common.bracket.set";
    public static final String COMMON_BRACKET_LISTMEMBERS = "BOSEconomy.common.bracket.listmembers";
    public static final String COMMON_BRACKET_ADDMEMBER = "BOSEconomy.common.bracket.addmember";
    public static final String COMMON_BRACKET_REMOVEMEMBER = "BOSEconomy.common.bracket.removemember";
    public static final String COMMON_BRACKET_SETMULTIPLIER = "BOSEconomy.common.bracket.setmultiplier";
    public static final String COMMON_BANK = "BOSEconomy.common.bank";
    public static final String COMMON_BANK_RENAME = "BOSEconomy.common.bank.rename";
    public static final String COMMON_BANK_LIST = "BOSEconomy.common.bank.list";
    public static final String COMMON_BANK_INFO = "BOSEconomy.common.bank.info";
    public static final String COMMON_BANK_MASTERY = "BOSEconomy.common.bank.mastery";
    public static final String COMMON_BANK_WITHDRAW = "BOSEconomy.common.bank.withdraw";
    public static final String COMMON_BANK_DEPOSIT = "BOSEconomy.common.bank.deposit";
    public static final String COMMON_BANK_ADDOWNER = "BOSEconomy.common.bank.addowner";
    public static final String COMMON_BANK_ADDMEMBER = "BOSEconomy.common.bank.addmember";
    public static final String COMMON_BANK_REMOVEMEMBER = "BOSEconomy.common.bank.removemember";
    public static final String COMMON_BANK_LISTMEMBERS = "BOSEconomy.common.bank.listmembers";
    public static final String COMMON_REQUEST = "BOSEconomy.common.request";
    public static final String COMMON_REQUEST_LIST = "BOSEconomy.common.request.list";
    public static final String COMMON_REQUEST_DO = "BOSEconomy.common.request.do";
    public static final String COMMON_ABOUT = "BOSEconomy.common.about";
    public static final String COMMON_HELP = "BOSEconomy.common.help";
    public static final String ADMIN = "BOSEconomy.admin";
    public static final String ADMIN_MONEY = "BOSEconomy.admin.money";
    public static final String ADMIN_MONEY_INFO = "BOSEconomy.admin.money.info";
    public static final String ADMIN_MONEY_SET = "BOSEconomy.admin.money.set";
    public static final String ADMIN_MONEY_ADD = "BOSEconomy.admin.money.add";
    public static final String ADMIN_MONEY_SUB = "BOSEconomy.admin.money.sub";
    public static final String ADMIN_MONEY_CLEAR = "BOSEconomy.admin.money.clear";
    public static final String ADMIN_MONEY_SCALE = "BOSEconomy.admin.money.scale";
    public static final String ADMIN_VIEWMASTERY = "BOSEconomy.admin.viewmastery";
    public static final String ADMIN_RELOAD = "BOSEconomy.admin.reload";
    public static final String ADMIN_SAVE = "BOSEconomy.admin.save";
    public static final String ADMIN_BRACKET = "BOSEconomy.admin.bracket";
    public static final String ADMIN_BRACKET_CREATE = "BOSEconomy.admin.bracket.create";
    public static final String ADMIN_BRACKET_REMOVE = "BOSEconomy.admin.bracket.remove";
    public static final String ADMIN_BRACKET_RENAME = "BOSEconomy.admin.bracket.rename";
    public static final String ADMIN_BRACKET_LIST = "BOSEconomy.admin.bracket.list";
    public static final String ADMIN_BRACKET_INFO = "BOSEconomy.admin.bracket.info";
    public static final String ADMIN_BRACKET_SET = "BOSEconomy.admin.bracket.set";
    public static final String ADMIN_BRACKET_LISTMEMBERS = "BOSEconomy.admin.bracket.listmembers";
    public static final String ADMIN_BRACKET_ADDMEMBER = "BOSEconomy.admin.bracket.addmember";
    public static final String ADMIN_BRACKET_REMOVEMEMBER = "BOSEconomy.admin.bracket.removemember";
    public static final String ADMIN_BRACKET_SETMULTIPLIER = "BOSEconomy.admin.bracket.setmultiplier";
    public static final String ADMIN_BRACKET_CLEAR = "BOSEconomy.admin.bracket.clear";
    public static final String ADMIN_BANK = "BOSEconomy.admin.bank";
    public static final String ADMIN_BANK_CREATE = "BOSEconomy.admin.bank.create";
    public static final String ADMIN_BANK_REMOVE = "BOSEconomy.admin.bank.remove";
    public static final String ADMIN_BANK_RENAME = "BOSEconomy.admin.bank.rename";
    public static final String ADMIN_BANK_LIST = "BOSEconomy.admin.bank.list";
    public static final String ADMIN_BANK_INFO = "BOSEconomy.admin.bank.info";
    public static final String ADMIN_BANK_MASTERY = "BOSEconomy.admin.bank.mastery";
    public static final String ADMIN_BANK_LISTMEMBERS = "BOSEconomy.admin.bank.listmembers";
    public static final String ADMIN_BANK_WITHDRAW = "BOSEconomy.admin.bank.withdraw";
    public static final String ADMIN_BANK_DEPOSIT = "BOSEconomy.admin.bank.deposit";
    public static final String ADMIN_BANK_ADDOWNER = "BOSEconomy.admin.bank.addowner";
    public static final String ADMIN_BANK_ADDMEMBER = "BOSEconomy.admin.bank.addmember";
    public static final String ADMIN_BANK_REMOVEMEMBER = "BOSEconomy.admin.bank.removemember";
    public static final String ADMIN_PAYDAY = "BOSEconomy.admin.payday";
    public static final String ADMIN_PAYDAY_RESET = "BOSEconomy.admin.payday.reset";
    public static final String ADMIN_PAYDAY_SCHEDULE = "BOSEconomy.admin.payday.schedule";
    public static final String ADMIN_PAYDAY_FORCE = "BOSEconomy.admin.payday.force";
    public static final String VANISH_SEE = "vanish.see";
  }
}

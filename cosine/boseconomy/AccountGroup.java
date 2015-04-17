package cosine.boseconomy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import org.bukkit.Server;
import org.bukkit.entity.Player;

class AccountGroup
{
  private static final int MAX_ERROR_MESSAGES = 5;
  public static final int NO_PLAYERS = 1;
  public static final int NO_BANKS = 2;
  public static final int NO_BRACKETS = 4;
  public static final int NO_SUBSETS = 8;
  private final BOSEconomy plugin;
  private final CommandHandler.BOSCommandSender sender;
  private final List<LocalAccount> accounts;
  private final List<String> errors;
  private final int flags;
  
  public class LocalAccount
  {
    public final Account account;
    public int flag;
    
    public LocalAccount(Account account)
    {
      this.account = account;
      this.flag = 0;
    }
  }
  
  public AccountGroup(BOSEconomy plugin)
  {
    this(plugin, null, null, 0);
  }
  
  public AccountGroup(BOSEconomy plugin, String s, CommandHandler.BOSCommandSender sender, int flags)
  {
    this.plugin = plugin;
    this.accounts = new LinkedList();
    this.errors = new LinkedList();
    this.sender = sender;
    this.flags = flags;
    if (s != null) {
      addAccounts(s);
    }
  }
  
  public void addAccount(Account a)
  {
    if ((a != null) && (!accountsContains(a))) {
      this.accounts.add(new LocalAccount(a));
    }
  }
  
  private boolean accountsContains(Account a)
  {
    for (LocalAccount la : this.accounts) {
      if (la.account == a) {
        return true;
      }
    }
    return false;
  }
  
  public boolean addAccounts(String s)
  {
    Scanner scan = new Scanner(s);
    scan.useDelimiter(",");
    this.errors.clear();
    

    boolean blockedPlayers = false;
    boolean blockedBanks = false;
    boolean blockedBrackets = false;
    boolean blockedGeneral = false;
    boolean blockedGeneralMe = false;
    boolean blockedGeneralOnline = false;
    boolean blockedGeneralOffline = false;
    boolean blockedGeneralPlayers = false;
    boolean blockedGeneralBanks = false;
    while (scan.hasNext())
    {
      String token = scan.next();
      if (token.length() > 0) {
        if (token.charAt(0) == '$')
        {
          if ((this.flags & 0x2) > 0)
          {
            if (!blockedBanks)
            {
              blockedBanks = true;
              this.errors
                .add("Bank accounts are not allowed in this account group.");
            }
          }
          else
          {
            if (this.sender != null) {
              if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.banks"))
              {
                if (blockedBanks) {
                  continue;
                }
                blockedBanks = true;
                this.errors
                  .add("You aren't allowed to specify bank accounts for account groups.");
                
                continue;
              }
            }
            token = token.substring(1);
            BankAccount a = 
              this.plugin.getAccountManager().getBankAccountByName(token);
            if (a == null) {
              this.errors.add("Could not find bank account '" + token + "'.");
            } else {
              addAccount(a);
            }
          }
        }
        else if (token.charAt(0) == '#')
        {
          if ((this.flags & 0x4) > 0)
          {
            if (!blockedBrackets)
            {
              blockedBrackets = true;
              this.errors.add("Brackets are not allowed in this account group.");
            }
          }
          else
          {
            if (this.sender != null) {
              if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.brackets"))
              {
                if (blockedBrackets) {
                  continue;
                }
                blockedBrackets = true;
                this.errors
                  .add("You aren't allowed to specify brackets for account groups.");
                
                continue;
              }
            }
            Bracket b = this.plugin.getBracketManager().getBracket(token.substring(1));
            if (b != null)
            {
              Iterator<Bracket.BracketMember> iterator = b.getMemberIterator();
              while (iterator.hasNext())
              {
                Account a = ((Bracket.BracketMember)iterator.next()).getAccount();
                if ((((a instanceof PlayerAccount)) && ((this.flags & 0x1) == 0)) || (
                  ((a instanceof BankAccount)) && ((this.flags & 0x2) == 0))) {
                  addAccount(a);
                }
              }
            }
          }
        }
        else if (token.charAt(0) == '@')
        {
          token = token.substring(1).toLowerCase();
          if (!token.equals("none")) {
            if (token.equals("me"))
            {
              if (this.sender.isPlayer())
              {
                if ((this.flags & 0x1) > 0)
                {
                  if (!blockedPlayers)
                  {
                    blockedPlayers = true;
                    this.errors.add("Players are not allowed in this account group.");
                  }
                }
                else
                {
                  if (this.sender != null) {
                    if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.players"))
                    {
                      if (blockedPlayers) {
                        continue;
                      }
                      blockedPlayers = true;
                      this.errors
                        .add("You aren't allowed to specify players for account groups.");
                      
                      continue;
                    }
                  }
                  PlayerAccount a = 
                    this.plugin.getAccountManager().getPlayerAccountByName(
                    this.sender.getAsPlayer().getName());
                  if (a == null) {
                    this.errors.add("Could not find player '" + token + "'.");
                  } else {
                    addAccount(a);
                  }
                }
              }
              else if (!blockedGeneralMe)
              {
                blockedGeneralMe = true;
                this.errors.add("Only players can use the '@me' subset.");
              }
            }
            else if ((this.flags & 0x8) > 0)
            {
              if (!blockedGeneral)
              {
                blockedGeneral = true;
                this.errors
                  .add("General subsets are not allowed in this account group.");
              }
            }
            else if (token.equals("online"))
            {
              if ((this.flags & 0x1) > 0)
              {
                if (!blockedPlayers)
                {
                  blockedPlayers = true;
                  this.errors.add("Players are not allowed in this account group.");
                }
              }
              else
              {
                if (this.sender != null) {
                  if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.general.online"))
                  {
                    if (blockedGeneralOnline) {
                      continue;
                    }
                    blockedGeneralOnline = true;
                    this.errors
                      .add("You aren't allowed to use the online subset for account groups.");
                    
                    continue;
                  }
                }
                Player[] players = this.plugin.getServer().getOnlinePlayers();
                for (int i = 0; i < players.length; i++) {
                  if ((this.plugin.canSeeVanish(this.sender)) || 
                    (!this.plugin.getPlayerVanished(players[i].getName()))) {
                    addAccount(this.plugin.getAccountManager()
                      .getPlayerAccountByName(players[i].getName()));
                  }
                }
              }
            }
            else if (token.equals("offline"))
            {
              if ((this.flags & 0x1) > 0)
              {
                if (!blockedPlayers)
                {
                  blockedPlayers = true;
                  this.errors.add("Players are not allowed in this account group.");
                }
              }
              else
              {
                if (this.sender != null) {
                  if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.general.offline"))
                  {
                    if (blockedGeneralOffline) {
                      continue;
                    }
                    blockedGeneralOffline = true;
                    this.errors
                      .add("You aren't allowed to use the offline subset for account groups.");
                    
                    continue;
                  }
                }
                Iterator<Account> iterator = 
                  this.plugin.getAccountManager().getAccountIterator();
                while (iterator.hasNext())
                {
                  Account a = (Account)iterator.next();
                  if (((a instanceof PlayerAccount)) && (!((PlayerAccount)a).hasPlayer())) {
                    addAccount(a);
                  }
                }
              }
            }
            else if (token.equals("players"))
            {
              if ((this.flags & 0x1) > 0)
              {
                if (!blockedPlayers)
                {
                  blockedPlayers = true;
                  this.errors.add("Players are not allowed in this account group.");
                }
              }
              else
              {
                if (this.sender != null) {
                  if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.general.players"))
                  {
                    if (blockedGeneralPlayers) {
                      continue;
                    }
                    blockedGeneralPlayers = true;
                    this.errors
                      .add("You aren't allowed to use the player subset for account groups.");
                    
                    continue;
                  }
                }
                Iterator<Account> iterator = 
                  this.plugin.getAccountManager().getAccountIterator();
                while (iterator.hasNext())
                {
                  Account a = (Account)iterator.next();
                  if ((a instanceof PlayerAccount)) {
                    addAccount(a);
                  }
                }
              }
            }
            else if (token.equals("banks"))
            {
              if ((this.flags & 0x2) > 0)
              {
                if (!blockedBanks)
                {
                  blockedBanks = true;
                  this.errors
                    .add("Bank accounts are not allowed in this account group.");
                }
              }
              else
              {
                if (this.sender != null) {
                  if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.general.banks"))
                  {
                    if (blockedGeneralBanks) {
                      continue;
                    }
                    blockedGeneralBanks = true;
                    this.errors
                      .add("You aren't allowed to use the bank account subset for account groups.");
                    
                    continue;
                  }
                }
                Iterator<Account> iterator = 
                  this.plugin.getAccountManager().getAccountIterator();
                while (iterator.hasNext())
                {
                  Account a = (Account)iterator.next();
                  if ((a instanceof BankAccount)) {
                    addAccount(a);
                  }
                }
              }
            }
            else {
              this.errors.add("Unrecognized general subset '" + token + "'.");
            }
          }
        }
        else if ((this.flags & 0x1) > 0)
        {
          if (!blockedPlayers)
          {
            blockedPlayers = true;
            this.errors.add("Players are not allowed in this account group.");
          }
        }
        else
        {
          if (this.sender != null) {
            if (!this.plugin.getCommandHandler().hasPermission(this.sender, "BOSEconomy.accountgroup.players"))
            {
              if (blockedPlayers) {
                continue;
              }
              blockedPlayers = true;
              this.errors
                .add("You aren't allowed to specify players for account groups.");
              
              continue;
            }
          }
          PlayerAccount a = 
            this.plugin.getAccountManager().getPlayerAccountByName(token);
          if (a == null) {
            this.errors.add("Could not find player '" + token + "'.");
          } else {
            addAccount(a);
          }
        }
      }
    }
    scan.close();
    return hadErrors();
  }
  
  public boolean hadErrors()
  {
    return this.errors.size() > 0;
  }
  
  public List<String> getErrors()
  {
    return this.errors;
  }
  
  public int getErrorCount()
  {
    return this.errors.size();
  }
  
  public void tellErrors(CommandHandler.BOSCommandSender sender)
  {
    int messageCount;
    int messageCount;
    if ((this.errors.size() <= 5) || (sender.isConsoleOrBlock())) {
      messageCount = this.errors.size();
    } else {
      messageCount = 4;
    }
    int i = 0;
    for (String error : this.errors)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + error);
      i++;
      if (i >= messageCount) {
        break;
      }
    }
    if (messageCount < this.errors.size()) {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "(" + (this.errors.size() - messageCount) + " more problems)");
    }
  }
  
  public void clearAccounts()
  {
    this.accounts.clear();
  }
  
  public List<LocalAccount> getAccountList()
  {
    return this.accounts;
  }
  
  public List<LocalAccount> getAccountList(int flag)
  {
    List<LocalAccount> list = new LinkedList();
    for (LocalAccount la : this.accounts) {
      if (la.flag == flag) {
        list.add(la);
      }
    }
    return list;
  }
  
  public boolean oneAccount()
  {
    return getSize() == 1;
  }
  
  public boolean oneAccount(int flag)
  {
    return getSize(flag) == 1;
  }
  
  public int getSize()
  {
    return this.accounts.size();
  }
  
  public int getSize(int flag)
  {
    int size = 0;
    for (LocalAccount la : this.accounts) {
      if (la.flag == flag) {
        size++;
      }
    }
    return size;
  }
  
  public String getAccountString(boolean capitalize, boolean quoteNames)
  {
    return getAccountString(capitalize, quoteNames, false, false, 0);
  }
  
  public String getAccountString(boolean capitalize, boolean quoteNames, boolean nullIfEmpty)
  {
    return getAccountString(capitalize, quoteNames, nullIfEmpty, false, 0);
  }
  
  public String getAccountString(boolean capitalize, boolean quoteNames, int flag)
  {
    return getAccountString(capitalize, quoteNames, false, true, flag);
  }
  
  public String getAccountString(boolean capitalize, boolean quoteNames, boolean nullIfEmpty, int flag)
  {
    return getAccountString(capitalize, quoteNames, nullIfEmpty, true, flag);
  }
  
  private String getAccountString(boolean capitalize, boolean quoteNames, boolean nullIfEmpty, boolean withFlag, int flag)
  {
    List<LocalAccount> list;
    if (withFlag)
    {
      List<LocalAccount> list = new LinkedList();
      for (LocalAccount la : this.accounts) {
        if (la.flag == flag) {
          list.add(la);
        }
      }
    }
    else
    {
      list = this.accounts;
    }
    String s;
    String s;
    if (list.size() == 0)
    {
      String s;
      if (nullIfEmpty) {
        s = null;
      } else {
        s = capitalize ? "No accounts" : "no accounts";
      }
    }
    else
    {
      String s;
      if (list.size() == 1)
      {
        LocalAccount la = (LocalAccount)list.get(0);
        String s;
        if ((la.account instanceof PlayerAccount))
        {
          s = 
          
            (capitalize ? "Player " : "player ") + (quoteNames ? "'" + la.account.getName() + "'" : la.account
            .getName());
        }
        else
        {
          String s;
          if ((la.account instanceof BankAccount)) {
            s = 
            
              (capitalize ? "Bank account " : "bank account ") + (quoteNames ? "'" + la.account.getName() + "'" : la.account
              .getName());
          } else {
            s = la.account.getName();
          }
        }
      }
      else
      {
        s = list.size() + " accounts";
      }
    }
    return s;
  }
}

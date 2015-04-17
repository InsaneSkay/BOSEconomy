package cosine.boseconomy;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;

public class BankAccount
  extends Account
{
  private HashMap<String, BankMember> members;
  
  public BankAccount(AccountManager manager, String name, double money)
  {
    super(manager, name, money);
    this.members = new HashMap();
  }
  
  public BankAccount(AccountManager manager, String name)
  {
    this(manager, name, 0.0D);
  }
  
  public class BankMember
  {
    private PlayerAccount account;
    private String tempName;
    private boolean isOwner;
    
    public BankMember(PlayerAccount account, boolean isOwner)
    {
      if (account == null) {
        throw new NullPointerException(
          "Attempted to create a BankMember with a null account.");
      }
      this.account = account;
      this.tempName = null;
      this.isOwner = isOwner;
    }
    
    public BankMember(String name, boolean isOwner)
    {
      if (name == null) {
        throw new NullPointerException(
          "Attempted to create a BankMember with a null temporary name.");
      }
      this.account = null;
      this.tempName = name;
      this.isOwner = isOwner;
    }
    
    public PlayerAccount getAccount()
    {
      if (this.account == null) {
        throw new BankAccount.UnresolvedAccountNameException(BankAccount.this);
      }
      return this.account;
    }
    
    public boolean isOwner()
    {
      return this.isOwner;
    }
    
    public void setOwner(boolean isOwner)
    {
      this.isOwner = isOwner;
    }
    
    public boolean resolveName()
    {
      if (this.account == null)
      {
        this.account = 
          BankAccount.this.getManager().getPlayerAccountByName(this.tempName);
        if (this.account == null) {
          return false;
        }
        this.tempName = null;
        return true;
      }
      return true;
    }
  }
  
  public String getType()
  {
    return "bank";
  }
  
  public String getTypeCaps()
  {
    return "Bank";
  }
  
  public String getDataName()
  {
    return '$' + getName();
  }
  
  public String getHashName()
  {
    return '$' + getName().toLowerCase();
  }
  
  public void setPaymentMoney(double money, boolean isPayday)
  {
    double diff = money - getMoney();
    if (diff != 0.0D)
    {
      setMoney(money);
      if (!this.manager.getPlugin().getSettingsManager().getSilentWages())
      {
        if (diff > 0.0D) {
          sendMessage((isPayday ? BOSEconomy.TAG_PAYDAY_COLOR : 
            BOSEconomy.TAG_PAYMENTS_COLOR) + 
            BOSEconomy.GOOD_COLOR + 
            "Bank account " + 
            getName() + 
            " receives " + 
            this.manager.getPlugin().getMoneyFormatter().generateString(diff, 
            BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
        } else {
          sendMessage((isPayday ? BOSEconomy.TAG_PAYDAY_COLOR : 
            BOSEconomy.TAG_BLANK_COLOR) + 
            BOSEconomy.BAD_COLOR + 
            "Bank account " + 
            getName() + 
            " loses " + 
            this.manager.getPlugin().getMoneyFormatter().generateString(-diff, 
            BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + ".");
        }
        tellMoney();
      }
    }
  }
  
  public void payAccount(double payMoney)
  {
    if (payMoney != 0.0D)
    {
      addMoney(payMoney);
      sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "Bank account " + getName() + " receives its wage of " + 
        this.manager.getPlugin().getMoneyFormatter().generateString(payMoney, 
        BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
      tellMoney();
    }
  }
  
  public void tellMoney()
  {
    sendMessage(BOSEconomy.GOOD_COLOR + getName() + " account balance: " + 
      getMoneyObject().toString(true), null);
  }
  
  public boolean isExcluded()
  {
    return true;
  }
  
  public void addOwner(PlayerAccount a)
  {
    if (a == null) {
      return;
    }
    BankMember bm = (BankMember)this.members.get(a.getDataName());
    if (bm == null)
    {
      this.members.put(a.getName(), new BankMember(a, true));
      this.manager.setChanged();
    }
    else if (!bm.isOwner())
    {
      bm.setOwner(true);
      this.manager.setChanged();
    }
  }
  
  public void addOwner(String name)
  {
    if (name == null) {
      return;
    }
    BankMember bm = (BankMember)this.members.get(name);
    if (bm == null)
    {
      this.members.put(name, new BankMember(name, true));
      this.manager.setChanged();
    }
    else if (!bm.isOwner())
    {
      bm.setOwner(true);
      this.manager.setChanged();
    }
  }
  
  public boolean isOwner(PlayerAccount a)
  {
    BankMember bm = (BankMember)this.members.get(a.getDataName());
    return (bm != null) && (bm.isOwner());
  }
  
  public boolean isOwner(Player p)
  {
    BankMember bm = (BankMember)this.members.get(p.getName());
    return (bm != null) && (bm.isOwner());
  }
  
  public void addMember(PlayerAccount a)
  {
    if (a == null) {
      return;
    }
    BankMember bm = (BankMember)this.members.get(a.getDataName());
    if (bm == null)
    {
      this.members.put(a.getName(), new BankMember(a, false));
      this.manager.setChanged();
    }
    else if (bm.isOwner())
    {
      bm.setOwner(false);
      this.manager.setChanged();
    }
  }
  
  public void addMember(String name)
  {
    if (name == null) {
      return;
    }
    BankMember bm = (BankMember)this.members.get(name);
    if (bm == null)
    {
      this.members.put(name, new BankMember(name, false));
      this.manager.setChanged();
    }
    else if (bm.isOwner())
    {
      bm.setOwner(false);
      this.manager.setChanged();
    }
  }
  
  public boolean isMember(PlayerAccount a)
  {
    BankMember bm = (BankMember)this.members.get(a.getDataName());
    return (bm != null) && (!bm.isOwner());
  }
  
  public boolean isMember(Player p)
  {
    BankMember bm = (BankMember)this.members.get(p.getName());
    return (bm != null) && (!bm.isOwner());
  }
  
  public boolean isMemberOrOwner(PlayerAccount a)
  {
    BankMember bm = (BankMember)this.members.get(a.getDataName());
    return bm != null;
  }
  
  public boolean isMemberOrOwner(Player p)
  {
    BankMember bm = (BankMember)this.members.get(p.getName());
    return bm != null;
  }
  
  public boolean removeMemberOrOwner(PlayerAccount a)
  {
    if (this.members.remove(a.getDataName()) != null)
    {
      this.manager.setChanged();
      return true;
    }
    return false;
  }
  
  public int getOwnerCount()
  {
    int total = 0;
    for (BankMember bm : this.members.values()) {
      if (bm.isOwner) {
        total++;
      }
    }
    return total;
  }
  
  public int getMemberCount()
  {
    int total = 0;
    for (BankMember bm : this.members.values()) {
      if (!bm.isOwner) {
        total++;
      }
    }
    return total;
  }
  
  public int getMemberAndOwnerCount()
  {
    return this.members.size();
  }
  
  public Iterator<BankMember> getIterator()
  {
    return this.members.values().iterator();
  }
  
  public List<BankMember> getMemberList()
  {
    return new LinkedList(this.members.values());
  }
  
  public void sendMessage(String message)
  {
    sendMessage(message, null);
  }
  
  public void sendMessage(String message, PlayerAccount sender)
  {
    for (BankMember bm : this.members.values()) {
      if (bm.getAccount() != sender) {
        bm.getAccount().sendMessage(message);
      }
    }
  }
  
  public void sendMessage(String message, Player sender)
  {
    for (BankMember bm : this.members.values()) {
      if ((bm.getAccount().getPlayer() != null) && 
        (bm.getAccount().getPlayer() != sender)) {
        bm.getAccount().sendMessage(message);
      }
    }
  }
  
  public void notifyPayment(double money, PlayerAccount payer)
  {
    String moneyNameProper = this.manager.getPlugin().getMoneyNameProper(money);
    if (payer == null)
    {
      sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "The server paid " + BOSEconomy.MONEY_COLOR + money + " " + 
        BOSEconomy.GOOD_COLOR + moneyNameProper, null);
      sendMessage(BOSEconomy.GOOD_COLOR + "  to bank account " + getName() + 
        ".", null);
    }
    else
    {
      sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "User " + payer + " paid " + BOSEconomy.MONEY_COLOR + money + " " + 
        BOSEconomy.GOOD_COLOR + moneyNameProper, payer);
      sendMessage(BOSEconomy.GOOD_COLOR + "  to bank account " + getName() + 
        ".", payer);
    }
    tellBalanceToAll();
  }
  
  public void tellBalanceToAll()
  {
    for (BankMember bm : this.members.values()) {
      tellBalance(bm.getAccount());
    }
  }
  
  public void tellBalance(PlayerAccount a)
  {
    if (a != null) {
      a.sendMessage(BOSEconomy.GOOD_COLOR + getName() + " account balance: " + 
        getMoneyObject().toString(true));
    }
  }
  
  public void resolveNames()
  {
    Iterator<BankMember> iterator = this.members.values().iterator();
    while (iterator.hasNext())
    {
      BankMember bm = (BankMember)iterator.next();
      if (!bm.resolveName())
      {
        this.manager.getPlugin().sendConsoleMessage(
          BOSEconomy.TAG_WARNING_COLOR + "Removing non-existent member '" + 
          bm.tempName + "' from bank account '" + getName() + "'.");
        iterator.remove();
      }
    }
  }
  
  public static class MemberNameComparator
    implements Comparator<BankAccount.BankMember>
  {
    public int compare(BankAccount.BankMember bm1, BankAccount.BankMember bm2)
    {
      return bm1.getAccount().getName().compareTo(bm2.getAccount().getName());
    }
  }
  
  public class UnresolvedAccountNameException
    extends RuntimeException
  {
    private static final long serialVersionUID = 1L;
    
    public UnresolvedAccountNameException() {}
  }
}

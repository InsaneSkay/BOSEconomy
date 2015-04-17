package cosine.boseconomy;

import cosine.boseconomy.bracket.setting.BracketMasterSetting;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class Account
{
  private static int BRACKET_REQUEST_DURATION = 120;
  protected final AccountManager manager;
  private String name;
  private final Money money;
  protected LinkedList<Bracket> memberBrackets;
  protected LinkedList<Bracket> masterBrackets;
  protected SubBracket requestBracket = null;
  protected long requestTime = 0L;
  
  public Account(AccountManager manager, String name, double money)
  {
    if (manager == null) {
      throw new NullPointerException(
        "Attempted to create account with a null manager.");
    }
    this.manager = manager;
    setName(name);
    this.money = new Money(manager.getPlugin());
    setMoney(money);
    this.memberBrackets = new LinkedList();
    this.masterBrackets = new LinkedList();
  }
  
  public Account(AccountManager manager, String name)
  {
    this(manager, name, 0.0D);
  }
  
  public abstract String getType();
  
  public abstract String getTypeCaps();
  
  public final String getName()
  {
    return this.name;
  }
  
  public abstract String getDataName();
  
  public abstract String getHashName();
  
  public final void setName(String name)
  {
    if (name == null) {
      this.name = "";
    } else {
      this.name = name;
    }
    this.manager.setChanged();
  }
  
  public final void rename(String newName)
  {
    if (newName == null) {
      newName = "";
    }
    if (!this.name.equals(newName))
    {
      String oldHash = getHashName();
      setName(newName);
      
      this.manager.renameAccount(oldHash, getHashName());
      
      Iterator<Bracket> iterator = getBracketIterator(false);
      while (iterator.hasNext())
      {
        Bracket b = (Bracket)iterator.next();
        b.renameMember(oldHash, getHashName());
      }
      this.manager.setChanged();
    }
  }
  
  public final double getMoney()
  {
    return this.money.getValue();
  }
  
  public final Money getMoneyObject()
  {
    return this.money;
  }
  
  public void setMoney(double money)
  {
    if (Money.checkValidity(money) == null) {
      this.money.setValue(money);
    } else {
      this.money.setValue(0.0D);
    }
    this.manager.setChanged();
  }
  
  public void addMoney(double money)
  {
    if ((money != 0.0D) && (Money.checkValidity(money) == null))
    {
      this.money.addValue(money);
      this.manager.setChanged();
    }
  }
  
  public abstract void setPaymentMoney(double paramDouble, boolean paramBoolean);
  
  public abstract void payAccount(double paramDouble);
  
  public abstract void tellMoney();
  
  public void sendMessage(String message) {}
  
  public abstract boolean isExcluded();
  
  public void addBracket(Bracket bracket, boolean master)
  {
    if (bracket != null)
    {
      if (master) {
        this.masterBrackets.add(bracket);
      } else {
        this.memberBrackets.add(bracket);
      }
      if (bracket == this.requestBracket) {
        submitBracketRequest(null);
      }
    }
  }
  
  public void removeBracket(Bracket bracket, boolean master)
  {
    if (bracket != null) {
      if (master) {
        this.masterBrackets.remove(bracket);
      } else {
        this.memberBrackets.remove(bracket);
      }
    }
  }
  
  public Iterator<Bracket> getBracketIterator(boolean master)
  {
    if (master) {
      return this.masterBrackets.iterator();
    }
    return this.memberBrackets.iterator();
  }
  
  public List<Bracket> getBracketList(boolean master)
  {
    if (master) {
      return new LinkedList(this.masterBrackets);
    }
    return new LinkedList(this.memberBrackets);
  }
  
  public void removeFromBrackets(boolean master)
  {
    if (master) {
      for (int i = this.masterBrackets.size(); i > 0; i--)
      {
        Bracket b = (Bracket)this.masterBrackets.getFirst();
        if (b != null)
        {
          BracketMasterSetting s = (BracketMasterSetting)b.getSetting("master");
          if (s != null) {
            s.setValue(null);
          }
        }
      }
    } else {
      for (int i = this.memberBrackets.size(); i > 0; i--)
      {
        Bracket b = (Bracket)this.memberBrackets.getFirst();
        if (b != null) {
          b.removeMember(this);
        }
      }
    }
  }
  
  public void clearBrackets(boolean master)
  {
    if (master) {
      this.masterBrackets.clear();
    } else {
      this.memberBrackets.clear();
    }
  }
  
  public int getBracketCount(boolean master)
  {
    if (master) {
      return this.masterBrackets.size();
    }
    return this.memberBrackets.size();
  }
  
  public double getTotalIncome()
  {
    double income = 0.0D;
    for (Bracket b : this.memberBrackets) {
      if (b.getPaymentInterval().getSeconds() > 0) {
        income = income + ((b instanceof SubBracket) ? -1.0D : 1.0D) * (
          b.getValue() / b.getPaymentInterval().getSeconds()) * 
          b.getMember(this).getCount();
      }
    }
    return income;
  }
  
  public void submitBracketRequest(SubBracket bracket)
  {
    if (bracket == null)
    {
      this.requestBracket = null;
      this.requestTime = 0L;
    }
    else if (System.currentTimeMillis() - this.requestTime > BRACKET_REQUEST_DURATION)
    {
      this.requestBracket = bracket;
      this.requestTime = System.currentTimeMillis();
    }
  }
  
  public AccountManager getManager()
  {
    return this.manager;
  }
  
  public static class NameComparator
    implements Comparator<Account>
  {
    public int compare(Account a1, Account a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
}

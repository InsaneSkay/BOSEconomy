package cosine.boseconomy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PlayerAccount
  extends Account
{
  private static final int MAX_PAY_COMMAND_TOLERANCE = 5000;
  private static final int PAY_COMMAND_MODIFIER = 1000;
  private long payCommandTimer = 0L;
  private Player player = null;
  private PermissionAttachment permAttachment = null;
  
  public PlayerAccount(AccountManager manager, String name, double money)
  {
    super(manager, name, money);
  }
  
  public PlayerAccount(AccountManager manager, String name)
  {
    this(manager, name, 0.0D);
  }
  
  public PlayerAccount(String name)
  {
    this(null, name, 0.0D);
  }
  
  public String getType()
  {
    return "player";
  }
  
  public String getTypeCaps()
  {
    return "Player";
  }
  
  public String getDataName()
  {
    return getName();
  }
  
  public String getHashName()
  {
    return '^' + getName().toLowerCase();
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
            BOSEconomy.TAG_PAYMENTS_COLOR) + BOSEconomy.GOOD_COLOR + "You receive " + 
            this.manager.getPlugin().getMoneyFormatter().generateString(diff, 
            BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
        } else {
          sendMessage((isPayday ? BOSEconomy.TAG_PAYDAY_COLOR : 
            BOSEconomy.TAG_BLANK_COLOR) + BOSEconomy.BAD_COLOR + "You lose " + 
            this.manager.getPlugin().getMoneyFormatter().generateString(-diff, 
            BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + ".");
        }
        tellMoney();
      }
    }
  }
  
  public void addToDefaultBracket()
  {
    if (this.manager.getPlugin().getSettingsManager().getDefaultBracket() != null) {
      if (this.manager.getPlugin().getSettingsManager().getDefaultBracket().length() > 0)
      {
        Bracket defaultBracket = 
          this.manager.getPlugin().getBracketManager().getDefaultBracket();
        
        defaultBracket.addMember(this);
      }
    }
  }
  
  public void payAccount(double payMoney)
  {
    if (payMoney != 0.0D)
    {
      addMoney(payMoney);
      sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "You receive your wage of " + 
        this.manager.getPlugin().getMoneyFormatter().generateString(payMoney, 
        BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
      tellMoney();
    }
  }
  
  public void tellMoney()
  {
    sendMessage(BOSEconomy.GOOD_COLOR + "Your money: " + 
      getMoneyObject().toString(true));
  }
  
  public Player getPlayer()
  {
    return this.player;
  }
  
  public void setPlayer(Player player)
  {
    if ((this.player != null) && (this.permAttachment != null)) {
      this.player.removeAttachment(this.permAttachment);
    }
    this.player = player;
    if (this.player == null) {
      this.permAttachment = null;
    } else {
      this.permAttachment = this.player.addAttachment(this.manager.getPlugin());
    }
  }
  
  public boolean updatePlayer()
  {
    this.player = this.manager.getPlugin().getServer().getPlayer(getName());
    return this.player != null;
  }
  
  public boolean hasPlayer()
  {
    return this.player != null;
  }
  
  public PermissionAttachment getPermAttachment()
  {
    return this.permAttachment;
  }
  
  public void releasePermAttachment()
  {
    if (this.player != null) {
      this.player.removeAttachment(this.permAttachment);
    }
  }
  
  public void updatePermissionNodes()
  {
    if (this.permAttachment != null)
    {
      List<String> allPermNodeList = 
        this.manager.getPlugin().getBracketManager().getPermissionNodeList();
      
      List<String> thisPermNodeList = new LinkedList();
      Iterator localIterator2;
      for (Iterator localIterator1 = this.memberBrackets.iterator(); localIterator1.hasNext(); localIterator2.hasNext())
      {
        Bracket b = (Bracket)localIterator1.next();
        localIterator2 = b.getPermissionNodes().iterator(); continue;String node = (String)localIterator2.next();
        if (!thisPermNodeList.contains(node)) {
          thisPermNodeList.add(node);
        }
      }
      for (String node : allPermNodeList) {
        if (thisPermNodeList.contains(node)) {
          this.permAttachment.setPermission(node, true);
        } else {
          this.permAttachment.unsetPermission(node);
        }
      }
    }
  }
  
  public void unsetPermissionNode(String node)
  {
    if ((this.permAttachment != null) && (node != null) && (!node.equals(""))) {
      this.permAttachment.unsetPermission(node);
    }
  }
  
  public boolean isExcluded()
  {
    Iterator<Bracket> iterator = getBracketIterator(false);
    while (iterator.hasNext())
    {
      Bracket b = (Bracket)iterator.next();
      if ((b.getExcluded()) && ((b.getMaster() == this) || (b.isMember(this)))) {
        return true;
      }
    }
    return false;
  }
  
  public void sendMessage(String message)
  {
    if (this.player != null) {
      this.player.sendMessage(message);
    }
  }
  
  public boolean denyPayCommand()
  {
    return this.payCommandTimer > System.currentTimeMillis();
  }
  
  public void sentPayCommand()
  {
    this.payCommandTimer = 
      (Math.max(System.currentTimeMillis() - 5000L, 
      this.payCommandTimer) + 1000L);
  }
}

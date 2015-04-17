package cosine.boseconomy;

import cosine.boseconomy.bracket.membertype.BracketMemberType;
import cosine.boseconomy.bracket.membertype.CanChangeValueSetting;
import cosine.boseconomy.bracket.membertype.IsAdminTypeSetting;
import cosine.boseconomy.bracket.setting.BracketAddMemberSetting;
import cosine.boseconomy.bracket.setting.BracketBankJoinSetting;
import cosine.boseconomy.bracket.setting.BracketBankLeaveSetting;
import cosine.boseconomy.bracket.setting.BracketCanChangeValueSetting;
import cosine.boseconomy.bracket.setting.BracketCanRenameSetting;
import cosine.boseconomy.bracket.setting.BracketChangeMultiplierSetting;
import cosine.boseconomy.bracket.setting.BracketDisabledSetting;
import cosine.boseconomy.bracket.setting.BracketExcludedSetting;
import cosine.boseconomy.bracket.setting.BracketIntervalSetting;
import cosine.boseconomy.bracket.setting.BracketMasterSetting;
import cosine.boseconomy.bracket.setting.BracketMaximumMembersSetting;
import cosine.boseconomy.bracket.setting.BracketMaximumMultiplierSetting;
import cosine.boseconomy.bracket.setting.BracketMaximumValueSetting;
import cosine.boseconomy.bracket.setting.BracketMinimumMultiplierSetting;
import cosine.boseconomy.bracket.setting.BracketMinimumValueSetting;
import cosine.boseconomy.bracket.setting.BracketOnlineModeSetting;
import cosine.boseconomy.bracket.setting.BracketPermissionNodesSetting;
import cosine.boseconomy.bracket.setting.BracketPlayerJoinSetting;
import cosine.boseconomy.bracket.setting.BracketPlayerLeaveSetting;
import cosine.boseconomy.bracket.setting.BracketRemoveMemberSetting;
import cosine.boseconomy.bracket.setting.BracketSetting;
import cosine.boseconomy.bracket.setting.BracketValueSetting;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class Bracket
{
  protected BracketManager manager;
  private String name;
  protected final SettingsGroup<BracketSetting<?>> settings;
  protected long lastTime = 0L;
  protected long lastOnlineTimeUpdate = 0L;
  private HashMap<String, BracketMember> members;
  private final BracketMemberType nonMemberType;
  private final BracketMemberType normalMemberType;
  private final BracketMemberType masterMemberType;
  
  public class BracketMember
  {
    private final Account account;
    private double mult;
    private int onlineTime = 0;
    
    public BracketMember(Account account, int mult)
    {
      if (account == null) {
        throw new NullPointerException(
          "Attempted to create a BracketMember with a null account.");
      }
      this.account = account;
      setCount(mult);
    }
    
    public BracketMember(Account account)
    {
      this(account, 1);
    }
    
    public Account getAccount()
    {
      return this.account;
    }
    
    public double getCount()
    {
      return this.mult;
    }
    
    public void setCount(double mult)
    {
      if ((mult <= 0.0D) || (Double.isInfinite(mult)) || (Double.isNaN(mult))) {
        this.mult = 0.0D;
      } else {
        this.mult = mult;
      }
      Bracket.this.manager.setChanged();
    }
    
    public int getOnlineTime()
    {
      return this.onlineTime;
    }
    
    public void setOnlineTime(int onlineTime)
    {
      this.onlineTime = onlineTime;
      Bracket.this.manager.getPaymentManager().setChanged();
    }
    
    public void addOnlineTime(int onlineTime)
    {
      this.onlineTime += onlineTime;
      Bracket.this.manager.getPaymentManager().setChanged();
    }
  }
  
  public Bracket(BracketManager manager, String name, double value)
  {
    this.manager = manager;
    setName(name);
    this.members = new HashMap();
    this.settings = new SettingsGroup();
    
    this.settings.addSetting(new BracketMasterSetting(this.manager.getPlugin()));
    
    this.settings.addSetting(new BracketValueSetting(this.manager.getPlugin(), value));
    this.settings.addSetting(new BracketCanChangeValueSetting());
    this.settings.addSetting(new BracketMaximumValueSetting(this.manager.getPlugin()));
    this.settings.addSetting(new BracketMinimumValueSetting(this.manager.getPlugin()));
    
    this.settings.addSetting(new BracketIntervalSetting(this.manager.getPlugin()));
    
    this.settings.addSetting(new BracketCanRenameSetting());
    
    this.settings.addSetting(new BracketAddMemberSetting());
    this.settings.addSetting(new BracketRemoveMemberSetting());
    
    this.settings.addSetting(new BracketPlayerJoinSetting());
    this.settings.addSetting(new BracketPlayerLeaveSetting());
    
    this.settings.addSetting(new BracketBankJoinSetting());
    this.settings.addSetting(new BracketBankLeaveSetting());
    
    this.settings.addSetting(new BracketChangeMultiplierSetting());
    this.settings.addSetting(new BracketMaximumMultiplierSetting());
    this.settings.addSetting(new BracketMinimumMultiplierSetting());
    
    this.settings.addSetting(new BracketOnlineModeSetting());
    
    this.settings.addSetting(new BracketExcludedSetting());
    
    this.settings.addSetting(new BracketDisabledSetting());
    this.settings.addSetting(new BracketPermissionNodesSetting());
    
    this.settings.addSetting(new BracketMaximumMembersSetting());
    



    this.nonMemberType = new BracketMemberType();
    this.nonMemberType.getSettings().addSetting(new IsAdminTypeSetting(false));
    this.nonMemberType.getSettings().addSetting(new CanChangeValueSetting(false, null));
    
    this.normalMemberType = new BracketMemberType();
    this.normalMemberType.getSettings().addSetting(new IsAdminTypeSetting(false));
    this.normalMemberType.getSettings().addSetting(new CanChangeValueSetting(false, null));
    
    this.masterMemberType = new BracketMemberType();
    this.masterMemberType.getSettings().addSetting(new IsAdminTypeSetting(false));
    this.masterMemberType.getSettings().addSetting(new CanChangeValueSetting(false, 
      (BracketCanChangeValueSetting)this.settings.getSetting("can-change-value")));
  }
  
  public Bracket(BracketManager manager, String name)
  {
    this(manager, name, 0.0D);
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final void setName(String name)
  {
    if (name == null) {
      this.name = "";
    } else {
      this.name = name;
    }
    this.manager.setChanged();
  }
  
  public double getValue()
  {
    return 
      ((BracketValueSetting)getSetting("value")).getObjectValue().doubleValue();
  }
  
  public Account getMaster()
  {
    return 
      ((BracketMasterSetting)getSetting("master")).getObjectValue();
  }
  
  public boolean hasMasterAccess(Account a)
  {
    return (getMaster() != null) && 
      (a != null) && (
      (getMaster() == a) || (((getMaster() instanceof BankAccount)) && 
      ((a instanceof PlayerAccount)) && 
      (((BankAccount)getMaster()).isOwner((PlayerAccount)a))));
  }
  
  public boolean hasMasterAccess(CommandHandler.BOSCommandSender sender)
  {
    if ((getMaster() != null) && (sender.isPlayer()))
    {
      PlayerAccount pa = getManager().getPlugin().getAccountManager()
        .getPlayerAccount(sender.getAsPlayer());
      return (pa != null) && (
        (getMaster() == pa) || (((getMaster() instanceof BankAccount)) && (((BankAccount)getMaster()).isOwner(pa))));
    }
    return false;
  }
  
  public long getLastTime()
  {
    return this.lastTime;
  }
  
  public void setLastTime(long lastTime)
  {
    if (lastTime == 0L) {
      this.lastTime = System.currentTimeMillis();
    } else {
      this.lastTime = lastTime;
    }
    this.manager.getPaymentManager().setChanged();
  }
  
  public TimeInterval getPaymentInterval()
  {
    TimeInterval t = 
      ((BracketIntervalSetting)getSetting("interval"))
      .getObjectValue();
    if (t == null) {
      return 
        this.manager.getPlugin().getSettingsManager().getDefaultBracketInterval();
    }
    return t;
  }
  
  public boolean usingDefaultPaymentInterval()
  {
    return ((BracketIntervalSetting)
      getSetting("interval")).getObjectValue() == null;
  }
  
  public final BracketSetting<?> getSetting(String name)
  {
    return (BracketSetting)this.settings.getSetting(name);
  }
  
  public Iterator<BracketSetting<?>> getSettingIterator()
  {
    return this.settings.iterator();
  }
  
  public List<BracketSetting<?>> getSettingList()
  {
    return this.settings.getSettingList();
  }
  
  public BracketMember addMember(Account account)
  {
    if (account != null)
    {
      BracketMember m = (BracketMember)this.members.get(account.getHashName());
      if (m == null)
      {
        m = new BracketMember(account);
        this.members.put(account.getHashName(), m);
        account.addBracket(this, false);
        if ((account instanceof PlayerAccount)) {
          ((PlayerAccount)account).updatePermissionNodes();
        }
        this.manager.setChanged();
        return m;
      }
    }
    return null;
  }
  
  public void removeMember(Account account)
  {
    if (account != null)
    {
      BracketMember m = (BracketMember)this.members.remove(account.getHashName());
      if (m != null)
      {
        account.removeBracket(this, false);
        if ((account instanceof PlayerAccount)) {
          ((PlayerAccount)account).updatePermissionNodes();
        }
        this.manager.setChanged();
      }
    }
  }
  
  public BracketMember getMember(Account account)
  {
    return (BracketMember)this.members.get(account.getHashName());
  }
  
  public BracketMember getMember(String name)
  {
    if (name == null) {
      return null;
    }
    if ((name.length() > 0) && (name.charAt(0) == '$')) {
      return (BracketMember)this.members.get(name.toLowerCase());
    }
    return (BracketMember)this.members.get('^' + name.toLowerCase());
  }
  
  public boolean isMember(Account account)
  {
    return getMember(account) != null;
  }
  
  public void renameMember(String oldHash, String newHash)
  {
    BracketMember bm = (BracketMember)this.members.remove(oldHash);
    if (bm != null) {
      this.members.put(newHash, bm);
    }
  }
  
  public void notifyRemoval()
  {
    if (getMaster() != null) {
      getMaster().removeBracket(this, true);
    }
    for (BracketMember bm : this.members.values())
    {
      bm.account.removeBracket(this, false);
      if ((getPermissionNodes() != null) && (!getPermissionNodes().equals(""))) {
        if ((bm.getAccount() instanceof PlayerAccount))
        {
          for (String node : getPermissionNodes()) {
            ((PlayerAccount)bm.getAccount()).unsetPermissionNode(node);
          }
          ((PlayerAccount)bm.getAccount()).updatePermissionNodes();
        }
      }
    }
  }
  
  public boolean getExcluded()
  {
    return 
      ((BracketExcludedSetting)getSetting("excluded")).getObjectValue().booleanValue();
  }
  
  public boolean getDisabled()
  {
    return 
      ((BracketDisabledSetting)getSetting("disabled")).getObjectValue().booleanValue();
  }
  
  public boolean getOnlineMode()
  {
    return 
      ((BracketOnlineModeSetting)getSetting("online-mode")).getObjectValue().booleanValue();
  }
  
  public List<String> getPermissionNodes()
  {
    return 
      ((BracketPermissionNodesSetting)getSetting("permission-nodes")).getObjectValue();
  }
  
  public BracketManager getManager()
  {
    return this.manager;
  }
  
  public void setManager(BracketManager manager)
  {
    this.manager = manager;
  }
  
  public Iterator<BracketMember> getMemberIterator()
  {
    return this.members.values().iterator();
  }
  
  public List<BracketMember> getMemberList()
  {
    return new LinkedList(this.members.values());
  }
  
  public int getMembersSize()
  {
    return this.members.size();
  }
  
  public abstract void buildPaySession(long paramLong, PaySession paramPaySession, boolean paramBoolean);
  
  public void updateOnlineTimes(long currentTime)
  {
    if ((!getDisabled()) && (getPaymentInterval().getSeconds() > 0))
    {
      if (this.lastOnlineTimeUpdate != 0L)
      {
        int intervalSeconds = 
          (int)((currentTime - this.lastOnlineTimeUpdate) / 1000L);
        for (BracketMember m : this.members.values()) {
          if ((m.account instanceof PlayerAccount))
          {
            if (((PlayerAccount)m.account).getPlayer() != null) {
              m.addOnlineTime(intervalSeconds);
            }
          }
          else if ((m.account instanceof BankAccount)) {
            m.addOnlineTime(intervalSeconds);
          }
        }
      }
      this.lastOnlineTimeUpdate = currentTime;
    }
  }
  
  public void resetLastOnlineTimeUpdate()
  {
    this.lastOnlineTimeUpdate = 0L;
  }
  
  public int getAddMemberValue()
  {
    return 
      ((BracketAddMemberSetting)getSetting("add-member")).getObjectValue().intValue();
  }
  
  public int getRemoveMemberValue()
  {
    return 
      ((BracketRemoveMemberSetting)getSetting("remove-member")).getObjectValue().intValue();
  }
  
  public int getPlayerJoinValue()
  {
    return 
      ((BracketPlayerJoinSetting)getSetting("player-join")).getObjectValue().intValue();
  }
  
  public int getPlayerLeaveValue()
  {
    return 
      ((BracketPlayerLeaveSetting)getSetting("player-leave")).getObjectValue().intValue();
  }
  
  public int getBankJoinValue()
  {
    return 
      ((BracketBankJoinSetting)getSetting("bank-join")).getObjectValue().intValue();
  }
  
  public int getBankLeaveValue()
  {
    return 
      ((BracketBankLeaveSetting)getSetting("bank-leave")).getObjectValue().intValue();
  }
  
  public int getChangeMultiplierValue()
  {
    return 
      ((BracketChangeMultiplierSetting)getSetting("change-multiplier")).getObjectValue().intValue();
  }
  
  public double getMaximumMultiplier()
  {
    return 
      ((BracketMaximumMultiplierSetting)getSetting("maximum-multiplier")).getObjectValue().doubleValue();
  }
  
  public double getMinimumMultiplier()
  {
    return 
      ((BracketMinimumMultiplierSetting)getSetting("minimum-multiplier")).getObjectValue().doubleValue();
  }
  
  public int getMaximumMembers()
  {
    return 
      ((BracketMaximumMembersSetting)getSetting("maximum-members")).getObjectValue().intValue();
  }
  
  public boolean getCanRename()
  {
    return 
      ((BracketCanRenameSetting)getSetting("can-rename")).getObjectValue().booleanValue();
  }
  
  public String toString()
  {
    return getName();
  }
  
  public abstract String getType();
  
  public abstract String getTypeCaps();
  
  public final BracketMemberType getMemberType(CommandHandler.BOSCommandSender sender, boolean considerBankMaster)
  {
    return getMemberType(sender, considerBankMaster, null);
  }
  
  public final BracketMemberType getMemberType(CommandHandler.BOSCommandSender sender, boolean considerBankMaster, String adminPerm)
  {
    if ((sender != null) && (sender.isPlayer()))
    {
      if (((adminPerm != null) && (sender.getSender().hasPermission(adminPerm))) || (
        (sender.getSender().isOp()) && (this.manager.getPlugin().getSettingsManager().getUseOpPermissions()))) {
        return this.manager.getAdminMemberType();
      }
      PlayerAccount pa = 
        this.manager.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
      if ((getMaster() == pa) || ((considerBankMaster) && (hasMasterAccess(pa)))) {
        return getMasterMemberType();
      }
      BracketMember bm = getMember(pa);
      if (bm == null) {
        return getNonMemberType();
      }
      return getNormalMemberType();
    }
    return this.manager.getAdminMemberType();
  }
  
  public final BracketMemberType getNonMemberType()
  {
    return this.nonMemberType;
  }
  
  public final BracketMemberType getNormalMemberType()
  {
    return this.normalMemberType;
  }
  
  public final BracketMemberType getMasterMemberType()
  {
    return this.masterMemberType;
  }
  
  public static class NameComparator
    implements Comparator<Bracket>
  {
    public int compare(Bracket b1, Bracket b2)
    {
      return b1.getName().compareTo(b2.getName());
    }
  }
  
  public static class MemberNameComparator
    implements Comparator<Bracket.BracketMember>
  {
    public int compare(Bracket.BracketMember bm1, Bracket.BracketMember bm2)
    {
      return bm1.getAccount().getName().compareTo(bm2.getAccount().getName());
    }
  }
}

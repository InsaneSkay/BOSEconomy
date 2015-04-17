package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Account;
import cosine.boseconomy.AccountManager;
import cosine.boseconomy.BOSEconomy;
import cosine.boseconomy.BankAccount;
import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.PlayerAccount;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

public class BracketMasterSetting
  extends BracketSetting<Account>
{
  public static final String NAME = "master";
  public static final Account DEFAULT_VALUE = null;
  private Account value;
  private final BOSEconomy plugin;
  
  public BracketMasterSetting(BOSEconomy plugin)
  {
    this(plugin, DEFAULT_VALUE);
  }
  
  public BracketMasterSetting(BOSEconomy plugin, Account value)
  {
    this.plugin = plugin;
    this.value = value;
  }
  
  public String getName()
  {
    return "master";
  }
  
  public String getStringValue()
  {
    if (this.value == null) {
      return "<None>";
    }
    return this.value.getName() + " (" + this.value.getTypeCaps() + ")";
  }
  
  public String getDataStringValue()
  {
    if (this.value == null) {
      return "";
    }
    return this.value.getDataName();
  }
  
  public Account getObjectValue()
  {
    return this.value;
  }
  
  public Account getDefaultValue()
  {
    return DEFAULT_VALUE;
  }
  
  public void setValue(Account value, Bracket bracket)
  {
    if ((value == null) || ((value instanceof PlayerAccount)) || ((value instanceof BankAccount)))
    {
      if ((bracket != null) && (this.value != null)) {
        this.value.removeBracket(bracket, true);
      }
      this.value = value;
      if (bracket != null)
      {
        if (this.value != null) {
          this.value.addBracket(bracket, true);
        }
        bracket.getManager().setChanged();
      }
    }
    else
    {
      throw new ClassCastException(
        "Attempted to set the master to an invalid account type: " + 
        value.getClass().getSimpleName());
    }
  }
  
  public boolean setValue(CommandHandler.BOSCommandSender sender, Bracket bracket, String value, boolean changeOnError, boolean checkSettings, boolean feedback)
  {
    if (sender != null)
    {
      BracketMemberType type = 
        bracket.getMemberType(sender, true, "BOSEconomy.admin.bracket.set");
      if (!type.isAdminType())
      {
        setMessage("You do not have permission to change this setting.");
        return false;
      }
    }
    Account newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    boolean fakeChange = this.value == newValue;
    if ((fakeChange) || (newValue == null) || ((newValue instanceof PlayerAccount)) || 
      ((newValue instanceof BankAccount)))
    {
      if ((!fakeChange) && (newValue != null)) {
        if (bracket.isMember(newValue))
        {
          if (changeOnError) {
            setToDefaultValue(bracket);
          }
          setMessage(((newValue instanceof BankAccount) ? "Bank account " : "User ") + 
            newValue.getName() + " cannot be both the master and a member of the " + 
            bracket.getName() + " bracket.");
          return false;
        }
      }
      if (feedback)
      {
        if ((!fakeChange) && (this.value != null)) {
          this.value.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + (
            (bracket.getMaster() instanceof PlayerAccount) ? "You are" : 
            new StringBuilder("Bank account ").append(bracket.getMaster().getName()).append(" is").toString()) + 
            " no longer the master of the " + bracket.getName() + " bracket.");
        }
        if (newValue == null)
        {
          if (sender != null) {
            if (fakeChange) {
              sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + 
                "Cleared the master of bracket " + bracket.getName() + ".");
            } else {
              sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + 
                "Cleared the master of bracket " + bracket.getName() + ".");
            }
          }
        }
        else
        {
          if (!fakeChange) {
            newValue.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + (
              (newValue instanceof PlayerAccount) ? "You are" : new StringBuilder("Bank account ")
              .append(newValue.getName()).append(" is").toString()) + " now the master of the " + 
              bracket.getName() + " bracket.");
          }
          if (sender != null) {
            if (fakeChange) {
              sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + 
                "Set the master of bracket '" + bracket.getName() + "' to " + (
                (newValue instanceof BankAccount) ? "bank account '" : "player '") + 
                newValue.getName() + "'.");
            } else {
              sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + 
                "Set the master of bracket '" + bracket.getName() + "' to " + (
                (newValue instanceof BankAccount) ? "bank account '" : "player '") + 
                newValue.getName() + "'.");
            }
          }
        }
      }
      if (!fakeChange) {
        setValue(newValue, bracket);
      }
      return true;
    }
    if (changeOnError) {
      setToDefaultValue(bracket);
    }
    setMessage("Account type " + newValue.getType() + " cannot be a bracket master.");
    return false;
  }
  
  public Account parseString(String value)
  {
    if ((value == null) || (value.length() == 0))
    {
      setParseSuccess(true);
      return getDefaultValue();
    }
    Account newMaster = this.plugin.getAccountManager().getAccountByName(value);
    if (newMaster == null)
    {
      if (value.charAt(0) == '$')
      {
        setMessage("Could not find a bank account named '" + value.substring(1) + 
          "'.");
        setParseSuccess(false);
        return null;
      }
      setMessage("Could not find a player named '" + value + "'.");
      setParseSuccess(false);
      return null;
    }
    setParseSuccess(true);
    return newMaster;
  }
  
  public boolean hasCustomFeedback()
  {
    return true;
  }
}

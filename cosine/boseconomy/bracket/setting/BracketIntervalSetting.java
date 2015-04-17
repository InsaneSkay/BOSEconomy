package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Account;
import cosine.boseconomy.BOSEconomy;
import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.SettingsManager;
import cosine.boseconomy.TimeInterval;
import cosine.boseconomy.TimeInterval.TimeIntervalFormatException;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

public class BracketIntervalSetting
  extends BracketSetting<TimeInterval>
{
  public static final String NAME = "interval";
  public static final TimeInterval DEFAULT_VALUE = null;
  private TimeInterval value;
  private final BOSEconomy plugin;
  
  public BracketIntervalSetting(BOSEconomy plugin)
  {
    this(plugin, DEFAULT_VALUE);
  }
  
  public BracketIntervalSetting(BOSEconomy plugin, TimeInterval value)
  {
    this.plugin = plugin;
    this.value = value;
  }
  
  public String getName()
  {
    return "interval";
  }
  
  public String getStringValue()
  {
    if (this.value == null) {
      return 
      
        "Default (" + this.plugin.getSettingsManager().getDefaultBracketInterval().toString() + ")";
    }
    return this.value.getString();
  }
  
  public String getDataStringValue()
  {
    if (this.value == null) {
      return "";
    }
    return this.value.getString();
  }
  
  public TimeInterval getObjectValue()
  {
    return this.value;
  }
  
  public TimeInterval getDefaultValue()
  {
    return DEFAULT_VALUE;
  }
  
  public void setValue(TimeInterval value, Bracket bracket)
  {
    this.value = value;
    if (bracket != null) {
      bracket.getManager().setChanged();
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
    TimeInterval newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    if (feedback) {
      if (newValue == null)
      {
        if (sender != null) {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + 
            "Set the interval of the '" + bracket.getName() + 
            "' bracket to the default interval. (" + 
            this.plugin.getSettingsManager().getDefaultBracketInterval().toString() + ")");
        }
        if ((bracket.getMaster() != null) && ((sender == null) || (!bracket.hasMasterAccess(sender)))) {
          bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + "An administrator set the interval of the '" + 
            bracket.getName() + "' bracket to the default interval. (" + 
            this.plugin.getSettingsManager().getDefaultBracketInterval().toString() + ")");
        }
      }
      else
      {
        if (sender != null) {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Set the interval of the '" + 
            bracket.getName() + "' bracket to '" + newValue.toString() + "'.");
        }
        if ((bracket.getMaster() != null) && ((sender == null) || (!bracket.hasMasterAccess(sender)))) {
          bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + "An administrator set the interval of the '" + 
            bracket.getName() + "' bracket to '" + newValue.toString() + "'.");
        }
      }
    }
    setValue(newValue, bracket);
    return true;
  }
  
  public TimeInterval parseString(String value)
  {
    if ((value == null) || (value.length() == 0) || (value.equalsIgnoreCase("default")))
    {
      setParseSuccess(true);
      return getDefaultValue();
    }
    try
    {
      newValue = new TimeInterval(value);
    }
    catch (TimeInterval.TimeIntervalFormatException ex)
    {
      TimeInterval newValue;
      setMessage(ex.getMessage());
      setParseSuccess(false);
      return null;
    }
    TimeInterval newValue;
    if (newValue.getTime() < 0)
    {
      setMessage("The time interval cannot be negative.");
      setParseSuccess(false);
      return null;
    }
    setParseSuccess(true);
    return newValue;
  }
  
  public boolean hasCustomFeedback()
  {
    return true;
  }
}

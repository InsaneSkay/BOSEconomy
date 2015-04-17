package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.BOSEconomy;
import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.MoneyFormatter;
import cosine.boseconomy.SettingsGroup;
import cosine.boseconomy.bracket.membertype.BracketMemberType;
import cosine.boseconomy.bracket.membertype.CanChangeValueSetting;

public class BracketValueSetting
  extends BracketSetting<Double>
{
  public static final String NAME = "value";
  public static final double DEFAULT_VALUE = 0.0D;
  private double value;
  private final BOSEconomy plugin;
  
  public BracketValueSetting(BOSEconomy plugin)
  {
    this(plugin, 0.0D);
  }
  
  public BracketValueSetting(BOSEconomy plugin, double value)
  {
    this.plugin = plugin;
    this.value = value;
  }
  
  public String getName()
  {
    return "value";
  }
  
  public String getStringValue()
  {
    return this.plugin.getMoneyFormatter().formatMoney(this.value);
  }
  
  public String getDataStringValue()
  {
    return String.valueOf(this.value);
  }
  
  public Double getObjectValue()
  {
    return Double.valueOf(this.value);
  }
  
  public Double getDefaultValue()
  {
    return Double.valueOf(0.0D);
  }
  
  public void setValue(Double value, Bracket bracket)
  {
    this.value = value.doubleValue();
    if (bracket != null) {
      bracket.getManager().setChanged();
    }
  }
  
  public boolean setValue(CommandHandler.BOSCommandSender sender, Bracket bracket, String value, boolean changeOnError, boolean checkSettings, boolean feedback)
  {
    boolean canChange = false;
    BracketMemberType type = 
      bracket.getMemberType(sender, true, "BOSEconomy.admin.bracket.set");
    if (type.isAdminType())
    {
      canChange = true;
    }
    else
    {
      CanChangeValueSetting s = 
        (CanChangeValueSetting)type.getSettings().getSetting("can-change-value");
      if ((s != null) || (
        (s != null) && (s.getObjectValue().booleanValue()))) {
        canChange = true;
      }
    }
    if (!canChange)
    {
      setMessage("You do not have permission to change this setting.");
      return false;
    }
    Double newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    if ((checkSettings) && (!type.isAdminType()))
    {
      BracketMaximumValueSetting maxSetting = 
        (BracketMaximumValueSetting)bracket.getSetting("maximum-value");
      double max;
      double max;
      if (maxSetting == null) {
        max = -1.0D;
      } else {
        max = maxSetting.getObjectValue().doubleValue();
      }
      if ((max >= 0.0D) && (newValue.doubleValue() > max))
      {
        if (changeOnError) {
          setValue(Double.valueOf(max), bracket);
        }
        setMessage("The value cannot be greater than the maximum (" + 
          this.plugin.getMoneyFormatter().formatMoney(max) + ").");
        return false;
      }
      BracketMinimumValueSetting minSetting = 
        (BracketMinimumValueSetting)bracket.getSetting("minimum-value");
      double min;
      double min;
      if (minSetting == null) {
        min = -1.0D;
      } else {
        min = minSetting.getObjectValue().doubleValue();
      }
      if ((min >= 0.0D) && (newValue.doubleValue() < min))
      {
        if (changeOnError) {
          setValue(Double.valueOf(min), bracket);
        }
        setMessage("The value cannot be less than the minimum (" + 
          this.plugin.getMoneyFormatter().formatMoney(min) + ").");
        return false;
      }
    }
    setValue(newValue, bracket);
    return true;
  }
  
  public Double parseString(String value)
  {
    if (value == null)
    {
      setParseSuccess(true);
      return getDefaultValue();
    }
    try
    {
      newValue = Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      double newValue;
      setMessage("The value should be a number.");
      setParseSuccess(false);
      return null;
    }
    double newValue;
    if (newValue < 0.0D)
    {
      setMessage("The value cannot be negative.");
      setParseSuccess(false);
      return null;
    }
    if (Double.isInfinite(newValue))
    {
      setMessage("The value cannot be infinite.");
      setParseSuccess(false);
      return null;
    }
    if (Double.isNaN(newValue))
    {
      setMessage("The value cannot be NaN.");
      setParseSuccess(false);
      return null;
    }
    setParseSuccess(true);
    return Double.valueOf(this.plugin.getMoneyFormatter().roundMoney(newValue));
  }
}

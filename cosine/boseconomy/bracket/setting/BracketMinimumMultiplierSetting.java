package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.BOSEconomy;
import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.RequestHandler;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

public class BracketMinimumMultiplierSetting
  extends BracketSetting<Double>
{
  public static final String NAME = "minimum-multiplier";
  public static final double DEFAULT_VALUE = -1.0D;
  private double value;
  
  public BracketMinimumMultiplierSetting()
  {
    this(-1.0D);
  }
  
  public BracketMinimumMultiplierSetting(double value)
  {
    this.value = value;
  }
  
  public String getName()
  {
    return "minimum-multiplier";
  }
  
  public String getStringValue()
  {
    return this.value < 0.0D ? "N/A" : String.valueOf(this.value);
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
    return Double.valueOf(-1.0D);
  }
  
  public void setValue(Double value, Bracket bracket)
  {
    this.value = value.doubleValue();
    if (bracket != null)
    {
      bracket.getManager().setChanged();
      bracket.getManager().getPlugin().getRequestHandler().checkRequestValidities();
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
    Double newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    if ((checkSettings) && (newValue.doubleValue() >= 0.0D))
    {
      BracketMaximumMultiplierSetting s = 
        (BracketMaximumMultiplierSetting)bracket.getSetting("maximum-multiplier");
      double max;
      double max;
      if (s == null) {
        max = -1.0D;
      } else {
        max = s.getObjectValue().doubleValue();
      }
      if ((max >= 0.0D) && (newValue.doubleValue() > max))
      {
        if (changeOnError) {
          setValue(Double.valueOf(max), bracket);
        }
        setMessage("The minimum multiplier cannot be less than the maximum (" + 
          max + ").");
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
    if ((value.trim().equalsIgnoreCase("N/A")) || (value.trim().equalsIgnoreCase("none")))
    {
      setParseSuccess(true);
      return Double.valueOf(-1.0D);
    }
    try
    {
      newValue = Double.parseDouble(value);
    }
    catch (NumberFormatException ex)
    {
      double newValue;
      setMessage("The minimum multiplier should be a number.");
      setParseSuccess(false);
      return null;
    }
    double newValue;
    if (Double.isInfinite(newValue))
    {
      setMessage("The minimum multiplier cannot be infinite.");
      setParseSuccess(false);
      return null;
    }
    if (Double.isNaN(newValue))
    {
      setMessage("The minimum multiplier cannot be NaN.");
      setParseSuccess(false);
      return null;
    }
    if (newValue < 0.0D)
    {
      setParseSuccess(true);
      return Double.valueOf(-1.0D);
    }
    setParseSuccess(true);
    return Double.valueOf(newValue);
  }
}

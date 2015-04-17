package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

public class BracketMaximumMembersSetting
  extends BracketSetting<Integer>
{
  public static final String NAME = "maximum-members";
  public static final int DEFAULT_VALUE = -1;
  private int value;
  
  public BracketMaximumMembersSetting()
  {
    this(-1);
  }
  
  public BracketMaximumMembersSetting(int value)
  {
    this.value = value;
  }
  
  public String getName()
  {
    return "maximum-members";
  }
  
  public String getStringValue()
  {
    return this.value < 0 ? "N/A" : String.valueOf(this.value);
  }
  
  public String getDataStringValue()
  {
    return String.valueOf(this.value);
  }
  
  public Integer getObjectValue()
  {
    return Integer.valueOf(this.value);
  }
  
  public Integer getDefaultValue()
  {
    return Integer.valueOf(-1);
  }
  
  public void setValue(Integer value, Bracket bracket)
  {
    this.value = value.intValue();
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
    Integer newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    setValue(newValue, bracket);
    return true;
  }
  
  public Integer parseString(String value)
  {
    if (value == null)
    {
      setParseSuccess(true);
      return getDefaultValue();
    }
    if ((value.trim().equalsIgnoreCase("N/A")) || (value.trim().equalsIgnoreCase("none")))
    {
      setParseSuccess(true);
      return Integer.valueOf(-1);
    }
    try
    {
      int newValue = Integer.parseInt(value);
      if (newValue <= -1) {
        newValue = -1;
      }
      setParseSuccess(true);
      return Integer.valueOf(newValue);
    }
    catch (NumberFormatException ex)
    {
      setMessage("The maximum members should be a number, or 'N/A'.");
      setParseSuccess(false);
    }
    return null;
  }
}

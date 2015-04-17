package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

public abstract class BracketADRSetting
  extends BracketSetting<Integer>
{
  public static final int DEFAULT_VALUE = 0;
  protected int value;
  
  public BracketADRSetting()
  {
    this(0);
  }
  
  public BracketADRSetting(int value)
  {
    this.value = value;
  }
  
  public String getStringValue()
  {
    return getDataStringValue();
  }
  
  public String getDataStringValue()
  {
    if (this.value == 0) {
      return "deny";
    }
    if (this.value == 1) {
      return "allow";
    }
    if (this.value == 2) {
      return "request";
    }
    return String.valueOf(this.value);
  }
  
  public Integer getObjectValue()
  {
    return Integer.valueOf(this.value);
  }
  
  public Integer getDefaultValue()
  {
    return Integer.valueOf(0);
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
    value = value.toLowerCase();
    int newValue;
    int newValue;
    if ((value.equals("deny")) || (value.equals("no")))
    {
      newValue = 0;
    }
    else
    {
      int newValue;
      if ((value.equals("allow")) || (value.equals("yes")))
      {
        newValue = 1;
      }
      else
      {
        int newValue;
        if ((value.equals("request")) || (value.equals("req")) || (value.equals("ask")))
        {
          newValue = 2;
        }
        else
        {
          try
          {
            newValue = Integer.parseInt(value);
          }
          catch (NumberFormatException ex)
          {
            int newValue;
            setMessage("The value should be either 'allow', 'deny', or 'request'.");
            setParseSuccess(false);
            return null;
          }
          if ((newValue != 0) && (newValue != 1) && 
            (newValue != 2))
          {
            setMessage("The value should be either 'allow', 'deny', or 'request'.");
            setParseSuccess(false);
            return null;
          }
        }
      }
    }
    setParseSuccess(true);
    return Integer.valueOf(newValue);
  }
}

package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.Setting;
import cosine.boseconomy.bracket.membertype.BracketMemberType;

abstract class BracketBooleanSetting
  extends BracketSetting<Boolean>
{
  public static final boolean DEFAULT_VALUE = false;
  protected boolean value;
  
  public BracketBooleanSetting()
  {
    this(false);
  }
  
  public BracketBooleanSetting(boolean value)
  {
    this.value = value;
  }
  
  public String getStringValue()
  {
    return getDataStringValue();
  }
  
  public String getDataStringValue()
  {
    return String.valueOf(this.value);
  }
  
  public Boolean getObjectValue()
  {
    return Boolean.valueOf(this.value);
  }
  
  public Boolean getDefaultValue()
  {
    return Boolean.valueOf(false);
  }
  
  public void setValue(Boolean value, Bracket bracket)
  {
    this.value = value.booleanValue();
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
    Boolean newValue = parseString(value);
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
  
  public Boolean parseString(String value)
  {
    if (value == null)
    {
      setParseSuccess(true);
      return getDefaultValue();
    }
    value = value.toLowerCase().trim();
    if (Setting.isBooleanFalseString(value, false))
    {
      setParseSuccess(true);
      return Boolean.valueOf(false);
    }
    if (Setting.isBooleanTrueString(value, false))
    {
      setParseSuccess(true);
      return Boolean.valueOf(true);
    }
    setMessage("The value should be either 'true' or 'false'.");
    setParseSuccess(false);
    return null;
  }
}

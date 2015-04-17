package cosine.boseconomy.bracket.membertype;

import cosine.boseconomy.Setting;

public abstract class BooleanSetting
  extends BracketMemberTypeSetting<Boolean>
{
  public static final boolean DEFAULT_VALUE = false;
  protected boolean value;
  
  public BooleanSetting()
  {
    this(false);
  }
  
  public BooleanSetting(boolean value)
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
  
  public void setValue(Boolean value)
  {
    this.value = value.booleanValue();
  }
  
  public Boolean parseString(String value)
  {
    if (value == null) {
      return getDefaultValue();
    }
    value = value.toLowerCase().trim();
    if (Setting.isBooleanFalseString(value, false)) {
      return Boolean.valueOf(false);
    }
    if (Setting.isBooleanTrueString(value, false)) {
      return Boolean.valueOf(true);
    }
    setMessage("The value should be either 'true' or 'false'.");
    return null;
  }
}

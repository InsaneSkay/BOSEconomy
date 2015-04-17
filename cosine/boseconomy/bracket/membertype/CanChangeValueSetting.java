package cosine.boseconomy.bracket.membertype;

import cosine.boseconomy.bracket.setting.BracketCanChangeValueSetting;

public class CanChangeValueSetting
  extends BooleanSetting
{
  public static final String NAME = "can-change-value";
  private final BracketCanChangeValueSetting kludgeValue;
  
  public CanChangeValueSetting(BracketCanChangeValueSetting kludgeValue)
  {
    this.kludgeValue = kludgeValue;
  }
  
  public CanChangeValueSetting(boolean value, BracketCanChangeValueSetting kludgeValue)
  {
    super(value);
    this.kludgeValue = kludgeValue;
  }
  
  public Boolean getObjectValue()
  {
    if (this.kludgeValue == null) {
      return super.getObjectValue();
    }
    return this.kludgeValue.getObjectValue();
  }
  
  public Boolean getDefaultValue()
  {
    if (this.kludgeValue == null) {
      return super.getDefaultValue();
    }
    return Boolean.valueOf(false);
  }
  
  public String getName()
  {
    return "can-change-value";
  }
}

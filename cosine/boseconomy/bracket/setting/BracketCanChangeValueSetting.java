package cosine.boseconomy.bracket.setting;

public class BracketCanChangeValueSetting
  extends BracketBooleanSetting
{
  public static final String NAME = "can-change-value";
  
  public BracketCanChangeValueSetting() {}
  
  public BracketCanChangeValueSetting(boolean value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "can-change-value";
  }
}

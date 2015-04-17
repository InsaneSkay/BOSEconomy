package cosine.boseconomy.bracket.setting;

public class BracketDisabledSetting
  extends BracketBooleanSetting
{
  public static final String NAME = "disabled";
  
  public BracketDisabledSetting() {}
  
  public BracketDisabledSetting(boolean value)
  {
    this.value = value;
  }
  
  public String getName()
  {
    return "disabled";
  }
}

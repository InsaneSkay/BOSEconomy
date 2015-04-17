package cosine.boseconomy.bracket.setting;

public class BracketExcludedSetting
  extends BracketBooleanSetting
{
  public static final String NAME = "excluded";
  
  public BracketExcludedSetting() {}
  
  public BracketExcludedSetting(boolean value)
  {
    this.value = value;
  }
  
  public String getName()
  {
    return "excluded";
  }
}

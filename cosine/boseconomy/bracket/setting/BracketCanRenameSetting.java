package cosine.boseconomy.bracket.setting;

public class BracketCanRenameSetting
  extends BracketBooleanSetting
{
  public static final String NAME = "can-rename";
  
  public BracketCanRenameSetting() {}
  
  public BracketCanRenameSetting(boolean value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "can-rename";
  }
}

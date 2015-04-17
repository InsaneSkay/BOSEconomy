package cosine.boseconomy.bracket.setting;

public class BracketChangeMultiplierSetting
  extends BracketADRSetting
{
  public static final String NAME = "change-multiplier";
  
  public BracketChangeMultiplierSetting() {}
  
  public BracketChangeMultiplierSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "change-multiplier";
  }
}

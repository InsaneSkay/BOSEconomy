package cosine.boseconomy.bracket.setting;

public class BracketPlayerLeaveSetting
  extends BracketADRSetting
{
  public static final String NAME = "player-leave";
  
  public BracketPlayerLeaveSetting() {}
  
  public BracketPlayerLeaveSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "player-leave";
  }
}

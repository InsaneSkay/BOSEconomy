package cosine.boseconomy.bracket.setting;

public class BracketPlayerJoinSetting
  extends BracketADRSetting
{
  public static final String NAME = "player-join";
  
  public BracketPlayerJoinSetting() {}
  
  public BracketPlayerJoinSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "player-join";
  }
}

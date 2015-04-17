package cosine.boseconomy.bracket.setting;

public class BracketRemoveMemberSetting
  extends BracketADRSetting
{
  public static final String NAME = "remove-member";
  
  public BracketRemoveMemberSetting() {}
  
  public BracketRemoveMemberSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "remove-member";
  }
}

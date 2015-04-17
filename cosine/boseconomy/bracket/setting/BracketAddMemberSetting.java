package cosine.boseconomy.bracket.setting;

public class BracketAddMemberSetting
  extends BracketADRSetting
{
  public static final String NAME = "add-member";
  
  public BracketAddMemberSetting() {}
  
  public BracketAddMemberSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "add-member";
  }
}

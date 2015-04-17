package cosine.boseconomy.bracket.setting;

public class BracketBankJoinSetting
  extends BracketADRSetting
{
  public static final String NAME = "bank-join";
  
  public BracketBankJoinSetting() {}
  
  public BracketBankJoinSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "bank-join";
  }
}

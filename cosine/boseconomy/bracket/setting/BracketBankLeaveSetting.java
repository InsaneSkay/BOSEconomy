package cosine.boseconomy.bracket.setting;

public class BracketBankLeaveSetting
  extends BracketADRSetting
{
  public static final String NAME = "bank-leave";
  
  public BracketBankLeaveSetting() {}
  
  public BracketBankLeaveSetting(int value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "bank-leave";
  }
}

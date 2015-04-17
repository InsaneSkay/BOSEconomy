package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.Bracket.BracketMember;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;

public class BracketOnlineModeSetting
  extends BracketBooleanSetting
{
  public static final String NAME = "online-mode";
  
  public BracketOnlineModeSetting() {}
  
  public BracketOnlineModeSetting(boolean value)
  {
    this.value = value;
  }
  
  public String getName()
  {
    return "online-mode";
  }
  
  public boolean setValue(CommandHandler.BOSCommandSender sender, Bracket bracket, String value, boolean changeOnError, boolean checkSettings, boolean feedback)
  {
    boolean currentValue = this.value;
    boolean ret = super.setValue(sender, bracket, value, changeOnError, checkSettings, feedback);
    if (this.value != currentValue)
    {
      if (this.value) {
        bracket.resetLastOnlineTimeUpdate();
      } else {
        for (Bracket.BracketMember member : bracket.getMemberList()) {
          member.setOnlineTime(0);
        }
      }
      bracket.getManager().setChanged();
    }
    return ret;
  }
}

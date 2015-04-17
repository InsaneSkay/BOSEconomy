package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.Setting;

public abstract class BracketSetting<T>
  extends Setting<T>
{
  public final void setToDefaultValue()
  {
    setToDefaultValue(null);
  }
  
  public final void setToDefaultValue(Bracket bracket)
  {
    setValue(getDefaultValue(), bracket);
  }
  
  public final void setValue(T value)
  {
    setValue(value, null);
  }
  
  public abstract void setValue(T paramT, Bracket paramBracket);
  
  public abstract boolean setValue(CommandHandler.BOSCommandSender paramBOSCommandSender, Bracket paramBracket, String paramString, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3);
  
  public boolean setValue(CommandHandler.BOSCommandSender sender, Bracket bracket, String[] array, int offset)
  {
    return setValue(sender, bracket, arrayToString(array, offset), false, true, true);
  }
  
  public boolean hasCustomFeedback()
  {
    return false;
  }
}

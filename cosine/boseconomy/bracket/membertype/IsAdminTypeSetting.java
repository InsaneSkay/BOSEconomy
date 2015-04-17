package cosine.boseconomy.bracket.membertype;

public class IsAdminTypeSetting
  extends BooleanSetting
{
  public static final String NAME = "is-admin-type";
  
  public IsAdminTypeSetting() {}
  
  public IsAdminTypeSetting(boolean value)
  {
    super(value);
  }
  
  public String getName()
  {
    return "is-admin-type";
  }
}

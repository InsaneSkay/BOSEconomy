package cosine.boseconomy.bracket.membertype;

import cosine.boseconomy.SettingsGroup;

public class BracketMemberType
{
  private final SettingsGroup<BracketMemberTypeSetting<?>> settings;
  private BracketMemberType parent;
  private String name;
  
  public BracketMemberType()
  {
    this.settings = new SettingsGroup();
  }
  
  public void setParent(BracketMemberType parent)
  {
    this.parent = parent;
    this.settings.setParent(this.parent.getSettings());
  }
  
  public SettingsGroup<BracketMemberTypeSetting<?>> getSettings()
  {
    return this.settings;
  }
  
  public boolean isAdminType()
  {
    BooleanSetting setting = (BooleanSetting)this.settings.getSetting("is-admin-type");
    if (setting == null) {
      return false;
    }
    return setting.getObjectValue().booleanValue();
  }
  
  public String getName()
  {
    return this.name;
  }
}

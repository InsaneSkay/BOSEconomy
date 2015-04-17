package cosine.boseconomy.bracket.setting;

import cosine.boseconomy.Bracket;
import cosine.boseconomy.Bracket.BracketMember;
import cosine.boseconomy.BracketManager;
import cosine.boseconomy.CommandHandler.BOSCommandSender;
import cosine.boseconomy.PlayerAccount;
import cosine.boseconomy.bracket.membertype.BracketMemberType;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class BracketPermissionNodesSetting
  extends BracketSetting<List<String>>
{
  public static final String NAME = "permission-nodes";
  public static final List<String> DEFAULT_VALUE = null;
  private final List<String> value;
  private String stringValue;
  private boolean stringUpToDate;
  
  public BracketPermissionNodesSetting()
  {
    this(DEFAULT_VALUE);
  }
  
  public BracketPermissionNodesSetting(List<String> value)
  {
    this.value = new LinkedList();
    if (value != null) {
      for (String node : value) {
        this.value.add(node);
      }
    }
  }
  
  public String getName()
  {
    return "permission-nodes";
  }
  
  public String getStringValue()
  {
    if (!this.stringUpToDate) {
      updateStringValue();
    }
    return this.stringValue;
  }
  
  public String getDataStringValue()
  {
    return getStringValue();
  }
  
  public List<String> getObjectValue()
  {
    List<String> list = new LinkedList();
    if (this.value != null) {
      for (String node : this.value) {
        list.add(node);
      }
    }
    return list;
  }
  
  public List<String> getDefaultValue()
  {
    return new LinkedList();
  }
  
  public void setValue(List<String> value, Bracket bracket)
  {
    if ((bracket != null) && (this.value.size() > 0)) {
      for (Bracket.BracketMember bm : bracket.getMemberList()) {
        if ((bm.getAccount() instanceof PlayerAccount)) {
          for (String node : this.value) {
            ((PlayerAccount)bm.getAccount()).unsetPermissionNode(node);
          }
        }
      }
    }
    this.value.clear();
    if (value != null) {
      for (String node : value) {
        this.value.add(node);
      }
    }
    this.stringUpToDate = false;
    if (bracket != null)
    {
      bracket.getManager().updatePermissionNodeList();
      for (Bracket.BracketMember bm : bracket.getMemberList()) {
        if ((bm.getAccount() instanceof PlayerAccount)) {
          ((PlayerAccount)bm.getAccount()).updatePermissionNodes();
        }
      }
      bracket.getManager().setChanged();
    }
  }
  
  public boolean setValue(CommandHandler.BOSCommandSender sender, Bracket bracket, String value, boolean changeOnError, boolean checkSettings, boolean feedback)
  {
    if (sender != null)
    {
      BracketMemberType type = 
        bracket.getMemberType(sender, true, "BOSEconomy.admin.bracket.set");
      if (!type.isAdminType())
      {
        setMessage("You do not have permission to change this setting.");
        return false;
      }
    }
    List<String> newValue = parseString(value);
    if (parseFailed())
    {
      if (changeOnError) {
        setToDefaultValue(bracket);
      }
      return false;
    }
    setValue(newValue, bracket);
    return true;
  }
  
  public List<String> parseString(String value)
  {
    List<String> newValue = new LinkedList();
    for (String node : this.value) {
      newValue.add(node);
    }
    if (value == null)
    {
      newValue.clear();
    }
    else
    {
      value.replaceAll("\\s+", "");
      if (value.length() == 0)
      {
        newValue.clear();
      }
      else
      {
        boolean adding;
        if (value.charAt(0) == '+')
        {
          boolean adding = true;
          value = value.substring(1);
        }
        else if (value.charAt(0) == '-')
        {
          boolean adding = false;
          value = value.substring(1);
        }
        else
        {
          newValue.clear();
          adding = true;
        }
        Scanner scan = new Scanner(value);
        scan.useDelimiter(",");
        while (scan.hasNext())
        {
          String node = scan.next();
          if (node.length() > 0) {
            if (adding)
            {
              if (!newValue.contains(node)) {
                newValue.add(node);
              }
            }
            else {
              newValue.remove(node);
            }
          }
        }
        scan.close();
      }
    }
    setParseSuccess(true);
    return newValue;
  }
  
  private void updateStringValue()
  {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String node : this.value)
    {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append(node);
    }
    this.stringValue = sb.toString();
    this.stringUpToDate = true;
  }
}

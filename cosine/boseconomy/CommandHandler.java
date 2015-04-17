package cosine.boseconomy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class CommandHandler
{
  private final BOSEconomy plugin;
  private final CommandMethods methods;
  private ArrayList<BOSCommand> commands;
  
  public CommandHandler(BOSEconomy plugin)
  {
    this.plugin = plugin;
    this.methods = new CommandMethods(this);
    


    this.commands = new ArrayList();
    
    this.commands.add(new HelpCommand());
    this.commands.add(new WalletCommand());
    this.commands.add(new IncomeCommand());
    this.commands.add(new MasteryCommand());
    this.commands.add(new PayCommand());
    this.commands.add(new InfoCommand());
    this.commands.add(new ViewmasteryCommand());
    this.commands.add(new SetCommand());
    this.commands.add(new AddCommand());
    this.commands.add(new SubCommand());
    this.commands.add(new ClearCommand());
    this.commands.add(new ScaleCommand());
    this.commands.add(new ReloadCommand());
    this.commands.add(new SaveCommand());
    this.commands.add(new StatsCommand());
    this.commands.add(new Top5Command());
    this.commands.add(new BracketCreateCommand());
    this.commands.add(new BracketRemoveCommand());
    this.commands.add(new BracketRenameCommand());
    this.commands.add(new BracketListCommand());
    this.commands.add(new BracketInfoCommand());
    this.commands.add(new BracketSetCommand());
    this.commands.add(new BracketSetmasterCommand());
    this.commands.add(new BracketRemovemasterCommand());
    this.commands.add(new BracketListmembersCommand());
    this.commands.add(new BracketAddmemberCommand());
    this.commands.add(new BracketRemovememberCommand());
    this.commands.add(new BracketPlayerJoinCommand());
    this.commands.add(new BracketPlayerLeaveCommand());
    

    this.commands.add(new BracketSetmultiplierCommand());
    this.commands.add(new BracketClearCommand());
    this.commands.add(new BankCreateCommand());
    this.commands.add(new BankRemoveCommand());
    this.commands.add(new BankRenameCommand());
    this.commands.add(new BankListCommand());
    this.commands.add(new BankInfoCommand());
    this.commands.add(new BankMasteryCommand());
    this.commands.add(new BankListmembersCommand());
    this.commands.add(new BankWithdrawCommand());
    this.commands.add(new BankDepositCommand());
    this.commands.add(new BankAddownerCommand());
    this.commands.add(new BankAddmemberCommand());
    this.commands.add(new BankRemovememberCommand());
    this.commands.add(new RequestListCommand());
    this.commands.add(new RequestDoCommand());
    this.commands.add(new PaydayResetCommand());
    this.commands.add(new AboutCommand());
  }
  
  public boolean tryCommands(CommandSender sender, Command cmd, String label, String[] args)
  {
    if ((sender instanceof BlockCommandSender))
    {
      if (((BlockCommandSender)sender).getBlock().getType() != Material.COMMAND)
      {
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Command submitted by unrecognized block type '" + 
          ((BlockCommandSender)sender).getBlock().getTypeId() + "'!");
        return false;
      }
      if (!this.plugin.getSettingsManager().getAllowCommandBlocks()) {
        return false;
      }
    }
    else if ((!(sender instanceof Player)) && (!(sender instanceof ConsoleCommandSender)))
    {
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
        "Command submitted by unrecognized sender type '" + 
        sender.getClass().getSimpleName() + "'!");
      return false;
    }
    BOSCommandSender bosSender = new BOSCommandSender(sender);
    try
    {
      String[] s = new String[args.length + 1];
      s[0] = label;
      for (int i = 1; i < s.length; i++) {
        s[i] = args[(i - 1)];
      }
      if (s.length > 0) {
        for (int i = 1; i < this.commands.size(); i++) {
          if (this.commands.get(i) != null)
          {
            int offset = ((BOSCommand)this.commands.get(i)).commandMatch(s);
            if (offset != -1)
            {
              boolean canUse = ((BOSCommand)this.commands.get(i)).canUse(bosSender);
              if (canUse) {
                ((BOSCommand)this.commands.get(i)).doCommand(bosSender, s, offset, false);
              } else {
                sender.sendMessage(BOSEconomy.BAD_COLOR + 
                  "You do not have permission to use this command.");
              }
              return true;
            }
          }
        }
      }
      int offset = ((BOSCommand)this.commands.get(0)).commandMatch(s);
      if (offset != -1)
      {
        boolean canUse = ((BOSCommand)this.commands.get(0)).canUse(bosSender);
        if (canUse) {
          ((BOSCommand)this.commands.get(0)).doCommand(bosSender, s, offset, false);
        } else {
          sender.sendMessage(BOSEconomy.BAD_COLOR + 
            "You do not have permission to view the BOSEconomy help.");
        }
        return true;
      }
      return false;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      if (bosSender.isPlayer())
      {
        bosSender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "That command threw an unhandled exception! Tell a server owner about this!");
        bosSender.sendMsgInfo(BOSEconomy.BAD_COLOR + "  (" + ex.getClass().getSimpleName() + ")");
        this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
          "Unhandled exception generated by a command from user '" + 
          bosSender.getAsPlayer().getName() + "'! Tell the plugin developer about this!");
      }
      else if (bosSender.isConsoleOrBlock())
      {
        bosSender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Unhandled exception generated by that command! Tell the plugin developer about this!");
      }
    }
    return false;
  }
  
  public ArrayList<BOSCommand> getCommands()
  {
    return this.commands;
  }
  
  public void updateCommandText()
  {
    for (BOSCommand c : this.commands) {
      c.updateUsageText();
    }
  }
  
  public boolean hasPermission(BOSCommandSender sender, String permission)
  {
    if (sender == null) {
      return true;
    }
    return hasPermission(sender.getSender(), permission);
  }
  
  public boolean hasPermission(BOSCommandSender sender, List<String> permissions)
  {
    if (sender == null) {
      return true;
    }
    return hasPermission(sender.getSender(), permissions);
  }
  
  public boolean hasPermission(Permissible p, String permission)
  {
    List<String> list = new LinkedList();
    list.add(permission);
    return hasPermission(p, list);
  }
  
  public boolean hasPermission(Permissible p, List<String> permissions)
  {
    if ((p == null) || ((p instanceof ConsoleCommandSender)) || ((p instanceof BlockCommandSender))) {
      return true;
    }
    if ((p instanceof Player))
    {
      if ((this.plugin.getSettingsManager().getUseOpPermissions()) && (((Player)p).isOp())) {
        return true;
      }
      for (String permission : permissions) {
        if (p.hasPermission(permission)) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isCommandLabel(String label)
  {
    return (label.equalsIgnoreCase("econ")) || 
      (label.equalsIgnoreCase("ec")) || (label.equalsIgnoreCase("bosecon")) || 
      (label.equalsIgnoreCase("boseconomy"));
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
  
  private static boolean isBracketAlias(String arg)
  {
    arg = arg.toLowerCase();
    return (arg.equals("br")) || (arg.equals("bracket")) || (arg.equals("brackets"));
  }
  
  private static boolean isBankAlias(String arg)
  {
    arg = arg.toLowerCase();
    return (arg.equals("ba")) || (arg.equals("bank")) || (arg.equals("banks"));
  }
  
  private static boolean isRequestAlias(String arg)
  {
    arg = arg.toLowerCase();
    return (arg.equals("req")) || (arg.equals("request")) || (arg.equals("requests"));
  }
  
  private static boolean isPaydayAlias(String arg)
  {
    arg = arg.toLowerCase();
    return (arg.equals("pd")) || (arg.equals("payday"));
  }
  
  public abstract class BOSCommand
  {
    protected List<String> permissions;
    protected String usageText;
    protected String helpText;
    
    public BOSCommand()
    {
      this.permissions = new LinkedList();
      updateUsageText();
    }
    
    public final boolean canUse(CommandHandler.BOSCommandSender sender)
    {
      return CommandHandler.this.hasPermission(sender.getSender(), this.permissions);
    }
    
    public abstract int commandMatch(String[] paramArrayOfString);
    
    public final String getUsageText()
    {
      return this.usageText;
    }
    
    public abstract void updateUsageText();
    
    public final String getHelpText()
    {
      return this.helpText;
    }
    
    public final void sendUsageMessage(CommandHandler.BOSCommandSender sender)
    {
      sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "Usage: " + getUsageText());
    }
    
    public abstract boolean doCommand(CommandHandler.BOSCommandSender paramBOSCommandSender, String[] paramArrayOfString, int paramInt, boolean paramBoolean);
  }
  
  private class WalletCommand
    extends CommandHandler.BOSCommand
  {
    public WalletCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.wallet");
      this.helpText = "Displays how much money you own.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length == 1) && (
        (CommandHandler.this.isCommandLabel(s[0])) || (s[0].equalsIgnoreCase("money")) || 
        (s[0].equalsIgnoreCase("wallet")))) {
        return 1;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        CommandHandler.this.methods.walletCommand(sender);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ";
    }
  }
  
  private class IncomeCommand
    extends CommandHandler.BOSCommand
  {
    public IncomeCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.income");
      this.helpText = "Lists your income bracket(s) and rate of payment.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("income"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        String pageArg = "1";
        if (s.length == 3) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.incomeCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ income [page]";
    }
  }
  
  private class MasteryCommand
    extends CommandHandler.BOSCommand
  {
    public MasteryCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.mastery");
      this.helpText = "Lists brackets that you are the master of.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("mastery"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        String pageArg = "1";
        if (s.length == 3) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.masteryCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ mastery [page]";
    }
  }
  
  private class PayCommand
    extends CommandHandler.BOSCommand
  {
    public PayCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.pay");
      this.permissions.add("BOSEconomy.common.bank.deposit");
      this.permissions.add("BOSEconomy.admin.bank.deposit");
      this.helpText = "Pays money to another user or bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 1) && 
        (s[0].equalsIgnoreCase("pay"))) {
        return 1;
      }
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("pay"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        try
        {
          double value = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(1 + offset)]));
          name2 = s[offset];
        }
        catch (Exception ex)
        {
          String name2;
          sendUsageMessage(sender);
          return true;
        }
        String name2;
        double value;
        CommandHandler.this.methods.payCommand(sender, name2, value);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "< /econ pay | /pay > <name> <amount>";
    }
  }
  
  private class InfoCommand
    extends CommandHandler.BOSCommand
  {
    public InfoCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.info");
      this.permissions.add("BOSEconomy.admin.bank.info");
      this.helpText = "Displays information about a player or bank.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (
        (s[1].equalsIgnoreCase("info")) || (s[1].equalsIgnoreCase("view")))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 1) || (s.length > offset + 2))
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length == offset + 2) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.infoCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ info <name> [page]";
    }
  }
  
  private class ViewmasteryCommand
    extends CommandHandler.BOSCommand
  {
    public ViewmasteryCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.viewmastery");
      this.helpText = "Lists brackets that a player or bank is the master of.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("viewmastery"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length > offset + 2) || (s.length < offset + 1))
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length == offset + 2) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.viewmasteryCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ viewmastery <name> [page]";
    }
  }
  
  private class SetCommand
    extends CommandHandler.BOSCommand
  {
    public SetCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.set");
      this.helpText = "Sets a player or bank's money.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("set"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        try
        {
          value = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(offset + 1)]));
        }
        catch (Exception ex)
        {
          double value;
          sendUsageMessage(sender);
          return true;
        }
        double value;
        String name = s[offset];
        
        CommandHandler.this.methods.setCommand(sender, name, value);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ set <name> <amount>";
    }
  }
  
  private class AddCommand
    extends CommandHandler.BOSCommand
  {
    public AddCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.add");
      this.helpText = "Adds to a player or bank's money.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("add"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        try
        {
          value = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(offset + 1)]));
        }
        catch (Exception ex)
        {
          double value;
          sendUsageMessage(sender);
          return true;
        }
        double value;
        String name = s[offset];
        
        CommandHandler.this.methods.addCommand(sender, name, value);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ add <name> <amount>";
    }
  }
  
  private class SubCommand
    extends CommandHandler.BOSCommand
  {
    public SubCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.sub");
      this.helpText = "Subtracts from a player or bank's money.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (
        (s[1].equalsIgnoreCase("sub")) || 
        (s[1].equalsIgnoreCase("subtract")))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        try
        {
          value = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(offset + 1)]));
        }
        catch (Exception ex)
        {
          double value;
          sendUsageMessage(sender);
          return true;
        }
        double value;
        String name = s[offset];
        
        CommandHandler.this.methods.subCommand(sender, name, value);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ sub <name> <amount>";
    }
  }
  
  private class ClearCommand
    extends CommandHandler.BOSCommand
  {
    public ClearCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.clear");
      this.helpText = "Sets a player or bank's money to zero.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("clear"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        String name = s[offset];
        
        CommandHandler.this.methods.clearCommand(sender, name);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ clear <name>";
    }
  }
  
  private class ScaleCommand
    extends CommandHandler.BOSCommand
  {
    public ScaleCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.money.scale");
      this.helpText = "Scales all of the money in the economy.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("scale"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.scaleCommand(sender, s[offset]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ scale <multiplier>";
    }
  }
  
  private class ReloadCommand
    extends CommandHandler.BOSCommand
  {
    public ReloadCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.reload");
      this.helpText = "Reloads BOSEconomy data.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (
        (s[1].equalsIgnoreCase("reload")) || 
        (s[1].equalsIgnoreCase("load")) || 
        (s[1].equalsIgnoreCase("refresh")))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        CommandHandler.this.methods.reloadCommand(sender, s, offset);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ reload [category1] ...";
    }
  }
  
  private class SaveCommand
    extends CommandHandler.BOSCommand
  {
    public SaveCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.save");
      this.helpText = "Immediately saves BOSEconomy data.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("save"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        CommandHandler.this.methods.saveCommand(sender, s, offset);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ save [category1] ...";
    }
  }
  
  private class StatsCommand
    extends CommandHandler.BOSCommand
  {
    public StatsCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.stats");
      this.helpText = "Displays some server statistics.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("stats"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        CommandHandler.this.methods.statsCommand(sender);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ stats";
    }
  }
  
  private class Top5Command
    extends CommandHandler.BOSCommand
  {
    public Top5Command()
    {
      super();
      this.permissions.add("BOSEconomy.common.top5");
      this.helpText = "Displays the top 5 wealthiest players.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (s[1].equalsIgnoreCase("top5"))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        CommandHandler.this.methods.top5Command(sender);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ top5";
    }
  }
  
  private class BracketCreateCommand
    extends CommandHandler.BOSCommand
  {
    public BracketCreateCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bracket.create");
      this.helpText = "Creates a new bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("create"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 3) || (s.length > offset + 4))
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketCreateCommand(sender, s[offset], s[(offset + 1)], 
          s[(offset + 2)], s.length == offset + 4 ? s[(offset + 3)] : null);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket create <bracket> <value> <type> [master]";
    }
  }
  
  private class BracketRemoveCommand
    extends CommandHandler.BOSCommand
  {
    public BracketRemoveCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bracket.remove");
      this.helpText = "Removes a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBracketAlias(s[1])) && (
        (s[2].equalsIgnoreCase("remove")) || (s[2].equalsIgnoreCase("rem")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketRemoveCommand(sender, s[offset]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bracket remove <bracket>";
    }
  }
  
  private class BracketRenameCommand
    extends CommandHandler.BOSCommand
  {
    public BracketRenameCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.rename");
      this.permissions.add("BOSEconomy.admin.bracket.rename");
      this.helpText = "Renames a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("rename"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketRenameCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket rename <bracket> <name>";
    }
  }
  
  private class BracketListCommand
    extends CommandHandler.BOSCommand
  {
    public BracketListCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bracket.list");
      this.helpText = "Lists all of the brackets.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("list"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length > offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length >= offset + 1) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.bracketListCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bracket list [page]";
    }
  }
  
  private class BracketInfoCommand
    extends CommandHandler.BOSCommand
  {
    public BracketInfoCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.info");
      this.permissions.add("BOSEconomy.admin.bracket.info");
      this.helpText = "Displays information about a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("info"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketInfoCommand(sender, s[offset]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bracket info <bracket>";
    }
  }
  
  private class BracketSetCommand
    extends CommandHandler.BOSCommand
  {
    public BracketSetCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.set");
      this.permissions.add("BOSEconomy.admin.bracket.set");
      this.helpText = "Changes a bracket's settings.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("set"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length < offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketSetCommand(sender, s[offset], s[(offset + 1)], s, 
          offset + 2);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket set <bracket> <setting> [value]";
    }
  }
  
  private class BracketSetmasterCommand
    extends CommandHandler.BOSCommand
  {
    public BracketSetmasterCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.set");
      this.permissions.add("BOSEconomy.admin.bracket.set");
      this.helpText = "Sets or removes a bracket's master.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("setmaster"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 1) || (s.length > offset + 2))
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketSetmasterCommand(sender, s[offset], 
          s.length == offset + 2 ? s[(offset + 1)] : null);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket setmaster <bracket> [master]";
    }
  }
  
  private class BracketRemovemasterCommand
    extends CommandHandler.BOSCommand
  {
    public BracketRemovemasterCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.set");
      this.permissions.add("BOSEconomy.admin.bracket.set");
      this.helpText = "Removes a bracket's master.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("removemaster"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketRemovemasterCommand(sender, s[offset]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket removemaster <bracket>";
    }
  }
  
  private class BracketListmembersCommand
    extends CommandHandler.BOSCommand
  {
    public BracketListmembersCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.listmembers");
      this.permissions.add("BOSEconomy.admin.bracket.listmembers");
      this.helpText = "Lists the members of a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBracketAlias(s[1])) && (
        (s[2].equalsIgnoreCase("members")) || 
        (s[2].equalsIgnoreCase("listm")) || 
        (s[2].equalsIgnoreCase("listmembers")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length <= offset) || (s.length > offset + 2))
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length == offset + 2) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.bracketListmembersCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket listmembers <bracket> [page]";
    }
  }
  
  private class BracketAddmemberCommand
    extends CommandHandler.BOSCommand
  {
    public BracketAddmemberCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.addmember");
      this.permissions.add("BOSEconomy.admin.bracket.addmember");
      this.helpText = "Adds a member to a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBracketAlias(s[1])) && (
        (s[2].equalsIgnoreCase("addm")) || 
        (s[2].equalsIgnoreCase("addmember")) || 
        (s[2].equalsIgnoreCase("addmembers")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketAddmemberCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket addmember <bracket> <member>";
    }
  }
  
  private class BracketRemovememberCommand
    extends CommandHandler.BOSCommand
  {
    public BracketRemovememberCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.removemember");
      this.permissions.add("BOSEconomy.admin.bracket.removemember");
      this.helpText = "Removes a member from a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBracketAlias(s[1])) && (
        (s[2].equalsIgnoreCase("removemember")) || 
        (s[2].equalsIgnoreCase("removemembers")) || 
        (s[2].equalsIgnoreCase("removem")) || 
        (s[2].equalsIgnoreCase("remm")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketRemovememberCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket removemember <bracket> <member>";
    }
  }
  
  private class BracketPlayerJoinCommand
    extends CommandHandler.BOSCommand
  {
    public BracketPlayerJoinCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.addmember");
      this.permissions.add("BOSEconomy.admin.bracket.addmember");
      this.helpText = "Adds you to a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("join"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketAddmemberCommand(sender, s[offset], "@me");
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bracket join <bracket>";
    }
  }
  
  private class BracketPlayerLeaveCommand
    extends CommandHandler.BOSCommand
  {
    public BracketPlayerLeaveCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.removemember");
      this.permissions.add("BOSEconomy.admin.bracket.removemember");
      this.helpText = "Removes you from a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("leave"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketRemovememberCommand(sender, s[offset], "@me");
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bracket leave <bracket>";
    }
  }
  
  private class BracketSetmultiplierCommand
    extends CommandHandler.BOSCommand
  {
    public BracketSetmultiplierCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bracket.setmultiplier");
      this.permissions.add("BOSEconomy.admin.bracket.setmultiplier");
      this.helpText = "Sets a member's multiplier in a bracket.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBracketAlias(s[1])) && (
        (s[2].equalsIgnoreCase("setmult")) || 
        (s[2].equalsIgnoreCase("setmultiplier")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 3)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketSetmultiplierCommand(sender, s[offset], s[(offset + 1)], 
          s[(offset + 2)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket setmultiplier <bracket> <member> <multiplier>";
    }
  }
  
  private class BracketClearCommand
    extends CommandHandler.BOSCommand
  {
    public BracketClearCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bracket.clear");
      this.helpText = "Removes an account from its brackets.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBracketAlias(s[1])) && 
        (s[2].equalsIgnoreCase("clear"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length < offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bracketClearCommand(sender, s[offset], s, offset + 1);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bracket clear <account> [filter1] ...";
    }
  }
  
  private class BankCreateCommand
    extends CommandHandler.BOSCommand
  {
    public BankCreateCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bank.create");
      this.helpText = "Creates a new bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBankAlias(s[1])) && 
        (s[2].equalsIgnoreCase("create"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 1) || (s.length > offset + 3))
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankCreateCommand(sender, s[offset], s.length >= offset + 2 ? 
          s[(offset + 1)] : "", 
          s.length == offset + 3 ? s[(offset + 2)] : "");
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank create <bank> [owners] [members]";
    }
  }
  
  private class BankRemoveCommand
    extends CommandHandler.BOSCommand
  {
    public BankRemoveCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.bank.remove");
      this.helpText = "Removes a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("remove")) || (s[2].equalsIgnoreCase("rem")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankRemoveCommand(sender, s[offset]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bank remove <bank>";
    }
  }
  
  private class BankRenameCommand
    extends CommandHandler.BOSCommand
  {
    public BankRenameCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.rename");
      this.permissions.add("BOSEconomy.admin.bank.rename");
      this.helpText = "Renames a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBankAlias(s[1])) && 
        (s[2].equalsIgnoreCase("rename"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankRenameCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bank rename <bank> <name>";
    }
  }
  
  private class BankListCommand
    extends CommandHandler.BOSCommand
  {
    public BankListCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.list");
      this.permissions.add("BOSEconomy.admin.bank.list");
      this.helpText = "Lists all bank accounts.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBankAlias(s[1])) && 
        (s[2].equalsIgnoreCase("list"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        String pageArg = "1";
        if (s.length >= offset + 1) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.bankListCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bank list [page]";
    }
  }
  
  private class BankInfoCommand
    extends CommandHandler.BOSCommand
  {
    public BankInfoCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.info");
      this.permissions.add("BOSEconomy.admin.bank.info");
      this.helpText = "Displays information about a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("info")) || (s[2].equalsIgnoreCase("view")) || 
        (s[2].equalsIgnoreCase("income")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 1) || (s.length > offset + 2))
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length == 5) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.bankInfoCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ bank info <bank> [page]";
    }
  }
  
  private class BankMasteryCommand
    extends CommandHandler.BOSCommand
  {
    public BankMasteryCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.mastery");
      this.permissions.add("BOSEconomy.admin.bank.mastery");
      this.helpText = "Lists brackets that a bank is the master of.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBankAlias(s[1])) && 
        (s[2].equalsIgnoreCase("mastery"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if ((s.length < offset + 1) || (s.length > offset + 2))
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length == 5) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.bankMasteryCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank mastery <bank> [page]";
    }
  }
  
  private class BankListmembersCommand
    extends CommandHandler.BOSCommand
  {
    public BankListmembersCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.listmembers");
      this.permissions.add("BOSEconomy.admin.bank.listmembers");
      this.helpText = "Lists the members of a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("members")) || 
        (s[2].equalsIgnoreCase("owners")) || 
        (s[2].equalsIgnoreCase("listmembers")) || 
        (s[2].equalsIgnoreCase("listm")) || 
        (s[2].equalsIgnoreCase("listowners")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length <= offset)
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length >= offset + 2) {
          pageArg = s[(offset + 1)];
        }
        CommandHandler.this.methods.bankListmembersCommand(sender, s[offset], pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank listmembers <bank> [page]";
    }
  }
  
  private class BankWithdrawCommand
    extends CommandHandler.BOSCommand
  {
    public BankWithdrawCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.withdraw");
      this.permissions.add("BOSEconomy.admin.bank.withdraw");
      this.helpText = "Withdraws money from a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("withdraw")) || 
        (s[2].equalsIgnoreCase("take")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        double value = 0.0D;
        try
        {
          value = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(offset + 1)]));
        }
        catch (Exception ex)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankWithdrawCommand(sender, s[offset], value);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank withdraw <bank> <amount>";
    }
  }
  
  private class BankDepositCommand
    extends CommandHandler.BOSCommand
  {
    public BankDepositCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.deposit");
      this.permissions.add("BOSEconomy.admin.bank.deposit");
      this.helpText = "Deposits money into a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("deposit")) || 
        (s[2].equalsIgnoreCase("pay")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        try
        {
          money = CommandHandler.this.plugin.getMoneyRounded(Double.parseDouble(s[(offset + 1)]));
        }
        catch (Exception ex)
        {
          double money;
          sendUsageMessage(sender);
          return true;
        }
        double money;
        CommandHandler.this.methods.bankDepositCommand(sender, s[offset], money);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank deposit <bank> <amount>";
    }
  }
  
  private class BankAddownerCommand
    extends CommandHandler.BOSCommand
  {
    public BankAddownerCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.addowner");
      this.permissions.add("BOSEconomy.admin.bank.addowner");
      this.helpText = "Adds an owner to a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("addo")) || 
        (s[2].equalsIgnoreCase("addowner")) || 
        (s[2].equalsIgnoreCase("addowners")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankAddownerCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank addowner <bank> <player>";
    }
  }
  
  private class BankAddmemberCommand
    extends CommandHandler.BOSCommand
  {
    public BankAddmemberCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.addmember");
      this.permissions.add("BOSEconomy.admin.bank.addmember");
      this.helpText = "Adds a member to a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && 
        (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("addm")) || 
        (s[2].equalsIgnoreCase("addmember")) || 
        (s[2].equalsIgnoreCase("addmembers")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankAddmemberCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank addmember <bank> <player>";
    }
  }
  
  private class BankRemovememberCommand
    extends CommandHandler.BOSCommand
  {
    public BankRemovememberCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.bank.removemember");
      this.permissions.add("BOSEconomy.admin.bank.removemember");
      this.helpText = "Removes a member or owner from a bank account.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isBankAlias(s[1])) && (
        (s[2].equalsIgnoreCase("removemember")) || 
        (s[2].equalsIgnoreCase("removeowner")) || 
        (s[2].equalsIgnoreCase("removem")) || 
        (s[2].equalsIgnoreCase("removeo")) || 
        (s[2].equalsIgnoreCase("removemembers")) || 
        (s[2].equalsIgnoreCase("removeowners")) || 
        (s[2].equalsIgnoreCase("remm")) || 
        (s[2].equalsIgnoreCase("remo")))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.bankRemovememberCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ bank removemember <bank> <player>";
    }
  }
  
  private class RequestListCommand
    extends CommandHandler.BOSCommand
  {
    public RequestListCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.request.list");
      

      this.helpText = "Lists all of your requests.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isRequestAlias(s[1])) && 
        (s[2].equalsIgnoreCase("list"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length > offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        String pageArg = "1";
        if (s.length >= offset + 1) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.requestListCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ request list [page]";
    }
  }
  
  private class RequestDoCommand
    extends CommandHandler.BOSCommand
  {
    public RequestDoCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.request.do");
      this.helpText = "Performs an action on a request.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isRequestAlias(s[1])) && 
        (s[2].equalsIgnoreCase("do"))) {
        return 3;
      }
      if ((s.length >= 3) && (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isRequestAlias(s[1]))) {
        try
        {
          Integer.parseInt(s[2]);
          
          return 2;
        }
        catch (NumberFormatException localNumberFormatException) {}
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length != offset + 2)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.requestDoCommand(sender, s[offset], s[(offset + 1)]);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = 
        "/econ request do <request> <action>";
    }
  }
  
  private class PaydayResetCommand
    extends CommandHandler.BOSCommand
  {
    public PaydayResetCommand()
    {
      super();
      this.permissions.add("BOSEconomy.admin.payday.reset");
      this.helpText = "Resets the payday timer.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 3) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (CommandHandler.isPaydayAlias(s[1])) && 
        (s[2].equalsIgnoreCase("reset"))) {
        return 3;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length > offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.paydayResetCommand(sender);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ payday reset";
    }
  }
  
  private class AboutCommand
    extends CommandHandler.BOSCommand
  {
    public AboutCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.about");
      this.helpText = "Displays information about the plugin.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0])) && (
        (s[1].equalsIgnoreCase("about")) || (s[1].equalsIgnoreCase("version")))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        if (s.length > offset + 1)
        {
          sendUsageMessage(sender);
          return true;
        }
        CommandHandler.this.methods.aboutCommand(sender);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ about";
    }
  }
  
  private class HelpCommand
    extends CommandHandler.BOSCommand
  {
    public HelpCommand()
    {
      super();
      this.permissions.add("BOSEconomy.common.help");
      this.helpText = "Lists the plugin commands and briefly describes them.";
    }
    
    public int commandMatch(String[] s)
    {
      if ((s.length >= 2) && 
        (CommandHandler.this.isCommandLabel(s[0]))) {
        return 2;
      }
      return -1;
    }
    
    public boolean doCommand(CommandHandler.BOSCommandSender sender, String[] s, int offset, boolean checkPermissions)
    {
      if ((!checkPermissions) || (canUse(sender)))
      {
        String pageArg = "1";
        if ((s.length == 3) && (s[1].equalsIgnoreCase("help"))) {
          pageArg = s[offset];
        }
        CommandHandler.this.methods.helpCommand(sender, pageArg);
        return true;
      }
      return false;
    }
    
    public void updateUsageText()
    {
      this.usageText = "/econ help [page]";
    }
  }
  
  public class BOSCommandSender
  {
    private final CommandSender sender;
    
    public BOSCommandSender(CommandSender sender)
    {
      this.sender = sender;
    }
    
    public void sendMsgInfo(String message)
    {
      sendMessage(false, null, null, BOSEconomy.TAG_BLANK_COLOR, message);
    }
    
    public void sendMsgCopy(String message)
    {
      sendMessage(true, BOSEconomy.TAG_BLANK_COLOR, null, BOSEconomy.TAG_BLANK_COLOR, message);
    }
    
    public void sendMessage(boolean copyPlayer, String copyPlayerTag, String consoleTag, String copyBlockTag, String message)
    {
      if (isPlayer())
      {
        this.sender.sendMessage(message);
        if (copyPlayer) {
          CommandHandler.this.plugin.sendConsoleMessage((copyPlayerTag == null ? "" : copyPlayerTag) + 
            "<" + this.sender.getName() + "> " + message);
        }
      }
      else if (isConsole())
      {
        CommandHandler.this.plugin.sendConsoleMessage((consoleTag == null ? "" : consoleTag) + message);
      }
      else if ((isBlock()) && (CommandHandler.this.getPlugin().getSettingsManager().getShowCommandBlockOutput()))
      {
        CommandHandler.this.plugin.sendConsoleMessage((copyBlockTag == null ? "" : copyBlockTag) + 
          "<" + getBlockName(true) + "> " + message);
      }
    }
    
    public String getBlockName(boolean caps)
    {
      if (isBlock())
      {
        BlockState state = getAsBlock().getBlock().getState();
        if ((state instanceof CommandBlock))
        {
          if (((CommandBlock)state).getName().equals("@"))
          {
            if (caps) {
              return "Block (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
            }
            return "block (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
          }
          if (caps) {
            return 
              "Block '" + ((CommandBlock)state).getName() + "' (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
          }
          return 
            "block '" + ((CommandBlock)state).getName() + "' (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
        }
        if (caps) {
          return "Block (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
        }
        return "block (" + state.getX() + "," + state.getY() + "," + state.getZ() + ")";
      }
      return "";
    }
    
    public boolean isPlayer()
    {
      return this.sender instanceof Player;
    }
    
    public boolean isConsole()
    {
      return this.sender instanceof ConsoleCommandSender;
    }
    
    public boolean isBlock()
    {
      return this.sender instanceof BlockCommandSender;
    }
    
    public boolean isConsoleOrBlock()
    {
      return ((this.sender instanceof ConsoleCommandSender)) || ((this.sender instanceof BlockCommandSender));
    }
    
    public Player getAsPlayer()
    {
      return (Player)this.sender;
    }
    
    public ConsoleCommandSender getAsConsole()
    {
      return (ConsoleCommandSender)this.sender;
    }
    
    public BlockCommandSender getAsBlock()
    {
      return (BlockCommandSender)this.sender;
    }
    
    public CommandSender getSender()
    {
      return this.sender;
    }
  }
}

package cosine.boseconomy;

import cosine.boseconomy.bracket.setting.BracketMasterSetting;
import cosine.boseconomy.bracket.setting.BracketMaximumMultiplierSetting;
import cosine.boseconomy.bracket.setting.BracketMinimumMultiplierSetting;
import cosine.boseconomy.bracket.setting.BracketSetting;
import cosine.boseconomy.bracket.setting.BracketValueSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

class CommandMethods
{
  public final int COMMAND_ERROR_LIMIT = 8;
  private final CommandHandler handler;
  
  public CommandMethods(CommandHandler handler)
  {
    this.handler = handler;
  }
  
  public void sendListMessage(CommandHandler.BOSCommandSender sender, Object[] list, int size, String pageArg, String title, int perPage, boolean numbered)
  {
    int page = 1;
    try
    {
      page = Integer.parseInt(pageArg);
      if (page < 1) {
        page = 1;
      }
    }
    catch (Exception ex)
    {
      page = 1;
    }
    int maxPage = size == 0 ? 1 : (int)Math.ceil(1.0D * size / perPage);
    if (page > maxPage) {
      page = maxPage;
    }
    sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - " + title + ":");
    sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "  Page: " + ChatColor.GREEN + page + "/" + 
      maxPage);
    for (int i = (page - 1) * perPage; i < Math.min(size, page * perPage); i++) {
      if (numbered)
      {
        String padding = "";
        if (i < 9) {
          padding = "  ";
        } else if (i < 99) {
          padding = " ";
        }
        sender.sendMsgInfo(i + 1 + "." + padding + list[i]);
      }
      else
      {
        sender.sendMsgInfo(list[i].toString());
      }
    }
  }
  
  public MoneyFormatter getMoneyFormatter()
  {
    return this.handler.getPlugin().getMoneyFormatter();
  }
  
  public String formatMoney(double value)
  {
    return getMoneyFormatter().formatMoney(value);
  }
  
  public String getMoneyNameProper(double value)
  {
    return this.handler.getPlugin().getMoneyNameProper(value);
  }
  
  public boolean isBadDouble(CommandHandler.BOSCommandSender sender, double value)
  {
    String validity = Money.checkValidity(value);
    if (validity == null) {
      return false;
    }
    sender.sendMsgInfo(BOSEconomy.BAD_COLOR + validity);
    return true;
  }
  
  private Bracket getBracketOrBust(CommandHandler.BOSCommandSender sender, String bracketName)
  {
    Bracket bracket = 
      this.handler.getPlugin().getBracketManager().getBracket(bracketName);
    if (bracket == null) {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "No bracket exists by the name of '" + bracketName + "'.");
    }
    return bracket;
  }
  
  private BankAccount getBankAccountOrBust(CommandHandler.BOSCommandSender sender, String bankName)
  {
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(bankName);
    if (bank == null) {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "No bank account exists by the name of '" + bankName + "'.");
    }
    return bank;
  }
  
  private boolean isBankAccountName(String name)
  {
    return (name.length() > 0) && (name.charAt(0) == '$');
  }
  
  private boolean isUnfulfilling(Bracket bracket)
  {
    return ((bracket instanceof WageBracket)) && 
      (((WageBracket)bracket).isUnfulfilling());
  }
  
  public void walletCommand(CommandHandler.BOSCommandSender sender)
  {
    if (sender.isBlock()) {
      return;
    }
    if (sender.isPlayer())
    {
      Account account = 
        this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
        sender.getAsPlayer().getName());
      if (account != null) {
        account.tellMoney();
      }
    }
    else if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "Your money: Infinite");
    }
  }
  
  public void incomeCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The server does not have an income.");
      return;
    }
    if (sender.isPlayer())
    {
      Account account = this.handler.getPlugin().getAccountManager()
        .getPlayerAccountByName(sender.getAsPlayer().getName());
      if (account == null) {
        account = this.handler.getPlugin().getAccountManager().createNoobAccount(
          sender.getAsPlayer().getName());
      }
      incomeCommandFeedback(sender, account, pageArg, 7, "Your Income Brackets");
    }
  }
  
  private void incomeCommandFeedback(CommandHandler.BOSCommandSender sender, Account account, String pageArg, int pageSize, String listTitle)
  {
    if (sender.isBlock()) {
      return;
    }
    List<Bracket> bracketList = account.getBracketList(false);
    Collections.sort(bracketList, new Bracket.NameComparator());
    int listIndex;
    if (sender.isPlayer())
    {
      String[] list = new String[account.getBracketCount(false)];
      listIndex = 0;
      for (Bracket b : bracketList)
      {
        boolean isUnfulfilling = isUnfulfilling(b);
        double multiplier = b.getMember(account).getCount();
        list[listIndex] = 
        



          ((isUnfulfilling ? BOSEconomy.BAD_COLOR : ChatColor.WHITE) + "  " + b.getName() + " (" + b.getTypeCaps() + (isUnfulfilling(b) ? ", unfulfilling" : "") + "): " + BOSEconomy.MONEY_COLOR + formatMoney(b.getValue()) + ChatColor.WHITE + " / " + b.getPaymentInterval().toString() + (multiplier != 1.0D ? " (x" + multiplier + ")" : ""));
        listIndex++;
      }
      sendListMessage(sender, list, list.length, pageArg, listTitle, pageSize, 
        false);
    }
    else if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - " + listTitle);
      for (Bracket b : bracketList)
      {
        boolean isUnfulfilling = isUnfulfilling(b);
        double multiplier = b.getMember(account).getCount();
        sender.sendMsgInfo((isUnfulfilling ? BOSEconomy.BAD_COLOR : ChatColor.WHITE) + 
          "  " + b.getName() + " (" + b.getTypeCaps() + (
          isUnfulfilling ? ", unfulfilling" : "") + "): " + 
          BOSEconomy.MONEY_COLOR + formatMoney(b.getValue()) + 
          ChatColor.WHITE + " / " + b.getPaymentInterval().toString() + (
          multiplier != 1.0D ? " (x" + multiplier + ")" : ""));
      }
    }
    double income = account.getTotalIncome();
    TimeInterval paydayInterval = 
      this.handler.getPlugin().getSettingsManager().getPaydayInterval();
    if (paydayInterval.getSeconds() == 0)
    {
      income *= 3600.0D;
      String incomeColor = BOSEconomy.NEUTRAL_COLOR;
      if (income > 0.0D) {
        incomeColor = BOSEconomy.GOOD_COLOR;
      } else if (income < 0.0D) {
        incomeColor = BOSEconomy.BAD_COLOR;
      }
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "Total income: " + 
        getMoneyFormatter().generateString(income, BOSEconomy.MONEY_COLOR, incomeColor) + 
        " / 1 hour");
    }
    else
    {
      income *= paydayInterval.getSeconds();
      String incomeColor = BOSEconomy.NEUTRAL_COLOR;
      if (income > 0.0D) {
        incomeColor = BOSEconomy.GOOD_COLOR;
      } else if (income < 0.0D) {
        incomeColor = BOSEconomy.BAD_COLOR;
      }
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "Total income: " + 
        getMoneyFormatter().generateString(income, BOSEconomy.MONEY_COLOR, incomeColor) + 
        " / " + paydayInterval.toString());
    }
  }
  
  public void masteryCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The server is not the master of any brackets.");
      return;
    }
    if (sender.isPlayer())
    {
      Account account = 
        this.handler.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
      if (account != null) {
        masteryCommandFeedback(sender, account, pageArg, "Your Bracket Mastery");
      }
    }
  }
  
  private void masteryCommandFeedback(CommandHandler.BOSCommandSender sender, Account account, String pageArg, String listTitle)
  {
    if (sender.isBlock()) {
      return;
    }
    List<Bracket> bracketList = account.getBracketList(true);
    Collections.sort(bracketList, new Bracket.NameComparator());
    int listIndex;
    if (sender.isPlayer())
    {
      String[] list = new String[account.getBracketCount(true)];
      listIndex = 0;
      for (Bracket b : bracketList)
      {
        list[listIndex] = ("  " + b.getName() + " (" + b.getTypeCaps() + ")");
        listIndex++;
      }
      sendListMessage(sender, list, list.length, pageArg, listTitle, 8, false);
    }
    else if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - " + listTitle);
      for (Bracket b : bracketList) {
        sender.sendMsgInfo("  " + b.getName() + " (" + b.getTypeCaps() + ")");
      }
    }
  }
  
  public void payCommand(CommandHandler.BOSCommandSender sender, String name2, double value)
  {
    if (sender.isConsoleOrBlock())
    {
      addCommand(sender, name2, value);
      return;
    }
    boolean canPayPlayers = 
      this.handler.hasPermission(sender, "BOSEconomy.common.pay");
    if (!this.handler.hasPermission(sender, "BOSEconomy.common.bank.deposit")) {}
    boolean canPayBanks = 
    
      this.handler.hasPermission(sender, 
      "BOSEconomy.admin.bank.deposit");
    if (value <= 0.0D)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Transactions must use values greater than zero.");
      return;
    }
    if (Double.isInfinite(value))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You can't afford to pay an infinite amount of " + 
        this.handler.getPlugin().getSettingsManager().getMoneyNamePlural() + ".");
      return;
    }
    if (Double.isNaN(value))
    {
      int r = (int)(Math.random() * 7.0D);
      if (r == 0) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Stop it.");
      } else if (r == 1) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "I think not.");
      } else if (r == 2) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "I'm sorry, " + (
          sender.isPlayer() ? sender.getAsPlayer().getName() : 
          "server") + ". I'm afraid I can't do that.");
      } else if (r == 3) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "I can't let you do that, " + (
          sender.isPlayer() ? sender.getAsPlayer().getName() : "server") + ".");
      } else if (r == 4) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "No.");
      } else if (r == 5) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Not happening.");
      } else {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Don't do that.");
      }
      return;
    }
    PlayerAccount a1 = 
      this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
      sender.getAsPlayer().getName());
    if (a1.denyPayCommand())
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Please wait a few seconds between payments.");
      return;
    }
    if (value > a1.getMoney())
    {
      if (this.handler.getPlugin().getSettingsManager().getMoneyName().equalsIgnoreCase("pylon"))
      {
        int r = (int)(Math.random() * 4.0D);
        if (r == 0) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "You must construct additional pylons.");
        } else if (r == 1) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "You have not enough pylons.");
        } else if (r == 2) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You need more pylons.");
        } else {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Not enough pylons.");
        }
      }
      else
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You can't afford to pay " + 
          getMoneyFormatter().generateString(
          value, BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + ".");
      }
      a1.tellMoney();
      return;
    }
    boolean isBank = isBankAccountName(name2);
    if (isBank) {
      name2 = name2.substring(1);
    }
    Account a2;
    if (isBank)
    {
      Account a2 = this.handler.getPlugin().getAccountManager().getBankAccountByName(name2);
      if (a2 == null) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Could not find a bank account named '" + name2 + "'.");
      }
    }
    else
    {
      List<Player> matches = this.handler.getPlugin().getServer().matchPlayer(name2);
      if ((this.handler.getPlugin().getUseVanishPlugin()) && (!this.handler.getPlugin().canSeeVanish(sender)))
      {
        Iterator<Player> matchIterator = matches.iterator();
        while (matchIterator.hasNext()) {
          if (this.handler.getPlugin().getPlayerVanished(((Player)matchIterator.next()).getName())) {
            matchIterator.remove();
          }
        }
      }
      if (matches.size() == 0)
      {
        if (this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
          name2) == null) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "Could not find any players matching '" + name2 + "'.");
        } else {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Could not pay " + name2 + 
            ": player is offline.");
        }
        return;
      }
      if (matches.size() > 1)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Found multiple players matching '" + name2 + "'.");
        return;
      }
      a2 = 
        this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
        ((Player)matches.get(0)).getName());
      if (a2 == null)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Could not find a player named '" + name2 + "'.");
        return;
      }
    }
    if ((!canPayPlayers) && ((a2 instanceof PlayerAccount)))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You are not allowed to pay other players.");
      return;
    }
    if ((!canPayBanks) && ((a2 instanceof BankAccount)))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You are not allowed to deposit into bank accounts.");
      return;
    }
    if (a1 == a2)
    {
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "You paid yourself " + 
        getMoneyFormatter().generateString(value, 
        BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
      a1.tellMoney();
      return;
    }
    if ((a2 instanceof BankAccount))
    {
      a1.sentPayCommand();
      a1.addMoney(-value);
      a2.addMoney(value);
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "You paid " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " to bank account '" + a2.getName() + "'.");
      a1.tellMoney();
      ((BankAccount)a2).notifyPayment(value, a1);
    }
    else if ((a2 instanceof PlayerAccount))
    {
      if (((PlayerAccount)a2).getPlayer() == null)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Player " + a2.getName() + " is not online.");
        return;
      }
      a1.sentPayCommand();
      a1.addMoney(-value);
      a2.addMoney(value);
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "You paid " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " to player '" + a2.getName() + "'.");
      a1.tellMoney();
      a2.sendMessage(BOSEconomy.GOOD_COLOR + "You received " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " from player " + a1.getName() + ".");
      a2.tellMoney();
    }
  }
  
  public void infoCommand(CommandHandler.BOSCommandSender sender, String name, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    Account account = 
      this.handler.getPlugin().getAccountManager().getAccountByName(name);
    if (account == null)
    {
      if ((name.length() > 0) && (name.charAt(0) == '$')) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Could not find bank account '" + name.substring(1) + "'.");
      } else {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Could not find account '" + 
          name + "'.");
      }
      return;
    }
    if (((account instanceof PlayerAccount)) && 
      (!this.handler.hasPermission(sender, "BOSEconomy.admin.money.info")))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You aren't allowed to view player information.");
      return;
    }
    if (((account instanceof BankAccount)) && 
      (!this.handler.hasPermission(sender, "BOSEconomy.admin.bank.info")) && 
      (!this.handler.hasPermission(sender, "BOSEconomy.common.bank.info")))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You aren't allowed to view bank account information.");
      return;
    }
    incomeCommandFeedback(sender, account, pageArg, 6, account.getName() + (
      (account instanceof PlayerAccount) ? "'s" : "") + " Income Brackets");
    if ((account instanceof PlayerAccount)) {
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + account.getName() + 
        " has: " + account.getMoneyObject().toString(true));
    } else if ((account instanceof BankAccount)) {
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + account.getName() + 
        " account balance: " + account.getMoneyObject().toString(true));
    }
  }
  
  public void viewmasteryCommand(CommandHandler.BOSCommandSender sender, String name, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    Account account = 
      this.handler.getPlugin().getAccountManager().getAccountByName(name);
    if (account == null)
    {
      if ((name.length() > 0) && (name.charAt(0) == '$')) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Could not find bank account '" + name.substring(1) + "'.");
      } else {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Could not find account '" + name + "'.");
      }
      return;
    }
    if (((account instanceof PlayerAccount)) && 
      (!this.handler.hasPermission(sender, "BOSEconomy.admin.viewmastery")))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You aren't allowed to view player mastery.");
      return;
    }
    if (((account instanceof BankAccount)) && 
      (!this.handler.hasPermission(sender, "BOSEconomy.admin.bank.mastery")))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You aren't allowed to view bank account mastery.");
      return;
    }
    masteryCommandFeedback(sender, account, pageArg, account.getName() + (
      (account instanceof PlayerAccount) ? "'s" : "") + " Bracket Mastery");
  }
  
  public void setCommand(CommandHandler.BOSCommandSender sender, String name, double value)
  {
    if (isBadDouble(sender, value)) {
      return;
    }
    AccountGroup g = new AccountGroup(this.handler.getPlugin(), name, sender, 0);
    if (g.hadErrors()) {
      g.tellErrors(sender);
    }
    if (g.getSize() == 0) {
      return;
    }
    for (AccountGroup.LocalAccount la : g.getAccountList())
    {
      Account account = la.account;
      String textColor = null;
      if (value > account.getMoney())
      {
        textColor = BOSEconomy.GOOD_COLOR;
      }
      else
      {
        if (value >= account.getMoney()) {
          continue;
        }
        textColor = BOSEconomy.BAD_COLOR;
      }
      account.setMoney(value);
      if ((account instanceof PlayerAccount))
      {
        if (value != 0.0D) {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + textColor + 
            "Your money was set to " + 
            account.getMoneyObject().toString(BOSEconomy.MONEY_COLOR, textColor) + 
            " by an administrator.");
        } else {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + textColor + 
            "Your money was cleared by an administrator.");
        }
      }
      else if ((account instanceof BankAccount)) {
        if (value != 0.0D) {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + textColor + 
            "Bank account " + account.getName() + "'s balance was set to " + 
            account.getMoneyObject().toString(BOSEconomy.MONEY_COLOR, textColor) + 
            " by an administrator.");
        } else {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + textColor + 
            "Bank account " + account.getName() + "'s balance was cleared by an administrator.");
        }
      }
    }
    if (value != 0.0D) {
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + 
        "Set the money of " + g.getAccountString(false, true) + " to " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + ".");
    } else {
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Cleared the money of " + 
        g.getAccountString(false, true) + ".");
    }
  }
  
  public void addCommand(CommandHandler.BOSCommandSender sender, String name, double value)
  {
    if (isBadDouble(sender, value)) {
      return;
    }
    if (value == 0.0D)
    {
      sender.sendMsgInfo(BOSEconomy.NEUTRAL_COLOR + 
        "Money value was zero. No changes were made.");
      return;
    }
    AccountGroup g = new AccountGroup(this.handler.getPlugin(), name, sender, 0);
    if (g.hadErrors()) {
      g.tellErrors(sender);
    }
    if (g.getSize() == 0) {
      return;
    }
    for (AccountGroup.LocalAccount la : g.getAccountList())
    {
      Account account = la.account;
      account.addMoney(value);
      if ((account instanceof PlayerAccount))
      {
        if (value > 0.0D)
        {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.GOOD_COLOR + "You were given " + 
            getMoneyFormatter().generateString(
            value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
            " by an administrator.");
          account.tellMoney();
        }
        else
        {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.BAD_COLOR + "An administrator removed " + 
            getMoneyFormatter().generateString(
            -value, BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + " from you.");
          account.tellMoney();
        }
      }
      else if ((account instanceof BankAccount)) {
        if (value > 0.0D)
        {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.GOOD_COLOR + "Bank account " + account.getName() + 
            " was given " + getMoneyFormatter().generateString(
            value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + " by an administrator.");
          account.tellMoney();
        }
        else
        {
          account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.BAD_COLOR + "An administrator removed " + 
            getMoneyFormatter().generateString(
            -value, BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + 
            " from bank account " + account.getName() + ".");
          account.tellMoney();
        }
      }
    }
    if (value > 0.0D) {
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Added " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " to " + g.getAccountString(false, true) + ".");
    } else {
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed " + 
        getMoneyFormatter().generateString(
        -value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " from " + g.getAccountString(false, true) + ".");
    }
  }
  
  public void subCommand(CommandHandler.BOSCommandSender sender, String name, double value)
  {
    addCommand(sender, name, -value);
  }
  
  public void clearCommand(CommandHandler.BOSCommandSender sender, String name)
  {
    setCommand(sender, name, 0.0D);
  }
  
  public void scaleCommand(CommandHandler.BOSCommandSender sender, String multArg)
  {
    double mult = 0.0D;
    try
    {
      mult = Double.parseDouble(multArg);
    }
    catch (Exception ex)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "The multiplier should be a number.");
      return;
    }
    if (Double.isInfinite(mult))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Cannot scale the economy by infinity.");
      return;
    }
    if (Double.isNaN(mult))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Cannot scale the economy by NaN.");
      return;
    }
    Iterator<Account> accountIterator = 
      this.handler.getPlugin().getAccountManager().getAccountIterator();
    while (accountIterator.hasNext())
    {
      Account account = (Account)accountIterator.next();
      account.setMoney(account.getMoney() * mult);
    }
    Iterator<Bracket> bracketIterator = 
      this.handler.getPlugin().getBracketManager().getIterator();
    while (bracketIterator.hasNext())
    {
      Bracket bracket = (Bracket)bracketIterator.next();
      BracketValueSetting s = (BracketValueSetting)bracket.getSetting("value");
      if (s != null) {
        s.setValue(Double.valueOf(s.getObjectValue().doubleValue() * Math.abs(mult)));
      }
    }
    this.handler.getPlugin().getSettingsManager().setInitialMoney(
      this.handler.getPlugin().getSettingsManager().getInitialMoney() * mult);
    

    this.handler.getPlugin().getServer().broadcastMessage(
      BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
      "The economy has been scaled by " + mult + ".");
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Scaled the economy by " + mult + ".");
  }
  
  public void reloadCommand(CommandHandler.BOSCommandSender sender, String[] categories, int offset)
  {
    if (categories.length <= offset)
    {
      this.handler.getPlugin().getBOSEDatabase().refresh();
      this.handler.getPlugin().getBOSEDatabase().refreshManagers();
      
      this.handler.getPlugin().getAccountManager().loadOnlinePlayers();
    }
    else
    {
      boolean reloadSettings = false;
      boolean reloadAccounts = false;
      boolean reloadBrackets = false;
      for (int i = offset; i < categories.length; i++) {
        if (categories[i].equalsIgnoreCase("settings"))
        {
          reloadSettings = true;
        }
        else if (categories[i].equalsIgnoreCase("accounts"))
        {
          reloadAccounts = true;
        }
        else if (categories[i].equalsIgnoreCase("brackets"))
        {
          reloadBrackets = true;
        }
        else
        {
          if (categories[i].equalsIgnoreCase("all"))
          {
            reloadSettings = true;
            reloadAccounts = true;
            reloadBrackets = true;
            break;
          }
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Invalid data category '" + 
            categories[i] + "'.");
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "Valid choices are: settings, accounts, brackets, all");
          return;
        }
      }
      this.handler.getPlugin().getBOSEDatabase().refresh();
      if (reloadSettings) {
        this.handler.getPlugin().getSettingsManager().refresh(
          this.handler.getPlugin().getBOSEDatabase(), "settings");
      }
      if (reloadAccounts)
      {
        this.handler.getPlugin().getAccountManager().refresh(
          this.handler.getPlugin().getBOSEDatabase(), "accounts");
        
        this.handler.getPlugin().getAccountManager().loadOnlinePlayers();
      }
      if (reloadBrackets)
      {
        this.handler.getPlugin().getBracketManager().refresh(
          this.handler.getPlugin().getBOSEDatabase(), "brackets");
        this.handler.getPlugin().getBracketManager().getPaymentManager().refresh(
          this.handler.getPlugin().getBOSEDatabase(), "payments");
      }
    }
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Plugin data reloaded.");
  }
  
  public void saveCommand(CommandHandler.BOSCommandSender sender, String[] categories, int offset)
  {
    if (categories.length <= offset)
    {
      this.handler.getPlugin().getBOSEDatabase().setManagersChanged(true);
    }
    else
    {
      boolean saveSettings = false;
      boolean saveAccounts = false;
      boolean saveBrackets = false;
      for (int i = offset; i < categories.length; i++) {
        if (categories[i].equalsIgnoreCase("settings"))
        {
          saveSettings = true;
        }
        else if (categories[i].equalsIgnoreCase("accounts"))
        {
          saveAccounts = true;
        }
        else if (categories[i].equalsIgnoreCase("brackets"))
        {
          saveBrackets = true;
        }
        else
        {
          if (categories[i].equalsIgnoreCase("all"))
          {
            saveSettings = true;
            saveAccounts = true;
            saveBrackets = true;
            break;
          }
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Invalid data category '" + 
            categories[i] + "'.");
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "Valid choices are: settings, accounts, brackets, all");
          return;
        }
      }
      if (saveSettings) {
        this.handler.getPlugin().getSettingsManager().setChanged();
      }
      if (saveAccounts) {
        this.handler.getPlugin().getAccountManager().setChanged();
      }
      if (saveBrackets)
      {
        this.handler.getPlugin().getBracketManager().setChanged();
        this.handler.getPlugin().getBracketManager().getPaymentManager()
          .setChanged();
      }
    }
    this.handler.getPlugin().getBOSEconomyTask().promptSave();
    
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Plugin data saved.");
  }
  
  public void statsCommand(CommandHandler.BOSCommandSender sender)
  {
    if (sender.isBlock()) {
      return;
    }
    sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - Statistics:");
    double totalMoney = this.handler.getPlugin().getAccountManager().getTotalMoney();
    int totalPlayers = 
      this.handler.getPlugin().getAccountManager().getTotalPlayers();
    double averageMoney = 
      this.handler.getPlugin().getAccountManager().getAverageMoney();
    

    sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "  Total players: " + 
      BOSEconomy.MONEY_COLOR + totalPlayers);
    sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "  Total money: " + 
      getMoneyFormatter().generateString(
      totalMoney, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR));
    sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + "  Average money: " + 
      getMoneyFormatter().generateString(
      averageMoney, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR));
  }
  
  public void top5Command(CommandHandler.BOSCommandSender sender)
  {
    if (sender.isBlock()) {
      return;
    }
    PlayerAccount[] top5 = this.handler.getPlugin().getAccountManager().getTop5();
    sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - Wealthiest Players:");
    for (int i = 0; i < top5.length; i++) {
      if (top5[i] != null) {
        sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "  " + (i + 1) + ". " + 
          BOSEconomy.GOOD_COLOR + top5[i].getName() + BOSEconomy.INFO_COLOR + 
          " - " + BOSEconomy.MONEY_COLOR + 
          formatMoney(top5[i].getMoney()));
      }
    }
  }
  
  public void bracketCreateCommand(CommandHandler.BOSCommandSender sender, String bracketName, String valueArg, String type, String masterName)
  {
    double value = 0.0D;
    try
    {
      value = this.handler.getPlugin().getMoneyRounded(Double.parseDouble(valueArg));
    }
    catch (Exception ex)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The value must be a number.");
      return;
    }
    Bracket bracket = 
      this.handler.getPlugin().getBracketManager().getBracket(bracketName);
    if (bracket != null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "A bracket named " + 
        bracket.getName() + " already exists.");
      return;
    }
    if (value < 0.0D)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Brackets cannot have negative values.");
      return;
    }
    if (isBadDouble(sender, value)) {
      return;
    }
    type = type.toLowerCase();
    if (type.equals("wage"))
    {
      bracket = 
        new WageBracket(this.handler.getPlugin().getBracketManager(), bracketName, 
        value);
    }
    else if ((type.equals("sub")) || (type.equals("subscription")))
    {
      bracket = 
        new SubBracket(this.handler.getPlugin().getBracketManager(), bracketName, 
        value);
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Unrecognized bracket type '" + type + "'.");
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Valid types are 'wage' and 'subscription'.");
      return;
    }
    if (masterName != null)
    {
      Account master = 
        this.handler.getPlugin().getAccountManager().getAccountByName(masterName);
      if (((master instanceof PlayerAccount)) || ((master instanceof BankAccount)))
      {
        BracketMasterSetting s = 
          (BracketMasterSetting)bracket.getSetting("master");
        if (s != null) {
          s.setValue(master);
        }
      }
    }
    this.handler.getPlugin().getBracketManager().addBracket(bracket);
    if ((bracket.getMaster() instanceof PlayerAccount)) {
      bracket.getMaster().sendMessage(
        BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
        bracket.getTypeCaps() + " bracket " + bracket.getName() + 
        " has been created with you as the bracket master.");
    } else if ((bracket.getMaster() instanceof BankAccount)) {
      ((BankAccount)bracket.getMaster()).sendMessage(
        BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
        bracket.getTypeCaps() + " bracket " + bracket.getName() + 
        " has been created with bank account " + bracket.getMaster().getName() + 
        " as the bracket master.", sender.isPlayer() ? 
        sender.getAsPlayer() : null);
    }
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Created " + bracket.getType() + 
      " bracket '" + bracket.getName() + "'.");
  }
  
  public void bracketRemoveCommand(CommandHandler.BOSCommandSender sender, String bracketName)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    if ((bracket.getMaster() instanceof PlayerAccount)) {
      bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
        BOSEconomy.NEUTRAL_COLOR + "Your bracket '" + 
        bracket.getName() + "' has been removed.");
    } else if ((bracket.getMaster() instanceof BankAccount)) {
      ((BankAccount)bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
        BOSEconomy.NEUTRAL_COLOR + "Bracket '" + bracket.getName() + 
        "' (mastered by bank account '" + bracket.getMaster().getName() + 
        "') has been removed.", 
        sender.isPlayer() ? sender.getAsPlayer() : null);
    }
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed bracket '" + bracket.getName() + "'.");
    

    this.handler.getPlugin().getBracketManager().removeBracket(bracket.getName());
  }
  
  public void bracketRenameCommand(CommandHandler.BOSCommandSender sender, String bracketName, String newName)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    boolean adminAccess = 
      this.handler.hasPermission(sender, "BOSEconomy.admin.bracket.rename");
    if (sender.isPlayer()) {
      if (!bracket.hasMasterAccess(sender)) {}
    }
    boolean masterAccess = 
    




      bracket.getCanRename();
    if ((adminAccess) || (masterAccess))
    {
      if ((newName == null) || (newName.length() == 0))
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Use a valid bracket name.");
        return;
      }
      if (newName.equals(bracket.getName()))
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "The bracket is already named '" + newName + "'.");
        return;
      }
      if (this.handler.getPlugin().getBracketManager().getBracket(newName) != null)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "A bracket named '" + newName + 
          "' already exists.");
        return;
      }
      if ((!masterAccess) && (bracket.getMaster() != null))
      {
        bracket.getMaster().sendMessage(
          BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + "The " + 
          bracket.getName() + " bracket has been renamed");
        bracket.getMaster().sendMessage(
          BOSEconomy.NEUTRAL_COLOR + "  to '" + newName + 
          "' by an administrator.");
      }
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Renamed the '" + 
        bracket.getName() + "' bracket to '" + newName + "'.");
      

      this.handler.getPlugin().getBracketManager().renameBracket(bracket.getName(), newName);
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You are not allowed to rename the '" + 
        bracket.getName() + "' bracket.");
    }
  }
  
  public void bracketListCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    List<Bracket> bracketList = this.handler.getPlugin().getBracketManager().getBracketList();
    Collections.sort(bracketList, new Bracket.NameComparator());
    if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - Bracket List");
      for (Bracket b : bracketList) {
        sender.sendMsgInfo(ChatColor.WHITE + "  " + b.getName() + ": " + 
          BOSEconomy.MONEY_COLOR + formatMoney(b.getValue()) + ChatColor.WHITE + 
          " / " + b.getPaymentInterval().toString() + (
          b.getMaster() == null ? "" : new StringBuilder(" (").append(b.getMaster().getName()).append(")").toString()));
      }
    }
    else if (sender.isPlayer())
    {
      ArrayList<String> list = new ArrayList(bracketList.size());
      for (Bracket b : bracketList) {
        list.add(ChatColor.WHITE + "  " + b.getName() + ": " + 
          BOSEconomy.MONEY_COLOR + formatMoney(b.getValue()) + ChatColor.WHITE + 
          " / " + b.getPaymentInterval().toString() + (
          b.getMaster() == null ? "" : new StringBuilder(" (").append(b.getMaster().getName()).append(")").toString()));
      }
      sendListMessage(sender, list.toArray(), list.size(), pageArg, 
        "Payment Brackets", 8, false);
    }
  }
  
  public void bracketInfoCommand(CommandHandler.BOSCommandSender sender, String bracketName)
  {
    if (sender.isBlock()) {
      return;
    }
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    if (!this.handler.hasPermission(sender, "BOSEconomy.admin.bracket.info")) {
      if (sender.isPlayer()) {
        if (((bracket.getMaster() instanceof PlayerAccount)) && 
        
          (bracket.getMaster().getName().equalsIgnoreCase(sender.getAsPlayer().getName()))) {
          break label104;
        }
      }
    }
    label104:
    boolean canView = 
    








      ((bracket.getMaster() instanceof BankAccount)) && 
      (((BankAccount)bracket.getMaster()).isOwner(sender.getAsPlayer()));
    if (canView)
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + bracket.getName() + " Bracket Information: ");
      if ((bracket instanceof WageBracket)) {
        sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "  Type: " + ChatColor.WHITE + "Wage" + (
          ((WageBracket)bracket).isUnfulfilling() ? BOSEconomy.BAD_COLOR + 
          " (Unfulfilling)" : ""));
      } else if ((bracket instanceof SubBracket)) {
        sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "  Type: " + ChatColor.WHITE + "Subscription");
      }
      sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "  Member Count: " + 
        ChatColor.WHITE + bracket.getMembersSize());
      
      List<BracketSetting<?>> settingList = bracket.getSettingList();
      Collections.sort(settingList, new Setting.NameComparator());
      for (BracketSetting<?> s : settingList) {
        sender.sendMsgInfo(BOSEconomy.INFO_COLOR + "  " + s.getName() + ": " + 
          ChatColor.WHITE + s.getStringValue());
      }
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You are not allowed to view information about the " + 
        bracket.getName() + " bracket.");
    }
  }
  
  public void bracketSetCommand(CommandHandler.BOSCommandSender sender, String bracketName, String setting, String[] args, int offset)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    boolean found = false;
    List<BracketSetting<?>> settingList = bracket.getSettingList();
    for (BracketSetting<?> s : settingList) {
      if (setting.equalsIgnoreCase(s.getName()))
      {
        found = true;
        if (s.setValue(sender, bracket, args, offset))
        {
          if (s.hasCustomFeedback()) {
            break;
          }
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Changed the setting '" + 
            ChatColor.WHITE + s.getName() + BOSEconomy.GOOD_COLOR + 
            "' to '" + ChatColor.WHITE + s.getStringValue() + 
            BOSEconomy.GOOD_COLOR + "' for bracket " + 
            bracket.getName() + ".");
          if ((bracket.getMaster() == null) || (bracket.hasMasterAccess(sender))) {
            break;
          }
          bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + "An administrator changed the setting '" + 
            ChatColor.WHITE + s.getName() + BOSEconomy.NEUTRAL_COLOR + "' to '" + 
            ChatColor.WHITE + s.getStringValue() + BOSEconomy.NEUTRAL_COLOR + 
            "' for bracket " + bracket.getName() + ".");
          

          break;
        }
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + s.getMessage());
        
        break;
      }
    }
    if (!found)
    {
      Collections.sort(settingList, new Setting.NameComparator());
      StringBuilder sb = new StringBuilder("");
      boolean first = true;
      for (BracketSetting<?> s : settingList) {
        if (first)
        {
          first = false;
          sb.append(s.getName());
        }
        else
        {
          sb.append(", ").append(s.getName());
        }
      }
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Unknown bracket setting '" + 
        setting + "'. Valid settings: " + sb.toString());
      return;
    }
  }
  
  public void bracketSetmasterCommand(CommandHandler.BOSCommandSender sender, String bracketName, String masterName)
  {
    if (masterName == null)
    {
      bracketSetCommand(sender, bracketName, "master", new String[0], 0);
    }
    else
    {
      String[] args = new String[1];
      args[0] = masterName;
      bracketSetCommand(sender, bracketName, "master", args, 0);
    }
  }
  
  public void bracketRemovemasterCommand(CommandHandler.BOSCommandSender sender, String bracketName)
  {
    bracketSetCommand(sender, bracketName, "master", new String[0], 0);
  }
  
  public void bracketListmembersCommand(CommandHandler.BOSCommandSender sender, String bracketName, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    if (!this.handler.hasPermission(sender, "BOSEconomy.admin.bracket.listmembers")) {
      if (sender.isPlayer()) {
        if (((bracket.getMaster() instanceof PlayerAccount)) && 
        
          (bracket.getMaster().getName().equalsIgnoreCase(sender.getAsPlayer().getName()))) {
          break label110;
        }
      }
    }
    label110:
    boolean canDo = 
    








      ((bracket.getMaster() instanceof BankAccount)) && 
      (((BankAccount)bracket.getMaster()).isOwner(sender.getAsPlayer()));
    if (canDo)
    {
      List<Bracket.BracketMember> memberList = bracket.getMemberList();
      Collections.sort(memberList, new Bracket.MemberNameComparator());
      int listIndex;
      if (sender.isPlayer())
      {
        String[] list = new String[bracket.getMembersSize()];
        listIndex = 0;
        for (Bracket.BracketMember bm : memberList)
        {
          list[listIndex] = 
          
            ("  " + bm.getAccount().getName() + (bm.getCount() != 1.0D ? " (x" + bm.getCount() + ")" : "") + ((bm.getAccount() instanceof BankAccount) ? " (Bank)" : ""));
          listIndex++;
          if (listIndex >= list.length) {
            break;
          }
        }
        sendListMessage(sender, list, listIndex, pageArg, bracket.getName() + 
          " Bracket Members (Total: " + bracket.getMembersSize() + ")", 8, false);
      }
      else if (sender.isConsole())
      {
        sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - " + bracket.getName() + 
          " Bracket Members (Total: " + bracket.getMembersSize() + ")");
        for (Bracket.BracketMember bm : memberList) {
          sender.sendMsgInfo("  " + bm.getAccount().getName() + (
            bm.getCount() != 1.0D ? " (x" + bm.getCount() + ")" : "") + (
            (bm.getAccount() instanceof BankAccount) ? " (Bank)" : ""));
        }
      }
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You are not allowed to view the member list of the '" + 
        bracket.getName() + "' bracket.");
    }
  }
  
  public void bracketAddmemberCommand(CommandHandler.BOSCommandSender sender, String bracketName, String memberString)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    boolean joinCommand = false;
    if (memberString.equalsIgnoreCase("@me"))
    {
      if (sender.isConsole())
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The server console cannot join brackets.");
        return;
      }
      if (sender.isBlock())
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Attempted to join a bracket.");
        return;
      }
      joinCommand = true;
    }
    AccountGroup g = 
      new AccountGroup(this.handler.getPlugin(), memberString, sender, 0);
    if (g.hadErrors()) {
      g.tellErrors(sender);
    }
    if (g.getSize() == 0) {
      return;
    }
    int addMemberValue = bracket.getAddMemberValue();
    int playerJoinValue = bracket.getPlayerJoinValue();
    int bankJoinValue = bracket.getBankJoinValue();
    boolean addMemberAllowOrRequest = 
      (addMemberValue == 1) || 
      (addMemberValue == 2);
    boolean playerJoinAllowOrRequest = 
      (playerJoinValue == 1) || 
      (playerJoinValue == 2);
    boolean bankJoinAllowOrRequest = 
      (bankJoinValue == 1) || 
      (bankJoinValue == 2);
    boolean adminAccess = 
    
      this.handler.hasPermission(sender, "BOSEconomy.admin.bracket.addmember");
    if (sender.isPlayer()) {}
    boolean masterAccess = 
    


      bracket.hasMasterAccess(sender);
    


    int FLAG_ACCEPT_ADD_MEMBER = 0;
    int FLAG_ACCEPT_SELF_JOIN = 2;
    int FLAG_ACCEPT_SELF_BANK_JOIN = 4;
    
    int FLAG_REJECT_NO_ADD_PERMISSION = 1;
    int FLAG_REJECT_NO_SELF_PERMISSION = 3;
    int FLAG_REJECT_FOR_MEMBERSHIP = 5;
    int FLAG_REJECT_FOR_SELF_MEMBERSHIP = 7;
    int FLAG_REJECT_FOR_MASTERY = 9;
    int FLAG_REJECT_FOR_SELF_MASTERY = 11;
    int FLAG_REJECT_REQUEST_FAILED = 13;
    int FLAG_REJECT_BRACKET_FULL = 15;
    int FLAG_REJECT_SELF_BRACKET_FULL = 17;
    




    PlayerAccount pa = null;
    if (sender.isPlayer()) {
      pa = this.handler.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
    }
    int bankJoinReqId = 0;
    
    int reqFailMessages = 0;
    for (AccountGroup.LocalAccount la : g.getAccountList())
    {
      boolean bracketIsFull = 
        (bracket.getMaximumMembers() >= 0) && (
        bracket.getMembersSize() >= bracket.getMaximumMembers());
      

      boolean accountIsSender = la.account == pa;
      boolean accountIsMember = bracket.isMember(la.account);
      boolean accountIsMaster = bracket.getMaster() == la.account;
      if (adminAccess)
      {
        if (accountIsSender)
        {
          if (accountIsMember) {
            la.flag = 7;
          } else if (accountIsMaster) {
            la.flag = 11;
          } else {
            la.flag = 2;
          }
        }
        else if (accountIsMember) {
          la.flag = 5;
        } else if (accountIsMaster) {
          la.flag = 9;
        } else {
          la.flag = 0;
        }
      }
      else if ((masterAccess) && (addMemberAllowOrRequest))
      {
        if (accountIsSender)
        {
          if (accountIsMember) {
            la.flag = 7;
          } else if (accountIsMaster) {
            la.flag = 11;
          } else if (bracketIsFull) {
            la.flag = 17;
          } else {
            la.flag = 0;
          }
        }
        else if (accountIsMember) {
          la.flag = 5;
        } else if (accountIsMaster) {
          la.flag = 9;
        } else if (bracketIsFull) {
          la.flag = 15;
        } else {
          la.flag = 0;
        }
      }
      else if (accountIsSender)
      {
        if (playerJoinAllowOrRequest)
        {
          if (accountIsMember) {
            la.flag = 7;
          } else if (bracketIsFull) {
            la.flag = 17;
          } else {
            la.flag = 2;
          }
        }
        else {
          la.flag = 3;
        }
      }
      else if ((la.account instanceof BankAccount))
      {
        if (bankJoinAllowOrRequest)
        {
          if ((pa != null) && (((BankAccount)la.account).isOwner(pa)))
          {
            if (accountIsMember) {
              la.flag = 5;
            } else {
              la.flag = 4;
            }
          }
          else {
            la.flag = 1;
          }
        }
        else {
          la.flag = 1;
        }
      }
      else {
        la.flag = 1;
      }
      if (la.flag % 2 == 0)
      {
        if (la.flag == 0)
        {
          if ((addMemberValue == 1) || (adminAccess))
          {
            la.account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              (la.account instanceof PlayerAccount) ? "You have" : 
              new StringBuilder("Bank account ").append(la.account.getName()).append(" has").toString()) + 
              " been added to the " + bracket.getName() + " bracket by " + (
              !masterAccess ? "an administrator" : "its master") + ".");
            
            bracket.addMember(la.account);
          }
          else if (addMemberValue == 2)
          {
            Request r = 
              new BracketAddMemberRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              la.account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                (la.account instanceof PlayerAccount) ? "You have" : 
                new StringBuilder("Bank account ").append(la.account.getName()).append(" has").toString()) + 
                " received a request (#" + r.getId() + ") to join the " + 
                bracket.getName() + " " + bracket.getType() + " bracket.");
              la.account.sendMessage(BOSEconomy.INFO_COLOR + "  " + 
                RequestHandler.getRequestViewMessage());
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 13;
            }
          }
        }
        else if (la.flag == 2)
        {
          if ((playerJoinValue == 1) || (adminAccess))
          {
            sender.sendMsgCopy(BOSEconomy.NEUTRAL_COLOR + "You have joined the '" + 
              bracket.getName() + "' bracket.");
            if (bracket.getMaster() != null) {
              bracket.getMaster().sendMessage(
                BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
                "User '" + la.account.getName() + "' has joined the '" + 
                bracket.getName() + "' bracket.");
            }
            bracket.addMember(la.account);
          }
          else if (playerJoinValue == 2)
          {
            Request r = 
              new BracketPlayerJoinRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, (PlayerAccount)la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              sender.sendMsgCopy(BOSEconomy.NEUTRAL_COLOR + 
                "You have submitted a request to join the '" + 
                bracket.getName() + "' bracket.");
              if (bracket.getMaster() != null)
              {
                bracket.getMaster().sendMessage(
                  BOSEconomy.TAG_BLANK_COLOR + 
                  BOSEconomy.NEUTRAL_COLOR + (
                  (bracket.getMaster() instanceof PlayerAccount) ? 
                  "You have" : new StringBuilder("Bank account ")
                  .append(bracket.getMaster().getName()).append(" has").toString()) + 
                  " received a request (#" + r.getId() + 
                  ") from user '" + la.account.getName() + 
                  "' to join the '" + bracket.getName() + "' bracket.");
                bracket.getMaster().sendMessage(BOSEconomy.INFO_COLOR + "  " + 
                  RequestHandler.getRequestViewMessage());
              }
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 13;
            }
          }
        }
        else if (la.flag == 4) {
          if ((bankJoinValue == 1) || (adminAccess))
          {
            ((BankAccount)la.account).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + "Bank account " + la.account.getName() + 
              " has been added to the " + bracket.getName() + " bracket by " + (
              adminAccess ? "an administrator" : new StringBuilder("user ").append(pa.getName()).toString()) + ".", pa);
            
            bracket.addMember(la.account);
          }
          else if (bankJoinValue == 2)
          {
            Request r = 
              new BracketBankJoinRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, (BankAccount)la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              bankJoinReqId = r.getId();
              ((BankAccount)la.account).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("User ").append(pa.getName()).toString()) + 
                " has submitted a request (#" + r.getId() + ") that bank account " + 
                la.account.getName() + " be added to the " + bracket.getName() + 
                " bracket.", pa);
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 13;
            }
          }
        }
      }
      else if (la.flag == 3) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You do not have permission to join the " + bracket.getName() + 
          " bracket.");
      } else if (la.flag == 7) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You are already a member of the " + bracket.getName() + 
          " bracket.");
      } else if (la.flag == 11) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You cannot be a member of the " + bracket.getName() + 
          " bracket because you are the bracket's master.");
      } else if (la.flag == 17) {
        if (bracket.getMaximumMembers() == 0) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "You cannot join the " + bracket.getName() + 
            " bracket because it does not accept any members. " + 
            "(Maximum members allowed is zero.)");
        } else {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "You cannot join the " + bracket.getName() + 
            " bracket because the bracket is full. Maximum members allowed is " + 
            bracket.getMaximumMembers() + ".");
        }
      }
    }
    if (!joinCommand)
    {
      int flagCount = g.getSize(0);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 0);
        if ((addMemberValue == 1) || (adminAccess))
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Added " + accountString + 
            " to the " + bracket.getName() + " bracket.");
          if ((bracket.getMaster() != null) && (bracket.getMaster() != pa)) {
            if ((bracket.getMaster() instanceof BankAccount)) {
              ((BankAccount)bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " has added " + accountString + " to the " + 
                bracket.getName() + " bracket.", pa);
            } else {
              bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " has added " + accountString + " to the " + 
                bracket.getName() + " bracket.");
            }
          }
        }
        else if (addMemberValue == 2)
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Sent " + (
            flagCount == 1 ? "a request" : "requests") + " asking " + 
            accountString + " to join the " + bracket.getName() + 
            " bracket.");
          if ((bracket.getMaster() != null) && (bracket.getMaster() != pa)) {
            if ((bracket.getMaster() instanceof BankAccount)) {
              ((BankAccount)bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " sent " + (flagCount == 1 ? "a request" : "requests") + " asking " + 
                accountString + " to join the " + bracket.getName() + " bracket.", pa);
            } else {
              bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " sent " + (flagCount == 1 ? "a request" : "requests") + " asking " + 
                accountString + " to join the " + bracket.getName() + " bracket.");
            }
          }
        }
      }
      flagCount = g.getSize(4);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 4);
        if ((bankJoinValue == 1) || (adminAccess))
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Added " + accountString + 
            " to the " + bracket.getName() + " bracket.");
          if (bracket.getMaster() != null) {
            bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
              " has added " + accountString + " to the " + bracket.getName() + " bracket.");
          }
        }
        else if (bankJoinValue == 2)
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Sent " + (
            flagCount == 1 ? "a request" : "requests") + " asking " + 
            accountString + " to join the " + bracket.getName() + 
            " bracket.");
          if (bracket.getMaster() != null)
          {
            bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              (bracket.getMaster() instanceof PlayerAccount) ? "You have" : new StringBuilder("Bank account ")
              .append(bracket.getMaster().getName()).append(" has").toString()) + " received " + (
              flagCount == 1 ? "a request (#" + bankJoinReqId + ")" : "requests") + 
              " for " + accountString + " to join the " + bracket.getName() + 
              " bracket.");
            bracket.getMaster().sendMessage(BOSEconomy.INFO_COLOR + "  " + 
              RequestHandler.getRequestViewMessage());
          }
        }
      }
      flagCount = g.getSize(1);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 1);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You do not have permission to add " + 
          accountString + " to the " + bracket.getName() + " bracket.");
      }
      flagCount = g.getSize(5);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(true, true, 5);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + accountString + (
          flagCount == 1 ? " is already a member" : " are already members") + 
          " of the " + bracket.getName() + " bracket.");
      }
      flagCount = g.getSize(9);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(true, true, 9);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + accountString + " cannot be " + (
          flagCount == 1 ? "a member" : "members") + " of the " + 
          bracket.getName() + " bracket because they are the bracket's master.");
      }
      flagCount = g.getSize(15);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(true, true, 15);
        if (bracket.getMaximumMembers() == 0) {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + accountString + " cannot be " + (
            flagCount == 1 ? "a member" : "members") + " of the " + bracket.getName() + 
            " bracket because it does not accept any members. " + 
            "(Maximum members allowed is zero.)");
        } else {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + accountString + " cannot be " + (
            flagCount == 1 ? "a member" : "members") + " of the " + bracket.getName() + 
            " bracket because the bracket is full. Maximum members allowed is " + 
            bracket.getMaximumMembers() + ".");
        }
      }
      flagCount = g.getSize(13);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 13);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Failed to send " + (flagCount == 1 ? "a request" : "requests") + 
          " for " + accountString + ".");
      }
    }
  }
  
  public void bracketRemovememberCommand(CommandHandler.BOSCommandSender sender, String bracketName, String memberString)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    boolean leaveCommand = false;
    if (memberString.equalsIgnoreCase("@me"))
    {
      if (sender.isConsole())
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The server console is not in any brackets.");
        return;
      }
      if (sender.isBlock())
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Attempted to leave a bracket.");
        return;
      }
      leaveCommand = true;
    }
    AccountGroup g = 
      new AccountGroup(this.handler.getPlugin(), memberString, sender, 0);
    if (g.hadErrors()) {
      g.tellErrors(sender);
    }
    if (g.getSize() == 0) {
      return;
    }
    int removeMemberValue = bracket.getRemoveMemberValue();
    int playerLeaveValue = bracket.getPlayerLeaveValue();
    int bankLeaveValue = bracket.getBankLeaveValue();
    boolean removeMemberAllowOrRequest = 
      (removeMemberValue == 1) || 
      (removeMemberValue == 2);
    boolean playerLeaveAllowOrRequest = 
      (playerLeaveValue == 1) || 
      (playerLeaveValue == 2);
    boolean bankLeaveAllowOrRequest = 
      (bankLeaveValue == 1) || 
      (bankLeaveValue == 2);
    boolean adminAccess = 
    
      this.handler.hasPermission(sender, "BOSEconomy.admin.bracket.removemember");
    if (sender.isPlayer()) {}
    boolean masterAccess = 
    


      bracket.hasMasterAccess(sender);
    


    int FLAG_ACCEPT_REMOVE_MEMBER = 0;
    int FLAG_ACCEPT_SELF_LEAVE = 2;
    int FLAG_ACCEPT_SELF_BANK_LEAVE = 4;
    
    int FLAG_REJECT_NO_REMOVE_PERMISSION = 1;
    int FLAG_REJECT_NO_SELF_PERMISSION = 3;
    int FLAG_REJECT_FOR_NONMEMBERSHIP = 5;
    int FLAG_REJECT_FOR_SELF_NONMEMBERSHIP = 7;
    int FLAG_REJECT_REQUEST_FAILED = 9;
    




    PlayerAccount pa = null;
    if (sender.isPlayer()) {
      pa = this.handler.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
    }
    int bankLeaveReqId = 0;
    
    int reqFailMessages = 0;
    for (AccountGroup.LocalAccount la : g.getAccountList())
    {
      boolean accountIsSender = la.account == pa;
      boolean accountIsMember = bracket.isMember(la.account);
      if (adminAccess)
      {
        if (accountIsSender)
        {
          if (accountIsMember) {
            la.flag = 2;
          } else {
            la.flag = 7;
          }
        }
        else if (accountIsMember) {
          la.flag = 0;
        } else {
          la.flag = 5;
        }
      }
      else if ((masterAccess) && (removeMemberAllowOrRequest))
      {
        if (accountIsSender)
        {
          if (accountIsMember) {
            la.flag = 0;
          } else {
            la.flag = 7;
          }
        }
        else if (accountIsMember) {
          la.flag = 0;
        } else {
          la.flag = 5;
        }
      }
      else if (accountIsSender)
      {
        if (playerLeaveAllowOrRequest)
        {
          if (accountIsMember) {
            la.flag = 2;
          } else {
            la.flag = 7;
          }
        }
        else {
          la.flag = 3;
        }
      }
      else if ((la.account instanceof BankAccount))
      {
        if (bankLeaveAllowOrRequest)
        {
          if ((pa != null) && (((BankAccount)la.account).isOwner(pa)))
          {
            if (accountIsMember) {
              la.flag = 4;
            } else {
              la.flag = 5;
            }
          }
          else {
            la.flag = 1;
          }
        }
        else {
          la.flag = 1;
        }
      }
      else {
        la.flag = 1;
      }
      if (la.flag % 2 == 0)
      {
        if (la.flag == 0)
        {
          if ((removeMemberValue == 1) || (adminAccess))
          {
            la.account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              (la.account instanceof PlayerAccount) ? "You have" : 
              new StringBuilder("Bank account ").append(la.account.getName()).append(" has").toString()) + 
              " been removed from the " + bracket.getName() + " bracket by " + (
              !masterAccess ? "an administrator" : "its master") + ".");
            
            bracket.removeMember(la.account);
          }
          else if (removeMemberValue == 2)
          {
            Request r = 
              new BracketRemoveMemberRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              la.account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                (la.account instanceof PlayerAccount) ? "You have" : 
                new StringBuilder("Bank account ").append(la.account.getName()).append(" has").toString()) + 
                " received a request (#" + r.getId() + ") to leave the " + 
                bracket.getName() + " " + bracket.getType() + " bracket.");
              la.account.sendMessage(BOSEconomy.INFO_COLOR + "  " + 
                RequestHandler.getRequestViewMessage());
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 9;
            }
          }
        }
        else if (la.flag == 2)
        {
          if ((playerLeaveValue == 1) || (adminAccess))
          {
            sender.sendMsgCopy(BOSEconomy.NEUTRAL_COLOR + "You have left the '" + 
              bracket.getName() + "' bracket.");
            if (bracket.getMaster() != null) {
              bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + "User '" + la.account.getName() + 
                "' has left the '" + bracket.getName() + "' bracket.");
            }
            bracket.removeMember(la.account);
          }
          else if (playerLeaveValue == 2)
          {
            Request r = 
              new BracketPlayerLeaveRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, (PlayerAccount)la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              sender.sendMsgCopy(BOSEconomy.NEUTRAL_COLOR + 
                "You have submitted a request to leave the '" + 
                bracket.getName() + "' bracket.");
              if (bracket.getMaster() != null)
              {
                bracket.getMaster().sendMessage(
                  BOSEconomy.TAG_BLANK_COLOR + 
                  BOSEconomy.NEUTRAL_COLOR + (
                  (bracket.getMaster() instanceof PlayerAccount) ? 
                  "You have" : new StringBuilder("Bank account ")
                  .append(bracket.getMaster().getName()).append(" has").toString()) + 
                  " received a request (#" + r.getId() + 
                  ") from user '" + la.account.getName() + 
                  "' to leave the '" + bracket.getName() + "' bracket.");
                bracket.getMaster().sendMessage(BOSEconomy.INFO_COLOR + "  " + 
                  RequestHandler.getRequestViewMessage());
              }
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 9;
            }
          }
        }
        else if (la.flag == 4) {
          if ((bankLeaveValue == 1) || (adminAccess))
          {
            ((BankAccount)la.account).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + "Bank account " + la.account.getName() + 
              " has been removed from the " + bracket.getName() + " bracket by " + (
              adminAccess ? "an administrator" : new StringBuilder("user ").append(pa.getName()).toString()) + ".", pa);
            
            bracket.removeMember(la.account);
          }
          else if (bankLeaveValue == 2)
          {
            Request r = 
              new BracketBankLeaveRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, (BankAccount)la.account);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              bankLeaveReqId = r.getId();
              ((BankAccount)la.account).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("User ").append(pa.getName()).toString()) + 
                " has submitted a request (#" + r.getId() + ") that bank account " + 
                la.account.getName() + " be removed from the " + bracket.getName() + 
                " bracket.", pa);
            }
            else
            {
              if (reqFailMessages < 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
              } else if (reqFailMessages == 8) {
                sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
                  "Suppressing additional request failure messages.");
              }
              reqFailMessages++;
              la.flag = 9;
            }
          }
        }
      }
      else if (la.flag == 3) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You do not have permission to leave the " + bracket.getName() + 
          " bracket.");
      } else if (la.flag == 7) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You are already not a member of the " + bracket.getName() + 
          " bracket.");
      }
    }
    if (!leaveCommand)
    {
      int flagCount = g.getSize(0);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 0);
        if ((removeMemberValue == 1) || (adminAccess))
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed " + accountString + 
            " from the " + bracket.getName() + " bracket.");
          if ((bracket.getMaster() != null) && (bracket.getMaster() != pa)) {
            if ((bracket.getMaster() instanceof BankAccount)) {
              ((BankAccount)bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " has removed " + accountString + " from the " + 
                bracket.getName() + " bracket.", pa);
            } else {
              bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " has removed " + accountString + " from the " + 
                bracket.getName() + " bracket.");
            }
          }
        }
        else if (removeMemberValue == 2)
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Sent " + (
            flagCount == 1 ? "a request" : "requests") + " asking " + 
            accountString + " to leave the " + bracket.getName() + 
            " bracket.");
          if ((bracket.getMaster() != null) && (bracket.getMaster() != pa)) {
            if ((bracket.getMaster() instanceof BankAccount)) {
              ((BankAccount)bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " sent " + (flagCount == 1 ? "a request" : "requests") + " asking " + 
                accountString + " to leave the " + bracket.getName() + " bracket.", pa);
            } else {
              bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + (
                adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + 
                " sent " + (flagCount == 1 ? "a request" : "requests") + " asking " + 
                accountString + " to leave the " + bracket.getName() + " bracket.");
            }
          }
        }
      }
      flagCount = g.getSize(4);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 4);
        if ((bankLeaveValue == 1) || (adminAccess))
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed " + accountString + 
            " from the " + bracket.getName() + " bracket.");
          if (bracket.getMaster() != null) {
            bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              adminAccess ? "An administrator" : new StringBuilder("Player ").append(pa.getName()).toString()) + " has removed " + 
              accountString + " from the " + bracket.getName() + " bracket.");
          }
        }
        else if (bankLeaveValue == 2)
        {
          sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Sent " + (
            flagCount == 1 ? "a request" : "requests") + " asking " + 
            accountString + " to leave the " + bracket.getName() + " bracket.");
          if (bracket.getMaster() != null)
          {
            bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
              BOSEconomy.NEUTRAL_COLOR + (
              (bracket.getMaster() instanceof PlayerAccount) ? "You have" : new StringBuilder("Bank account ")
              .append(bracket.getMaster().getName()).append(" has").toString()) + " received " + (
              flagCount == 1 ? "a request (#" + bankLeaveReqId + ")" : "requests") + 
              " for " + accountString + " to leave the " + bracket.getName() + " bracket.");
            bracket.getMaster().sendMessage(BOSEconomy.INFO_COLOR + "  " + 
              RequestHandler.getRequestViewMessage());
          }
        }
      }
      flagCount = g.getSize(1);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 1);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You do not have permission to remove " + accountString + " from the " + 
          bracket.getName() + " bracket.");
      }
      flagCount = g.getSize(5);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(true, true, 5);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          accountString + (
          flagCount == 1 ? " is already not a member" : " are already not members") + 
          " of the " + bracket.getName() + " bracket.");
      }
      flagCount = g.getSize(9);
      if (flagCount > 0)
      {
        String accountString = 
          g.getAccountString(false, true, 9);
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Failed to send " + (flagCount == 1 ? "a request" : "requests") + 
          " for " + accountString + ".");
      }
    }
  }
  
  public void bracketSetmultiplierCommand(CommandHandler.BOSCommandSender sender, String bracketName, String memberName, String countArg)
  {
    Bracket bracket = getBracketOrBust(sender, bracketName);
    if (bracket == null) {
      return;
    }
    boolean adminAccess = 
    
      this.handler.hasPermission(sender, 
      "BOSEconomy.admin.bracket.setmultiplier");
    int changeMultValue = bracket.getChangeMultiplierValue();
    if (sender.isPlayer()) {
      if (!bracket.hasMasterAccess(sender)) {}
    }
    boolean masterAccess = 
    




      (changeMultValue == 1) || (changeMultValue == 2);
    if ((adminAccess) || (masterAccess))
    {
      try
      {
        double mult = Double.parseDouble(countArg);
        if (mult < 0.0D)
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "The multiplier should be greater than or equal to zero.");
          return;
        }
        if (Double.isInfinite(mult))
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "The multiplier cannot be infinity.");
          return;
        }
        if (Double.isNaN(mult))
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "The multiplier cannot be NaN.");
          return;
        }
      }
      catch (Exception ex)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "The multiplier should be a number."); return;
      }
      double mult;
      BracketMaximumMultiplierSetting maxMultSetting;
      if (!adminAccess)
      {
        maxMultSetting = 
          (BracketMaximumMultiplierSetting)bracket.getSetting("maximum-multiplier");
        double max;
        double max;
        if (maxMultSetting != null) {
          max = maxMultSetting.getObjectValue().doubleValue();
        } else {
          max = -1.0D;
        }
        if ((max >= 0.0D) && (mult > max))
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "The multiplier cannot be greater than the maximum (" + max + ").");
          return;
        }
        BracketMinimumMultiplierSetting minMultSetting = 
          (BracketMinimumMultiplierSetting)bracket.getSetting("minimum-multiplier");
        double min;
        double min;
        if (minMultSetting != null) {
          min = minMultSetting.getObjectValue().doubleValue();
        } else {
          min = -1.0D;
        }
        if ((min >= 0.0D) && (mult < min))
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "The multiplier cannot be less than the minimum (" + min + ").");
          return;
        }
      }
      AccountGroup g = 
        new AccountGroup(this.handler.getPlugin(), memberName, sender, 0);
      if (g.hadErrors()) {
        g.tellErrors(sender);
      }
      if (g.getSize() == 0) {
        return;
      }
      for (AccountGroup.LocalAccount la : g.getAccountList())
      {
        Account member = la.account;
        
        Bracket.BracketMember bracketMember = bracket.getMember(member);
        if (bracketMember != null) {
          if ((changeMultValue == 1) || (adminAccess))
          {
            if ((member instanceof PlayerAccount))
            {
              member.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + "Your multiplier has been set to " + 
                mult + " in");
              member.sendMessage(BOSEconomy.NEUTRAL_COLOR + "  bracket " + 
                bracket.getName() + " by " + (
                !masterAccess ? "an administrator" : "its master") + ".");
            }
            else if ((member instanceof BankAccount))
            {
              member.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.NEUTRAL_COLOR + "Bank account " + member.getName() + 
                " had its multiplier");
              member.sendMessage(BOSEconomy.NEUTRAL_COLOR + "  set to " + mult + 
                " in bracket " + bracket.getName() + " by " + (
                !masterAccess ? "an administrator" : "its master") + ".");
            }
            bracketMember.setCount(mult);
          }
          else if (changeMultValue == 2)
          {
            Request r = 
              new BracketChangeMultiplierRequest(this.handler.getPlugin()
              .getRequestHandler(), bracket, member, mult);
            String requestStatus = 
              this.handler.getPlugin().getRequestHandler().addRequest(r);
            if (requestStatus == null)
            {
              member.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
                BOSEconomy.GOOD_COLOR + (
                (member instanceof PlayerAccount) ? "You have" : 
                new StringBuilder("Bank account ").append(member.getName()).append(" has").toString()) + 
                " received a request (#" + r.getId() + 
                ") to change multiplier");
              member.sendMessage(BOSEconomy.GOOD_COLOR + "  to " + mult + 
                " for the " + bracket.getName() + " " + bracket.getType() + 
                " bracket.");
              member.sendMessage(BOSEconomy.INFO_COLOR + "  " + 
                RequestHandler.getRequestViewMessage());
            }
            else
            {
              sender.sendMsgInfo(BOSEconomy.BAD_COLOR + requestStatus);
            }
          }
        }
      }
      if ((changeMultValue == 1) || (adminAccess))
      {
        sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Set multiplier of " + 
          g.getAccountString(false, true) + " to " + mult + " in bracket " + 
          bracket.getName() + ".");
        if ((!masterAccess) && 
          (bracket.getMaster() != null))
        {
          bracket.getMaster().sendMessage(
            BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
            "An administrator set the multiplier of " + 
            g.getAccountString(false, false));
          bracket.getMaster().sendMessage(
            BOSEconomy.NEUTRAL_COLOR + "  to " + mult + " for the " + 
            bracket.getName() + " bracket.");
        }
      }
      else if (changeMultValue == 2)
      {
        String requestString = g.getSize() == 1 ? "request" : "requests";
        sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Sent multiplier change " + 
          requestString + " to " + g.getAccountString(false, true) + ".");
      }
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You are not allowed to alter multipliers of the '" + bracket.getName() + 
        "' bracket.");
    }
  }
  
  public void bracketClearCommand(CommandHandler.BOSCommandSender sender, String accountName, String[] filters, int offset)
  {
    boolean filterMember = false;
    boolean filterMaster = false;
    if (offset == filters.length)
    {
      filterMember = true;
      filterMaster = true;
    }
    else
    {
      for (int i = offset; i < filters.length; i++)
      {
        String filter = filters[i].toLowerCase();
        if (filter.equals("master"))
        {
          filterMaster = true;
        }
        else if (filter.equals("member"))
        {
          filterMember = true;
        }
        else
        {
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "Unrecognized bracket filter '" + filters[i] + "'.");
          sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
            "Valid filters are: member, master");
          return;
        }
      }
    }
    AccountGroup g = 
      new AccountGroup(this.handler.getPlugin(), accountName, sender, 0);
    if (g.hadErrors()) {
      g.tellErrors(sender);
    }
    if (g.getSize() == 0) {
      return;
    }
    String filterInfo = "";
    if (filterMember)
    {
      if (filterMaster) {
        filterInfo = "member or master";
      } else {
        filterInfo = "member";
      }
    }
    else if (filterMaster) {
      filterInfo = "master";
    }
    for (AccountGroup.LocalAccount la : g.getAccountList())
    {
      Account account = la.account;
      if (filterMember) {
        account.removeFromBrackets(false);
      }
      if (filterMaster) {
        account.removeFromBrackets(true);
      }
      account.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
        BOSEconomy.NEUTRAL_COLOR + (
        (account instanceof BankAccount) ? "Bank account " + 
        account.getName() + " was" : "You were") + 
        " removed from brackets");
      account.sendMessage(BOSEconomy.NEUTRAL_COLOR + "  in which " + (
        (account instanceof BankAccount) ? "it was" : "you were") + " a " + 
        filterInfo + ".");
    }
    String wasOrWere = 
      (g.oneAccount()) && 
      (!(((AccountGroup.LocalAccount)g.getAccountList().get(0)).account instanceof PlayerAccount)) ? 
      "it was" : "they were";
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed " + 
      g.getAccountString(false, false) + " from brackets in which " + wasOrWere + 
      " a " + filterInfo + ".");
  }
  
  public void bankCreateCommand(CommandHandler.BOSCommandSender sender, String bankName, String owners, String members)
  {
    if ((bankName == null) || (bankName.length() == 0))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Use a valid bank account name.");
      return;
    }
    BankAccount bank = 
      new BankAccount(this.handler.getPlugin().getAccountManager(), bankName);
    if (!this.handler.getPlugin().getAccountManager().addAccount(bank))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "A bank account named '" + 
        bank.getName() + "' already exists.");
      return;
    }
    AccountGroup gOwners = 
      new AccountGroup(this.handler.getPlugin(), owners, sender, 2);
    if (gOwners.hadErrors()) {
      gOwners.tellErrors(sender);
    }
    AccountGroup gMembers = 
      new AccountGroup(this.handler.getPlugin(), members, sender, 2);
    if (gMembers.hadErrors()) {
      gMembers.tellErrors(sender);
    }
    for (AccountGroup.LocalAccount la : gOwners.getAccountList())
    {
      Account a = la.account;
      bank.addOwner((PlayerAccount)a);
      a.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + "Bank account '" + 
        bank.getName() + "' has been created with you listed as an owner.");
      if (gMembers.getAccountList().contains(a)) {
        gMembers.getAccountList().remove(a);
      }
    }
    for (AccountGroup.LocalAccount la : gMembers.getAccountList())
    {
      Account a = la.account;
      bank.addMember((PlayerAccount)a);
      a.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + "Bank account '" + 
        bank.getName() + "' has been created with you listed as a member.");
    }
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Created bank account '" + bank.getName() + "'.");
  }
  
  public void bankRemoveCommand(CommandHandler.BOSCommandSender sender, String bankName)
  {
    BankAccount bank = getBankAccountOrBust(sender, bankName);
    if (bank == null) {
      return;
    }
    bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
      "Bank account '" + bank.getName() + "' has been removed by an administrator.");
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed bank account '" + 
      bank.getName() + "'.");
    this.handler.getPlugin().getAccountManager().removeAccount(bank);
  }
  
  public void bankRenameCommand(CommandHandler.BOSCommandSender sender, String bankName, String newName)
  {
    BankAccount bank = getBankAccountOrBust(sender, bankName);
    if (bank == null) {
      return;
    }
    boolean adminAccess = this.handler.hasPermission(sender, "BOSEconomy.admin.bank.rename");
    boolean ownerAccess = (sender.isPlayer()) && (bank.isOwner(sender.getAsPlayer()));
    if ((adminAccess) || (ownerAccess))
    {
      if ((newName == null) || (newName.length() == 0))
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Use a valid bank account name.");
        return;
      }
      if (newName.equals(bank.getName()))
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "The bank account is already named '" + newName + "'.");
        return;
      }
      if (this.handler.getPlugin().getAccountManager().getBankAccountByName(newName) != null)
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "A bank account named '" + 
          newName + "' already exists.");
        return;
      }
      String ownerOrUser = (!sender.isPlayer()) || (!ownerAccess) ? 
        "an administrator" : sender.getAsPlayer().getName();
      bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
        "Bank account " + bank.getName() + " has been renamed to '" + newName + 
        "' by " + ownerOrUser + ".");
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Renamed bank account '" + 
        bank.getName() + "' to '" + newName + "'.");
      

      bank.rename(newName);
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You are not allowed to rename the '" + 
        bank.getName() + "' bank account.");
    }
  }
  
  public void bankListCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    Iterator<Account> iterator = 
      this.handler.getPlugin().getAccountManager().getAccountIterator();
    if (sender.isPlayer())
    {
      ArrayList<String> list = new ArrayList();
      while (iterator.hasNext())
      {
        Account a = (Account)iterator.next();
        if ((a instanceof BankAccount)) {
          list.add("  " + a.getName() + " (" + BOSEconomy.MONEY_COLOR + 
            formatMoney(a.getMoney()) + ChatColor.WHITE + ")");
        }
      }
      sendListMessage(sender, list.toArray(), list.size(), pageArg, 
        "Bank Accounts", 8, false);
    }
    else if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - Bank Accounts");
      while (iterator.hasNext())
      {
        Account a = (Account)iterator.next();
        if ((a instanceof BankAccount)) {
          sender.sendMsgInfo("  " + a.getName() + " (" + BOSEconomy.MONEY_COLOR + 
            formatMoney(a.getMoney()) + ChatColor.WHITE + ")");
        }
      }
    }
  }
  
  public void bankInfoCommand(CommandHandler.BOSCommandSender sender, String bankName, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    BankAccount bank = getBankAccountOrBust(sender, bankName);
    if (bank == null) {
      return;
    }
    boolean canView = 
      this.handler.hasPermission(sender, "BOSEconomy.admin.bank.info");
    if ((!canView) && (sender.isPlayer())) {
      canView = bank.isMemberOrOwner(sender.getAsPlayer());
    }
    if (canView)
    {
      incomeCommandFeedback(sender, bank, pageArg, 6, bank.getName() + 
        " Income Brackets");
      sender.sendMsgInfo(BOSEconomy.GOOD_COLOR + bank.getName() + 
        " account balance: " + bank.getMoneyObject().toString(true));
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You aren't allowed to view information about the '" + 
        bank.getName() + "' bank account.");
    }
  }
  
  public void bankMasteryCommand(CommandHandler.BOSCommandSender sender, String bankName, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    BankAccount bank = getBankAccountOrBust(sender, bankName);
    if (bank == null) {
      return;
    }
    boolean canView = 
      this.handler.hasPermission(sender, "BOSEconomy.admin.bank.mastery");
    if ((!canView) && (sender.isPlayer())) {
      canView = bank.isMemberOrOwner(sender.getAsPlayer());
    }
    if (canView) {
      masteryCommandFeedback(sender, bank, pageArg, bank.getName() + 
        " Bracket Mastery");
    } else {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "You aren't allowed to view the bracket mastery of the '" + 
        bank.getName() + "' bank account.");
    }
  }
  
  public void bankListmembersCommand(CommandHandler.BOSCommandSender sender, String name, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(name);
    if (bank == null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Could not find a bank account named '" + name + "'.");
      return;
    }
    boolean canView = 
      this.handler.hasPermission(sender, "BOSEconomy.admin.bank.listmembers");
    if ((!canView) && (sender.isPlayer())) {
      canView = bank.isMemberOrOwner(sender.getAsPlayer());
    }
    if (canView)
    {
      List<BankAccount.BankMember> memberList = bank.getMemberList();
      Collections.sort(memberList, new BankAccount.MemberNameComparator());
      if (sender.isConsole())
      {
        sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - " + bank.getName() + 
          " Account Members");
        for (BankAccount.BankMember bm : memberList) {
          if (bm.isOwner()) {
            sender.sendMsgInfo("  " + bm.getAccount().getName() + " (Owner)");
          }
        }
        for (BankAccount.BankMember bm : memberList) {
          if (!bm.isOwner()) {
            sender.sendMsgInfo("  " + bm.getAccount().getName());
          }
        }
      }
      else if (sender.isPlayer())
      {
        String[] list = new String[bank.getMemberAndOwnerCount()];
        int listIndex = 0;
        for (BankAccount.BankMember bm : memberList) {
          if (bm.isOwner())
          {
            list[listIndex] = ("  " + bm.getAccount().getName() + " (Owner)");
            listIndex++;
            if (listIndex >= list.length) {
              break;
            }
          }
        }
        for (BankAccount.BankMember bm : memberList) {
          if (!bm.isOwner())
          {
            list[listIndex] = ("  " + bm.getAccount().getName());
            listIndex++;
            if (listIndex >= list.length) {
              break;
            }
          }
        }
        sendListMessage(sender, list, listIndex, pageArg, bank.getName() + 
          " Bank Account Members", 8, false);
      }
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You aren't allow to view the members of the '" + 
        bank.getName() + "' bank account.");
    }
  }
  
  public void bankWithdrawCommand(CommandHandler.BOSCommandSender sender, String name, double value)
  {
    if (value <= 0.0D)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Withdrawls must use values greater than zero.");
      return;
    }
    if (isBadDouble(sender, value)) {
      return;
    }
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(name);
    if (bank == null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Could not find a bank account named '" + name + "'.");
      return;
    }
    if (value > bank.getMoney())
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The " + bank.getName() + 
        " bank account cannot afford that withdrawl.");
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + bank.getName() + 
        " account balance: " + bank.getMoneyObject().toString(false));
      return;
    }
    PlayerAccount account = null;
    if (sender.isPlayer()) {
      account = this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
        sender.getAsPlayer().getName());
    }
    boolean adminAccess = this.handler.hasPermission(sender, "BOSEconomy.admin.bank.withdraw");
    boolean memberAccess = account == null ? false : bank.isMemberOrOwner(account);
    if ((memberAccess) || (adminAccess))
    {
      if (account != null) {
        account.addMoney(value);
      }
      bank.addMoney(-value);
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "You withdrew " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " from bank account '" + bank.getName() + "'.");
      if (account != null) {
        account.tellMoney();
      }
      if ((adminAccess) && (!memberAccess)) {
        bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "An administrator withdrew " + 
          getMoneyFormatter().generateString(
          value, BOSEconomy.MONEY_COLOR, BOSEconomy.NEUTRAL_COLOR) + 
          " from bank account '" + bank.getName() + "'.");
      } else {
        bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
          "Player '" + account.getName() + "' withdrew " + 
          getMoneyFormatter().generateString(value, BOSEconomy.MONEY_COLOR, 
          BOSEconomy.NEUTRAL_COLOR) + 
          " from bank account '" + bank.getName() + "'.");
      }
      bank.tellMoney();
    }
    else
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You cannot withdraw from the " + 
        bank.getName() + " bank account.");
    }
  }
  
  public void bankDepositCommand(CommandHandler.BOSCommandSender sender, String name, double value)
  {
    if (value <= 0.0D)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Deposits must use values greater than zero.");
      return;
    }
    if (isBadDouble(sender, value)) {
      return;
    }
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(name);
    if (bank == null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Could not find bank account '" + 
        name + "'.");
      return;
    }
    PlayerAccount account = null;
    if (sender.isPlayer()) {
      account = this.handler.getPlugin().getAccountManager().getPlayerAccountByName(
        sender.getAsPlayer().getName());
    }
    if ((account != null) && (value > account.getMoney()))
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You can't afford to deposit " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR) + ".");
      account.tellMoney();
      return;
    }
    if (account != null)
    {
      if (account.denyPayCommand())
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Please wait a few seconds between payments.");
        return;
      }
      account.sentPayCommand();
    }
    if (account != null) {
      account.addMoney(-value);
    }
    bank.addMoney(value);
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "You deposited " + 
      getMoneyFormatter().generateString(
      value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
      " into bank account '" + bank.getName() + "'.");
    if (account != null)
    {
      account.tellMoney();
      bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "Player '" + account.getName() + "' deposited " + 
        getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " into bank account '" + bank.getName() + "'.");
    }
    else
    {
      bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + 
        "An administrator deposited " + getMoneyFormatter().generateString(
        value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR) + 
        " into bank account '" + bank.getName() + "'.");
    }
    bank.tellMoney();
  }
  
  public void bankAddownerCommand(CommandHandler.BOSCommandSender sender, String bank, String player)
  {
    bankAddplayer(sender, bank, player, true);
  }
  
  public void bankAddmemberCommand(CommandHandler.BOSCommandSender sender, String bank, String player)
  {
    bankAddplayer(sender, bank, player, false);
  }
  
  private void bankAddplayer(CommandHandler.BOSCommandSender sender, String bankName, String playerNames, boolean addOwner)
  {
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(bankName);
    if (bank == null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Could not find a bank account named '" + bankName + "'.");
      return;
    }
    boolean adminAccess = 
      ((addOwner) && 
      (this.handler.hasPermission(sender, "BOSEconomy.admin.bank.addowner"))) || (
      (!addOwner) && 
      (this.handler.hasPermission(sender, "BOSEconomy.admin.bank.addmember")));
    boolean ownerAccess = 
      (sender.isPlayer()) && (bank.isOwner(sender.getAsPlayer()));
    if ((ownerAccess) || (adminAccess))
    {
      AccountGroup g = 
        new AccountGroup(this.handler.getPlugin(), playerNames, sender, 
        2);
      if (g.hadErrors()) {
        g.tellErrors(sender);
      }
      if (g.getSize() == 0) {
        return;
      }
      String ownerOrMember = addOwner ? "an owner" : "a member";
      int FLAG_ACCEPT = 0;
      int FLAG_REJECT = 1;
      for (AccountGroup.LocalAccount la : g.getAccountList())
      {
        PlayerAccount player = (PlayerAccount)la.account;
        if (addOwner)
        {
          if (bank.isOwner(player))
          {
            la.flag = 1;
            continue;
          }
        }
        else if (bank.isMember(player))
        {
          la.flag = 1;
          continue;
        }
        player.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.GOOD_COLOR + "You've been made " + ownerOrMember + 
          " of bank");
        if (sender.isPlayer()) {
          player.sendMessage(BOSEconomy.GOOD_COLOR + 
            "  account " + 
            bank.getName() + 
            " by " + (
            (adminAccess) && (!ownerAccess) ? "an administrator" : 
            sender.getAsPlayer().getName()) + ".");
        } else {
          player.sendMessage(BOSEconomy.GOOD_COLOR + "  account " + 
            bank.getName() + " by an administrator.");
        }
      }
      String ownerOrMemberAccept;
      String ownerOrMemberAccept;
      if (g.oneAccount(0)) {
        ownerOrMemberAccept = addOwner ? "an owner" : "a member";
      } else {
        ownerOrMemberAccept = addOwner ? "owners" : "members";
      }
      String ownerOrMemberReject;
      String ownerOrMemberReject;
      if (g.oneAccount(1)) {
        ownerOrMemberReject = addOwner ? "an owner" : "a member";
      } else {
        ownerOrMemberReject = addOwner ? "owners" : "members";
      }
      if (g.getSize(0) > 0)
      {
        sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Added " + 
          g.getAccountString(false, true, 0) + " as " + 
          ownerOrMemberAccept + " of bank account '" + 
          bank.getName() + "'.");
        bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + (
          (sender.isPlayer()) && (ownerAccess) ? "Player " + sender.getAsPlayer().getName() : 
          "An administrator") + " added " + g.getAccountString(false, true, 0) + 
          " as " + ownerOrMemberAccept + " of bank account '" + bank.getName() + "'.", 
          sender.isPlayer() ? sender.getAsPlayer() : null);
      }
      if (g.getSize(1) > 0) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Failed to add " + 
          g.getAccountString(false, true, 1) + ". " + (
          g.oneAccount(1) ? "Account is" : "Accounts are") + 
          " already " + ownerOrMemberReject + " of bank account '" + 
          bank.getName() + "'.");
      }
      if (addOwner) {
        for (AccountGroup.LocalAccount la : g.getAccountList(0)) {
          bank.addOwner((PlayerAccount)la.account);
        }
      } else {
        for (AccountGroup.LocalAccount la : g.getAccountList(0)) {
          bank.addMember((PlayerAccount)la.account);
        }
      }
    }
    else if (sender.isPlayer())
    {
      if (bank.isMember(sender.getAsPlayer())) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "Only owners can add " + (
          addOwner ? "other owners" : "members") + " to bank accounts.");
      } else {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You are not an owner of the " + bank.getName() + 
          " bank account.");
      }
    }
  }
  
  public void bankRemovememberCommand(CommandHandler.BOSCommandSender sender, String bankName, String playerNames)
  {
    BankAccount bank = 
      this.handler.getPlugin().getAccountManager().getBankAccountByName(bankName);
    if (bank == null)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Could not find a bank account named '" + bankName + "'.");
      return;
    }
    boolean adminAccess = this.handler.hasPermission(sender, "BOSEconomy.admin.bank.removemember");
    boolean ownerAccess = (sender.isPlayer()) && (bank.isOwner(sender.getAsPlayer()));
    if ((ownerAccess) || (adminAccess))
    {
      AccountGroup g = 
        new AccountGroup(this.handler.getPlugin(), playerNames, sender, 
        2);
      if (g.hadErrors()) {
        g.tellErrors(sender);
      }
      if (g.getSize() == 0) {
        return;
      }
      for (AccountGroup.LocalAccount la : g.getAccountList())
      {
        PlayerAccount member = (PlayerAccount)la.account;
        if (bank.isMemberOrOwner(member))
        {
          if (sender.isPlayer()) {
            member.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.BAD_COLOR + 
              "You've been removed from bank account " + bank.getName() + " by " + (
              (adminAccess) && (!ownerAccess) ? "an administrator" : 
              sender.getAsPlayer().getName()) + ".");
          } else {
            member.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.BAD_COLOR + 
              "You've been removed from bank account " + bank.getName() + 
              " by an administrator.");
          }
          bank.removeMemberOrOwner(member);
        }
      }
      sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "Removed " + 
        g.getAccountString(false, true) + " from bank account '" + bank.getName() + "'.");
      bank.sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + (
        (!sender.isPlayer()) || ((adminAccess) && (!ownerAccess)) ? "An administrator" : 
        sender.getAsPlayer().getName()) + 
        " removed " + g.getAccountString(false, false) + " from bank account '" + 
        bank.getName() + "'.", sender.isPlayer() ? sender.getAsPlayer() : null);
    }
    else if (sender.isPlayer())
    {
      if (bank.isMember(sender.getAsPlayer())) {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "Only owners can remove members from bank accounts.");
      } else {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "You are not an owner of the '" + 
          bank.getName() + "' bank account.");
      }
    }
  }
  
  public void requestListCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + "The server console does not have requests.");
      return;
    }
    if (sender.isPlayer())
    {
      PlayerAccount account = 
        this.handler.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
      
      ArrayList<String> list = new ArrayList();
      
      Iterator localIterator = this.handler.getPlugin().getRequestHandler().getReceivedRequests(account).iterator();
      while (localIterator.hasNext())
      {
        Request r = (Request)localIterator.next();
        
        list.add("  " + BOSEconomy.INFO_COLOR + r.getId() + ". (Received) " + 
          ChatColor.WHITE + r.getInfoAsReceiver());
      }
      for (Request r : this.handler.getPlugin().getRequestHandler().getSentRequests(
        account)) {
        list.add("  " + BOSEconomy.INFO_COLOR + r.getId() + ". (Sent) " + 
          ChatColor.WHITE + r.getInfoAsSender());
      }
      sendListMessage(sender, list.toArray(), list.size(), pageArg, 
        "Your Requests", 4, false);
    }
  }
  
  public void requestDoCommand(CommandHandler.BOSCommandSender sender, String requestArg, String action)
  {
    if (sender.isBlock()) {
      return;
    }
    int id = 0;
    try
    {
      id = Integer.parseInt(requestArg);
    }
    catch (Exception ex)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "The request ID should be a whole number.");
      return;
    }
    if (sender.isConsole())
    {
      sender.sendMsgInfo("The server console cannot interact with requests.");
      return;
    }
    if (sender.isPlayer())
    {
      PlayerAccount account = 
        this.handler.getPlugin().getAccountManager().getPlayerAccount(sender.getAsPlayer());
      
      Request request = 
        this.handler.getPlugin().getRequestHandler().getRequestById(id);
      if ((request == null) || (
        (!request.isReceiver(account)) && (!request.isSender(account))))
      {
        sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
          "You have no request with ID number " + id + ".");
        return;
      }
      request.handleResponse(account, action);
    }
  }
  
  public void paydayResetCommand(CommandHandler.BOSCommandSender sender)
  {
    if (this.handler.getPlugin().getSettingsManager().getPaydayInterval()
      .getSeconds() == 0)
    {
      sender.sendMsgInfo(BOSEconomy.BAD_COLOR + 
        "Payday mode is not currently enabled.");
      return;
    }
    this.handler.getPlugin().getBracketManager().setLastPayday(
      System.currentTimeMillis());
    sender.sendMsgCopy(BOSEconomy.GOOD_COLOR + "The payday timer has been reset.");
  }
  
  public void aboutCommand(CommandHandler.BOSCommandSender sender)
  {
    if (sender.isBlock()) {
      return;
    }
    sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy v" + ChatColor.WHITE + 
      "0.7.8.1");
  }
  
  public void helpCommand(CommandHandler.BOSCommandSender sender, String pageArg)
  {
    if (sender.isBlock()) {
      return;
    }
    if (sender.isConsole())
    {
      sender.sendMsgInfo(BOSEconomy.PLUGIN_COLOR + "BOSEconomy" + " - Help");
      for (CommandHandler.BOSCommand c : this.handler.getCommands())
      {
        sender.sendMsgInfo(BOSEconomy.INFO_COLOR + c.getUsageText());
        sender.sendMsgInfo("  " + c.getHelpText());
      }
    }
    else if (sender.isPlayer())
    {
      String[] list = new String[2 * this.handler.getCommands().size()];
      int listIndex = 0;
      for (CommandHandler.BOSCommand c : this.handler.getCommands()) {
        if (c.canUse(sender))
        {
          list[listIndex] = (BOSEconomy.INFO_COLOR + c.getUsageText());
          list[(listIndex + 1)] = ("  " + c.getHelpText());
          listIndex += 2;
        }
      }
      sendListMessage(sender, list, listIndex, pageArg, "Help", 8, false);
    }
  }
}

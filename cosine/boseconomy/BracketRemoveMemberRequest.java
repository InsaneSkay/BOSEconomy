package cosine.boseconomy;

class BracketRemoveMemberRequest
  extends Request
{
  private Bracket bracket;
  private Account receiver;
  
  public BracketRemoveMemberRequest(RequestHandler handler, Bracket bracket, Account receiver)
  {
    super(handler);
    if ((bracket == null) || (receiver == null)) {
      throw new NullPointerException();
    }
    this.bracket = bracket;
    this.receiver = receiver;
  }
  
  public void handleResponse(Account account, String action)
  {
    if (account == null) {
      return;
    }
    if (!isValid())
    {
      account.sendMessage(BOSEconomy.BAD_COLOR + 
        "This request is no longer valid.");
      this.handler.removeRequest(this);
      return;
    }
    boolean isReceiver = isReceiver(account);
    boolean isSender = isSender(account);
    if ((!isReceiver) && (!isSender)) {
      return;
    }
    action = action.toLowerCase();
    if ((isSender) && 
      (action.equals("cancel")))
    {
      account.sendMessage(BOSEconomy.GOOD_COLOR + "Request cancelled.");
      this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
        BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + 
        "' cancelled a request for " + (
        (this.receiver instanceof BankAccount) ? "bank account '" : "user '") + 
        this.receiver.getName() + "' to leave the " + this.bracket.getName() + " " + 
        this.bracket.getType() + " bracket.");
      this.receiver.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
        BOSEconomy.NEUTRAL_COLOR + "A request for " + (
        (this.receiver instanceof BankAccount) ? "bank account " + 
        this.receiver.getName() : "you") + " to leave the " + 
        this.bracket.getName() + " " + this.bracket.getType() + 
        " bracket has been cancelled.");
      this.handler.removeRequest(this);
      return;
    }
    if (isReceiver)
    {
      if (action.equals("accept"))
      {
        account.sendMessage(BOSEconomy.GOOD_COLOR + 
          "Request accepted. " + (
          (this.receiver instanceof BankAccount) ? "Bank account " + 
          this.receiver.getName() + " has" : "You have") + " been removed from the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + "' accepted a request " + (
          (this.receiver instanceof BankAccount) ? "for bank account '" + 
          this.receiver.getName() + "'" : "") + " to leave the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        if ((this.receiver instanceof BankAccount)) {
          ((BankAccount)this.receiver).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + "A request for bank account " + 
            this.receiver.getName() + " to leave the " + this.bracket.getName() + " " + this.bracket.getType() + 
            " bracket has been accepted.", (account instanceof PlayerAccount) ? 
            (PlayerAccount)account : null);
        }
        if (this.bracket.getMaster() != null) {
          this.bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.GOOD_COLOR + "User " + account.getName() + " accepted a request " + (
            (this.receiver instanceof BankAccount) ? "for bank account " + this.receiver.getName() : "") + 
            "to leave the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        }
        this.handler.removeRequest(this);
        this.bracket.removeMember(this.receiver);
        return;
      }
      if (action.equals("reject"))
      {
        account.sendMessage(BOSEconomy.GOOD_COLOR + "Request rejected.");
        this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + "' rejected a request " + (
          (this.receiver instanceof BankAccount) ? "for bank account '" + 
          this.receiver.getName() + "'" : "") + " to leave the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        if ((this.receiver instanceof BankAccount)) {
          ((BankAccount)this.receiver).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + "A request for bank account " + 
            this.receiver.getName() + " to leave the " + this.bracket.getName() + 
            " " + this.bracket.getType() + " bracket has been rejected.", 
            (account instanceof PlayerAccount) ? (PlayerAccount)account : null);
        }
        if (this.bracket.getMaster() != null) {
          this.bracket.getMaster().sendMessage(
            BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.NEUTRAL_COLOR + 
            "User " + 
            account.getName() + 
            " rejected a request " + (
            (this.receiver instanceof BankAccount) ? "for bank account " + 
            this.receiver.getName() : "") + " to leave the " + 
            this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        }
        this.handler.removeRequest(this);
        return;
      }
    }
    String actionList = null;
    if (isSender)
    {
      if (isReceiver) {
        actionList = "'accept', 'reject', or 'cancel'";
      } else {
        actionList = "'cancel'";
      }
    }
    else if (isReceiver) {
      actionList = "'accept' or 'reject'";
    }
    account.sendMessage(BOSEconomy.BAD_COLOR + "Unrecognized action '" + action + "'.");
    account.sendMessage(BOSEconomy.BAD_COLOR + "You can only " + actionList + " this request.");
  }
  
  public boolean isReceiver(Account account)
  {
    return (this.receiver == account) || (
      ((this.receiver instanceof BankAccount)) && ((account instanceof PlayerAccount)) && 
      (((BankAccount)this.receiver).isOwner((PlayerAccount)account)));
  }
  
  public boolean isSender(Account account)
  {
    return this.bracket.hasMasterAccess(account);
  }
  
  public boolean isValid()
  {
    return (this.handler.getPlugin().getBracketManager().containsBracket(this.bracket)) && 
      (this.bracket.isMember(this.receiver));
  }
  
  public String checkAdditionConflict(Request other)
  {
    if (((other instanceof BracketRemoveMemberRequest)) && 
      (((BracketRemoveMemberRequest)other).getBracket() == this.bracket) && 
      (((BracketRemoveMemberRequest)other).getReceiver() == this.receiver)) {
      return 
      
        ((this.receiver instanceof BankAccount) ? "Bank account " : "User ") + this.receiver.getName() + " already has a request to leave the " + this.bracket.getName() + " bracket.";
    }
    if (((other instanceof BracketPlayerLeaveRequest)) && 
      (((BracketPlayerLeaveRequest)other).getBracket() == this.bracket) && 
      (((BracketPlayerLeaveRequest)other).getSender() == this.receiver)) {
      return 
      
        "User " + this.receiver.getName() + " has already requested to join the " + this.bracket.getName() + " bracket.";
    }
    return null;
  }
  
  public String getInfoAsSender()
  {
    return getInfoAsOther();
  }
  
  public String getInfoAsReceiver()
  {
    return 
    


      "Request for " + ((this.receiver instanceof BankAccount) ? "bank account " + this.receiver.getName() : "you") + " to leave the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket. (" + this.bracket.getManager().getPlugin().getMoneyFormatter().generateString(this.bracket.getValue()) + " / " + this.bracket.getPaymentInterval().toString() + ")";
  }
  
  public String getInfoAsOther()
  {
    return 
    

      "Request for " + ((this.receiver instanceof BankAccount) ? "bank account " : "user ") + this.receiver.getName() + " to leave the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.";
  }
  
  public Bracket getBracket()
  {
    return this.bracket;
  }
  
  public Account getReceiver()
  {
    return this.receiver;
  }
}

package cosine.boseconomy;

class BracketBankJoinRequest
  extends Request
{
  private Bracket bracket;
  private BankAccount sender;
  
  public BracketBankJoinRequest(RequestHandler handler, Bracket bracket, BankAccount sender)
  {
    super(handler);
    if ((bracket == null) || (sender == null)) {
      throw new NullPointerException();
    }
    this.bracket = bracket;
    this.sender = sender;
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
        "' cancelled a request from bank account " + this.sender.getName() + 
        " to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
      if (this.bracket.getMaster() != null) {
        this.bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
          "A request from bank account " + this.sender.getName() + " to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket has been cancelled.");
      }
      this.handler.removeRequest(this);
      return;
    }
    if (isReceiver)
    {
      if (action.equals("accept"))
      {
        account.sendMessage(BOSEconomy.GOOD_COLOR + 
          "Request accepted. Bank account " + this.sender.getName() + 
          " has been added to the " + this.bracket.getName() + " " + 
          this.bracket.getType() + " bracket.");
        this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + 
          "' accepted a request from bank account " + this.sender.getName() + 
          " to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        this.sender.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "A request from bank account " + 
          this.sender.getName() + " to join the " + this.bracket.getName() + " " + 
          this.bracket.getType() + " bracket has been accepted.");
        if (((account instanceof PlayerAccount)) && 
          ((this.bracket.getMaster() instanceof BankAccount))) {
          ((BankAccount)this.bracket.getMaster()).sendMessage(
            BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + "User " + 
            account.getName() + " accepted a request from bank account " + 
            this.sender.getName() + " to join the " + this.bracket.getName() + 
            " " + this.bracket.getType() + " bracket.", 
            (PlayerAccount)account);
        }
        this.handler.removeRequest(this);
        this.bracket.addMember(this.sender);
        return;
      }
      if (action.equals("reject"))
      {
        account.sendMessage(BOSEconomy.GOOD_COLOR + "Request rejected.");
        this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + 
          "' rejected a request from bank account " + this.sender.getName() + 
          " to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        this.sender.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "A request from bank account " + 
          this.sender.getName() + " to join the " + this.bracket.getName() + " " + 
          this.bracket.getType() + " bracket has been rejected.");
        if (((account instanceof PlayerAccount)) && 
          ((this.bracket.getMaster() instanceof BankAccount))) {
          ((BankAccount)this.bracket.getMaster()).sendMessage(
            BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.GOOD_COLOR + "User " + 
            account.getName() + " rejected a request for bank account " + 
            this.sender.getName() + " to join the " + this.bracket.getName() + 
            " " + this.bracket.getType() + " bracket.", 
            (PlayerAccount)account);
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
    account.sendMessage(BOSEconomy.BAD_COLOR + "Unrecognized action '" + action + 
      "'.");
    account.sendMessage(BOSEconomy.BAD_COLOR + "You can only " + actionList + 
      " this request.");
  }
  
  public boolean isReceiver(Account account)
  {
    return this.bracket.hasMasterAccess(account);
  }
  
  public boolean isSender(Account account)
  {
    return (account == this.sender) || (
      ((account instanceof PlayerAccount)) && 
      (this.sender.isOwner((PlayerAccount)account)));
  }
  
  public boolean isValid()
  {
    return (this.handler.getPlugin().getBracketManager().containsBracket(this.bracket)) && 
      (this.bracket.getMembersSize() < this.bracket.getMaximumMembers()) && (this.bracket.getMaster() != this.sender) && 
      (!this.bracket.isMember(this.sender));
  }
  
  public String checkAdditionConflict(Request other)
  {
    if (((other instanceof BracketBankJoinRequest)) && 
      (((BracketBankJoinRequest)other).getBracket() == this.bracket) && 
      (((BracketBankJoinRequest)other).getSender() == this.sender)) {
      return 
      
        "Bank account " + this.sender.getName() + " has already requested to join the " + this.bracket.getName() + " bracket.";
    }
    if (((other instanceof BracketAddMemberRequest)) && 
      (((BracketAddMemberRequest)other).getBracket() == this.bracket) && 
      (((BracketAddMemberRequest)other).getReceiver() == this.sender)) {
      return 
      
        "Bank account " + this.sender.getName() + " already has a request to join the " + this.bracket.getName() + " bracket.";
    }
    return null;
  }
  
  public String getInfoAsSender()
  {
    return getInfoAsOther();
  }
  
  public String getInfoAsReceiver()
  {
    return getInfoAsOther();
  }
  
  public String getInfoAsOther()
  {
    return 
      "Request from bank account " + this.sender.getName() + " to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.";
  }
  
  public Bracket getBracket()
  {
    return this.bracket;
  }
  
  public BankAccount getSender()
  {
    return this.sender;
  }
}

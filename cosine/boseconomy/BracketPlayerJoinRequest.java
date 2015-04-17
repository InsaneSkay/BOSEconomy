package cosine.boseconomy;

class BracketPlayerJoinRequest
  extends Request
{
  private Bracket bracket;
  private PlayerAccount sender;
  
  public BracketPlayerJoinRequest(RequestHandler handler, Bracket bracket, PlayerAccount sender)
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
        "' cancelled a request to join the " + 
        this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
      if (this.bracket.getMaster() != null) {
        this.bracket.getMaster().sendMessage(BOSEconomy.TAG_BLANK_COLOR + BOSEconomy.NEUTRAL_COLOR + 
          "A request from " + this.sender.getName() + " to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + 
          " bracket has been cancelled.");
      }
      this.handler.removeRequest(this);
      return;
    }
    if (isReceiver)
    {
      if (action.equals("accept"))
      {
        account.sendMessage(BOSEconomy.GOOD_COLOR + "Request accepted. User " + 
          this.sender.getName() + " has been added to the " + this.bracket.getName() + 
          " " + this.bracket.getType() + " bracket.");
        this.handler.getPlugin().sendConsoleMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "User '" + account.getName() + 
          "' accepted a request from " + this.sender.getName() + " to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        this.sender.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "Your request to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + 
          " bracket has been accepted.");
        if (((account instanceof PlayerAccount)) && 
          ((this.bracket.getMaster() instanceof BankAccount))) {
          ((BankAccount)this.bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.GOOD_COLOR + "User " + account.getName() + 
            " accepted a request from user " + this.sender.getName() + " to join the " + 
            this.bracket.getName() + " " + this.bracket.getType() + " bracket.", 
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
          "' rejected a request from " + this.sender.getName() + " to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + " bracket.");
        this.sender.sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
          BOSEconomy.NEUTRAL_COLOR + "Your request to join the " + 
          this.bracket.getName() + " " + this.bracket.getType() + 
          " bracket has been rejected.");
        if (((account instanceof PlayerAccount)) && 
          ((this.bracket.getMaster() instanceof BankAccount))) {
          ((BankAccount)this.bracket.getMaster()).sendMessage(BOSEconomy.TAG_BLANK_COLOR + 
            BOSEconomy.GOOD_COLOR + "User " + account.getName() + 
            " rejected a request from user " + this.sender.getName() + " to join the " + 
            this.bracket.getName() + " " + this.bracket.getType() + " bracket.", 
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
    account.sendMessage(BOSEconomy.BAD_COLOR + "Unrecognized action '" + action + "'.");
    account.sendMessage(BOSEconomy.BAD_COLOR + "You can only " + actionList + " this request.");
  }
  
  public boolean isReceiver(Account account)
  {
    return this.bracket.hasMasterAccess(account);
  }
  
  public boolean isSender(Account account)
  {
    return account == this.sender;
  }
  
  public boolean isValid()
  {
    return (this.handler.getPlugin().getBracketManager().containsBracket(this.bracket)) && 
      ((this.bracket.getMaximumMembers() < 0) || (this.bracket.getMembersSize() < this.bracket.getMaximumMembers())) && 
      (this.bracket.getMaster() != this.sender) && (!this.bracket.isMember(this.sender));
  }
  
  public String checkAdditionConflict(Request other)
  {
    if (((other instanceof BracketPlayerJoinRequest)) && 
      (((BracketPlayerJoinRequest)other).getBracket() == this.bracket) && 
      (((BracketPlayerJoinRequest)other).getSender() == this.sender)) {
      return 
      
        "User " + this.sender.getName() + " has already requested to join the " + this.bracket.getName() + " bracket.";
    }
    if (((other instanceof BracketAddMemberRequest)) && 
      (((BracketAddMemberRequest)other).getBracket() == this.bracket) && 
      (((BracketAddMemberRequest)other).getReceiver() == this.sender)) {
      return 
      
        "User " + this.sender.getName() + " already has a request to join the " + this.bracket.getName() + " bracket.";
    }
    return null;
  }
  
  public String getInfoAsSender()
  {
    return 
      "Request from you to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.";
  }
  
  public String getInfoAsReceiver()
  {
    return getInfoAsOther();
  }
  
  public String getInfoAsOther()
  {
    return 
      "Request from " + this.sender.getName() + " to join the " + this.bracket.getName() + " " + this.bracket.getType() + " bracket.";
  }
  
  public Bracket getBracket()
  {
    return this.bracket;
  }
  
  public PlayerAccount getSender()
  {
    return this.sender;
  }
}

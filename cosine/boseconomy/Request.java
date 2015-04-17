package cosine.boseconomy;

public abstract class Request
{
  private static int nextId = 1;
  protected final RequestHandler handler;
  private final int id;
  protected long expirationTime = 0L;
  
  public Request(RequestHandler handler)
  {
    this.handler = handler;
    this.id = nextId;
    nextId += 1;
  }
  
  public abstract void handleResponse(Account paramAccount, String paramString);
  
  public abstract boolean isReceiver(Account paramAccount);
  
  public abstract boolean isSender(Account paramAccount);
  
  public abstract boolean isValid();
  
  public abstract String checkAdditionConflict(Request paramRequest);
  
  public abstract String getInfoAsSender();
  
  public abstract String getInfoAsReceiver();
  
  public abstract String getInfoAsOther();
  
  public final boolean expired()
  {
    return (this.expirationTime > 0L) && (System.currentTimeMillis() >= this.expirationTime);
  }
  
  public final int getId()
  {
    return this.id;
  }
}

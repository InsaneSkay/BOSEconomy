package cosine.boseconomy;

public abstract class DatabaseManager
{
  protected boolean firstRefresh = true;
  private boolean changed = false;
  
  public final boolean getChanged()
  {
    return this.changed;
  }
  
  public final void setChanged()
  {
    this.changed = true;
  }
  
  public final void setChanged(boolean changed)
  {
    this.changed = changed;
  }
  
  public abstract void refresh(Database paramDatabase, String paramString);
  
  public abstract void commit(Database paramDatabase, String paramString);
}

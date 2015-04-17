package cosine.boseconomy;

import cosine.boseconomy.bracket.setting.BracketAddMemberSetting;
import cosine.boseconomy.bracket.setting.BracketRemoveMemberSetting;
import java.util.Iterator;
import java.util.List;

class WageBracket
  extends Bracket
{
  private boolean masterInDebt = false;
  
  public WageBracket(BracketManager manager, String name, double value)
  {
    super(manager, name, value);
    
    ((BracketAddMemberSetting)getSetting("add-member"))
      .setValue(Integer.valueOf(1));
    ((BracketRemoveMemberSetting)
      getSetting("remove-member"))
      .setValue(Integer.valueOf(1));
  }
  
  public WageBracket(BracketManager manager, String name)
  {
    super(manager, name);
    
    ((BracketAddMemberSetting)getSetting("add-member"))
      .setValue(Integer.valueOf(1));
    ((BracketRemoveMemberSetting)
      getSetting("remove-member"))
      .setValue(Integer.valueOf(1));
  }
  
  public void buildPaySession(long currentTime, PaySession paySession, boolean isPayday)
  {
    if ((!getDisabled()) && (getPaymentInterval().getSeconds() > 0))
    {
      if (this.lastTime == 0L)
      {
        setLastTime(currentTime);
        return;
      }
      List<Bracket.BracketMember> memberList = getMemberList();
      Bracket.BracketMember m;
      if (isPayday)
      {
        int timeIntervalSeconds = (int)((currentTime - this.lastTime) / 1000L);
        
        double moneyPerSecond = getValue() / getPaymentInterval().getSeconds();
        if (getOnlineMode())
        {
          for (Bracket.BracketMember m : memberList)
          {
            double payValue = m.getOnlineTime() * moneyPerSecond * m.getCount();
            
            m.setOnlineTime(0);
            
            paySession.addTransfer(getMaster(), payValue, m.getAccount());
          }
        }
        else
        {
          double basePayValue = timeIntervalSeconds * moneyPerSecond;
          for (Iterator localIterator2 = memberList.iterator(); localIterator2.hasNext();)
          {
            m = (Bracket.BracketMember)localIterator2.next();
            
            double payValue = basePayValue * m.getCount();
            
            paySession.addTransfer(getMaster(), payValue, m.getAccount());
          }
        }
        setLastTime(currentTime);
      }
      else
      {
        int timeIntervalSeconds = (int)((currentTime - this.lastTime) / 1000L);
        if (timeIntervalSeconds >= getPaymentInterval().getSeconds())
        {
          int payCount = 
            timeIntervalSeconds / getPaymentInterval().getSeconds();
          if (getOnlineMode())
          {
            double moneyPerSecond = 
              getValue() / getPaymentInterval().getSeconds();
            for (Bracket.BracketMember m : memberList)
            {
              double payValue = 
                m.getOnlineTime() * moneyPerSecond * m.getCount();
              
              m.setOnlineTime(0);
              
              paySession.addTransfer(getMaster(), payValue, m.getAccount());
            }
          }
          else
          {
            double basePayValue = getValue() * payCount;
            for (Bracket.BracketMember m : memberList)
            {
              double payValue = basePayValue * m.getCount();
              
              paySession.addTransfer(getMaster(), payValue, m.getAccount());
            }
          }
          setLastTime(this.lastTime + 
            1000L * payCount * getPaymentInterval().getSeconds());
        }
      }
    }
  }
  
  public boolean isUnfulfilling()
  {
    return this.masterInDebt;
  }
  
  public String getType()
  {
    return "wage";
  }
  
  public String getTypeCaps()
  {
    return "Wage";
  }
}

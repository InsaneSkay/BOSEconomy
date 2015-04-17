package cosine.boseconomy;

import cosine.boseconomy.bracket.setting.BracketAddMemberSetting;
import cosine.boseconomy.bracket.setting.BracketRemoveMemberSetting;
import java.util.Iterator;
import java.util.List;

class SubBracket
  extends Bracket
{
  public SubBracket(BracketManager manager, String name, double value)
  {
    super(manager, name, value);
    
    ((BracketAddMemberSetting)getSetting("add-member"))
      .setValue(Integer.valueOf(2));
    ((BracketRemoveMemberSetting)
      getSetting("remove-member"))
      .setValue(Integer.valueOf(1));
  }
  
  public SubBracket(BracketManager manager, String name)
  {
    super(manager, name);
    
    ((BracketAddMemberSetting)getSetting("add-member"))
      .setValue(Integer.valueOf(2));
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
            
            paySession.addTransfer(m.getAccount(), payValue, getMaster());
          }
        }
        else
        {
          double basePayValue = timeIntervalSeconds * moneyPerSecond;
          for (Iterator localIterator2 = memberList.iterator(); localIterator2.hasNext();)
          {
            m = (Bracket.BracketMember)localIterator2.next();
            
            double payValue = basePayValue * m.getCount();
            
            paySession.addTransfer(m.getAccount(), payValue, getMaster());
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
              
              paySession.addTransfer(m.getAccount(), payValue, getMaster());
            }
          }
          else
          {
            double basePayValue = getValue() * payCount;
            for (Bracket.BracketMember m : memberList)
            {
              double payValue = basePayValue * m.getCount();
              
              paySession.addTransfer(m.getAccount(), payValue, getMaster());
            }
          }
          setLastTime(this.lastTime + 
            1000L * payCount * getPaymentInterval().getSeconds());
        }
      }
    }
  }
  
  public String getType()
  {
    return "subscription";
  }
  
  public String getTypeCaps()
  {
    return "Subscription";
  }
}

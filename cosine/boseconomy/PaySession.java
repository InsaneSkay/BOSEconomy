package cosine.boseconomy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class PaySession
{
  private final BOSEconomy plugin;
  private List<Node> list;
  private boolean isPayday = false;
  
  public PaySession(BOSEconomy plugin)
  {
    this.plugin = plugin;
    this.list = new LinkedList();
  }
  
  public void addTransfer(Account from, double value, Account to)
  {
    Node nFrom = getNode(from);
    Node nTo = getNode(to);
    NodeTrans t = new NodeTrans(nFrom, value, nTo);
    nFrom.addTrans(t);
    nTo.addTrans(t);
  }
  
  public void run()
  {
    if (this.list.size() == 0) {
      return;
    }
    long nanoTime = 0L;
    






















    LinkedList<Node> tempList = new LinkedList();
    for (Node n : this.list) {
      tempList.add(n);
    }
    boolean progress = true;
    Object li;
    Node n;
    for (; (progress) && (tempList.size() > 0); ((ListIterator)li).hasNext())
    {
      progress = false;
      li = tempList.listIterator();
      continue;
      n = (Node)((ListIterator)li).next();
      if (n.payOut())
      {
        progress = true;
        ((ListIterator)li).remove();
      }
      else if (!n.hasInbound())
      {
        n.removeAllTransfers();
        
        ((ListIterator)li).remove();
      }
    }
    for (Node n : this.list) {
      if (n.account != null) {
        n.account.setPaymentMoney(n.money, this.isPayday);
      }
    }
  }
  
  public void setPayday(boolean isPayday)
  {
    this.isPayday = isPayday;
  }
  
  private Node getNode(Account a)
  {
    for (Node n : this.list) {
      if (n.account == a) {
        return n;
      }
    }
    Node newNode = new Node(a);
    this.list.add(newNode);
    return newNode;
  }
  
  private class Node
  {
    public final Account account;
    public double money;
    public final LinkedList<PaySession.NodeTrans> trans;
    
    public Node(Account account)
    {
      this.account = account;
      if (account == null) {
        this.money = 0.0D;
      } else {
        this.money = account.getMoney();
      }
      this.trans = new LinkedList();
    }
    
    public void addTrans(PaySession.NodeTrans t)
    {
      this.trans.add(t);
    }
    
    public boolean payOut()
    {
      double totalPayment = 0.0D;
      for (PaySession.NodeTrans t : this.trans) {
        if (t.from == this) {
          totalPayment += t.value;
        }
      }
      if ((totalPayment == 0.0D) || (totalPayment <= this.money) || (this.account == null))
      {
        Iterator<PaySession.NodeTrans> iterator = this.trans.iterator();
        while (iterator.hasNext())
        {
          PaySession.NodeTrans t = (PaySession.NodeTrans)iterator.next();
          if (t.from == this)
          {
            if (t.to.account != null) {
              t.to.addMoney(t.value);
            }
            if (this.account != null) {
              this.money -= t.value;
            }
            t.to.trans.remove(t);
            iterator.remove();
          }
        }
        return true;
      }
      if (this.money <= 0.0D) {
        return false;
      }
      double startMoney = this.money;
      for (PaySession.NodeTrans t : this.trans) {
        if (t.from == this)
        {
          double value = 
            PaySession.this.plugin.getMoneyFloored(startMoney * (t.value / totalPayment));
          if (t.to.account != null) {
            t.to.addMoney(value);
          }
          this.money -= value;
          t.value -= value;
        }
      }
      double minimumUnit = Math.pow(10.0D, -PaySession.this.plugin.getFractionalDigits());
      for (PaySession.NodeTrans t : this.trans)
      {
        if (this.money <= 0.0D) {
          break;
        }
        if (t.from == this)
        {
          if (t.to.account != null) {
            t.to.addMoney(minimumUnit);
          }
          this.money -= minimumUnit;
        }
      }
      if ((this.money > 0.0D) && 
        (PaySession.this.plugin.debug)) {
        PaySession.this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + this.money + 
          " not distributed due to rounding errors.");
      }
      return false;
    }
    
    public void removeAllTransfers()
    {
      for (PaySession.NodeTrans t : this.trans) {
        if (t.from == this) {
          t.to.trans.remove(t);
        } else {
          t.from.trans.remove(t);
        }
      }
      this.trans.clear();
    }
    
    public boolean hasInbound()
    {
      for (PaySession.NodeTrans t : this.trans) {
        if (t.to == this) {
          return true;
        }
      }
      return false;
    }
    
    public void addMoney(double money)
    {
      this.money += money;
    }
  }
  
  private class NodeTrans
  {
    public final PaySession.Node from;
    public double value;
    public final PaySession.Node to;
    
    public NodeTrans(PaySession.Node from, double value, PaySession.Node to)
    {
      if (value < 0.0D)
      {
        this.from = to;
        this.value = (-value);
        this.to = from;
      }
      else
      {
        this.from = from;
        this.value = value;
        this.to = to;
      }
    }
  }
}

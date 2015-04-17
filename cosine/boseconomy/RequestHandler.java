package cosine.boseconomy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RequestHandler
{
  private BOSEconomy plugin;
  private HashMap<Integer, Request> requests;
  
  public RequestHandler(BOSEconomy plugin)
  {
    this.plugin = plugin;
    this.requests = new HashMap();
  }
  
  public String addRequest(Request r)
  {
    if (r != null)
    {
      for (Request request : this.requests.values())
      {
        String conflict = r.checkAdditionConflict(request);
        if (conflict != null) {
          return conflict;
        }
      }
      this.requests.put(Integer.valueOf(r.getId()), r);
    }
    return null;
  }
  
  public void removeRequest(Request r)
  {
    this.requests.remove(Integer.valueOf(r.getId()));
  }
  
  public void checkRequestValidities()
  {
    Iterator<Request> iterator = this.requests.values().iterator();
    while (iterator.hasNext()) {
      if (!((Request)iterator.next()).isValid()) {
        iterator.remove();
      }
    }
  }
  
  public Request getRequestById(int id)
  {
    return (Request)this.requests.get(Integer.valueOf(id));
  }
  
  public List<Request> getSentRequests(Account account)
  {
    checkRequestValidities();
    LinkedList<Request> sent = new LinkedList();
    for (Request request : this.requests.values()) {
      if (request.isSender(account)) {
        sent.add(request);
      }
    }
    return sent;
  }
  
  public List<Request> getReceivedRequests(Account account)
  {
    checkRequestValidities();
    LinkedList<Request> received = new LinkedList();
    for (Request request : this.requests.values()) {
      if (request.isReceiver(account)) {
        received.add(request);
      }
    }
    return received;
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
  
  public static String getRequestViewMessage()
  {
    return "Use the command '/econ request list' to view your requests.";
  }
}

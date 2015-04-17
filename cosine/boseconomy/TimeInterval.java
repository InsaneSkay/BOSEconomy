package cosine.boseconomy;

import java.util.Scanner;

public class TimeInterval
{
  public static final TimeInterval NULL_INTERVAL = new TimeInterval(0);
  private final int time;
  private final int multiplier;
  private final int seconds;
  private final String unit;
  private final String string;
  
  public TimeInterval(int time, String unit)
  {
    this.time = time;
    this.multiplier = unitToSeconds(unit);
    this.seconds = (time * this.multiplier);
    

    this.unit = secondsToUnit(this.multiplier);
    this.string = createString();
  }
  
  public TimeInterval(int time, int multiplier)
  {
    this.time = time;
    this.multiplier = multiplier;
    this.seconds = (time * multiplier);
    this.unit = secondsToUnit(multiplier);
    this.string = createString();
  }
  
  public TimeInterval(int seconds)
  {
    this(seconds, 1);
  }
  
  public TimeInterval(String interval)
    throws TimeInterval.TimeIntervalFormatException
  {
    boolean goodFormat = false;
    String numberString = null;String unitString = null;
    Scanner scan = new Scanner(interval);
    if (scan.hasNext())
    {
      numberString = scan.next();
      if (scan.hasNext())
      {
        unitString = scan.next();
        if (!scan.hasNext()) {
          goodFormat = true;
        }
      }
    }
    scan.close();
    if (!goodFormat) {
      throw new TimeIntervalFormatException(
        "Expected input of the form '[number] [unit]'. Example: '4 hours'");
    }
    try
    {
      this.time = Integer.parseInt(numberString);
    }
    catch (NumberFormatException ex)
    {
      throw new TimeIntervalFormatException("Could not parse '" + numberString + 
        "' as a number.");
    }
    this.multiplier = unitToSeconds(unitString);
    if (this.multiplier == 0) {
      throw new TimeIntervalFormatException("Unrecognized time unit '" + unitString + "'.");
    }
    this.seconds = (this.time * this.multiplier);
    this.unit = secondsToUnit(this.multiplier);
    this.string = createString();
  }
  
  public int getTime()
  {
    return this.time;
  }
  
  public int getMultiplier()
  {
    return this.multiplier;
  }
  
  public int getSeconds()
  {
    return this.seconds;
  }
  
  public String getUnit()
  {
    return this.unit;
  }
  
  public String getString()
  {
    return this.string;
  }
  
  private String createString()
  {
    if (this.multiplier == 0) {
      return "none";
    }
    return this.time + " " + this.unit + ((this.time == 1) || (this.time == -1) ? "" : Character.valueOf('s'));
  }
  
  public String toString()
  {
    return getString();
  }
  
  public static String secondsToUnit(int seconds)
  {
    if (seconds == 1) {
      return "second";
    }
    if (seconds == 60) {
      return "minute";
    }
    if (seconds == 3600) {
      return "hour";
    }
    if (seconds == 86400) {
      return "day";
    }
    if (seconds == 604800) {
      return "week";
    }
    if (seconds == 0) {
      return "none";
    }
    return "unknown";
  }
  
  public static int unitToSeconds(String unit)
  {
    unit = unit.toLowerCase();
    if ((unit.equals("seconds")) || (unit.equals("second"))) {
      return 1;
    }
    if ((unit.equals("minutes")) || (unit.equals("minute"))) {
      return 60;
    }
    if ((unit.equals("hours")) || (unit.equals("hour"))) {
      return 3600;
    }
    if ((unit.equals("days")) || (unit.equals("day"))) {
      return 86400;
    }
    if ((unit.equals("weeks")) || (unit.equals("week"))) {
      return 604800;
    }
    return 0;
  }
  
  public class TimeIntervalFormatException
    extends Exception
  {
    private static final long serialVersionUID = -8241366064843480562L;
    
    public TimeIntervalFormatException(String message)
    {
      super();
    }
  }
  
  public class Units
  {
    public static final int SECOND = 1;
    public static final int MINUTE = 60;
    public static final int HOUR = 3600;
    public static final int DAY = 86400;
    public static final int WEEK = 604800;
    
    public Units() {}
  }
}

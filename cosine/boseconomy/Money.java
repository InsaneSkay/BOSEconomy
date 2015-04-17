package cosine.boseconomy;

class Money
{
  private final BOSEconomy plugin;
  private double value = 0.0D;
  private String text = null;
  private String textGoodColor = null;
  private String textBadColor = null;
  
  public Money(BOSEconomy plugin)
  {
    this(plugin, 0.0D);
  }
  
  public Money(BOSEconomy plugin, double value)
  {
    this.plugin = plugin;
    setValue(value);
  }
  
  public void setValue(double value)
  {
    value = this.plugin.getMoneyRounded(value);
    if (this.value != value)
    {
      this.text = null;
      this.textGoodColor = null;
      this.textBadColor = null;
      this.value = value;
    }
  }
  
  public void addValue(double value)
  {
    value = this.plugin.getMoneyRounded(value);
    if (value != 0.0D)
    {
      this.text = null;
      this.textGoodColor = null;
      this.textBadColor = null;
      this.value += value;
    }
  }
  
  public double getValue()
  {
    return this.value;
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
  
  public String toString()
  {
    return toString(null, null);
  }
  
  public String toString(boolean good)
  {
    if (good)
    {
      if (this.textGoodColor == null) {
        this.textGoodColor = this.plugin.getMoneyFormatter().generateString(
          this.value, BOSEconomy.MONEY_COLOR, BOSEconomy.GOOD_COLOR);
      }
      return this.textGoodColor;
    }
    if (this.textBadColor == null) {
      this.textBadColor = this.plugin.getMoneyFormatter().generateString(
        this.value, BOSEconomy.MONEY_COLOR, BOSEconomy.BAD_COLOR);
    }
    return this.textBadColor;
  }
  
  public String toString(String moneyColor, String nameColor)
  {
    if ((moneyColor == null) || (nameColor == null))
    {
      if (this.text == null) {
        this.text = this.plugin.getMoneyFormatter().generateString(this.value);
      }
      return this.text;
    }
    return this.plugin.getMoneyFormatter().generateString(this.value, moneyColor, nameColor);
  }
  
  public static String checkValidity(double value)
  {
    if (Double.isInfinite(value)) {
      return "The value cannot be infinity.";
    }
    if (Double.isNaN(value)) {
      return "The value cannot be NaN.";
    }
    return null;
  }
}

package cosine.boseconomy;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MoneyFormatter
{
  private final BOSEconomy plugin;
  private final DecimalFormat format;
  private final DecimalFormatSymbols formatSymbols;
  
  public MoneyFormatter(BOSEconomy plugin)
  {
    this.format = new DecimalFormat();
    this.formatSymbols = new DecimalFormatSymbols();
    this.format.setDecimalFormatSymbols(this.formatSymbols);
    this.plugin = plugin;
    refresh();
  }
  
  public void refresh()
  {
    this.format.setMinimumIntegerDigits(1);
    this.format.setMaximumFractionDigits(this.plugin.getSettingsManager().getFractionalDigits());
    this.format.setMinimumFractionDigits(this.plugin.getSettingsManager().getFractionalDigits());
    this.format.setGroupingSize(this.plugin.getSettingsManager().getMoneyDigitGrouping());
    this.format.setGroupingUsed(this.plugin.getSettingsManager().getMoneyDigitGrouping() > 0);
  }
  
  public String formatMoney(double value)
  {
    return this.format.format(value);
  }
  
  public double roundMoney(double value)
  {
    double mult = Math.pow(10.0D, this.plugin.getSettingsManager().getFractionalDigits());
    return Math.rint(value * mult) / mult;
  }
  
  public double floorMoney(double value)
  {
    double mult = 
      (value > 0.0D ? 1 : -1) * Math.pow(10.0D, this.plugin.getSettingsManager().getFractionalDigits());
    return (int)(value * mult) / mult;
  }
  
  public String generateString(double value)
  {
    return generateString(value, null, null);
  }
  
  public String generateString(double value, String moneyColor, String nameColor)
  {
    if ((moneyColor == null) || (nameColor == null)) {
      return formatMoney(value) + " " + this.plugin.getMoneyNameProper(value);
    }
    return 
      moneyColor + formatMoney(value) + nameColor + " " + this.plugin.getMoneyNameProper(value);
  }
}

package cosine.boseconomy;

public class SettingsManager
  extends DatabaseManager
{
  private static int MAX_FRACTIONAL_DIGITS = 6;
  private BOSEconomy plugin;
  private boolean resave;
  
  public static class Defaults
  {
    public static final boolean DEBUG = false;
    public static final String DEFAULT_BRACKET = "default";
    public static final TimeInterval DEFAULT_BRACKET_INTERVAL = new TimeInterval(1, 
      3600);
    public static final int FRACTIONAL_DIGITS = 0;
    public static final double INITIAL_MONEY = 100.0D;
    public static final String MONEY_NAME = "coin";
    public static final String MONEY_NAME_PLURAL = "coins";
    public static final int MONEY_DIGIT_GROUPING = 3;
    public static final TimeInterval PAYDAY_INTERVAL = new TimeInterval(1, 86400);
    public static final TimeInterval SAVE_INTERVAL = new TimeInterval(1, 60);
    public static final boolean SILENT_WAGES = false;
    public static final boolean USE_OP_PERMISSIONS = false;
    public static final boolean HIDE_VANISHED_PLAYERS = false;
    public static final boolean ALLOW_COMMAND_BLOCKS = false;
    public static final boolean SHOW_COMMAND_BLOCK_OUTPUT = false;
    public static final boolean SHOW_CONSOLE_COLORS = false;
  }
  
  private String defaultBracket = "default";
  private TimeInterval defaultBracketInterval = Defaults.DEFAULT_BRACKET_INTERVAL;
  private int fractionalDigits = 0;
  private double initialMoney = 100.0D;
  private String moneyName = "coin";
  private String moneyNamePlural = "coins";
  private String moneyNameCaps;
  private String moneyNamePluralCaps;
  private int moneyDigitGrouping = 3;
  private TimeInterval paydayInterval = Defaults.PAYDAY_INTERVAL;
  private TimeInterval saveInterval = Defaults.SAVE_INTERVAL;
  private boolean silentWages = false;
  private boolean useOpPermissions = false;
  private boolean hideVanishedPlayers = false;
  private boolean allowCommandBlocks = false;
  private boolean showCommandBlockOutput = false;
  private boolean showConsoleColors = false;
  
  public SettingsManager(BOSEconomy plugin)
  {
    this.plugin = plugin;
    formatMoneyName();
  }
  
  public void setInitialMoney(double initialMoney)
  {
    if ((Double.isInfinite(initialMoney)) || (Double.isNaN(initialMoney))) {
      this.initialMoney = 0.0D;
    } else {
      this.initialMoney = initialMoney;
    }
    setChanged();
  }
  
  public double getInitialMoney()
  {
    return this.initialMoney;
  }
  
  private void formatMoneyName()
  {
    if (this.moneyName == null) {
      this.moneyName = "";
    }
    if (this.moneyNamePlural == null) {
      this.moneyNamePlural = "";
    }
    if (this.moneyName.length() == 0) {
      this.moneyNameCaps = "";
    } else {
      this.moneyNameCaps = (this.moneyName.substring(0, 1).toUpperCase() + this.moneyName.substring(1));
    }
    if (this.moneyNamePlural.length() == 0) {
      this.moneyNamePluralCaps = "";
    } else {
      this.moneyNamePluralCaps = 
        (this.moneyNamePlural.substring(0, 1).toUpperCase() + this.moneyNamePlural.substring(1));
    }
  }
  
  public void setFractionalDigits(int fractionalDigits)
  {
    if (fractionalDigits != this.fractionalDigits)
    {
      if (fractionalDigits < 0) {
        this.fractionalDigits = 0;
      } else if (fractionalDigits > MAX_FRACTIONAL_DIGITS) {
        this.fractionalDigits = MAX_FRACTIONAL_DIGITS;
      } else {
        this.fractionalDigits = fractionalDigits;
      }
      this.plugin.getMoneyFormatter().refresh();
      setChanged();
    }
  }
  
  public int getFractionalDigits()
  {
    return this.fractionalDigits;
  }
  
  public void setDefaultBracketInterval(TimeInterval defaultBracketInterval)
  {
    if ((defaultBracketInterval == null) || (defaultBracketInterval.getTime() < 0)) {
      this.defaultBracketInterval = TimeInterval.NULL_INTERVAL;
    } else {
      this.defaultBracketInterval = defaultBracketInterval;
    }
    setChanged();
  }
  
  public TimeInterval getDefaultBracketInterval()
  {
    return this.defaultBracketInterval;
  }
  
  public void setPaydayInterval(TimeInterval paydayInterval)
  {
    if ((paydayInterval == null) || (paydayInterval.getTime() < 0)) {
      this.paydayInterval = TimeInterval.NULL_INTERVAL;
    } else {
      this.paydayInterval = paydayInterval;
    }
    setChanged();
  }
  
  public TimeInterval getPaydayInterval()
  {
    return this.paydayInterval;
  }
  
  public void setMoneyName(String name)
  {
    if (name == null) {
      this.moneyName = "";
    } else {
      this.moneyName = name;
    }
    setChanged();
  }
  
  public String getMoneyName()
  {
    return this.moneyName;
  }
  
  public void setMoneyNamePlural(String name)
  {
    if (name == null) {
      this.moneyNamePlural = "";
    } else {
      this.moneyNamePlural = name;
    }
    setChanged();
  }
  
  public String getMoneyNamePlural()
  {
    return this.moneyNamePlural;
  }
  
  public String getMoneyNameCaps()
  {
    return this.moneyNameCaps;
  }
  
  public String getMoneyNamePluralCaps()
  {
    return this.moneyNamePluralCaps;
  }
  
  public void setMoneyDigitGrouping(int moneyDigitGrouping)
  {
    if (this.moneyDigitGrouping != moneyDigitGrouping)
    {
      if ((moneyDigitGrouping < 0) || (moneyDigitGrouping > 127)) {
        this.moneyDigitGrouping = 0;
      } else {
        this.moneyDigitGrouping = moneyDigitGrouping;
      }
      this.plugin.getMoneyFormatter().refresh();
    }
  }
  
  public int getMoneyDigitGrouping()
  {
    return this.moneyDigitGrouping;
  }
  
  public String getDefaultBracket()
  {
    return this.defaultBracket;
  }
  
  public void setDefaultBracket(String defaultBracket)
  {
    if ((defaultBracket == null) || (defaultBracket.length() == 0)) {
      this.defaultBracket = null;
    } else {
      this.defaultBracket = defaultBracket;
    }
    setChanged();
  }
  
  public void setSaveInterval(TimeInterval saveInterval)
  {
    if ((saveInterval == null) || (saveInterval.getTime() < 0)) {
      this.saveInterval = TimeInterval.NULL_INTERVAL;
    } else {
      this.saveInterval = saveInterval;
    }
    setChanged();
  }
  
  public TimeInterval getSaveInterval()
  {
    return this.saveInterval;
  }
  
  public void setSilentWages(boolean silentWages)
  {
    this.silentWages = silentWages;
    setChanged();
  }
  
  public boolean getSilentWages()
  {
    return this.silentWages;
  }
  
  public void setUseOpPermissions(boolean opPermissions)
  {
    this.useOpPermissions = opPermissions;
    setChanged();
  }
  
  public boolean getUseOpPermissions()
  {
    return this.useOpPermissions;
  }
  
  public void setHideVanishedPlayers(boolean hideVanishedPlayers)
  {
    this.hideVanishedPlayers = hideVanishedPlayers;
    setChanged();
  }
  
  public boolean getHideVanishedPlayers()
  {
    return this.hideVanishedPlayers;
  }
  
  public void setAllowCommandBlocks(boolean allowCommandBlocks)
  {
    this.allowCommandBlocks = allowCommandBlocks;
    setChanged();
  }
  
  public boolean getAllowCommandBlocks()
  {
    return this.allowCommandBlocks;
  }
  
  public void setShowCommandBlockOutput(boolean showCommandBlockOutput)
  {
    this.showCommandBlockOutput = showCommandBlockOutput;
    setChanged();
  }
  
  public boolean getShowCommandBlockOutput()
  {
    return this.showCommandBlockOutput;
  }
  
  public void setShowConsoleColors(boolean showConsoleColors)
  {
    this.showConsoleColors = showConsoleColors;
    setChanged();
  }
  
  public boolean getShowConsoleColors()
  {
    return this.showConsoleColors;
  }
  
  public BOSEconomy getPlugin()
  {
    return this.plugin;
  }
  
  public TimeInterval refreshTimeInterval(DatabaseList list, String intervalName, TimeInterval defaultInterval)
  {
    String value = list.getString(intervalName);
    if (value == null) {
      return defaultInterval;
    }
    if (value.length() == 0)
    {
      this.resave = true;
      return TimeInterval.NULL_INTERVAL;
    }
    try
    {
      return new TimeInterval(value);
    }
    catch (TimeInterval.TimeIntervalFormatException ex)
    {
      this.plugin.sendConsoleMessage(BOSEconomy.TAG_WARNING_COLOR + 
        "Encountered improperly formatted " + intervalName + " '" + value + 
        "' in the settings file. Defaulting to '" + 
        defaultInterval.toString() + "'.");
      this.resave = true;
    }
    return defaultInterval;
  }
  
  public void refresh(Database database, String listName)
  {
    if (this.plugin.debug) {
      this.plugin.sendConsoleMessage("[BOSEconomy Debug] Refreshing settings from the database.");
    }
    DatabaseList list = database.getRoot().getOrCreateList(listName);
    







    this.resave = false;
    if (list.containsValueKey("debug"))
    {
      this.plugin.debug = list.getBoolean("debug");
    }
    else
    {
      this.plugin.debug = false;
      this.resave = true;
    }
    if (list.containsValueKey("default-bracket"))
    {
      setDefaultBracket(list.getString("default-bracket"));
    }
    else
    {
      this.defaultBracket = "default";
      this.resave = true;
    }
    setDefaultBracketInterval(refreshTimeInterval(list, 
      "default-bracket-interval", Defaults.DEFAULT_BRACKET_INTERVAL));
    if (list.containsValueKey("fractional-digits"))
    {
      setFractionalDigits(list.getInt("fractional-digits"));
    }
    else
    {
      this.fractionalDigits = 0;
      this.resave = true;
    }
    if (list.containsValueKey("initial-money"))
    {
      setInitialMoney(list.getDouble("initial-money"));
    }
    else
    {
      this.initialMoney = 100.0D;
      this.resave = true;
    }
    if (list.containsValueKey("money-name"))
    {
      setMoneyName(list.getString("money-name"));
    }
    else
    {
      this.moneyName = "coin";
      this.resave = true;
    }
    if (list.containsValueKey("money-name-plural"))
    {
      setMoneyNamePlural(list.getString("money-name-plural"));
    }
    else
    {
      this.moneyNamePlural = "coins";
      this.resave = true;
    }
    if (list.containsValueKey("money-digit-grouping"))
    {
      setMoneyDigitGrouping(list.getInt("money-digit-grouping"));
    }
    else
    {
      this.moneyDigitGrouping = 3;
      this.resave = true;
    }
    setPaydayInterval(refreshTimeInterval(list, "payday-interval", 
      Defaults.PAYDAY_INTERVAL));
    if (list.containsValueKey("silent-wages"))
    {
      setSilentWages(list.getBoolean("silent-wages"));
    }
    else
    {
      setSilentWages(false);
      this.resave = true;
    }
    setSaveInterval(refreshTimeInterval(list, "save-interval", 
      Defaults.SAVE_INTERVAL));
    if (list.containsValueKey("use-op-permissions"))
    {
      setUseOpPermissions(list.getBoolean("use-op-permissions"));
    }
    else
    {
      setUseOpPermissions(false);
      this.resave = true;
    }
    if (list.containsValueKey("hide-vanished-players"))
    {
      setHideVanishedPlayers(list.getBoolean("hide-vanished-players"));
    }
    else
    {
      setHideVanishedPlayers(false);
      this.resave = true;
    }
    if (list.containsValueKey("allow-command-blocks"))
    {
      setAllowCommandBlocks(list.getBoolean("allow-command-blocks"));
    }
    else
    {
      setAllowCommandBlocks(false);
      this.resave = true;
    }
    if (list.containsValueKey("show-command-block-output"))
    {
      setShowCommandBlockOutput(list.getBoolean("show-command-block-output"));
    }
    else
    {
      setShowCommandBlockOutput(false);
      this.resave = true;
    }
    if (list.containsValueKey("show-console-colors"))
    {
      setShowConsoleColors(list.getBoolean("show-console-colors"));
    }
    else
    {
      setShowConsoleColors(false);
      this.resave = true;
    }
    formatMoneyName();
    

    setChanged(this.resave);
  }
  
  public void commit(Database database, String listName)
  {
    DatabaseList list = new DatabaseList();
    
    list.setBoolean("debug", this.plugin.debug);
    

    list.setString("default-bracket", getDefaultBracket());
    list.setString("default-bracket-interval", getDefaultBracketInterval()
      .toString());
    list.setInt("fractional-digits", getFractionalDigits());
    list.setDouble("initial-money", getInitialMoney());
    list.setString("money-name", getMoneyName());
    list.setString("money-name-plural", getMoneyNamePlural());
    list.setInt("money-digit-grouping", getMoneyDigitGrouping());
    list.setString("payday-interval", getPaydayInterval().toString());
    list.setString("save-interval", getSaveInterval().toString());
    list.setBoolean("silent-wages", getSilentWages());
    list.setBoolean("use-op-permissions", getUseOpPermissions());
    list.setBoolean("hide-vanished-players", getHideVanishedPlayers());
    list.setBoolean("allow-command-blocks", getAllowCommandBlocks());
    list.setBoolean("show-command-block-output", getShowCommandBlockOutput());
    list.setBoolean("show-console-colors", getShowConsoleColors());
    
    database.getRoot().removeListKey(listName);
    database.getRoot().setList(listName, list);
    if (this.plugin.debug) {
      this.plugin.sendConsoleMessage("[BOSEconomy Debug] Committed settings to the database.");
    }
  }
  
  public static class Names
  {
    public static final String DEBUG = "debug";
    public static final String DEFAULT_BRACKET = "default-bracket";
    public static final String DEFAULT_BRACKET_INTERVAL = "default-bracket-interval";
    public static final String FRACTIONAL_DIGITS = "fractional-digits";
    public static final String INITIAL_MONEY = "initial-money";
    public static final String MONEY_NAME = "money-name";
    public static final String MONEY_NAME_PLURAL = "money-name-plural";
    public static final String MONEY_DIGIT_GROUPING = "money-digit-grouping";
    public static final String PAYDAY_INTERVAL = "payday-interval";
    public static final String SAVE_INTERVAL = "save-interval";
    public static final String SILENT_WAGES = "silent-wages";
    public static final String USE_BUKKIT_PERMISSIONS = "use-bukkit-permissions";
    public static final String USE_OP_PERMISSIONS = "use-op-permissions";
    public static final String HIDE_VANISHED_PLAYERS = "hide-vanished-players";
    public static final String ALLOW_COMMAND_BLOCKS = "allow-command-blocks";
    public static final String SHOW_COMMAND_BLOCK_OUTPUT = "show-command-block-output";
    public static final String SHOW_CONSOLE_COLORS = "show-console-colors";
  }
}

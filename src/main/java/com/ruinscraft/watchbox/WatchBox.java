package com.ruinscraft.watchbox;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class WatchBox extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private MySQLHelper mySQLHelper;

    @Override
    public void onEnable() {
    	
    	// check config.yml for database credentials
    	if(!config.contains("mysql.host") || !config.contains("mysql.port") || !config.contains("mysql.database")
    	   || !config.contains("mysql.username") || !config.contains("mysql.password")) {
    		
    		getLogger().info("Failed to find database information in config.yml!");
    		getLogger().info("Adding default values to config.yml");
    		
    		config.addDefault("mysql.host", "localhost");
    		config.addDefault("mysql.port", 3306);
    		config.addDefault("mysql.database", "mcatlas");
    		config.addDefault("mysql.username", "mcatlasdev");
    		config.addDefault("mysql.password", "password123");
    		
    		config.options().copyDefaults(true);
            saveConfig();
    		
    	}
    	
    	setupMySQLHelper();
    	
    	// TODO: setup command
    	//this.getCommand("shop").setExecutor(new ShopCommandExecutor(this));
    	//this.getCommand("shop").setTabCompleter(this);
    	
    	//Bukkit.getPluginManager().registerEvents(this.sl, this);
    	
    	getLogger().info("DukesMart has been enabled!");
    }

    @Override
    public void onDisable() {
    	getLogger().info("DukesMart has been disabled!");
    }

    public void setupMySQLHelper() {
    	// load MySQL database info
    	String host     = config.getString("mysql.host");
    	int    port     = config.getInt("mysql.port");
    	String database = config.getString("mysql.database");
    	String username = config.getString("mysql.username");
    	String password = config.getString("mysql.password");
    	
    	this.mySQLHelper = new MySQLHelper(host, port, database, username, password);
    }
    
    public MySQLHelper getMySQLHelper() {
    	return this.mySQLHelper;
    }
}
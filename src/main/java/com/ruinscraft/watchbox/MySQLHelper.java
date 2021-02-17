package com.ruinscraft.watchbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class MySQLHelper {
	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;

	public MySQLHelper(String host, int port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		
		CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
            	// Shops table
                try (Statement statement = connection.createStatement()) {
                    statement.execute("");
                }
                // Ledger table
                try (Statement statement = connection.createStatement()){
                	statement.execute("");
                }
                
                // Transaction history table
                try (Statement statement = connection.createStatement()){
                	statement.execute("");
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
	}
	
	private Connection getConnection() {
        String jdbcUrl = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database;

        try {
            return DriverManager.getConnection(jdbcUrl, this.username, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

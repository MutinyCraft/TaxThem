package com.mutinycraft.jigsaw.TaxThem;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * User: Jigsaw
 * Date: 5/18/13
 * Time: 10:16 PM
 */

public class TaxThem extends JavaPlugin {

    public static Economy econ = null;

    private Logger log;
    private File configFile;
    private FileConfiguration config;
    private double[] taxRates;
    private double[] taxBrackets;
    private int taxPeriod;
    private int taxPeriodInitial;
    private int updatePeriod;
    private int updatePeriodInitial;
    // Thread safe queue
    private ConcurrentLinkedQueue<String> taxQueue;


    private static final String VERSION = " 1.1";

    public void onEnable() {
        log = this.getLogger();
        taxRates = new double[3];
        taxBrackets = new double[3];
        taxQueue = new ConcurrentLinkedQueue<String>();

        // Make sure the config.yml exists.
        try {
            configFile = new File(getDataFolder(), "config.yml");
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        config = new YamlConfiguration();
        loadYamls();
        setConfigOptions();

        // Check for Vault.
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Schedule Tasks
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new PlayerBalanceTask(this.getDataFolder(),
                this), updatePeriodInitial, updatePeriod);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new PlayerTaxTask(this), taxPeriodInitial,
                taxPeriod);

        log.info(this.getName() + VERSION + " enabled!");
    }

    /**
     *
     */
    private void loadYamls() {
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception
     */
    private void firstRun() throws Exception {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(getResource("config.yml"), configFile);
        }
    }

    /**
     * @param in
     * @param file
     */
    private void copy(InputStream in, File file) {
        try {
            OutputStream fout = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                fout.write(buf, 0, len);
            }
            fout.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void setConfigOptions() {
        // Tax period:
        taxPeriod = config.getInt("Tax-Period") * 20;
        taxPeriodInitial = config.getInt("Tax-Period-Initial") * 20;
        // Balance update period:
        updatePeriod = config.getInt("Balance-Update-Period") * 20;
        updatePeriodInitial = config.getInt("Balance-Update-Period-Initial") * 20;
        // Tax rates:
        taxRates[0] = config.getDouble("Tax-Rate-Low");
        taxRates[1] = config.getDouble("Tax-Rate-Medium");
        taxRates[2] = config.getDouble("Tax-Rate-High");
        // Tax brackets;
        taxBrackets[0] = config.getDouble("Low-Earner");
        taxBrackets[1] = config.getDouble("Medium-Earner");
        taxBrackets[2] = config.getDouble("High-Earner");
    }

    /**
     * Return the tax rate of the provided bracket.  Brackets are simply indexes in the array.  This should check
     * that the index is valid, but that is handled by the caller.
     *
     * @param i
     * @return
     */
    public double getTaxRates(int i) {
        return taxRates[i];
    }

    /**
     * Return the tax bracket range of the provided bracket.  Brackets are simply indexes in the array.  This should
     * check that the index is valid, but that is handled by the caller.
     *
     * @param i
     * @return
     */
    public double getTaxBrackets(int i) {
        return taxBrackets[i];
    }

    /**
     * Return a queue containing all player that need to have tax collected. This will not allow contain duplicates.
     * This is safe for use by multiple threads.
     *
     * @return taxQueue
     */
    public ConcurrentLinkedQueue<String> getTaxQueue() {
        return taxQueue;
    }

    /**
     * Adds a player to be taxed to the queue if they are not already in the queue.  This is safe for use by multiple
     * threads.
     *
     * @param player to add to the queue.
     */
    public void addPlayerToQueue(String player) {
        if (!taxQueue.contains(player)) {
            taxQueue.add(player);
        }
    }

    /**
     * Allows the caller to easily log a message to the console.
     *
     * @param message to display.
     */
    public void logMessage(String message) {
        log.info(message);
    }

    /**
     * Checks to ensure that a Vault supported economy is installed on the server.  If not it will return false.
     *
     * @return true if economy enabled, false otherwise.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


    public void onDisable() {
        log.info(this.getName() + VERSION + " disabled!");
    }

}

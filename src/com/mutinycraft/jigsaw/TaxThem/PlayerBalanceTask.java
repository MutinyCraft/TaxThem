package com.mutinycraft.jigsaw.TaxThem;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * User: Jigsaw
 * Date: 5/19/13
 * Time: 12:02 AM
 */

public class PlayerBalanceTask extends BukkitRunnable {

    String path;
    TaxThem plugin;

    public PlayerBalanceTask(File file, TaxThem p) {
        path = file.getParent();
        plugin = p;
    }

    /**
     * Method that takes advantage of the fact that Essentials creates a user file for every player.  These files are
     * used to get all players that have ever played on a server and then check them for tax collection.  This is an
     * asynchronous task and should not access any Bukkit API methods.
     */
    @Override
    public void run() {
        long start = System.currentTimeMillis();
        File file = new File(path + File.separator + "Essentials" + File.separator + "userdata");
        File[] files = file.listFiles();
        for (File f : files) {
            taxCheck(f.getName().split("\\.")[0]);
        }
        long end = System.currentTimeMillis();
        plugin.logMessage("Successfully completed balance update in " + (end - start) + "ms.");
    }

    /**
     * Private helper method to determine if a player is to be taxed.  If they are to be taxed this method adds them
     * to the taxQueue.
     *
     * @param player to be checked.
     */
    private void taxCheck(String player) {
        double balance = plugin.econ.getBalance(player);
        if (balance > plugin.getTaxBrackets(2)) {
            if (plugin.getTaxRates(2) > 0) {
                plugin.addPlayerToQueue(player);
            }
        } else if (balance > plugin.getTaxBrackets(1)) {
            if (plugin.getTaxRates(1) > 0) {
                plugin.addPlayerToQueue(player);
            }
        } else if (balance > plugin.getTaxBrackets(0)) {
            if (plugin.getTaxRates(0) > 0) {
                plugin.addPlayerToQueue(player);
            }
        }
    }
}

package com.mutinycraft.jigsaw.TaxThem;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * User: Jigsaw
 * Date: 5/18/13
 * Time: 10:49 PM
 */

public class PlayerTaxTask extends BukkitRunnable {

    TaxThem plugin;

    public PlayerTaxTask(TaxThem p) {
        plugin = p;
    }

    /**
     * Runnable that will collect the proper taxes from the players current in the queue to be taxed.  If the queue
     * becomes too long it may cause a drop in server ticks.  This shouldn't normally be an issue.
     */
    @Override
    public void run() {
        long start = System.currentTimeMillis();
        for (String player : plugin.getTaxQueue()) {
            double balance = plugin.econ.getBalance(player);
            if (balance > plugin.getTaxBrackets(2)) {
                if (plugin.getTaxRates(2) > 0) {
                    plugin.econ.withdrawPlayer(player, (balance * plugin.getTaxRates(2)));
                }
            } else if (balance > plugin.getTaxBrackets(1)) {
                if (plugin.getTaxRates(1) > 0) {
                    plugin.econ.withdrawPlayer(player, (balance * plugin.getTaxRates(1)));
                }
            } else if (balance > plugin.getTaxBrackets(0)) {
                if (plugin.getTaxRates(0) > 0) {
                    plugin.econ.withdrawPlayer(player, (balance * plugin.getTaxRates(0)));
                }
            }
        }
        long end = System.currentTimeMillis();
        plugin.logMessage("Successfully completed withdrawals in " + (end - start) + "ms.");
    }
}

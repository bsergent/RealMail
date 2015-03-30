/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.challengercity.plugins.realmail;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
/**
 * 
 *
 * @author Ben Sergent V
 */
public class RealMail extends JavaPlugin {

    // TODO Notify players on login if they have new mail - PlayerJoinEvent w/ EventListener
    // TODO Attachments
    // TODO Check if mailbox is full
    // TODO Check double chests
    // TODO Chest break event deleted mailbox
    // TODO Protect chest, override permission
    // TODO Lock chest, override permission
    // TODO Reload config command
    
    private String version = "0.0.3";
    private org.bukkit.configuration.file.FileConfiguration mailboxesConfig = null;
    private java.io.File mailboxesFile = null;
    
    @Override
    public void onEnable() {
        getLogger().info("Real Mail v"+version+" enabled.");
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        // Check mailboxes.yml
        if (mailboxesFile == null) {
            mailboxesFile = new java.io.File(getDataFolder(), "mailboxes.yml");
        }
        mailboxesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(mailboxesFile);
        
        getServer().getPluginManager().registerEvents(new InventoryOpenListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Real Mail v"+version+" disabled.");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        // Commands
        if (cmd.getName().equalsIgnoreCase("realmail")) {
            if (!(sender instanceof Player)) { // If run by the console
                    sender.sendMessage("This command can only be run by a player.");
            } else {
                    Player player = (Player) sender;
                    if (args.length>0) {
                        if (args[0].equalsIgnoreCase("setmailbox")) {
                            if (args.length > 1) {
                                if (player.hasPermission("realmail.admin.setmailbox.others")) {
                                    if (player.getTargetBlock(null, 5).getTypeId() == 54) {
                                        mailboxesConfig.set(args[1]+".x", player.getTargetBlock(null, 5).getX());
                                        mailboxesConfig.set(args[1]+".y", player.getTargetBlock(null, 5).getY());
                                        mailboxesConfig.set(args[1]+".z", player.getTargetBlock(null, 5).getZ());
                                        mailboxesConfig.set(args[1]+".world", player.getTargetBlock(null, 5).getWorld().getName());
                                        try {
                                            mailboxesConfig.save(mailboxesFile);
                                            player.sendMessage(args[1]+"'s mailbox set at: "+player.getTargetBlock(null, 5).getX()+","+player.getTargetBlock(null, 5).getY()+","+player.getTargetBlock(null, 5).getZ());
                                        } catch (Exception ex) {
                                            player.sendMessage("Failed to set mailbox.");
                                            ex.printStackTrace();
                                        }
                                        //org.bukkit.block.Chest chest = (org.bukkit.block.Chest) player.getTargetBlock(null, 5);
                                        //org.bukkit.inventory.Inventory chestInv = chest.getBlockInventory();
                                        
                                    } else {
                                        player.sendMessage("Please look at the chest that will be the mailbox.");
                                    }
                                } else {
                                    player.sendMessage("You don't have permission to set other players' mailboxes.");
                                }
                            } else {
                                if (player.hasPermission("realmail.user.setmailbox") && !getConfig().getBoolean("universalmailboxes", false)) {
                                    if (player.getTargetBlock(null, 5).getTypeId() == 54) {
                                        mailboxesConfig.set(player.getPlayerListName()+".x", player.getTargetBlock(null, 5).getX());
                                        mailboxesConfig.set(player.getPlayerListName()+".y", player.getTargetBlock(null, 5).getY());
                                        mailboxesConfig.set(player.getPlayerListName()+".z", player.getTargetBlock(null, 5).getZ());
                                        mailboxesConfig.set(player.getPlayerListName()+".world", player.getTargetBlock(null, 5).getWorld().getName());
                                        try {
                                            mailboxesConfig.save(mailboxesFile);
                                            player.sendMessage("Mailbox set at: "+player.getTargetBlock(null, 5).getX()+","+player.getTargetBlock(null, 5).getY()+","+player.getTargetBlock(null, 5).getZ());
                                        } catch (Exception ex) {
                                            player.sendMessage("Failed to set mailbox.");
                                            ex.printStackTrace();
                                        }
                                        //org.bukkit.block.Chest chest = (org.bukkit.block.Chest) player.getTargetBlock(null, 5);
                                        //org.bukkit.inventory.Inventory chestInv = chest.getBlockInventory();
                                        
                                    } else {
                                        player.sendMessage("Please look at the chest that will be the mailbox.");
                                    }
                                } else {
                                    player.sendMessage("You don't have permission for this command.");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("send") && player.hasPermission("realmail.user.sendmail")) {
                            if (args.length > 1) {
                                if (player.getItemInHand().getTypeId() == 387) {
                                    sendBook(player.getItemInHand().clone(), player, args[1], false);
                                } else if (player.getItemInHand().getTypeId() == 386) {
                                    player.sendMessage("Please sign the book with the subject as the title.");
                                } else {
                                    player.sendMessage("Please select a book in your hotbar that you would like to send.");
                                }
                            } else {
                                player.sendMessage("Send to who?");
                            }
                        } else if (args[0].equalsIgnoreCase("bulkmail")) {
                            if (player.hasPermission("realmail.admin.bulkmail")) {
                                if (player.getItemInHand().getTypeId() == 387) {
                                    java.util.Set set = mailboxesConfig.getKeys(false);
                                    Object[] mailboxes = set.toArray();
                                    if (set != null) {
                                        for (int i = 0; i < mailboxes.length; i++) {
                                            sendBook(player.getItemInHand().clone(), player, mailboxes[i].toString(), true);
                                        }
                                    }
                                    player.sendMessage("Book sent to all players who have a mailbox on this server.");
                                    player.setItemInHand(new org.bukkit.inventory.ItemStack(0));
                                } else if (player.getItemInHand().getTypeId() == 386) {
                                    player.sendMessage("Please sign the book with the subject as the title.");
                                } else {
                                    player.sendMessage("Please select a book in your hotbar that you would like to send.");
                                }
                            } else {
                                player.sendMessage("You don't have permission for this command.");
                            }
                        } else if (args[0].equalsIgnoreCase("delmailbox")) {
                            if (args.length > 1){
                                if (player.hasPermission("realmail.admin.delmailbox.others")) {
                                    mailboxesConfig.set(args[1]+".x", null);
                                    mailboxesConfig.set(args[1]+".y", null);
                                    mailboxesConfig.set(args[1]+".z", null);
                                    mailboxesConfig.set(args[1]+".world", null);
                                    mailboxesConfig.set(args[1], null);
                                    try {
                                        mailboxesConfig.save(mailboxesFile);
                                        player.sendMessage(args[1]+"'s mailbox deleted.");
                                    } catch (Exception ex) {
                                        player.sendMessage("Failed to delete mailbox.");
                                        ex.printStackTrace();
                                    }
                                } else {
                                    player.sendMessage("You don't have permission to delete other players mailboxes.");
                                }
                            } else {
                                if (player.hasPermission("realmail.user.delmailbox")) {
                                    mailboxesConfig.set(player.getPlayerListName()+".x", null);
                                    mailboxesConfig.set(player.getPlayerListName()+".y", null);
                                    mailboxesConfig.set(player.getPlayerListName()+".z", null);
                                    mailboxesConfig.set(player.getPlayerListName()+".world", null);
                                    mailboxesConfig.set(player.getPlayerListName(), null);
                                    try {
                                        mailboxesConfig.save(mailboxesFile);
                                        player.sendMessage(player.getPlayerListName()+"'s mailbox deleted.");
                                    } catch (Exception ex) {
                                        player.sendMessage("Failed to delete mailbox.");
                                        ex.printStackTrace();
                                    }
                                } else {
                                    player.sendMessage("You don't have permission for this command.");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("clear")) {
                            if (player.hasPermission("realmail.user.clear")) {
                                if (mailboxesConfig.contains(player.getPlayerListName())) {
                                    org.bukkit.World world = Bukkit.getWorld((String) mailboxesConfig.get(player.getPlayerListName()+".world"));
                                    org.bukkit.block.Block block = world.getBlockAt(mailboxesConfig.getInt(player.getPlayerListName()+".x"), mailboxesConfig.getInt(player.getPlayerListName()+".y"), mailboxesConfig.getInt(player.getPlayerListName()+".z"));
                                    if (block.getTypeId() == 54) {
                                        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                                        org.bukkit.inventory.Inventory chestInv = chest.getBlockInventory();
                                        chestInv.remove(new org.bukkit.inventory.ItemStack(387));
                                        player.sendMessage("Mailbox cleared.");
                                    } else {
                                        player.sendMessage("Your mailbox is missing. Use "+org.bukkit.ChatColor.ITALIC+"/rm setmailbox"+org.bukkit.ChatColor.RESET+" on a chest.");
                                    }
                                } else {
                                    player.sendMessage("You don't have a mailbox. Use "+org.bukkit.ChatColor.ITALIC+"/rm setmailbox"+org.bukkit.ChatColor.RESET+" on a chest.");
                                }
                            } else {
                                player.sendMessage("You don't have permission for this command.");
                            }
                        } else if (args[0].equalsIgnoreCase("help")) {
                            sendHelpContents(player);
                        } else if (args[0].equalsIgnoreCase("version")) {
                            player.sendMessage("RealMail v"+version);
                            player.sendMessage("Developed by Ben21897/ha1fBit");
                            player.sendMessage("http://dev.bukkit.org/server-mods/realmail/");
                        } else {
                            sendHelpContents(player);
                        }
                    } else {
                        sendHelpContents(player);
                    }
            }
            return true;
        }
        
        return false;
    }
    
    private boolean sendHelpContents(Player player) {
        player.sendMessage(org.bukkit.ChatColor.YELLOW+"---------"+org.bukkit.ChatColor.WHITE+" Real Mail "+org.bukkit.ChatColor.YELLOW+"-----------------------------");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm send [player]:"+org.bukkit.ChatColor.WHITE+" Send the held book");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm bulkmail:"+org.bukkit.ChatColor.WHITE+" Send the held book to everyone (Admins)");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm setmailbox:"+org.bukkit.ChatColor.WHITE+" Set your mailbox/chest");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm delmailbox:"+org.bukkit.ChatColor.WHITE+" Delete you mailbox");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm clear:"+org.bukkit.ChatColor.WHITE+" Clears your mail");
        player.sendMessage(org.bukkit.ChatColor.GOLD+"/rm version:"+org.bukkit.ChatColor.WHITE+" Returns the current version");
        return true;
    }
    
    private boolean sendBook(org.bukkit.inventory.ItemStack bookStack, Player fromPlayer, String toString, boolean bulk) {
        Player target  = org.bukkit.Bukkit.getServer().getPlayer(toString);
        try {
            java.util.Date dateRaw = java.util.Calendar.getInstance().getTime();
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat();
            format.applyPattern(getConfig().getString("dateformat"));
            String dateString = format.format(dateRaw);
            
            org.bukkit.inventory.meta.BookMeta bookMeta = (org.bukkit.inventory.meta.BookMeta) bookStack.getItemMeta();
            java.util.List<String> oldPages = bookMeta.getPages();
            java.util.List<String> newPages = new java.util.LinkedList<String>();
            newPages.add("§0From: "+fromPlayer.getDisplayName()+"\n§0To: "+toString+"\n§0Subject: "+ bookMeta.getTitle() +"\n§0Date: "+dateString+"\n§0\n§0\n§0\n§0\n§0\n§0\n§0\n§0\n§l§1--Real Mail--§r");
            if (oldPages.size()>0) {
                for (int i = 0; i < oldPages.size(); i++) {
                    newPages.add(oldPages.get(i));
                }
            }
            bookMeta.setPages(newPages);
            
            java.util.List<String> lore = new java.util.LinkedList<String>();
            lore.add("§7to  "+toString);
            lore.add("§7on "+dateString);
            bookMeta.setLore(lore);
            
            bookStack.setItemMeta(bookMeta);

            /* Send Book */
            //target.getInventory().addItem((org.bukkit.inventory.ItemStack) bookCraftStack);
            //fromPlayer.setItemInHand(new org.bukkit.inventory.ItemStack(0));
            if (mailboxesConfig.contains(toString)) {
                org.bukkit.World world = Bukkit.getWorld((String) mailboxesConfig.get(toString+".world"));
                org.bukkit.block.Block block = world.getBlockAt(mailboxesConfig.getInt(toString+".x"), mailboxesConfig.getInt(toString+".y"), mailboxesConfig.getInt(toString+".z"));
                if (block.getTypeId() == 54) {
                    org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                    org.bukkit.inventory.Inventory chestInv = chest.getBlockInventory();
                    chestInv.addItem(bookStack);
                    setSignStatus(true, chest.getBlock(), toString);
                    if (!bulk) {
                        fromPlayer.sendMessage("Mail Sent!");
                        fromPlayer.setItemInHand(new org.bukkit.inventory.ItemStack(0));
                    }
                    if (target != null) {
                        target.sendMessage("You've got mail!");
                    }
                    // If there's a sign, mark as unread
                } else {
                    if (target != null) {
                        target.sendMessage(fromPlayer.getDisplayName()+" tried to send you a message, but your mailbox is missing!");
                        target.sendMessage("Use "+org.bukkit.ChatColor.ITALIC+"/rm setmailbox"+org.bukkit.ChatColor.RESET+" on a chest.");
                    }
                    if (!bulk) {
                        fromPlayer.sendMessage("Failed to send.");
                        fromPlayer.sendMessage("They don't have a mailbox!");
                    }
                }
            } else {
                if (target != null) {
                    target.sendMessage(fromPlayer.getDisplayName()+" tried to send you a message, but you don't have a mailbox!");
                    target.sendMessage("Use "+org.bukkit.ChatColor.ITALIC+"/rm setmailbox"+org.bukkit.ChatColor.RESET+" on a chest.");
                }
                if (!bulk) {
                    fromPlayer.sendMessage("Failed to send.");
                    fromPlayer.sendMessage("They don't have a mailbox!");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (!bulk) {
                fromPlayer.sendMessage("Failed to mail the book.");
            }
        }
        return true;
    }
    
    public boolean setSignStatus(boolean unread, org.bukkit.block.Block chestBlock, String ownerName) {
        for (int x = chestBlock.getX()-getConfig().getInt("signradius"); x <= chestBlock.getX()+getConfig().getInt("signradius"); x++) {
            for (int y = chestBlock.getY()-getConfig().getInt("signradius"); y <= chestBlock.getY()+getConfig().getInt("signradius"); y++) {
                for (int z = chestBlock.getZ()-getConfig().getInt("signradius"); z <= chestBlock.getZ()+getConfig().getInt("signradius"); z++) {
                    org.bukkit.block.Block block = chestBlock.getWorld().getBlockAt(x, y, z);
                    //if (block.getType() == org.bukkit.Material.SIGN || block.getType() == org.bukkit.Material.SIGN_POST) {
                    if (block.getTypeId() == 63 || block.getTypeId() == 68) {
                        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
                        if (sign.getLine(0).equals("[Mailbox]")) {
                            sign.setLine(1, ownerName);
                            if (unread) {
                                sign.setLine(2, "  §a"+getConfig().getString("unreadmailsigntext"));
                                mailboxesConfig.set(ownerName+".unread", true);
                            } else {
                                sign.setLine(2, getConfig().getString("readmailsigntext"));
                                mailboxesConfig.set(ownerName+".unread", false);
                            }
                            sign.update();
                            try {
                                mailboxesConfig.save(mailboxesFile);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public final class InventoryOpenListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onInventoryOpenEvent(org.bukkit.event.inventory.InventoryOpenEvent e) {
            if (e.getInventory().getHolder() instanceof org.bukkit.block.Chest){
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) e.getInventory().getHolder();
                if (mailboxesConfig.contains(e.getPlayer().getName())) { // Is owner
                    int x = mailboxesConfig.getInt(e.getPlayer().getName()+".x");
                    int y = mailboxesConfig.getInt(e.getPlayer().getName()+".y");
                    int z = mailboxesConfig.getInt(e.getPlayer().getName()+".z");
                    org.bukkit.World world = Bukkit.getWorld(mailboxesConfig.getString(e.getPlayer().getName()+".world"));
                    if (chest.getX() == x && chest.getY() == y && chest.getZ() == z && chest.getWorld() == world) {
                        setSignStatus(false, chest.getBlock(), e.getPlayer().getName());
                    }
                }
            }
        }
    }
    
    public final class LoginListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
            if (mailboxesConfig.contains(e.getPlayer().getName()) && getConfig().getBoolean("login_notification")) {
                if (mailboxesConfig.getBoolean(e.getPlayer().getName()+".unread", false)) {
                    e.getPlayer().sendMessage("You've got mail! Check you mailbox.");
                }
            }
        }
    }
    
}

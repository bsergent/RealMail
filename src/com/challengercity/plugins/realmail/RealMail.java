/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.challengercity.plugins.realmail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
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
    
    private String version = "0.1.0";
    private org.bukkit.configuration.file.FileConfiguration mailboxesConfig = null;
    private java.io.File mailboxesFile = null;
    private ItemMeta mailboxCouponMeta = null;
    
    /* Mailbox Commands */
    private final String mailboxTextureBlue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNDllZmFhYWI1MzI1NTlmZmY5YWY3NWRhNmFjNGRkNzlkMTk5ZGNmMmZkNDk3Yzg1NDM4MDM4NTY0In19fQ==";
    private final String mailboxTextureWhite = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM5ZTE5NzFjYmMzYzZmZWFhYjlkMWY4NWZjOWQ5YmYwODY3NjgzZjQxMjk1NWI5NjExMTdmZTY2ZTIifX19";
    private final String mailboxIdBlue = "48614330-6c44-47be-85ec-33ed037cf48c";
    private final String mailboxIdWhite = "480bff09-ed89-4214-a2bd-dab19fa5177d";
    
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        ItemStack blueMailboxCoupon = new ItemStack(Material.PAPER, 1);
        mailboxCouponMeta = blueMailboxCoupon.getItemMeta();
        mailboxCouponMeta.setDisplayName("§rMailbox Recipe");
        mailboxCouponMeta.setLore(Arrays.asList("§r§7Right-click with this coupon","§r§7to get a mailbox"));
        blueMailboxCoupon.setItemMeta(mailboxCouponMeta);
        ShapedRecipe blueMailboxRecipe = new ShapedRecipe(blueMailboxCoupon);
        blueMailboxRecipe.shape("  w", "iii", "ici");
        blueMailboxRecipe.setIngredient('w', org.bukkit.Material.WOOL, -1);
        blueMailboxRecipe.setIngredient('i', org.bukkit.Material.IRON_INGOT);
        blueMailboxRecipe.setIngredient('c', org.bukkit.Material.CHEST);
        this.getServer().addRecipe(blueMailboxRecipe);
        
        // Check mailboxes.yml
        if (mailboxesFile == null) {
            mailboxesFile = new java.io.File(getDataFolder(), "mailboxes.yml");
        }
        mailboxesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(mailboxesFile);
        
        getServer().getPluginManager().registerEvents(new MailListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        
        getLogger().log(Level.INFO, "Real Mail v{0} enabled.", version);
    }
    
    /*public static ItemStack setSkin(ItemStack item, String texture){
        org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack cis = null;
        net.minecraft.server.v1_7_R1.ItemStack nis = null;
        if (item instanceof org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack) {
            cis = (org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack) item;
            try {
                Field handle = CraftItemStack.class.getDeclaredField("handle");
                handle.setAccessible(true);
                nis = (net.minecraft.server.v1_7_R1.ItemStack)handle.get(cis);
            } catch (Exception ex) {
                return null;
            }
        }
        NBTTagCompound tag = nis.tag;
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setString("SkullOwner", texture);
        nis.tag = tag;
        return cis;
    }*/
    
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Real Mail v{0} disabled.", version);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        // Commands
        if (cmd.getName().equalsIgnoreCase("realmail")) {
            if (!(sender instanceof Player)) { // If run by the console
                    sender.sendMessage("This command can only be run by a player.");
            } else {
                    Player player = (Player) sender;
                    if (args.length>0) {
                        if (args[0].equalsIgnoreCase("clear")) {
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
    
    public boolean setMailboxFlag(boolean up, org.bukkit.Location boxLocation) {
        return true;
    }
    
    public final class MailListener implements org.bukkit.event.Listener {
        
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onUseItemEvent(org.bukkit.event.player.PlayerInteractEvent e) { // Detect mailbox texture cycling
            if (e.getItem() != null) {
                ItemStack is = e.getItem();
                ItemStack toBeRemoved = is.clone();
                toBeRemoved.setAmount(1);
                /* Exchange Coupon */
                if (is.getType() == Material.PAPER && is.getItemMeta().equals(mailboxCouponMeta)) {
                    e.getPlayer().getInventory().removeItem(toBeRemoved);
                    getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdBlue+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureBlue+"\"}]}}}");
                    e.getPlayer().sendMessage(ChatColor.GOLD+"You exchanged your recipe for a mailbox.");
                }
                /* Cycle texture */
                if (is.getType() == Material.SKULL_ITEM && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && is.getItemMeta().hasLore() && is.getItemMeta().getLore().get(1).contains("Punch to change texture")) {
                    e.getPlayer().getInventory().removeItem(toBeRemoved);
                    if (is.getItemMeta().getLore().get(0).contains("Blue")) {
                        getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7White\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdWhite+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureWhite+"\"}]}}}");
                    } else if (is.getItemMeta().getLore().get(0).contains("White")) {
                        getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdBlue+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureBlue+"\"}]}}}");
                    }
                    e.getPlayer().sendMessage(ChatColor.GOLD+"You changed your mailbox's texture.");
                }
            }
        }
        
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent e) { // Detect placing of mailboxes
            if (e.getItemInHand() != null) {
                ItemStack is = e.getItemInHand();
                if (is.getType() == Material.SKULL_ITEM && is.getItemMeta().hasLore() && is.getItemMeta().getLore().get(1).contains("Punch to change texture")) {
                    
                    List<Location> locations = (List<Location>) mailboxesConfig.getList(e.getPlayer().getUniqueId()+".mailboxes", new LinkedList<Location>());
                    locations.add(e.getBlock().getLocation());
                    mailboxesConfig.set(e.getPlayer().getUniqueId()+".mailboxes", locations);
                    
                    List<String> players = (List<String>) mailboxesConfig.getList("players", new LinkedList<String>());
                    if (!players.contains(e.getPlayer().getUniqueId().toString())) {
                        players.add(e.getPlayer().getUniqueId().toString());
                    }
                    mailboxesConfig.set("players", players);
                    
                    try {
                        mailboxesConfig.save(mailboxesFile);
                        e.getPlayer().sendMessage(ChatColor.GOLD+"Mailbox placed");
                    } catch (Exception ex) {
                        e.getPlayer().sendMessage(ChatColor.GOLD+"Failed to place mailbox");
                        ex.printStackTrace();
                    }
                }
                
            }
        }
        
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent e) { // Detect breaking of mailboxes
            List<String> players = (List<String>) mailboxesConfig.getList("players", new LinkedList<String>());
            for (String p : players) {
                List<Location> locations = (List<Location>) mailboxesConfig.getList(e.getPlayer().getUniqueId()+".mailboxes", new LinkedList<Location>());
                for (Location loc : locations) {
                    if (e.getBlock().getLocation().equals(loc)) {
                        
                        locations.remove(e.getBlock().getLocation());
                        mailboxesConfig.set(e.getPlayer().getUniqueId()+".mailboxes", locations);
                        
                        try {
                            mailboxesConfig.save(mailboxesFile);
                            e.setCancelled(true);
                            e.getBlock().setType(Material.AIR);
                            getServer().dispatchCommand(getServer().getConsoleSender(), "summon Item "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+" {Item:{id:minecraft:skull, Count:1, Damage: 3, tag:{display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdBlue+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureBlue+"\"}]}}}}}");
                        } catch (Exception ex) {
                            e.getPlayer().sendMessage(ChatColor.GOLD+"Failed to remove mailbox");
                            ex.printStackTrace();
                        }
                        return;
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

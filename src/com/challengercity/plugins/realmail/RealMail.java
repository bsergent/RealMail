/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.challengercity.plugins.realmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
/**
 * 
 *
 * @author Ben Sergent V
 */
public class RealMail extends JavaPlugin {

    // TODO Check if mailbox is full
    // TODO Add letter delivery queue for the deliver at a specific time option
    // TODO Add permissions for crafting mailboxes
    
    private String version = "0.2.0";
    private org.bukkit.configuration.file.FileConfiguration mailboxesConfig = null;
    private java.io.File mailboxesFile = null;
    private org.bukkit.configuration.file.FileConfiguration packagesConfig = null;
    private java.io.File packagesFile = null;
    private ItemMeta mailboxRecipeMeta = null;
    private org.bukkit.inventory.meta.BookMeta stationaryMeta = null;
    
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
        mailboxRecipeMeta = blueMailboxCoupon.getItemMeta();
        mailboxRecipeMeta.setDisplayName("§rMailbox Recipe");
        mailboxRecipeMeta.setLore(Arrays.asList("§r§7Right-click with this coupon","§r§7to get a mailbox"));
        blueMailboxCoupon.setItemMeta(mailboxRecipeMeta);
        ShapedRecipe blueMailboxRecipe = new ShapedRecipe(blueMailboxCoupon);
        blueMailboxRecipe.shape("  w", "iii", "ici");
        blueMailboxRecipe.setIngredient('w', org.bukkit.Material.WOOL, -1);
        blueMailboxRecipe.setIngredient('i', org.bukkit.Material.IRON_INGOT);
        blueMailboxRecipe.setIngredient('c', org.bukkit.Material.CHEST);
        this.getServer().addRecipe(blueMailboxRecipe);
        
        ItemStack stationary = new ItemStack(Material.BOOK_AND_QUILL, 1);
        stationaryMeta = (org.bukkit.inventory.meta.BookMeta) stationary.getItemMeta();
        stationaryMeta.setDisplayName("§rStationary");
        stationaryMeta.setLore(Arrays.asList("§r§7Right-click a mailbox to send after signing","§r§7Use the name of the recipient as the title"));
        stationaryMeta.addPage("");
        stationary.setItemMeta(stationaryMeta);
        ShapelessRecipe stationaryRecipe = new ShapelessRecipe(stationary);
        stationaryRecipe.addIngredient(Material.PAPER);
        stationaryRecipe.addIngredient(Material.FEATHER);
        this.getServer().addRecipe(stationaryRecipe);
        
        if (mailboxesFile == null) {
            mailboxesFile = new java.io.File(getDataFolder(), "mailboxes.yml");
        }
        mailboxesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(mailboxesFile);
        
        if (packagesFile == null) {
            packagesFile = new java.io.File(getDataFolder(), "packages.yml");
        }
        packagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(packagesFile);
        
        getServer().getPluginManager().registerEvents(new MailListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        
        getLogger().log(Level.INFO, "RealMail v{0} enabled.", version);
    }
    
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "RealMail v{0} disabled.", version);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (cmd.getName().equalsIgnoreCase("mail")) { // TODO Eventually add video on how to use
            if (args.length == 0 || (args.length < 2 && args[0].equals("1"))) { // Show crafting
                sender.sendMessage(new String[] {
                    ChatColor.GOLD+""+ChatColor.BOLD+"RealMail - Crafting Recipes",
                    ChatColor.GOLD+"Mailbox:",
                    ChatColor.DARK_GRAY+"  --"+ChatColor.WHITE+"w   w"+ChatColor.WHITE+" = wool (1x)",
                    ChatColor.GRAY+"  i i i   i"+ChatColor.GOLD+" = iron ingot (5x)",
                    ChatColor.GRAY+"  i "+ChatColor.DARK_RED+"c"+ChatColor.GRAY+"i   c"+ChatColor.WHITE+" = chest (1x)",
                    ChatColor.GOLD+"Stationary:",
                    ChatColor.WHITE+"  1x paper and 1x feather",
                    ChatColor.WHITE+"Use /mail 2 for usage"
                });
            } else if (args.length < 2) {
                if (args[0].equals("2")) { // Show usage
                    sender.sendMessage(new String[] {
                        ChatColor.GOLD+""+ChatColor.BOLD+"RealMail - Usage information",
                        ChatColor.GOLD+"Sending a letter:",
                        ChatColor.WHITE+"  1. Craft some stationary",
                        ChatColor.WHITE+"  2. Type your letter",
                        ChatColor.WHITE+"  3. Attach items if you wish (see /mail 3)",
                        ChatColor.WHITE+"  4. Sign the book/stationary as the recipient's username",
                        ChatColor.WHITE+"  5. Right-click a mailbox with the letter",
                        ChatColor.WHITE+"Use /mail 3 for packaging"
                    });
                } else if (args[0].equals("3")) { // Show attachments
                    sender.sendMessage(new String[] {
                        ChatColor.GOLD+""+ChatColor.BOLD+"RealMail - Packaging",
                        ChatColor.GOLD+"Attach:",
                        ChatColor.WHITE+"  1. Pick up the item to be attached with your cursor",
                        ChatColor.WHITE+"  2. Drop it/left-click again on the letter",
                        ChatColor.GOLD+"Detach:",
                        ChatColor.WHITE+"  1. Pick up the package with your cursor",
                        ChatColor.WHITE+"  2. Right-click empty slots with the package",
                        ChatColor.WHITE+"Use /mail 1 for crafting"
                    });
                }
            }
                   
        }
        
        return true;
    }
    
    private boolean sendBook(org.bukkit.inventory.ItemStack bookStack, Player fromPlayer, String toString, boolean bulk) {
        /*Player target  = org.bukkit.Bukkit.getServer().getPlayer(toString);
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

             Send Book 
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
        }*/
        return true;
    }
    
    public boolean setSignStatus(boolean unread, org.bukkit.block.Block chestBlock, String ownerName) {
        /*for (int x = chestBlock.getX()-getConfig().getInt("signradius"); x <= chestBlock.getX()+getConfig().getInt("signradius"); x++) {
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
        }*/
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
                if (is.getType() == Material.PAPER && is.getItemMeta().equals(mailboxRecipeMeta)) {
                    e.getPlayer().getInventory().removeItem(toBeRemoved);
                    getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdBlue+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureBlue+"\"}]}}}");
                    e.getPlayer().sendMessage(ChatColor.GOLD+"You exchanged your recipe for a mailbox.");
                }
                /* Cycle texture */
                else if (is.getType() == Material.SKULL_ITEM && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && is.getItemMeta().hasLore() && is.getItemMeta().getLore().get(1).contains("Punch to change texture")) {
                    e.getPlayer().getInventory().removeItem(toBeRemoved);
                    if (is.getItemMeta().getLore().get(0).contains("Blue")) {
                        getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7White\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdWhite+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureWhite+"\"}]}}}");
                    } else if (is.getItemMeta().getLore().get(0).contains("White")) {
                        getServer().dispatchCommand(getServer().getConsoleSender(), "give "+e.getPlayer().getName()+" skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\""+mailboxIdBlue+"\",Name:\"ha1fBit\",Properties:{textures:[{Value:\""+mailboxTextureBlue+"\"}]}}}");
                    }
                    e.getPlayer().sendMessage(ChatColor.GOLD+"You changed your mailbox's texture.");
                }
                /* Stationary Stuff */
                else if (is.getType() == Material.WRITTEN_BOOK && is.getItemMeta().hasLore() && (is.getItemMeta().getDisplayName().equals("§rLetter") || is.getItemMeta().getDisplayName().equals("§rPackage")) && is.getItemMeta().getLore().get(0).contains("§r§7Right-click a mailbox to send")) {
                        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.SKULL)) {

                            List<String> players = (List<String>) mailboxesConfig.getList("players", new LinkedList<String>());
                            for (String p : players) {
                                List<Location> locations = (List<Location>) mailboxesConfig.getList(e.getPlayer().getUniqueId()+".mailboxes", new LinkedList<Location>());
                                for (Location loc : locations) {
                                    if (e.getClickedBlock().getLocation().equals(loc)) {
                                        org.bukkit.inventory.meta.BookMeta newLetter = (org.bukkit.inventory.meta.BookMeta) is.getItemMeta();
                                        org.bukkit.OfflinePlayer recipient = Bukkit.getOfflinePlayer(newLetter.getTitle());
                                        if (recipient != null) {
                                            java.util.Date dateRaw = java.util.Calendar.getInstance().getTime();
                                            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat();
                                            format.applyPattern(getConfig().getString("dateformat"));
                                            String dateString = format.format(dateRaw);
                                            
                                            List<String> oldLore = (List<String>) newLetter.getLore();
                                            List<String> lore = (List<String>) new LinkedList(Arrays.asList("§r§7To: "+newLetter.getTitle(), "§r§7Date: "+dateString));
                                            for (String oldLoreLine : oldLore) {
                                                if (oldLoreLine.contains("ID")) {
                                                    lore.add(oldLoreLine);
                                                    break;
                                                }
                                            }
                                            newLetter.setLore(lore);
                                            
                                            List<org.bukkit.inventory.meta.BookMeta> letters = (List<org.bukkit.inventory.meta.BookMeta>) mailboxesConfig.getList(recipient.getUniqueId()+".letters", new LinkedList<org.bukkit.inventory.meta.BookMeta>());
                                            letters.add(newLetter);
                                            mailboxesConfig.set(recipient.getUniqueId()+".letters", letters);
                                            mailboxesConfig.set(recipient.getUniqueId()+".unread", true);
                                            try {
                                                mailboxesConfig.save(mailboxesFile);
                                                e.getPlayer().getInventory().remove(is);
                                                e.getPlayer().sendMessage(ChatColor.GOLD+"Letter sent.");
                                                setMailboxFlag(true, loc);
                                                if (recipient.getPlayer() != null) {
                                                    recipient.getPlayer().sendMessage(ChatColor.GOLD+"You've got mail! Check your mailbox.");
                                                }
                                            } catch (Exception ex) {
                                                e.getPlayer().sendMessage(ChatColor.GOLD+"Failed to send the letter.");
                                                ex.printStackTrace();
                                            }
                                        } else {
                                            e.getPlayer().sendMessage(ChatColor.GOLD+"Recipient did not exist. Made letter editable again.");
                                            newLetter.setDisplayName("§rStationary");
                                            e.getItem().setType(Material.BOOK_AND_QUILL);
                                            e.getItem().setItemMeta(newLetter);
                                        }
                                        
                                    }
                                }
                            }
                    }
                }
            } // End empty hand detection
            
            /* Open Mailbox */
                else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.SKULL) && e.getPlayer().getItemInHand().getType() != Material.WRITTEN_BOOK) {
                    List<String> players = (List<String>) mailboxesConfig.getList("players", new LinkedList<String>());
                    for (String p : players) {
                        if (getConfig().getBoolean("universal_mailboxes") || p.equals(e.getPlayer().getUniqueId().toString())) {
                            List<Location> locations = (List<Location>) mailboxesConfig.getList(p+".mailboxes", new LinkedList<Location>());
                            for (Location loc : locations) {
                                if (e.getClickedBlock().getLocation().equals(loc)) {
                                    Inventory mailInv = Bukkit.createInventory(e.getPlayer(), getConfig().getInt("mailbox_rows", 4) * 9, "Mailbox");
                                    List<org.bukkit.inventory.meta.BookMeta> letters = (List<org.bukkit.inventory.meta.BookMeta>) mailboxesConfig.getList(e.getPlayer().getUniqueId()+".letters", new LinkedList<org.bukkit.inventory.meta.BookMeta>());
                                    for (org.bukkit.inventory.meta.BookMeta letterMeta : letters) {
                                        ItemStack newBook = null;
                                        if (letterMeta.getDisplayName().contains("Stationary")) {
                                            newBook = new ItemStack(Material.BOOK_AND_QUILL, 1);
                                        } else {
                                            newBook = new ItemStack(Material.WRITTEN_BOOK, 1);
                                        }
                                        newBook.setItemMeta(letterMeta);
                                        HashMap leftover = mailInv.addItem(newBook);
                                        if (!leftover.isEmpty()) {
                                            e.getPlayer().sendMessage(ChatColor.GOLD+"Not all letters could be shown. Please empty your mailbox.");
                                            break;
                                        }
                                    }
                                    e.getPlayer().openInventory(mailInv);
                                }
                            }
                        } else {
                            e.getPlayer().sendMessage(ChatColor.GOLD+"That's not your mailbox. Use /mail to find out how to craft your own.");
                        }
                    }
                }
        }
        
        /* Signing Letters */
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onEditBook(org.bukkit.event.player.PlayerEditBookEvent e) {
            if (e.isSigning() && e.getNewBookMeta().getDisplayName().equals("§rStationary")) {
                org.bukkit.inventory.meta.BookMeta bookMeta = e.getNewBookMeta();
                bookMeta.setDisplayName("§rLetter");
                e.setNewBookMeta(bookMeta);
            }
        }
        
        /* Only Letters in Mailbox */
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
            if (e.getInventory().getName().equals("Mailbox")) {
                ItemStack cursor = e.getCursor();
                ItemStack current = e.getCurrentItem();
                
                boolean allowCursor = false;
                boolean allowCurrent = false;
                
                if (cursor == null || cursor.getType() == Material.AIR) {
                    allowCursor = true;
                } else {
                    if (cursor.hasItemMeta()) {
                        if (cursor.getItemMeta().hasDisplayName()) {
                            if (cursor.getItemMeta().getDisplayName().contains("Stationary") || cursor.getItemMeta().getDisplayName().contains("Letter") || cursor.getItemMeta().getDisplayName().contains("Package")) {
                                allowCursor = true;
                            }
                        }
                    }
                }
                if (current == null || current.getType() == Material.AIR) {
                    allowCurrent = true;
                } else {
                    if (current.hasItemMeta()) {
                        if (current.getItemMeta().hasDisplayName()) {
                            if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Letter") || current.getItemMeta().getDisplayName().contains("Package")) {
                                allowCurrent = true;
                            }
                        }
                    }
                }
                if ((!allowCursor) || (!allowCurrent)) {
                    e.setCancelled(true);
                }
            } else if (e.getInventory().getType() == InventoryType.MERCHANT) {
                ItemStack cursor = e.getCursor();
                ItemStack current = e.getCurrentItem();
                
                boolean allowCursor = false;
                boolean allowCurrent = false;
                
                if (cursor == null || cursor.getType() == Material.AIR) {
                    allowCursor = true;
                } else {
                    if (cursor.hasItemMeta()) {
                        if (cursor.getItemMeta().hasDisplayName()) {
                            if (cursor.getItemMeta().getDisplayName().contains("Stationary") || cursor.getItemMeta().getDisplayName().contains("Letter") || cursor.getItemMeta().getDisplayName().contains("Package")) {
                                allowCursor = true;
                            }
                        }
                    }
                }
                if (current == null || current.getType() == Material.AIR) {
                    allowCurrent = true;
                } else {
                    if (current.hasItemMeta()) {
                        if (current.getItemMeta().hasDisplayName()) {
                            if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Letter") || current.getItemMeta().getDisplayName().contains("Package")) {
                                allowCurrent = true;
                            }
                        }
                    }
                }
                if (allowCursor || allowCurrent) {
                    e.setCancelled(true);
                }
            }
            
            /* Attach */
            if (e.isLeftClick()) {
                ItemStack current = e.getCurrentItem();
                ItemStack cursor = e.getCursor();
                
                if ((current == null || current.getType() == Material.BOOK_AND_QUILL) && cursor != null && cursor.getType() != Material.AIR) {
                    if (current.hasItemMeta()) {
                        if (current.getItemMeta().hasDisplayName()) {
                            if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Package")) {
                                List<ItemStack> attachments = new LinkedList<ItemStack>();
                                String code = "";
                                ItemMeta im = current.getItemMeta();
                                if (im.hasLore()) {
                                    for (String loreLine : im.getLore()) {
                                        if (loreLine.contains("ID")) {
                                            code = loreLine.replace("§r§7ID: ", "");
                                            attachments = (List<ItemStack>) packagesConfig.getList(code, new LinkedList<ItemStack>());
                                            break;
                                        }
                                    }
                                }
                                if (attachments.size() < getConfig().getInt("max_attachments", 4)) {
                                    if (code.equals("")) {
                                        code = UUID.randomUUID().toString();
                                        List<String> lore = im.getLore();
                                        lore.add("§r§7ID: "+code);
                                        im.setLore(lore);
                                    }
                                    attachments.add(cursor.clone());
                                    packagesConfig.set(code, attachments);
                                    try {
                                        packagesConfig.save(packagesFile);
                                        e.getWhoClicked().sendMessage(ChatColor.GOLD+cursor.getType().name()+" x"+cursor.getAmount()+" attached.");
                                        im.setDisplayName("§rPackage");
                                        current.setItemMeta(im);
                                        e.setCursor(new ItemStack(Material.AIR));
                                        //cursor.setType(Material.AIR);
                                        //cursor.setAmount(0);
                                        e.setResult(Event.Result.DENY);
                                    } catch (Exception ex) {
                                        e.getWhoClicked().sendMessage(ChatColor.GOLD+"Could not attach the item.");
                                    }
                                } else {
                                    e.getWhoClicked().sendMessage(ChatColor.GOLD+"Max items already attached. ("+getConfig().getInt("max_attachments", 4)+")");
                                    e.setResult(Event.Result.DENY);
                                }
                            }
                        }
                    }
                }
            }
            
            /* Detach */
            if (e.isRightClick() && e.getCursor() != null && e.getCursor().getType() != Material.AIR && (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)) {
                if (e.getCursor().getType() == Material.WRITTEN_BOOK || e.getCursor().getType() == Material.BOOK_AND_QUILL) {
                    if (e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasDisplayName() && e.getCursor().getItemMeta().getDisplayName().contains("Package") && e.getCursor().getItemMeta().hasLore()) {
                        for (String loreLine : e.getCursor().getItemMeta().getLore()) {
                            if (loreLine.contains("ID")) {
                                String code = loreLine.replace("§r§7ID: ", "");
                                if (packagesConfig.contains(code)) {
                                    
                                    List<ItemStack> attachments = (List<ItemStack>) packagesConfig.getList(code, new LinkedList<ItemStack>());
                                    
                                    e.setCurrentItem(attachments.get(0));
                                    attachments.remove(0);
                                    
                                    if (attachments.size() <= 0) {
                                        ItemMeta im = e.getCursor().getItemMeta();
                                        ArrayList<String> lore2 = (ArrayList<String>) im.getLore();
                                        for (String loreLine2 : (ArrayList<String>) lore2.clone()) {
                                            if (loreLine2.contains("ID")) {
                                                lore2.remove(loreLine2);
                                                break;
                                            }
                                        }
                                        im.setLore(lore2);
                                        e.getCursor().setItemMeta(im);
                                        attachments = null;
                                    }
                                    
                                    packagesConfig.set(code, attachments);
                                    
                                    try {
                                        packagesConfig.save(packagesFile);
                                        e.setResult(Event.Result.DENY);
                                    } catch (Exception ex) {
                                        e.getWhoClicked().sendMessage(ChatColor.GOLD+"Could not detach item.");
                                        ex.printStackTrace();
                                    }
                                    
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        /* Save Mailbox on Close */
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
            if (e.getInventory().getName().equals("Mailbox")) {
                List<BookMeta> letters = new LinkedList<BookMeta>();
                for (ItemStack is : e.getInventory().getContents()) { // TODO Should do anything about extra letters?
                    if (is != null && is.hasItemMeta()) {
                        if (!(is.getItemMeta() instanceof org.bukkit.inventory.meta.BookMeta)) {
                            // TODO Throw items that can't be stored on the ground
                        } else {
                            letters.add((org.bukkit.inventory.meta.BookMeta) is.getItemMeta());
                        }
                    }
                }
                mailboxesConfig.set(e.getPlayer().getUniqueId()+".letters", letters);
                mailboxesConfig.set(e.getPlayer().getUniqueId()+".unread", false);
                //setMailboxFlag(false, loc); TODO Update mailbox flag on read
                try {
                    mailboxesConfig.save(mailboxesFile);
                } catch (Exception ex) {
                    getLogger().log(Level.INFO, "Failed to save {0}''s mailbox.", e.getPlayer().getName());
                    ex.printStackTrace();
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
        
        /*@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
        public void onCraft(org.bukkit.event.inventory.CraftItemEvent e) { // Detect stationary crafting
            if (e.getRecipe().getResult().hasItemMeta() && e.getRecipe().getResult().getItemMeta().hasLore() && e.getRecipe().getResult().getItemMeta().getDisplayName().equals(stationaryMeta.getDisplayName())) {
                ItemStack result = e.getRecipe().getResult();
                ItemMeta im = result.getItemMeta();
                List<String> lore = im.getLore();
                lore.add("§r§7Code: "+UUID.randomUUID());
                im.setLore(lore);
                result.setItemMeta(im);
                e.getInventory().setResult(result);
            }
        }*/
    }
    
    public final class LoginListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
            if (mailboxesConfig.contains(e.getPlayer().getUniqueId().toString()) && getConfig().getBoolean("login_notification")) {
                if (mailboxesConfig.getBoolean(e.getPlayer().getUniqueId()+".unread", false)) {
                    try {
                        Bukkit.getScheduler().runTaskLater(RealMail.this, new LoginRunnable(e), 20*10);
                    } catch (Exception ex) {
                        e.getPlayer().sendMessage(ChatColor.GOLD+"You've got mail! Check your mailbox.");
                    }
                }
            }
        }
    }
    
    public final class LoginRunnable implements Runnable {
        
        private org.bukkit.event.player.PlayerJoinEvent event;

        public LoginRunnable(org.bukkit.event.player.PlayerJoinEvent e) {
            this.event = e;
        }

        @Override
        public void run() {
           event.getPlayer().sendMessage(ChatColor.GOLD+"You've got mail! Check your mailbox.");
        }
        
    }
    
}

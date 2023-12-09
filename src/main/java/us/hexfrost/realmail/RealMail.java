package us.hexfrost.realmail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
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

	protected org.bukkit.configuration.file.FileConfiguration mailboxesConfig = null;
	private java.io.File mailboxesFile = null;
	protected org.bukkit.configuration.file.FileConfiguration packagesConfig = null;
	private java.io.File packagesFile = null;
	protected org.bukkit.configuration.file.FileConfiguration languageConfig = null;
	private java.io.File languageFile = null;
	private ItemMeta mailboxRecipeMeta = null;
	private org.bukkit.inventory.meta.BookMeta stationeryMeta = null;
	private String prefix = ChatColor.WHITE + "[" + ChatColor.GOLD + "Mail" + ChatColor.WHITE + "]";

	/* Mailbox Textures */
	private final String mailboxTextureBlue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNDllZmFhYWI1MzI1NTlmZmY5YWY3NWRhNmFjNGRkNzlkMTk5ZGNmMmZkNDk3Yzg1NDM4MDM4NTY0In19fQ==";
	private final String mailboxIdBlue = "48614330-6c44-47be-85ec-33ed037cf48c";
	private final String mailboxTextureWhite = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM5ZTE5NzFjYmMzYzZmZWFhYjlkMWY4NWZjOWQ5YmYwODY3NjgzZjQxMjk1NWI5NjExMTdmZTY2ZTIifX19";
	private final String mailboxIdWhite = "480bff09-ed89-4214-a2bd-dab19fa5177d";
	private final String mailboxTextureRed = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZhODljZTg1OTMyYmVjMWExYzNmMzFjYjdjMDg1YTViZmIyYWM3ZTQwNDA5NDIwOGMzYWQxMjM4NzlkYTZkYSJ9fX0=";
	private final String mailboxIdRed = "6a71ad04-2422-41f3-a501-6ea5707aaef3";
	private final String mailboxTextureGreen = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJiY2NiNTI0MDg4NWNhNjRlNDI0YTBjMTY4YTc4YzY3NmI4Yzg0N2QxODdmNmZiZjYwMjdhMWZlODZlZSJ9fX0=";
	private final String mailboxIdGreen = "60621c0e-cb3e-471b-a237-4dec155f4889";

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		ItemStack blueMailboxCoupon = new ItemStack(Material.PAPER, 1);
		mailboxRecipeMeta = blueMailboxCoupon.getItemMeta();
		mailboxRecipeMeta.setDisplayName("§rMailbox Recipe");
		mailboxRecipeMeta.setLore(Arrays.asList("§r§7Right-click with this coupon", "§r§7to get a mailbox"));
		blueMailboxCoupon.setItemMeta(mailboxRecipeMeta);
		ShapedRecipe blueMailboxRecipe = new ShapedRecipe(blueMailboxCoupon);
		blueMailboxRecipe.shape("  w", "iii", "ici");
		blueMailboxRecipe.setIngredient('w', org.bukkit.Material.WHITE_WOOL, -1);
		blueMailboxRecipe.setIngredient('i', org.bukkit.Material.IRON_INGOT);
		blueMailboxRecipe.setIngredient('c', org.bukkit.Material.CHEST);
		this.getServer().addRecipe(blueMailboxRecipe);

		ItemStack stationery = new ItemStack(Material.WRITABLE_BOOK, 1);
		stationeryMeta = (org.bukkit.inventory.meta.BookMeta) stationery.getItemMeta();
		stationeryMeta.setDisplayName("§rStationery");
		stationeryMeta.setLore(Arrays.asList("§r§7Right-click a mailbox to send after signing", "§r§7Use the name of the recipient as the title"));
		stationeryMeta.addPage("");
		stationery.setItemMeta(stationeryMeta);
		ShapelessRecipe stationeryRecipe = new ShapelessRecipe(stationery);
		stationeryRecipe.addIngredient(Material.PAPER);
		stationeryRecipe.addIngredient(Material.FEATHER);
		this.getServer().addRecipe(stationeryRecipe);

		if (getConfig().getString("prefix") != null) {
			prefix = getConfig().getString("prefix").replaceAll("&", "§");
		}
		prefix = prefix + " ";

		if (mailboxesFile == null) {
			mailboxesFile = new java.io.File(getDataFolder(), "mailboxes.yml");
		}
		mailboxesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(mailboxesFile);

		if (packagesFile == null) {
			packagesFile = new java.io.File(getDataFolder(), "packages.yml");
		}
		packagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(packagesFile);

		if (languageFile == null) {
			languageFile = new java.io.File(getDataFolder(), "language.yml");
			if (!languageFile.exists()) {
				try {
					InputStream in = getResource("language.yml");
					OutputStream out = new FileOutputStream(languageFile);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
					in.close();
				} catch (IOException ex) {
					if (getConfig().getBoolean("verbose_errors", false)) {
						getLogger().log(Level.WARNING, "Could not create a default languages.yml file.");
					}
				}
			}

		}
		languageConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(languageFile);

		if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("offline_mode", false)) {
			OfflineHandler.loadCaches();
		}

		// TODO Send analytics here

		getServer().getPluginManager().registerEvents(new MailListener(), this);
		getServer().getPluginManager().registerEvents(new LoginListener(), this);
		this.getCommand("realmail").setTabCompleter(new MailTabCompleter());

		getLogger().log(Level.INFO, "RealMail v{0} enabled.", getVersion());
	}

	@Override
	public void onDisable() {
		if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("offline_mode", false)) {
			OfflineHandler.saveCaches();
		}

		getLogger().log(Level.INFO, "RealMail v{0} disabled.", getVersion());
	}

	public String getVersion() {
		return getDescription().getVersion();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("realmail")) { // TODO Separate commands into separate files and then register with Bukkit
			//<editor-fold defaultstate="collapsed" desc="Intruction Commands">
			if (args.length == 0 || (args.length < 2 && args[0].equals("1"))) { // Show crafting
				sender.sendMessage(new String[]{
					ChatColor.GOLD + "" + ChatColor.BOLD + "RealMail - Crafting Recipes",
					ChatColor.GOLD + "Mailbox:",
					ChatColor.DARK_GRAY + "  --" + ChatColor.WHITE + "w   w" + ChatColor.WHITE + " = wool (1x)",
					ChatColor.GRAY + "  i i i   i" + ChatColor.WHITE + " = iron ingot (5x)",
					ChatColor.GRAY + "  i " + ChatColor.DARK_RED + "c" + ChatColor.GRAY + "i   " + ChatColor.DARK_RED + "c" + ChatColor.WHITE + " = chest (1x)",
					ChatColor.GOLD + "Stationery:",
					ChatColor.WHITE + "  1x paper and 1x feather",
					ChatColor.WHITE + "Use /mail 2 for usage"
				});
			} else if (args.length < 2) {
				if (args[0].equals("2")) { // Show usage
					sender.sendMessage(new String[]{
						ChatColor.GOLD + "" + ChatColor.BOLD + "RealMail - Usage information",
						ChatColor.GOLD + "Sending a letter:",
						ChatColor.WHITE + "  1. Craft some stationery" + (getConfig().getBoolean("let_players_spawn_stationary", false) ? " or use /mail new" : ""),
						ChatColor.WHITE + "  2. Type your letter",
						ChatColor.WHITE + "- Type [Subject:mySubject] on first line for subject",
						ChatColor.WHITE + "  3. Attach items if you wish (see /mail 3)",
						ChatColor.WHITE + "  4. Sign the book/stationery as the recipient's username",
						ChatColor.WHITE + "  5. Right-click a mailbox with the letter",
						ChatColor.WHITE + "Use /mail 3 for packaging"
					});
				} else if (args[0].equals("3")) { // Show attachments
					sender.sendMessage(new String[]{
						ChatColor.GOLD + "" + ChatColor.BOLD + "RealMail - Packaging",
						ChatColor.GOLD + "Attach:",
						ChatColor.WHITE + "  1. Pick up the item to be attached with your cursor",
						ChatColor.WHITE + "  2. Drop it/left-click again on the letter (" + getConfig().getInt("max_attachments", 4) + " stacks max)",
						ChatColor.GOLD + "Detach:",
						ChatColor.WHITE + "  1. Pick up the package with your cursor",
						ChatColor.WHITE + "  2. Right-click empty slots with the package",
						ChatColor.WHITE + "Example: http://bit.ly/1Cijgbl",
						sender.hasPermission("realmail.admin.seeAdminHelp") ? ChatColor.WHITE + "Use /mail 4 for administration" : ChatColor.WHITE + "Use /mail 1 for crafting"
					});
				} else if (args[0].equals("4")) { // Show adminministration
					if (sender.hasPermission("realmail.admin.seeAdminHelp")) {
						sender.sendMessage(new String[]{
							ChatColor.GOLD + "" + ChatColor.BOLD + "RealMail - Administration",
							ChatColor.GOLD + "/mail send " + ChatColor.WHITE + " Send the letter in your hand to the addressed player",
							ChatColor.GOLD + "/mail bulksend " + ChatColor.WHITE + " Send the letter in your hand to all players with mailboxes",
							ChatColor.GOLD + "/mail spawn <mailbox|stationery> " + ChatColor.WHITE + " Spawn in a mailbox or some stationery",
							ChatColor.GOLD + "/mail open [player] " + ChatColor.WHITE + " Open your mailbox or that of another player",
							ChatColor.WHITE + "Use /mail 1 for crafting"
						});
					} else {
						sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.seeAdminHelp", "You do not have permission to see the admin commands."));
					}
				}
			} //</editor-fold>

			if (args.length >= 1 && args[0].equals("version")) {
				sender.sendMessage(new String[]{ChatColor.GOLD + "RealMail v" + getVersion(), "Go to http://dev.bukkit.org/bukkit-plugins/realmail/ for updates."});
			} else if (args.length >= 1 && args[0].equals("reload")) {
				Bukkit.getServer().getPluginManager().getPlugin("RealMail").reloadConfig();
			} else if (!(sender instanceof Player)) {
				sender.sendMessage(prefix + ChatColor.WHITE + "This command can only be run by a player.");
			} else {
				//<editor-fold defaultstate="collapsed" desc="Player-only Commands">
				Player player = (Player) sender;
				if (args.length >= 1) {
					if (args[0].equals("send")) {
						if (player.hasPermission("realmail.admin.sendmailAnywhere")) {
							ItemStack itemHand = player.getItemInHand();
							if (itemHand.getType() == Material.WRITTEN_BOOK && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName() && (itemHand.getItemMeta().getDisplayName().contains("Letter") || itemHand.getItemMeta().getDisplayName().contains("Package"))) {
								BookMeta bookMeta = (BookMeta) itemHand.getItemMeta();
								sendMail(itemHand, player, Bukkit.getOfflinePlayer(bookMeta.getTitle()).getUniqueId(), true);
							} else {
								sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.onlyLettersAndPackages", "You may only send letters and packages."));
							}
						} else {
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.sendFromAnywhere", "You do not have permission to send mail from anywhere."));
						}
					} else if (args[0].equals("bulksend")) {
						if (player.hasPermission("realmail.admin.bulkmail")) {
							ItemStack itemHand = player.getItemInHand();

							// If valid letter/package
							if (itemHand.getType() == Material.WRITTEN_BOOK && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName() && (itemHand.getItemMeta().getDisplayName().contains("Letter") || itemHand.getItemMeta().getDisplayName().contains("Package"))) {

								List<ItemStack> attachments = new ArrayList<>();
								
								// Check if the item is a package
								boolean isPackage = itemHand.getItemMeta().getDisplayName().contains("Package") && itemHand.getItemMeta().hasLore();
								String originalCode = "";
								if (isPackage) {
									// Get original code
									for (String l : itemHand.getItemMeta().getLore())
										if (l.contains("ID"))
											originalCode = l.replace("§r§7ID: ", "");
									
									// Get original attachments
									attachments = (List<ItemStack>)packagesConfig.getList(originalCode, new LinkedList<>());
								}

								// Loop through all known players
								List<String> playerUUIDs = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());
								for (String uuid : playerUUIDs) {
									
									// Update lore and packages config if package
									if (isPackage) {
										String newCode = generatePackageCode();
										
										// Update lore with new code
										ItemMeta im = itemHand.getItemMeta();
										List<String> lore = im.getLore();
										for (int l = 0; l < lore.size(); l++)
											if (lore.get(l).contains("ID"))
												lore.set(l, "§r§7ID: " + newCode);
										im.setLore(lore);
										itemHand.setItemMeta(im);
										
										// Duplicate attachments
										List<ItemStack> newAttachments = new LinkedList<>();
										for (ItemStack a : attachments)
											newAttachments.add(a.clone());
										
										// Add new entry to packages config
										packagesConfig.set(newCode, newAttachments);
									}
									
									// Send letter/package
									sendMail(itemHand, player, OfflineHandler.getLocalUUID(UUID.fromString(uuid)), false, true);
								}

								// After looping through the players, get rid of the original attachment code and save the new ones
								if (itemHand.getItemMeta().getDisplayName().contains("Package") && itemHand.getItemMeta().hasLore()) {
									packagesConfig.set(originalCode, null);
									try {
										packagesConfig.save(packagesFile);
									} catch (Exception ex) {
										if (getConfig().getBoolean("verbose_errors", false)) {
											Bukkit.getLogger().log(Level.INFO, "Failed to save newly generated package UUIDs.");
										}
									}
								}
								sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.bulkSent", "Letter sent to all players on the server."));
							} else {
								sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.onlyLettersAndPackages", "You may only send signed letters and packages."));
							}
						} else {
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.bulkSend", "You do not have permission to mail players in bulk."));
						}
					} else if (args[0].equals("spawn")) {
						if (args.length != 2) {
							sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("commandSyntax.spawn", "Command syntax: /realmail spawn <mailbox|stationery>"));
						} else if (args[1].equals("mailbox")) {
							if (player.hasPermission("realmail.admin.spawn.mailbox")) {
								giveMailbox(player);
							} else {
								player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.spawnMailbox", "You do not have permission to spawn mailboxes."));
							}
						} else if (args[1].equals("stationary") || args[1].equals("stationery")) {
							if (player.hasPermission("realmail.admin.spawn.stationary")) {
								giveStationery(player);
							} else {
								player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.spawnStationary", "You do not have permission to spawn stationery."));
							}
						} else {
							sender.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("commandSyntax.spawn", "Command syntax: /realmail spawn <mailbox|stationery>"));
						}
					} else if (args[0].equals("open")) {
						if (args.length >= 2) {
							if (player.hasPermission("realmail.admin.openMailboxAnywhere.others")) {
								if (!mailboxesConfig.getList(OfflineHandler.getPublicUUID(Bukkit.getOfflinePlayer(args[1]).getUniqueId()) + ".letters", new LinkedList<String>()).isEmpty()) {
									openMailbox(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), player);
								} else {
									player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.doesntHaveMailbox", "{0} does not have a mailbox.").replaceAll("\\{0}", args[1]));
								}
							} else {
								player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.viewOtherMailbox", "You do not have permission to view other players' mailboxes."));
							}
						} else if (player.hasPermission("realmail.admin.openMailboxAnywhere")) {
							openMailbox(player.getUniqueId(), player);
						} else {
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.openCommand", "You do not have permission to view your mailbox via command."));
						}
					} else if (args[0].equals("new")) {
						if (player.hasPermission("realmail.admin.spawn.stationary") || getConfig().getBoolean("let_players_spawn_stationary", false)) {
							giveStationery(player);
						} else {
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.spawnStationary", "You do not have permission to spawn stationery.")); // TODO Start replaces with language compatible here
						}
					} else if (args[0].equals("clear")) {
						UUID publicUUID = OfflineHandler.getPublicUUID(player.getUniqueId());
						mailboxesConfig.set(publicUUID + ".letters", new LinkedList<BookMeta>());
						mailboxesConfig.set(publicUUID + ".unread", false);
						RealMail.this.updateMailboxFlags(publicUUID);
						try {
							mailboxesConfig.save(mailboxesFile);
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.clearMail", "You have cleared all letters and packages from your mailbox."));
						} catch (Exception ex) {
							getLogger().log(Level.INFO, "Failed to clear {0}''s mailbox.", player.getName());
							if (getConfig().getBoolean("verbose_errors", false)) {
								ex.printStackTrace();
							}
							player.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.clearMailFailed", "Your mailbox couldn't be cleared due to an error."));
						}
					}
				}
//</editor-fold>
			}

		}

		return true;
	}

	public void giveMailbox(Player ply) {
		giveMailbox(ply, "Blue");
	}
	public void giveMailbox(Player ply, String color) {
		String id;
		String base64;
		switch (color) {
			case "Blue":
				id = mailboxIdBlue;
				base64 = mailboxTextureBlue;
				break;
			case "White":
				id = mailboxIdWhite;
				base64 = mailboxTextureWhite;
				break;
			case "Red":
				id = mailboxIdRed;
				base64 = mailboxTextureRed;
				break;
			case "Green":
				id = mailboxIdGreen;
				base64 = mailboxTextureGreen;
				break;
			default:
				return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("minecraft:give ");
		sb.append(ply.getName());
		sb.append(" minecraft:player_head{display:{Name:\"{\\\"text\\\":\\\"Mailbox\\\", \\\"color\\\":\\\"reset\\\", \\\"italic\\\":false}\",Lore:[\"{\\\"text\\\":\\\"");
		sb.append(color);
		sb.append("\\\", \\\"color\\\":\\\"gray\\\", \\\"italic\\\":false}\",\"{\\\"text\\\":\\\"Punch to change texture\\\", \\\"color\\\":\\\"gray\\\",\\\"italic\\\":false}\"]},SkullOwner:{Id:\"");
		sb.append(id);
		sb.append("\",Properties:{textures:[{Value:\"");
		sb.append(base64);
		sb.append("\"}]}}} 1");
		getServer().dispatchCommand(getServer().getConsoleSender(), sb.toString());
	}

	public void giveStationery(Player ply) {
		ItemStack stationery = new ItemStack(Material.WRITABLE_BOOK, 1);
		stationery.setItemMeta(stationeryMeta);
		ply.getInventory().addItem(stationery);
	}

	public boolean openMailbox(UUID localUUID, Player viewer) {
		String name = Bukkit.getOfflinePlayer(localUUID).getName();
		UUID publicUUID = OfflineHandler.getPublicUUID(localUUID);
		if (name == null) {
			name = "Someone";
		}
		String title = name + "'s Mailbox";
		if (title.length() > 32) {
			title = title.replace("'s Mailbox", "");
		}
		if (title.length() > 32) {
			title = "Mailbox";
			viewer.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.openedMailbox", "Opened {0}'s mailbox.").replaceAll("\\{0}", name));
		}
		Inventory mailInv = Bukkit.createInventory(viewer, getConfig().getInt("mailbox_rows", 2) * 9, title);
		@SuppressWarnings("unchecked")
		List<BookMeta> letters = (List<BookMeta>)mailboxesConfig.getList(publicUUID + ".letters", new LinkedList<BookMeta>());
		for (BookMeta letterMeta : letters) {
			ItemStack newBook;
			if (letterMeta.getDisplayName().contains("Stationary") || letterMeta.getDisplayName().contains("Stationery")) {
				newBook = new ItemStack(Material.WRITABLE_BOOK, 1);
			} else {
				newBook = new ItemStack(Material.WRITTEN_BOOK, 1);
			}
			newBook.setItemMeta(letterMeta);
			HashMap<Integer, ItemStack> leftover = mailInv.addItem(newBook);
			if (!leftover.isEmpty()) {
				viewer.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.notAllShown", "Not all letters could be shown. Please empty your mailbox."));
				break;
			}
		}
		viewer.openInventory(mailInv);
		return true;
	}

	public boolean sendMail(ItemStack mailItem, Player fromPlayer, UUID localUUID, boolean sendMessages) {
		return sendMail(mailItem, fromPlayer, localUUID, sendMessages, false);
	}
	public boolean sendMail(ItemStack mailItem, Player fromPlayer, UUID localUUID, boolean sendMessages, boolean bulkMail) {
		UUID onlineUUID = OfflineHandler.getPublicUUID(localUUID);
		if (mailItem.getType() != Material.WRITTEN_BOOK) {
			return false;
		}
		BookMeta mailMeta = (BookMeta) mailItem.getItemMeta();
		if (bulkMail)
			mailMeta.setTitle("Everyone");
		if (mailboxesConfig.getList(onlineUUID + ".letters", new LinkedList<>()).size() < (getConfig().getInt("mailbox_rows", 4) * 9)) {
			java.util.Date dateRaw = java.util.Calendar.getInstance().getTime();
			java.text.SimpleDateFormat format = new java.text.SimpleDateFormat();
			format.applyPattern(getConfig().getString("dateformat"));
			String dateString = format.format(dateRaw);

			List<String> oldLore = (List<String>)mailMeta.getLore();
			List<String> lore = (List<String>)new LinkedList<>(Arrays.asList("§r§7To: " + mailMeta.getTitle(), "§r§7Date: " + dateString));
			for (String oldLoreLine : oldLore) {
				if (oldLoreLine.contains("ID")) {
					lore.add(oldLoreLine);
					break;
				}
			}
			mailMeta.setLore(lore);

			@SuppressWarnings("unchecked")
			List<BookMeta> letters = (List<BookMeta>)mailboxesConfig.getList(onlineUUID + ".letters", new LinkedList<>());
			letters.add(mailMeta);
			mailboxesConfig.set(onlineUUID + ".letters", letters);
			mailboxesConfig.set(onlineUUID + ".unread", true);
			try {
				mailboxesConfig.save(mailboxesFile);
				fromPlayer.getInventory().remove(mailItem);
				if (sendMessages) {
					fromPlayer.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.letterSent", "Letter sent to {0}.").replaceAll("\\{0}", Bukkit.getOfflinePlayer(onlineUUID).getName()));
					if (getConfig().getBoolean("enable_sounds", true)) {
						fromPlayer.playSound(fromPlayer.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.2f);
					}
				}
				updateMailboxFlags(localUUID);
				if (Bukkit.getPlayer(localUUID) != null) {
					Bukkit.getPlayer(localUUID).sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.gotMail", "You've got mail! Check your mailbox. Use /mail to learn how to craft one."));
					if (getConfig().getBoolean("enable_sounds", true)) {
						Bukkit.getPlayer(localUUID).playSound(Bukkit.getPlayer(localUUID).getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.8f, 1.2f);
						Bukkit.getScheduler().runTaskLater(this, new Runnable() {
							private UUID playerID;

							@Override
							public void run() {
								Bukkit.getPlayer(playerID).playSound(Bukkit.getPlayer(playerID).getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.8f, 1.4f);
							}

							private Runnable init(UUID id) {
								playerID = id;
								return this;
							}
						}.init(localUUID), 10);
					}
				}
			} catch (Exception ex) {
				fromPlayer.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.failedToSend", "Failed to send the letter."));
				if (getConfig().getBoolean("verbose_errors", false)) {
					ex.printStackTrace();
				}
			}
		} else {
			if (sendMessages) {
				fromPlayer.sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.mailboxFull", "Recipient's mailbox was full. Please try again later."));
			}
			if (Bukkit.getPlayer(localUUID) != null) {
				Bukkit.getPlayer(localUUID).sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.mailboxFullReceiver", "{0} tried to send you mail, but you mailbox was full. Consider emptying it out.").replaceAll("\\{0}", fromPlayer.getName()));
			}
		}
		return true;
	}

	public boolean updateMailboxFlags(UUID localUUID) {
		return true;
	}
	
	/**
	 * Generate a new package code guaranteed to not conflict with an existing code
	 * @return 8 character UUID substring
	 */
	public String generatePackageCode() {
		String code;
		do
			code = UUID.randomUUID().toString().substring(0, 8);
		while (packagesConfig.contains(code));
		return code;
	}

	public final class MailListener implements org.bukkit.event.Listener {

		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onUseItemEvent(org.bukkit.event.player.PlayerInteractEvent e) {
			if (e.getItem() != null) {
				ItemStack is = e.getItem();
				ItemStack toBeRemoved = is.clone();
				toBeRemoved.setAmount(1);
				/* Exchange Coupon */
				if (is.getType() == Material.PAPER && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains("Mailbox Recipe")) {
					e.getPlayer().getInventory().removeItem(toBeRemoved);
					giveMailbox(e.getPlayer());
					e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.exchangedRecipe", "You exchanged your recipe for a mailbox."));
				} /* Cycle texture */ else if (is.getType() == Material.PLAYER_HEAD && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && is.getItemMeta().hasLore() && is.getItemMeta().getLore().get(1).contains("Punch to change texture")) {
					e.getPlayer().getInventory().removeItem(toBeRemoved);
					if (is.getItemMeta().getLore().get(0).contains("Blue")) {
						giveMailbox(e.getPlayer(), "White");
						//getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:give " + e.getPlayer().getName() + " minecraft:skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7White\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\"" + mailboxIdWhite + "\",Name:\"ha1fBit\",Properties:{textures:[{Value:\"" + mailboxTextureWhite + "\"}]}}}");
					} else if (is.getItemMeta().getLore().get(0).contains("White")) {
						giveMailbox(e.getPlayer(), "Red");
						//getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:give " + e.getPlayer().getName() + " minecraft:skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Red\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\"" + mailboxIdRed + "\",Name:\"ha1fBit\",Properties:{textures:[{Value:\"" + mailboxTextureRed + "\"}]}}}");
					} else if (is.getItemMeta().getLore().get(0).contains("Red")) {
						giveMailbox(e.getPlayer(), "Green");
						//getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:give " + e.getPlayer().getName() + " minecraft:skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Green\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\"" + mailboxIdGreen + "\",Name:\"ha1fBit\",Properties:{textures:[{Value:\"" + mailboxTextureGreen + "\"}]}}}");
					} else if (is.getItemMeta().getLore().get(0).contains("Green")) {
						giveMailbox(e.getPlayer(), "Blue");
						//getServer().dispatchCommand(getServer().getConsoleSender(), "minecraft:give " + e.getPlayer().getName() + " minecraft:skull 1 3 {display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\"" + mailboxIdBlue + "\",Name:\"ha1fBit\",Properties:{textures:[{Value:\"" + mailboxTextureBlue + "\"}]}}}");
					}
					e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.textureChange", "You changed your mailbox's texture."));
				} /* Stationery Stuff */ else if (is.getType() == Material.WRITTEN_BOOK && is.hasItemMeta() && is.getItemMeta().hasLore() && is.getItemMeta().hasDisplayName() && (is.getItemMeta().getDisplayName().contains("§rLetter") || is.getItemMeta().getDisplayName().contains("§rPackage"))) {
					if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.PLAYER_HEAD)) {

						@SuppressWarnings("unchecked")
						List<String> players = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());

						playersLoop:
						for (String p : players) {
							@SuppressWarnings("unchecked")
							List<Location> playersMailboxLocations = (List<Location>)mailboxesConfig.getList(OfflineHandler.getPublicUUID(UUID.fromString(p)) + ".mailboxes", new LinkedList<>());
							for (Location loc : playersMailboxLocations) {
								if (e.getClickedBlock().getLocation().equals(loc)) {
									org.bukkit.inventory.meta.BookMeta newLetter = (org.bukkit.inventory.meta.BookMeta) is.getItemMeta();
									if (e.getPlayer().hasPermission("realmail.user.sendmail")) {
										if (getConfig().getBoolean("universal_mailboxes", false) || (!getConfig().getBoolean("universal_mailboxes", false) && p.equals(e.getPlayer().getUniqueId() + "")) || e.getPlayer().hasPermission("realmail.admin.sendmailAnywhere")) {
											ItemStack newLetterItem = new ItemStack(Material.WRITTEN_BOOK);
											newLetterItem.setItemMeta(newLetter);
											RealMail.this.sendMail(newLetterItem, e.getPlayer(), Bukkit.getOfflinePlayer(newLetter.getTitle()).getUniqueId(), true);
											e.setCancelled(true);
										} else {
											e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.notYourMailbox", "That's not your mailbox. Use /mail to find out how to craft your own."));
										}
									} else {
										e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.sendMail", "You do not have permission to send mail."));
									}
									break playersLoop;
								}
							}
						}

					}
				}
			} // End empty hand detection

			/* Open Mailbox */
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.PLAYER_HEAD) && e.getPlayer().getItemInHand().getType() != Material.WRITTEN_BOOK) {
				@SuppressWarnings("unchecked")
				List<String> players = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());
				OfflinePlayer mailboxOwner = null;
				for (String p : players) {
					@SuppressWarnings("unchecked")
					List<Location> locations = (List<Location>)mailboxesConfig.getList(OfflineHandler.getPublicUUID(UUID.fromString(p)) + ".mailboxes", new LinkedList<>());
					for (Location loc : locations) {
						if (e.getClickedBlock().getLocation().equals(loc)) {
							mailboxOwner = Bukkit.getOfflinePlayer(OfflineHandler.getLocalUUID(UUID.fromString(p)));
						}
					}
				}
				if (mailboxOwner != null) {
					if (getConfig().getBoolean("universal_mailboxes", false)) {
						openMailbox(e.getPlayer().getUniqueId(), e.getPlayer());
					} else if (getConfig().getBoolean("lock_mailboxes", true)) {
						if (mailboxOwner.getUniqueId().equals(e.getPlayer().getUniqueId()) || e.getPlayer().hasPermission("realmail.admin.openMailboxAnywhere.others")) {
							openMailbox(e.getPlayer().getUniqueId(), e.getPlayer());
						} else {
							e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.notYourMailbox", "That's not your mailbox. Use /mail to find out how to craft your own."));
						}
					} else {
						openMailbox(e.getPlayer().getUniqueId(), e.getPlayer());
					}
				}
			}
		}

		//<editor-fold defaultstate="collapsed" desc="Signing Letters">
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onEditBook(org.bukkit.event.player.PlayerEditBookEvent e) {
			if (e.getPreviousBookMeta() != null && e.getPreviousBookMeta().hasDisplayName() && (e.getPreviousBookMeta().getDisplayName().contains("Stationary") || e.getPreviousBookMeta().getDisplayName().contains("Stationery") || e.getPreviousBookMeta().getDisplayName().contains("Package"))) {

				BookMeta newBM = e.getNewBookMeta();
				if (e.getPreviousBookMeta().getDisplayName().contains("Package")) {
					newBM.setDisplayName("§rPackage");
					newBM.setLore(e.getPreviousBookMeta().getLore());
				} else {
					newBM.setDisplayName("§rStationery");
					newBM.setLore(Arrays.asList("§r§7Right-click a mailbox to send after signing", "§r§7Use the name of the recipient as the title"));
				}
				e.setNewBookMeta(newBM);

				if (e.isSigning()) {
					if (newBM.getDisplayName().contains("Stationary") || newBM.getDisplayName().contains("Stationery")) {
						newBM.setDisplayName("§rLetter");
					}

					List<String> bookLore = newBM.getLore();
					bookLore.add("§r§7To: " + newBM.getTitle());
					newBM.setLore(bookLore);

					if (newBM.getPageCount() >= 1) {
						String firstPage = newBM.getPages().get(0); // [subject|subj|s:Test Subject;moon|moonrune|rune;burn|burnonread|selfdestruct|destruct]
						firstPage = firstPage.split("\n")[0];
						if (firstPage.matches("^(.*)\\[Subject:(.*)\\](.*)$")) { // [Subject:Test Subject]

							firstPage = firstPage.replaceFirst("^(.*)\\[Subject:", "");
							firstPage = firstPage.replaceFirst("\\](.*)", "");
							newBM.setDisplayName(newBM.getDisplayName() + " - " + firstPage);
						}
					}

					// Check if the recipient exists before signing
					if (mailboxesConfig.getList("players", new LinkedList<String>()).contains(OfflineHandler.getPublicUUID(Bukkit.getOfflinePlayer(e.getNewBookMeta().getTitle()).getUniqueId()) + "")) {
						e.setNewBookMeta(newBM);
					} else {
						e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.unknownRecipient", "Could not sign. {0} is not a known player on this server.").replaceAll("\\{0}", e.getNewBookMeta().getTitle()));
						e.setSigning(false);
					}
				}
			}
		}
		//</editor-fold>

		@SuppressWarnings("unchecked")
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
			//<editor-fold defaultstate="collapsed" desc="Only letters and attachments in mailboxes and no villagers">
			if (e.getView().getTitle().contains("Mailbox")) {
				ItemStack cursor = e.getCursor();
				ItemStack current = e.getCurrentItem();

				boolean allowCursor = false;
				boolean allowCurrent = false;

				if (cursor == null || cursor.getType() == Material.AIR) {
					allowCursor = true;
				} else if (cursor.hasItemMeta()) {
					if (cursor.getItemMeta().hasDisplayName()) {
						if (cursor.getItemMeta().getDisplayName().contains("Stationary") || cursor.getItemMeta().getDisplayName().contains("Stationery") || cursor.getItemMeta().getDisplayName().contains("Letter") || cursor.getItemMeta().getDisplayName().contains("Package")) {
							allowCursor = true;
						}
					}
				}
				if (current == null || current.getType() == Material.AIR) {
					allowCurrent = true;
				} else if (current.hasItemMeta()) {
					if (current.getItemMeta().hasDisplayName()) {
						if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Stationery") || current.getItemMeta().getDisplayName().contains("Letter") || current.getItemMeta().getDisplayName().contains("Package")) {
							allowCurrent = true;
						}
					}
				}
				if ((!allowCursor) || (!allowCurrent)) {
					e.setCancelled(true);
				}
			} else if (e.getInventory().getType() == InventoryType.MERCHANT) {
				ItemStack cursor = e.getCursor();
				ItemStack current = e.getCurrentItem();

				boolean disallowCursor = false;
				boolean disallowCurrent = false;

				if (cursor != null && cursor.hasItemMeta()) {
					if (cursor.getItemMeta().hasDisplayName()) {
						if (cursor.getItemMeta().getDisplayName().contains("Stationary") || cursor.getItemMeta().getDisplayName().contains("Stationery") || cursor.getItemMeta().getDisplayName().contains("Letter") || cursor.getItemMeta().getDisplayName().contains("Package")) {
							disallowCursor = true;
						}
					}
				}
				if (current != null && current.hasItemMeta()) {
					if (current.getItemMeta().hasDisplayName()) {
						if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Stationery") || current.getItemMeta().getDisplayName().contains("Letter") || current.getItemMeta().getDisplayName().contains("Package")) {
							disallowCurrent = true;
						}
					}
				}
				if (disallowCursor || disallowCurrent) {
					e.setCancelled(true);
				}
			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="Attach items">
			if (e.isLeftClick()) { // TODO Fix all these creative bugs
				ItemStack current = e.getCurrentItem();
				ItemStack cursor = e.getCursor();

				if ((current == null || current.getType() == Material.WRITABLE_BOOK) && cursor != null && cursor.getType() != Material.AIR) {
					if (current != null && current.hasItemMeta()) {
						if (current.getItemMeta().hasDisplayName()) {
							if (current.getItemMeta().getDisplayName().contains("Stationary") || current.getItemMeta().getDisplayName().contains("Stationery") || current.getItemMeta().getDisplayName().contains("Package")) {
								if (cursor != null && cursor.hasItemMeta() && cursor.getItemMeta().hasDisplayName() && cursor.getItemMeta().getDisplayName().contains("Package")) {
									e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.packageInPackage", "You can't put packages inside of packages. You'll create package-ception."));
									e.setResult(Event.Result.DENY);
								} else if (e.getWhoClicked().hasPermission("realmail.user.attach")) {
									if (e.getClick() != ClickType.CREATIVE) {
										List<ItemStack> attachments = new LinkedList<>();
										String code = "";
										ItemMeta im = current.getItemMeta();
										if (im.hasLore()) {
											for (String loreLine : im.getLore()) {
												if (loreLine.contains("ID")) {
													code = loreLine.replace("§r§7ID: ", "");
													attachments = (List<ItemStack>)packagesConfig.getList(code, new LinkedList<>());
													break;
												}
											}
										}
										if (attachments.size() < getConfig().getInt("max_attachments", 4) || e.getWhoClicked().hasPermission("realmail.admin.bypassAttachmentLimits")) {
											if (code.equals("")) {
												code = generatePackageCode();
												List<String> lore = im.getLore();

												boolean hasDetachInstr = false;
												for (String detachLoreLine : lore) {
													if (detachLoreLine.contains("to detach")) {
														hasDetachInstr = true;
													}
												}

												if (!hasDetachInstr) {
													lore.add("§r§7Right-click empty slot with package to detach");
												}

												lore.add("§r§7ID: " + code);
												im.setLore(lore);
											}
											attachments.add(cursor.clone());
											packagesConfig.set(code, attachments);
											try {
												packagesConfig.save(packagesFile);
												e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + cursor.getType().name() + " x" + cursor.getAmount() + " attached.");
												im.setDisplayName("§rPackage");
												current.setItemMeta(im);
												e.setCursor(new ItemStack(Material.AIR));
//cursor.setType(Material.AIR);
//cursor.setAmount(0);
												e.setResult(Event.Result.DENY);
											} catch (Exception ex) {
												e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.couldNotAttach", "Could not attach the item."));
											}
										} else {
											e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.maxAlreadyAttached", "Max items already attached. ({0})").replaceAll("\\{0}", getConfig().getInt("max_attachments", 4) + ""));
											e.setResult(Event.Result.DENY);
										}
									} else {
										e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.attachInCreative", "Attaching and detaching items in creative is currently disabled due to bugs."));
									}
								} else {
									e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.attachments", "You do not have permission to attach items."));
									e.setResult(Event.Result.DENY);
								}
							}
						}
					}
				}
			}
//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="Detach items">
			if ((e.isRightClick() || e.getClick() == ClickType.CREATIVE) && e.getCursor() != null && e.getCursor().getType() != Material.AIR && (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)) {
				if (e.getCursor().getType() == Material.WRITTEN_BOOK || e.getCursor().getType() == Material.WRITABLE_BOOK) {
					if (e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasDisplayName() && e.getCursor().getItemMeta().getDisplayName().contains("Package") && e.getCursor().getItemMeta().hasLore()) {
						for (String loreLine : e.getCursor().getItemMeta().getLore()) {
							if (loreLine.contains("ID")) {
								String code = loreLine.replace("§r§7ID: ", "");
								if (packagesConfig.contains(code)) {

									List<ItemStack> attachments = (List<ItemStack>)packagesConfig.getList(code, new LinkedList<>());
									
									if (attachments.size() > 0) {
										e.setCurrentItem(attachments.get(0));
										attachments.remove(0);
									}

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
										e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.couldNotDetach", "Could not detach item."));
										if (getConfig().getBoolean("verbose_errors", false))
											ex.printStackTrace();
									}

								} else {
									e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.couldNotDetachCode", "Cound not detach item, unknown code."));
								}
								break;
							}
						}
					}
				}
			}
//</editor-fold>
		}

		//<editor-fold defaultstate="collapsed" desc="Save mailbox on close">
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
			if (e.getView().getTitle().contains("Mailbox")) {
				List<BookMeta> letters = new LinkedList<BookMeta>();
				for (ItemStack is : e.getInventory().getContents()) { // TODO Should do anything about extra letters?
					if (is != null && is.hasItemMeta() && is.getItemMeta() instanceof BookMeta) {
						letters.add((org.bukkit.inventory.meta.BookMeta) is.getItemMeta());
					}
				}
				String ownerName = e.getView().getTitle();
				ownerName = ownerName.replace("'s Mailbox", "");
				UUID publicUUID = OfflineHandler.getPublicUUID(Bukkit.getOfflinePlayer(ownerName).getUniqueId());
				mailboxesConfig.set(publicUUID + ".letters", letters);
				mailboxesConfig.set(publicUUID + ".unread", false);
				RealMail.this.updateMailboxFlags(Bukkit.getOfflinePlayer(ownerName).getUniqueId());
				try {
					mailboxesConfig.save(mailboxesFile);
				} catch (Exception ex) {
					getLogger().log(Level.INFO, "Failed to save {0}''s mailbox.", e.getPlayer().getName());
					if (getConfig().getBoolean("verbose_errors", false)) {
						ex.printStackTrace();
					}
				}
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Detect mailbox placing">
		@SuppressWarnings("unchecked")
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent e) {
			if (e.getItemInHand() != null) {
				ItemStack is = e.getItemInHand();

				if (is.getType() == Material.PLAYER_HEAD && is.getItemMeta().hasLore() && is.getItemMeta().getLore().size() >= 2 && is.getItemMeta().getLore().get(1).contains("Punch to change texture")) {

					List<Location> locations = (List<Location>)mailboxesConfig.getList(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()) + ".mailboxes", new LinkedList<>());
					locations.add(e.getBlock().getLocation());
					mailboxesConfig.set(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()) + ".mailboxes", locations);

					List<String> players = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());
					if (!players.contains(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()).toString())) {
						players.add(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()) + "");
					}
					mailboxesConfig.set("players", players);

					try {
						mailboxesConfig.save(mailboxesFile);
						e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.mailboxPlaced", "Mailbox placed."));
					} catch (Exception ex) {
						e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.failedToPlaceMailbox", "Failed to place mailbox."));
						if (getConfig().getBoolean("verbose_errors", false)) {
							ex.printStackTrace();
						}
					}
				}

			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Detect mailbox breaking">
		@SuppressWarnings("unchecked")
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent e) {
			List<String> playerUUIDs = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());
			for (String uuid : playerUUIDs) {
				List<Location> locations = (List<Location>)mailboxesConfig.getList(uuid + ".mailboxes", new LinkedList<>());
				for (Location loc : locations) {
					if (e.getBlock().getLocation().equals(loc)) {

						locations.remove(e.getBlock().getLocation());
						mailboxesConfig.set(uuid + ".mailboxes", locations);

						try {
							mailboxesConfig.save(mailboxesFile);
							e.setCancelled(true);
							e.getBlock().setType(Material.AIR);
							getServer().dispatchCommand(getServer().getConsoleSender(), "summon Item " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " {Item:{id:minecraft:skull, Count:1, Damage: 3, tag:{display:{Name:\"§rMailbox\",Lore:[\"§r§7Blue\",\"§r§7Punch to change texture\"]},SkullOwner:{Id:\"" + mailboxIdBlue + "\",Name:\"ha1fBit\",Properties:{textures:[{Value:\"" + mailboxTextureBlue + "\"}]}}}}}");
						} catch (Exception ex) {
							e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.failedToRemoveMailbox", "Failed to remove mailbox."));
							if (getConfig().getBoolean("verbose_errors", false)) {
								ex.printStackTrace();
							}
						}
						return;
					}
				}
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Crafting">
		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
		public void onCraft(org.bukkit.event.inventory.CraftItemEvent e) {
			if (e.getRecipe().getResult().hasItemMeta() && e.getRecipe().getResult().getItemMeta().hasLore() && e.getRecipe().getResult().getItemMeta().getDisplayName().contains(stationeryMeta.getDisplayName())) { // Stationery
				if (!e.getWhoClicked().hasPermission("realmail.user.craft.stationary")) {
					e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.craftStationary", "You do not have permission to craft stationery."));
					e.setResult(Event.Result.DENY);
				} else if (getConfig().getBoolean("reusable_feather", true) && e.getResult() == Event.Result.ALLOW || e.getResult() == Event.Result.DEFAULT) {
					ItemStack[] cTable = e.getInventory().getMatrix();
					for (ItemStack is : cTable) {
						if (is != null && is.getData().getItemType() == Material.FEATHER) {
							is.setAmount(is.getAmount() + 1);
						}
					}
				}
			} else if (e.getRecipe().getResult().hasItemMeta() && e.getRecipe().getResult().getItemMeta().hasLore() && e.getRecipe().getResult().getItemMeta().getDisplayName().contains(mailboxRecipeMeta.getDisplayName())) { // Mailbox
				if (!e.getWhoClicked().hasPermission("realmail.user.craft.mailbox")) {
					e.getWhoClicked().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("noperm.craftMailbox", "You do not have permission to craft a mailbox."));
					e.setResult(Event.Result.DENY);
				}
			}
		}
		//</editor-fold>
	}
	
	public final class LoginListener implements org.bukkit.event.Listener {

		@org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
		public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
			if (mailboxesConfig.getList("players", new LinkedList<String>()).contains(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()).toString())) {
				if (getConfig().getBoolean("login_notification")) {
					if (mailboxesConfig.getBoolean(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()) + ".unread", false)) {
						try {
							Bukkit.getScheduler().runTaskLater(RealMail.this, new LoginRunnable(e), 20 * 10);
						} catch (IllegalArgumentException ex) {
							e.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.gotMailLogin", "You've got mail! Check your mailbox."));
						}
					}
				}
			} else {
				@SuppressWarnings("unchecked")
				List<String> knownPlayers = (List<String>)mailboxesConfig.getList("players", new LinkedList<>());
				knownPlayers.add(OfflineHandler.getPublicUUID(e.getPlayer().getUniqueId()).toString());
				mailboxesConfig.set("players", knownPlayers);
				try {
					mailboxesConfig.save(mailboxesFile);
				} catch (IOException ex) {
					getLogger().log(Level.WARNING, "Failed to add {0} to the mail list.", e.getPlayer().getName());
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
			event.getPlayer().sendMessage(prefix + ChatColor.WHITE + languageConfig.getString("mail.gotMailLogin", "You've got mail! Check your mailbox."));
			if (getConfig().getBoolean("enable_sounds", true)) {
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.8f, 1.2f);
				Bukkit.getScheduler().runTaskLater(RealMail.this, new Runnable() {
					private Player player;

					@Override
					public void run() {
						player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.8f, 1.4f);
					}

					private Runnable init(Player ply) {
						player = ply;
						return this;
					}
				}.init(event.getPlayer()), 10);
			}
		}

	}

}

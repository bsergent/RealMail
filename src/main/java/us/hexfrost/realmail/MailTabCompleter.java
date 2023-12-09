package us.hexfrost.realmail;

import com.mysql.cj.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;

/**
 * @author Ben Sergent V @ http://hexfrost.us
 */
public class MailTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		final List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			StringUtil.copyPartialMatches(args[0], Arrays.asList(new String[] {"clear", "version"}), completions);
			if (StringUtils.startsWithIgnoreCase("bulkmail", args[0])
					&& sender.hasPermission("realmail.admin.bulkmail"))
				completions.add("bulksend");
			if (StringUtils.startsWithIgnoreCase("spawn", args[0])
					&& (sender.hasPermission("realmail.admin.spawn.mailbox") || sender.hasPermission("realmail.admin.spawn.stationery")))
				completions.add("spawn");
			if (StringUtils.startsWithIgnoreCase("new", args[0])
					&& (sender.hasPermission("realmail.admin.spawn.stationery")
						|| RealMail.getPlugin(RealMail.class).getConfig().getBoolean("let_players_spawn_stationary", false)))
				completions.add("new");
			if (StringUtils.startsWithIgnoreCase("open", args[0])
					&& (sender.hasPermission("realmail.admin.openMailboxAnywhere") || sender.hasPermission("realmail.admin.openMailboxAnywhere.others")))
				completions.add("open");
			if (StringUtils.startsWithIgnoreCase("send", args[0])
					&& sender.hasPermission("realmail.admin.sendmailAnywhere"))
				completions.add("send");
		} else if (args.length == 2) {
			switch (args[0]) {
				case "open":
					if (sender.hasPermission("realmail.admin.openMailboxAnywhere.others"))
						StringUtil.copyPartialMatches(args[1], getPlayers(), completions);
					break;
				case "spawn":
					if (StringUtils.startsWithIgnoreCase("spawn", args[1]) 
							&& (sender.hasPermission("realmail.admin.spawn.mailbox")))
						completions.add("mailbox");
					if (StringUtils.startsWithIgnoreCase("spawn", args[1]) 
							&& sender.hasPermission("realmail.admin.spawn.stationery"))
						completions.add("stationery");
					break;
			}
		}
		Collections.sort(completions);
		return completions;
	}
	
	public List<String> getPlayers() {
		FileConfiguration mailboxesConfig = RealMail.getPlugin(RealMail.class).mailboxesConfig;
		@SuppressWarnings("unchecked")
		final List<String> playersUUIDs = (List<String>)mailboxesConfig.getList("players", new ArrayList<>());
		final List<String> usernames = new ArrayList<>();
		for (String uuid : playersUUIDs)
			usernames.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		return usernames;
	}

}

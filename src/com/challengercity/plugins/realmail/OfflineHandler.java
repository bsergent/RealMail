package com.challengercity.plugins.realmail;

import com.evilmidget38.NameFetcher;
import com.evilmidget38.UUIDFetcher;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author Ben Sergent V at http://sergenttech.net/
 */
public class OfflineHandler {
    
    private static final HashMap<UUID, UUID> onlineCache = new HashMap<UUID, UUID>();
    private static final HashMap<UUID, UUID> localCache = new HashMap<UUID, UUID>();
    
    public static UUID getPublicUUID(UUID localUUID) {
        if (Bukkit.getOnlineMode()) {
            return localUUID;
        } else {
            if (onlineCache.containsKey(localUUID)) {
                return onlineCache.get(localUUID);
            }
            try {
                UUID id = UUIDFetcher.getUUIDOf(Bukkit.getOfflinePlayer(localUUID).getName());
                onlineCache.put(localUUID, id);
                return id;
            } catch (Exception ex) {
                if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                    Bukkit.getLogger().log(Level.INFO, "Failed to get online UUID for {0}.", Bukkit.getOfflinePlayer(localUUID).getName());
                }
                return localUUID;
            }
        }
    }
    
    public static UUID getLocalUUID(UUID onlineUUID) {
        if (Bukkit.getOnlineMode()) {
            return onlineUUID;
        } else {
            if (localCache.containsKey(onlineUUID)) {
                return onlineCache.get(onlineUUID);
            }
            Map<UUID, String> result;
            try {
                result = new NameFetcher(Arrays.asList(onlineUUID)).call();
                localCache.put(onlineUUID, Bukkit.getOfflinePlayer(result.get(onlineUUID)).getUniqueId());
                return Bukkit.getOfflinePlayer(result.get(onlineUUID)).getUniqueId();
            } catch (Exception ex) {
                if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                    Bukkit.getLogger().log(Level.INFO, "Failed to get offline UUID.");
                }
                return onlineUUID;
            }
        }
    }
    
    public static void loadCaches() {
        // TODO Load and save caches
    }
    
    public static void saveCaches() {
        
    }
    
}

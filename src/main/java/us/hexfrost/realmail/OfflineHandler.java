package us.hexfrost.realmail;

import com.evilmidget38.NameFetcher;
import com.evilmidget38.UUIDFetcher;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
    
    public static final String OFFLINECACHEFILE = RealMail.getPlugin(RealMail.class).getDataFolder()+"/offlineUUIDs.cache";
    
    private static HashMap<UUID, UUID> publicCache = new HashMap<UUID, UUID>();
    private static HashMap<UUID, UUID> localCache = new HashMap<UUID, UUID>();
    
    public static UUID getPublicUUID(UUID localUUID) {
        if (!RealMail.getPlugin(RealMail.class).getConfig().getBoolean("offline_mode", false)) {
            return localUUID;
        } else {
            if (publicCache.containsKey(localUUID)) {
                return publicCache.get(localUUID);
            }
            try {
                UUID id = UUIDFetcher.getUUIDOf(Bukkit.getOfflinePlayer(localUUID).getName());
                publicCache.put(localUUID, id);
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
        if (!RealMail.getPlugin(RealMail.class).getConfig().getBoolean("offline_mode", false)) {
            return onlineUUID;
        } else {
            if (localCache.containsKey(onlineUUID)) {
                return publicCache.get(onlineUUID);
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
    
    public static void saveCaches() {
        try {
            ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(OFFLINECACHEFILE)));
            try {
                output.writeObject(publicCache);
                output.writeObject(localCache);
                if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                    Bukkit.getLogger().log(Level.INFO, "Saved Offline UUIDs cache.");
                }
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                Bukkit.getLogger().log(Level.INFO, "Failed to save Offline UUIDs cache.");
            }
        }
    }
    
	@SuppressWarnings("unchecked")
    public static void loadCaches() {
         try {
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(OFFLINECACHEFILE)));
            try {
                publicCache = (HashMap<UUID, UUID>)input.readObject();
                localCache = (HashMap<UUID, UUID>)input.readObject();
                if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                    Bukkit.getLogger().log(Level.INFO, "Loaded Offline UUIDs cache.");
                }
            } finally {
                input.close();
            }
        } catch (ClassNotFoundException ex){
            if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                Bukkit.getLogger().log(Level.INFO, "Failed to load Offline UUIDs cache. Will create new cache.");
            }
        } catch (IOException ex){
            if (RealMail.getPlugin(RealMail.class).getConfig().getBoolean("verbose_errors", false)) {
                Bukkit.getLogger().log(Level.INFO, "Failed to load Offline UUIDs cache. Will create new cache.");
            }
        }
    } 
    
}

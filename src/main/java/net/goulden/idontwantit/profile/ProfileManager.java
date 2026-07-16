package net.goulden.idontwantit.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.goulden.idontwantit.IDontWantIt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Minecraft.getInstance().gameDirectory.toPath().resolve("config/IDontWantIt");
    private static final Path PROFILES_FILE = CONFIG_DIR.resolve("profiles.json");

    private static final Map<String, ItemProfile> profiles = new HashMap<>();
    private static boolean whitelistMode = false;

    public static class ItemProfile {
        public String name;
        public String iconItemId;
        public Set<String> ignoredItems;
        public boolean isActive;

        public ItemProfile(String name, String iconItemId) {
            this.name = name;
            this.iconItemId = iconItemId;
            this.ignoredItems = new HashSet<>();
            this.isActive = false;
        }

        public Item getIconItem() {
            if (iconItemId == null || iconItemId.isEmpty()) {
                return Items.PAPER;
            }
            ResourceLocation id = ResourceLocation.tryParse(iconItemId);
            if (id == null) {
                return Items.PAPER;
            }
            return BuiltInRegistries.ITEM.get(id);
        }
    }

    public static class ProfileData {
        public Map<String, ItemProfile> profiles;
        public boolean whitelistMode;

        public ProfileData() {
            this.profiles = new HashMap<>();
            this.whitelistMode = false;
        }
    }

    public static void saveProfiles() {
        try (Writer writer = Files.newBufferedWriter(PROFILES_FILE)) {
            ProfileData data = new ProfileData();
            data.profiles = profiles;
            data.whitelistMode = whitelistMode;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            IDontWantIt.LOGGER.error("Error al guardar perfiles", e);
        }
    }

    public static void renameProfile(String oldName, String newName) {

        ItemProfile profile = profiles.remove(oldName);

        if (profile == null)
            return;

        profile.name = newName;

        profiles.put(newName, profile);

        saveProfiles();
    }

    public static int getActiveProfileCount() {
        int count = 0;
        for (ItemProfile profile : profiles.values()) {
            if (profile.isActive) count++;
        }
        return count;
    }

    public static boolean isProfileActive(String profileName) {
        ItemProfile profile = profiles.get(profileName);
        return profile != null && profile.isActive;
    }

    public static void toggleProfileState(String profileName) {
        ItemProfile profile = profiles.get(profileName);
        if (profile != null) {
            profile.isActive = !isProfileActive(profileName);
            saveProfiles();
        }
    }

    public static Map<String, ItemProfile> getAllProfiles() {
        return profiles;
    }

    public static ItemProfile getProfile(String name) {
        return profiles.get(name);
    }

    public static void createProfile(String name, String iconItemId) {
        if (!profiles.containsKey(name)) {
            profiles.put(name, new ItemProfile(name, iconItemId));
            saveProfiles();
        }
    }

    public static void deleteProfile(String name) {
        if (profiles.containsKey(name)) {
            profiles.remove(name);
            saveProfiles();
        }
    }

    public static void addIgnoredItem(String profileName, Item item) {
        ItemProfile profile = profiles.get(profileName);
        if (profile != null) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            profile.ignoredItems.add(itemId.toString());
            saveProfiles();
        }
    }

    public static void removeIgnoredItem(String profileName, Item item) {
        ItemProfile profile = profiles.get(profileName);
        if (profile != null) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            profile.ignoredItems.remove(itemId.toString());
            saveProfiles();
        }
    }

    public static boolean isItemIgnored(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        String itemIdString = itemId.toString();
        boolean isInActiveProfiles = false;
        boolean hasActiveProfiles = false;

        for (ItemProfile profile : profiles.values()) {
            if (profile.isActive) {
                hasActiveProfiles = true;
                if (profile.ignoredItems.contains(itemIdString)) {
                    isInActiveProfiles = true;
                    break;
                }
            }
        }

        if (whitelistMode) {
            if (!hasActiveProfiles) return true;
            return !isInActiveProfiles;
        } else {
            return isInActiveProfiles;
        }
    }

    public static boolean isItemIgnoredInProfile(String profileName, Item item) {
        ItemProfile profile = profiles.get(profileName);
        if (profile == null) return false;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return profile.ignoredItems.contains(itemId.toString());
    }

    public static boolean isWhitelistMode() {
        return whitelistMode;
    }

    public static void toggleWhitelistMode() {
        whitelistMode = !whitelistMode;
        saveProfiles();
    }
}
package com.github.xnaut97.cosmos.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.*;

public final class SkullProfileService {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final long PROFILE_CACHE_TTL_MS = TimeUnit.HOURS.toMillis(6);
    private static final long PROFILE_FAILURE_COOLDOWN_MS = TimeUnit.MINUTES.toMillis(10);
    private static final long RATE_LIMIT_COOLDOWN_MS = TimeUnit.MINUTES.toMillis(30);
    private static final long WARNING_COOLDOWN_MS = TimeUnit.MINUTES.toMillis(5);

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "cosmos-skin-loader");
        thread.setDaemon(true);
        return thread;
    });

    private static final ConcurrentMap<UUID, CacheEntry> UUID_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, CacheEntry> NAME_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, GameProfile> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, CompletableFuture<GameProfile>> PROFILE_REQUESTS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> FAILURE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, ProfileApplier> APPLIERS = new ConcurrentHashMap<>();
    private static volatile long lastWarningAt;

    private SkullProfileService() {
    }

    public static GameProfile getCachedProfile(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        GameProfile profile = getCachedProfile(player.getUniqueId(), player.getName());
        return profile;
    }

    public static CompletableFuture<GameProfile> loadProfileAsync(OfflinePlayer player) {
        if (player == null) {
            return completedNullFuture();
        }
        return loadProfileAsync(player.getUniqueId(), player.getName());
    }

    public static CompletableFuture<GameProfile> loadProfileAsync(UUID uuid, String name) {
        GameProfile cached = getCachedProfile(uuid, name);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        String key = profileKey(uuid, name);
        if (key == null || isCoolingDown(key)) {
            return completedNullFuture();
        }

        return PROFILE_REQUESTS.computeIfAbsent(key, ignored -> CompletableFuture.supplyAsync(() -> {
            try {
                GameProfile profile = fetchProfile(uuid, name);
                cacheProfile(profile);
                return profile;
            } catch (Exception ex) {
                applyFailureCooldown(key, ex);
                warnCompact(ex);
                return null;
            }
        }, EXECUTOR).whenComplete((profile, throwable) -> PROFILE_REQUESTS.remove(key)));
    }

    public static GameProfile getOrCreateTextureProfile(String textureId) {
        if (textureId == null || textureId.isEmpty()) {
            return null;
        }
        return TEXTURE_CACHE.computeIfAbsent(textureId, SkullProfileService::createTextureProfile);
    }

    public static void applyGameProfile(SkullMeta meta, GameProfile gameProfile) {
        if (meta == null || gameProfile == null) {
            return;
        }
        try {
            ProfileApplier applier = APPLIERS.computeIfAbsent(meta.getClass(), ProfileApplier::resolve);
            applier.apply(meta, gameProfile);
        } catch (Exception ex) {
            warnCompact(ex);
        }
    }

    private static GameProfile getCachedProfile(UUID uuid, String name) {
        GameProfile profile = uuid == null ? null : getIfFresh(UUID_CACHE, uuid);
        if (profile != null) {
            return profile;
        }
        return name == null ? null : getIfFresh(NAME_CACHE, normalizeName(name));
    }

    private static void cacheProfile(GameProfile profile) {
        if (profile == null) {
            return;
        }
        CacheEntry entry = new CacheEntry(profile, System.currentTimeMillis() + PROFILE_CACHE_TTL_MS);
        if (profile.getId() != null) {
            UUID_CACHE.put(profile.getId(), entry);
            FAILURE_COOLDOWNS.remove("uuid:" + profile.getId().toString());
        }
        if (profile.getName() != null) {
            String nameKey = normalizeName(profile.getName());
            NAME_CACHE.put(nameKey, entry);
            FAILURE_COOLDOWNS.remove("name:" + nameKey);
        }
    }

    private static GameProfile fetchProfile(UUID uuid, String name) throws Exception {
        String compactUuid = uuid == null ? null : uuid.toString().replace("-", "");
        String profileName = name;

        if (compactUuid == null && name != null && !name.isEmpty()) {
            JsonObject user = readJson("https://api.mojang.com/users/profiles/minecraft/" + name);
            if (user == null || !user.has("id")) {
                return null;
            }
            compactUuid = user.get("id").getAsString();
            if (user.has("name")) {
                profileName = user.get("name").getAsString();
            }
        }

        if (compactUuid == null) {
            return null;
        }

        JsonObject session = readJson("https://sessionserver.mojang.com/session/minecraft/profile/" + compactUuid + "?unsigned=false");
        if (session == null || !session.has("properties")) {
            return null;
        }

        UUID resolvedUuid = fromCompactUuid(session.has("id") ? session.get("id").getAsString() : compactUuid);
        String resolvedName = session.has("name") ? session.get("name").getAsString() : profileName;
        GameProfile profile = new GameProfile(resolvedUuid, resolvedName);

        JsonArray properties = session.getAsJsonArray("properties");
        for (JsonElement element : properties) {
            JsonObject property = element.getAsJsonObject();
            if (!property.has("name") || !"textures".equals(property.get("name").getAsString())) {
                continue;
            }
            String value = property.get("value").getAsString();
            String signature = property.has("signature") ? property.get("signature").getAsString() : null;
            if (signature == null) {
                profile.getProperties().put("textures", new Property("textures", value));
            } else {
                profile.getProperties().put("textures", new Property("textures", value, signature));
            }
            break;
        }

        return profile;
    }

    private static JsonObject readJson(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setUseCaches(true);
            connection.setRequestProperty("User-Agent", "Cosmos");
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT || status == HttpURLConnection.HTTP_NOT_FOUND) {
                return null;
            }
            if (status == 429) {
                throw new ProfileRequestException(status, "Mojang profile rate limit");
            }
            if (status < 200 || status >= 300) {
                throw new ProfileRequestException(status, "Mojang profile request failed");
            }
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static GameProfile createTextureProfile(String textureId) {
        String textureUrl = textureId.startsWith("http://") || textureId.startsWith("https://")
                ? textureId
                : "http://textures.minecraft.net/texture/" + textureId;
        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        UUID id = UUID.nameUUIDFromBytes(encoded.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", encoded));
        return profile;
    }

    private static UUID fromCompactUuid(String compactUuid) {
        String value = compactUuid.replace("-", "");
        return UUID.fromString(value.substring(0, 8) + "-"
                + value.substring(8, 12) + "-"
                + value.substring(12, 16) + "-"
                + value.substring(16, 20) + "-"
                + value.substring(20));
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private static String profileKey(UUID uuid, String name) {
        if (uuid != null) {
            return "uuid:" + uuid.toString();
        }
        String normalized = normalizeName(name);
        return normalized.isEmpty() ? null : "name:" + normalized;
    }

    private static <K> GameProfile getIfFresh(ConcurrentMap<K, CacheEntry> cache, K key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt < System.currentTimeMillis()) {
            cache.remove(key, entry);
            return null;
        }
        return entry.profile;
    }

    private static boolean isCoolingDown(String key) {
        Long retryAt = FAILURE_COOLDOWNS.get(key);
        if (retryAt == null) {
            return false;
        }
        if (retryAt <= System.currentTimeMillis()) {
            FAILURE_COOLDOWNS.remove(key, retryAt);
            return false;
        }
        return true;
    }

    private static void applyFailureCooldown(String key, Exception ex) {
        long cooldown = ex instanceof ProfileRequestException
                && ((ProfileRequestException) ex).status == 429 ? RATE_LIMIT_COOLDOWN_MS : PROFILE_FAILURE_COOLDOWN_MS;
        FAILURE_COOLDOWNS.put(key, System.currentTimeMillis() + cooldown);
    }

    private static void warnCompact(Exception ex) {
        long now = System.currentTimeMillis();
        if (now - lastWarningAt < WARNING_COOLDOWN_MS) {
            return;
        }
        lastWarningAt = now;
        String message = ex instanceof ProfileRequestException ? ex.getMessage() : ex.getClass().getSimpleName() + ": " + ex.getMessage();
        Bukkit.getLogger().warning("Cosmos skin profile lookup skipped/failed: " + message);
    }

    private static CompletableFuture<GameProfile> completedNullFuture() {
        return CompletableFuture.completedFuture(null);
    }

    private static final class CacheEntry {
        private final GameProfile profile;
        private final long expiresAt;

        private CacheEntry(GameProfile profile, long expiresAt) {
            this.profile = profile;
            this.expiresAt = expiresAt;
        }
    }

    private static final class ProfileRequestException extends IOException {
        private final int status;

        private ProfileRequestException(int status, String message) {
            super(message + " (HTTP " + status + ")");
            this.status = status;
        }
    }

    private static final class ProfileApplier {
        private final Method setProfile;
        private final Field profileField;
        private final Constructor<?> resolvableConstructor;
        private final Method resolvableFactory;

        private ProfileApplier(Method setProfile, Field profileField, Constructor<?> resolvableConstructor, Method resolvableFactory) {
            this.setProfile = setProfile;
            this.profileField = profileField;
            this.resolvableConstructor = resolvableConstructor;
            this.resolvableFactory = resolvableFactory;
        }

        private static ProfileApplier resolve(Class<?> metaClass) {
            Method setProfile = findSetProfile(metaClass);
            if (setProfile != null) {
                return new ProfileApplier(setProfile, null, null, null);
            }

            Field profileField = findProfileField(metaClass);
            if (profileField == null) {
                return new ProfileApplier(null, null, null, null);
            }

            Class<?> fieldType = profileField.getType();
            if (fieldType.isAssignableFrom(GameProfile.class)) {
                return new ProfileApplier(null, profileField, null, null);
            }

            Constructor<?> constructor = findResolvableConstructor(fieldType);
            Method factory = constructor == null ? findResolvableFactory(fieldType) : null;
            return new ProfileApplier(null, profileField, constructor, factory);
        }

        private void apply(SkullMeta meta, GameProfile gameProfile) throws Exception {
            if (setProfile != null) {
                setProfile.invoke(meta, gameProfile);
                return;
            }
            if (profileField == null) {
                return;
            }

            Object value = gameProfile;
            Class<?> fieldType = profileField.getType();
            if (!fieldType.isAssignableFrom(GameProfile.class)) {
                value = createResolvable(gameProfile);
            }
            if (value != null) {
                profileField.set(meta, value);
            }
        }

        private Object createResolvable(GameProfile gameProfile) throws Exception {
            if (resolvableConstructor != null) {
                return resolvableConstructor.newInstance(gameProfile);
            }
            if (resolvableFactory != null) {
                return resolvableFactory.invoke(null, gameProfile);
            }
            return null;
        }

        private static Method findSetProfile(Class<?> metaClass) {
            try {
                Method method = metaClass.getDeclaredMethod("setProfile", GameProfile.class);
                method.setAccessible(true);
                return method;
            } catch (Exception ignored) {
                return null;
            }
        }

        private static Field findProfileField(Class<?> metaClass) {
            Class<?> current = metaClass;
            while (current != null) {
                try {
                    Field field = current.getDeclaredField("profile");
                    field.setAccessible(true);
                    return field;
                } catch (Exception ignored) {
                    current = current.getSuperclass();
                }
            }
            return null;
        }

        private static Constructor<?> findResolvableConstructor(Class<?> fieldType) {
            try {
                Constructor<?> constructor = fieldType.getDeclaredConstructor(GameProfile.class);
                constructor.setAccessible(true);
                return constructor;
            } catch (Exception ignored) {
                return null;
            }
        }

        private static Method findResolvableFactory(Class<?> fieldType) {
            for (String name : new String[]{"create", "of"}) {
                try {
                    Method method = fieldType.getDeclaredMethod(name, GameProfile.class);
                    method.setAccessible(true);
                    return method;
                } catch (Exception ignored) {
                }
            }
            return null;
        }
    }
}

package com.github.xnaut97.cosmos.utilities.java;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class ClassScanner {

    private final Plugin plugin;

    private final List<String> filteredPaths = new ArrayList<>();

    public ClassScanner(Plugin plugin) {
        this.plugin = plugin;
    }

    public ClassScanner filterPackage(String basePath) {
        this.filteredPaths.add(basePath.replace('.', '/'));
        return this;
    }

    public Set<Class<?>> scan(String packageName) {
        String path = packageName.replace('.', '/');
        URL url = plugin.getClass().getClassLoader().getResource(path);
        if (url == null) {
            plugin.getLogger().warning("Unable to locate " + packageName);
            return new HashSet<>();
        }

        switch (url.getProtocol()) {
            case "file":
                try {
                    return scanDirectory(new File(url.toURI()), packageName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            case "jar":
                return scanJar(url, path, packageName);
            default:
                return new HashSet<>();
        }
    }

    public Set<Class<?>> scanAllPluginClasses() {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = plugin.getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8.name());

            File jarFile = new File(decoded);

            if (!jarFile.getName().endsWith(".jar"))
                return new HashSet<>();

            try (JarFile jar = new JarFile(jarFile)) {
                jar.stream()
                        .filter(e -> e.getName().endsWith(".class"))
                        .filter(e -> !e.getName().startsWith("META-INF/"))
                        .filter(e -> !e.getName().contains("$"))
                        .filter(e -> filteredPaths.isEmpty() || filteredPaths.stream().anyMatch(e.getName()::startsWith))
                        .map(e -> e.getName().replace('/', '.').replaceAll("\\.class$", ""))
                        .forEach(name -> {
                            if (name.contains("module-info") || name.contains("META-INF")) return;

                            try {
                                Class<?> clazz = Class.forName(name);
                                if (Modifier.isAbstract(clazz.getModifiers())) return;

                                classes.add(clazz);
                            } catch (ClassNotFoundException exception) {
                                System.out.println("No such class by name: " + name);
                            }
                        });
            }

        } catch (Throwable ignored) {
        }

        return classes;
    }

    @SuppressWarnings("unchecked")
    public <T> void loadClasses(
            Class<T> baseClass,
            Class<?>[] constructorParams,
            Object[] constructorArgs,
            Consumer<T> callback
    ) {
        for (Class<?> clazz : scanAllPluginClasses()) {
            if (!baseClass.isAssignableFrom(clazz)) continue;
            if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum() || clazz.isAnonymousClass()) continue;

            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(constructorParams);
                constructor.setAccessible(true);
                T instance = (T) constructor.newInstance(constructorArgs);
                callback.accept(instance);
            } catch (Exception ignored) {
            }
        }
    }


    // ───────── helpers ─────────
    private Set<Class<?>> scanDirectory(File dir, String pkg) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            Files.walk(dir.toPath()).forEach(p -> {
                String name = p.toString();
                if (name.endsWith(".class")) {
                    String className = pkg + '.' +
                            dir.toPath().relativize(p).toString()
                                    .replace(File.separatorChar, '.')
                                    .replaceAll("\\.class$", "");
                    load(classes, className);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private Set<Class<?>> scanJar(URL url, String path, String pkg) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String jarPath = url.getPath().substring(5, url.getPath().indexOf('!')); // strip "file:"
            jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());
            try (JarFile jar = new JarFile(jarPath)) {
                jar.stream()
                        .filter(e -> e.getName().startsWith(path) && e.getName().endsWith(".class"))
                        .forEach(e -> load(classes,
                                e.getName().replace('/', '.').replaceAll("\\.class$", "")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private void load(Set<Class<?>> out, String className) {
        try {
            out.add(Class.forName(className));
        } catch (ClassNotFoundException ignored) {
        }
    }
}

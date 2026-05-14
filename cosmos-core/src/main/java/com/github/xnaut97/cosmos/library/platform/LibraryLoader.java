package com.github.xnaut97.cosmos.library.platform;

import com.alessiodp.libby.LibraryManager;
import com.github.xnaut97.cosmos.library.Libraries;
import lombok.Getter;

import java.util.Arrays;

@Getter
public abstract class LibraryLoader<M extends LibraryManager> {

    private final M libraryManager;

    private boolean isLoaded;

    public LibraryLoader(M libraryManager) {
        this.libraryManager = libraryManager;

        libraryManager.addJitPack();
        libraryManager.addSonatype();
        libraryManager.addJCenter();
        libraryManager.addMavenCentral();

        loadLibraries();

    }

    public void loadLibraries() {
        Arrays.stream(Libraries.values()).forEach(lib -> {
            getLibraryManager().loadLibrary(lib.createLibrary());

        });
        isLoaded = true;
    }
}

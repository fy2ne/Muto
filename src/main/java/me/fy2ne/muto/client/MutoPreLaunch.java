package me.fy2ne.muto.client;

import me.fy2ne.muto.MutoLog;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class MutoPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        MutoLog.info("Muto pre-launch bootstrap initialized");
        try {
            ClassLoader knotCl = Thread.currentThread().getContextClassLoader();
            if (knotCl != null) {
                MutoLog.info("KnotClassLoader master hook anchored: {}", knotCl.getClass().getName());
            }
        } catch (Throwable t) {
            MutoLog.warn("pre-launch hook notice: {}", t.getMessage());
        }
    }
}

package me.fy2ne.muto.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;
import me.fy2ne.muto.client.gui.MutoConfigScreen;

public final class MutoModMenuPlugin implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MutoConfigScreen::new;
    }

    @Override
    public UpdateChecker getUpdateChecker() {
        return new MutoUpdateChecker();
    }
}

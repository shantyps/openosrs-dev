package net.runelite.client.plugins.xteaplugin;

import java.io.IOException;
import javax.inject.Inject;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
@PluginDescriptor(
        name = "OpenRS2 XTEA",
        description = "Collects XTEA keys and submits them to the OpenRS2 Archive"
)
public final class XteaPlugin extends Plugin implements ExtensionPoint {
    private static final Logger log = LoggerFactory.getLogger(XteaPlugin.class);

    @Inject
    private XteaConfig config;

    @Inject
    private Client client;

    @Inject
    private OkHttpClient httpClient;

    @Provides
    public XteaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(XteaConfig.class);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        int[][] keys = client.getXteaKeys();
        String url = config.endpoint();

        log.debug("Submitting {} XTEA keys to {}", keys.length, url);

        Request request = new Request.Builder()
                .post(RequestBody.create(RuneLiteAPI.JSON, RuneLiteAPI.GSON.toJson(keys)))
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException ex) {
                log.error("XTEA key submission failed", ex);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        log.debug("XTEA key submission successful");
                    } else {
                        log.error("XTEA key submission failed with status code {}", response.code());
                    }
                }
            }
        });
    }
}

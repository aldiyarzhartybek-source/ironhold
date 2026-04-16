package com.ironhold.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Objects;

/**
 * Централизованный сервис загрузки/выгрузки ассетов.
 *
 * Для Stage 0 ассеты собираются программно и регистрируются в AssetManager,
 * чтобы экраны не создавали ресурсы напрямую.
 */
public final class AssetService {

    private final RuntimeAssetManager assets = new RuntimeAssetManager();
    private boolean queued;
    private boolean runtimeLoaded;

    public void queueCoreAssets() {
        assets.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        assets.load(AssetCatalog.MAP_LEVEL_0, TiledMap.class);
        queued = true;
    }

    public boolean update() {
        if (!queued) {
            return false;
        }
        if (!runtimeLoaded) {
            loadRuntimeAssets();
            runtimeLoaded = true;
        }
        return assets.update();
    }

    public float getProgress() {
        return queued ? assets.getProgress() : 0f;
    }

    public Skin getSkin() {
        ensureLoaded();
        return assets.get(AssetCatalog.SKIN_UI, Skin.class);
    }

    public BitmapFont getFont() {
        ensureLoaded();
        return assets.get(AssetCatalog.FONT_DEFAULT, BitmapFont.class);
    }

    public Texture getTestTexture() {
        ensureLoaded();
        return assets.get(AssetCatalog.TEXTURE_TEST, Texture.class);
    }

    public TiledMap getLevel0Map() {
        ensureLoaded();
        return assets.get(AssetCatalog.MAP_LEVEL_0, TiledMap.class);
    }

    public void dispose() {
        assets.dispose();
    }

    private void ensureLoaded() {
        if (!queued || !assets.isFinished()) {
            throw new IllegalStateException("Assets are not loaded yet");
        }
    }

    private void loadRuntimeAssets() {
        BitmapFont font = new BitmapFont();
        Texture testTexture = createTestTexture();
        Skin skin = createSkin(font, testTexture);

        assets.register(AssetCatalog.FONT_DEFAULT, BitmapFont.class, font);
        assets.register(AssetCatalog.TEXTURE_TEST, Texture.class, testTexture);
        assets.register(AssetCatalog.SKIN_UI, Skin.class, skin);
    }

    private static Texture createTestTexture() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.9f, 0.9f, 0.95f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Skin createSkin(BitmapFont font, Texture uiTexture) {
        Objects.requireNonNull(font, "font");
        Objects.requireNonNull(uiTexture, "uiTexture");

        Skin skin = new Skin();
        skin.add("default-font", font);
        skin.add("button-base", uiTexture);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("label", labelStyle);

        Drawable up = skin.newDrawable("button-base", new Color(0.2f, 0.2f, 0.28f, 1f));
        Drawable over = skin.newDrawable("button-base", new Color(0.28f, 0.28f, 0.38f, 1f));
        Drawable down = skin.newDrawable("button-base", new Color(0.16f, 0.16f, 0.22f, 1f));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = up;
        buttonStyle.over = over;
        buttonStyle.down = down;
        buttonStyle.font = font;
        skin.add("default", buttonStyle);

        return skin;
    }

    private static final class RuntimeAssetManager extends AssetManager {
        private <T> void register(String fileName, Class<T> type, T asset) {
            addAsset(fileName, type, asset);
        }
    }
}

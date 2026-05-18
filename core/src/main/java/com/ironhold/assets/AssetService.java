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
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.ironhold.ui.GameTheme;

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
        pixmap.setColor(GameTheme.TEXTURE_NEUTRAL);
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
        labelStyle.fontColor = GameTheme.UI_TEXT;
        skin.add("label", labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = GameTheme.PATH_TEAL;
        skin.add("title", titleStyle);

        Label.LabelStyle mutedLabelStyle = new Label.LabelStyle();
        mutedLabelStyle.font = font;
        mutedLabelStyle.fontColor = GameTheme.UI_TEXT_MUTED;
        skin.add("label-muted", mutedLabelStyle);

        Drawable up = skin.newDrawable("button-base", GameTheme.BUTTON_UP);
        Drawable over = skin.newDrawable("button-base", GameTheme.BUTTON_OVER);
        Drawable down = skin.newDrawable("button-base", GameTheme.BUTTON_DOWN);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = up;
        buttonStyle.over = over;
        buttonStyle.down = down;
        buttonStyle.font = font;
        buttonStyle.fontColor = GameTheme.UI_TEXT;
        skin.add("default", buttonStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = up;
        scrollStyle.vScroll = down;
        scrollStyle.vScrollKnob = over;

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorSelected = GameTheme.UI_TEXT;
        listStyle.fontColorUnselected = GameTheme.UI_TEXT_MUTED;
        listStyle.selection = down;
        listStyle.background = up;

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = font;
        selectBoxStyle.fontColor = GameTheme.UI_TEXT;
        selectBoxStyle.background = up;
        selectBoxStyle.scrollStyle = scrollStyle;
        selectBoxStyle.listStyle = listStyle;
        skin.add("default", selectBoxStyle);

        return skin;
    }

    private static final class RuntimeAssetManager extends AssetManager {
        private <T> void register(String fileName, Class<T> type, T asset) {
            addAsset(fileName, type, asset);
        }
    }
}

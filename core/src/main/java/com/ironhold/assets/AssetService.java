package com.ironhold.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
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
        BitmapFont[] fonts = buildFonts();   // [0] = regular, [1] = title
        BitmapFont font      = fonts[0];
        BitmapFont titleFont = fonts[1];

        Texture testTexture = createTestTexture();
        Skin skin = createSkin(font, titleFont, testTexture);

        assets.register(AssetCatalog.FONT_DEFAULT, BitmapFont.class, font);
        assets.register(AssetCatalog.TEXTURE_TEST, Texture.class, testTexture);
        assets.register(AssetCatalog.SKIN_UI, Skin.class, skin);
        // titleFont is owned by the skin and will be disposed with it.
    }

    /**
     * Attempts to generate fonts from {@link AssetCatalog#FONT_TTF} via FreeType
     * (sizes 22px regular, 52px title, Linear filtering, white colour).
     * Falls back to scaled default {@link BitmapFont} if the TTF file is absent.
     *
     * @return two-element array: {@code [regularFont, titleFont]}
     */
    private static BitmapFont[] buildFonts() {
        com.badlogic.gdx.files.FileHandle ttfHandle =
            Gdx.files.internal(AssetCatalog.FONT_TTF);

        if (ttfHandle.exists()) {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfHandle);
            try {
                BitmapFont regular = gen.generateFont(fontParam(22));
                BitmapFont title   = gen.generateFont(fontParam(52));
                return new BitmapFont[]{ regular, title };
            } finally {
                gen.dispose();
            }
        }

        // ── Fallback: default LibGDX bitmap font ───────────────────────────
        Gdx.app.log("AssetService",
            "TTF not found at '" + AssetCatalog.FONT_TTF + "' — using default bitmap font.");
        BitmapFont regular = new BitmapFont();
        regular.getRegion().getTexture()
            .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        BitmapFont title = new BitmapFont();
        title.getData().setScale(2.4f);
        title.getRegion().getTexture()
            .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        return new BitmapFont[]{ regular, title };
    }

    /** Shared FreeType parameters: Linear filtering, white colour, full hinting. */
    private static FreeTypeFontParameter fontParam(int size) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size      = size;
        p.color     = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        p.hinting   = FreeTypeFontGenerator.Hinting.Full;
        return p;
    }

    private static Texture createTestTexture() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(GameTheme.TEXTURE_NEUTRAL);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Skin createSkin(BitmapFont font, BitmapFont titleFont, Texture uiTexture) {
        Objects.requireNonNull(font,      "font");
        Objects.requireNonNull(titleFont, "titleFont");
        Objects.requireNonNull(uiTexture, "uiTexture");

        Skin skin = new Skin();

        // ── Fonts ──────────────────────────────────────────────────────────
        skin.add("default-font", font);
        skin.add("title-font",   titleFont);   // skin disposes this

        // ── Button drawables — white-bordered NinePatch ────────────────────
        // 16×16 pixel, 2px white border; stretches cleanly to any button size.
        Drawable btnUp   = borderedNinePatch(skin, "btn-up",   GameTheme.BUTTON_UP,   GameTheme.UI_TEXT_MUTED, 2);
        Drawable btnOver = borderedNinePatch(skin, "btn-over", GameTheme.BUTTON_OVER, Color.WHITE,             2);
        Drawable btnDown = borderedNinePatch(skin, "btn-down", GameTheme.BUTTON_DOWN, GameTheme.UI_TEXT_MUTED, 2);

        // Plain flat drawable (used as scroll pane background)
        skin.add("plain-tex", uiTexture);
        Drawable plain = skin.newDrawable("plain-tex", GameTheme.BUTTON_UP);

        // ── Label styles ───────────────────────────────────────────────────
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font      = font;
        labelStyle.fontColor = GameTheme.UI_TEXT;
        skin.add("default",  labelStyle);
        skin.add("label",    labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font      = titleFont;
        titleStyle.fontColor = Color.WHITE;
        skin.add("title", titleStyle);

        Label.LabelStyle mutedStyle = new Label.LabelStyle();
        mutedStyle.font      = font;
        mutedStyle.fontColor = GameTheme.UI_TEXT_MUTED;
        skin.add("label-muted", mutedStyle);

        // ── TextButton style — default (white border) ─────────────────────
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up            = btnUp;
        buttonStyle.over          = btnOver;
        buttonStyle.down          = btnDown;
        buttonStyle.font          = font;
        buttonStyle.fontColor     = GameTheme.UI_TEXT;
        buttonStyle.overFontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        // ── TextButton style — menu-button (gold border) ───────────────────
        // Dark panel with gold border; matches the in-game tower/HUD accent colour.
        Drawable menuUp   = borderedNinePatch(skin, "menu-btn-up",
            new Color(0.11f, 0.14f, 0.20f, 1.00f), Color.WHITE, 3);
        Drawable menuOver = borderedNinePatch(skin, "menu-btn-over",
            new Color(0.16f, 0.20f, 0.30f, 0.95f), Color.WHITE, 3);
        Drawable menuDown = borderedNinePatch(skin, "menu-btn-down",
            new Color(0.07f, 0.09f, 0.13f, 1.00f), Color.WHITE, 3);

        TextButton.TextButtonStyle menuButtonStyle = new TextButton.TextButtonStyle();
        menuButtonStyle.up            = menuUp;
        menuButtonStyle.over          = menuOver;
        menuButtonStyle.down          = menuDown;
        menuButtonStyle.font          = font;
        // fontColor WHITE lets us drive the displayed colour via label.setColor().
        // overFontColor is kept null so Scene2D won't snap the colour automatically.
        menuButtonStyle.fontColor     = Color.WHITE;
        menuButtonStyle.overFontColor = null;
        skin.add("menu-button", menuButtonStyle);

        // ── SelectBox / ScrollPane / List — styled to match menu-button ──────
        // The dropdown list uses the same dark fill + white border aesthetic.
        Drawable selectBg   = borderedNinePatch(skin, "sel-bg",
            new Color(0.11f, 0.14f, 0.20f, 1.00f), Color.WHITE, 3);
        Drawable selectOver = borderedNinePatch(skin, "sel-bg-over",
            new Color(0.16f, 0.20f, 0.30f, 0.95f), Color.WHITE, 3);
        Drawable listSelBg  = borderedNinePatch(skin, "sel-list-sel",
            new Color(0.16f, 0.20f, 0.30f, 1.00f), Color.WHITE, 1);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background  = plain;          // no extra border behind the dropdown
        scrollStyle.vScroll     = selectBg;
        scrollStyle.vScrollKnob = selectOver;

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font                = font;
        listStyle.fontColorSelected   = Color.WHITE;
        listStyle.fontColorUnselected = GameTheme.UI_TEXT_MUTED;
        listStyle.selection           = listSelBg;
        listStyle.background          = selectBg;

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font        = font;
        selectBoxStyle.fontColor   = Color.WHITE;
        selectBoxStyle.background  = selectBg;
        selectBoxStyle.scrollStyle = scrollStyle;
        selectBoxStyle.listStyle   = listStyle;
        skin.add("default", selectBoxStyle);

        return skin;
    }

    /**
     * Creates a rounded-corner bordered NinePatch drawable.
     *
     * <p>The pixmap is 32×32 with a corner radius of 7px.
     * The border colour is drawn as the outer rounded rect; the fill colour as
     * the inset rounded rect, giving smooth rounded corners at any button size.
     */
    private static Drawable borderedNinePatch(Skin skin, String key,
                                              Color fill, Color border, int borderPx) {
        int size   = 32;
        int radius = 7;
        Pixmap pm  = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        // Outer rounded rect — border colour
        fillRoundedRect(pm, 0, 0, size, size, radius, border);
        // Inner rounded rect — fill colour, inset by borderPx
        int innerR = Math.max(0, radius - borderPx);
        fillRoundedRect(pm, borderPx, borderPx,
            size - borderPx * 2, size - borderPx * 2, innerR, fill);

        Texture tex = new Texture(pm);
        pm.dispose();
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add(key + "-tex", tex);   // skin manages disposal
        int pad = radius + 1;
        NinePatch patch = new NinePatch(new TextureRegion(tex), pad, pad, pad, pad);
        return new NinePatchDrawable(patch);
    }

    /**
     * Fills a rounded rectangle into {@code pm} using the given colour.
     * Implemented as two overlapping filled rects plus four corner circles.
     */
    private static void fillRoundedRect(Pixmap pm, int x, int y,
                                        int w, int h, int r, Color c) {
        if (w <= 0 || h <= 0) return;
        pm.setColor(c);
        pm.fillRectangle(x,     y + r, w,         h - r * 2);   // vertical bar
        pm.fillRectangle(x + r, y,     w - r * 2, h);            // horizontal bar
        pm.fillCircle(x + r,         y + r,         r);           // top-left
        pm.fillCircle(x + w - r - 1, y + r,         r);           // top-right
        pm.fillCircle(x + r,         y + h - r - 1, r);           // bottom-left
        pm.fillCircle(x + w - r - 1, y + h - r - 1, r);           // bottom-right
    }

    private static final class RuntimeAssetManager extends AssetManager {
        private <T> void register(String fileName, Class<T> type, T asset) {
            addAsset(fileName, type, asset);
        }
    }
}

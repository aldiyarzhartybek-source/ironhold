package com.ironhold.core.fx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.BloomEffect;

/**
 * Wraps gdx-vfx so the GameScreen can apply Bloom to the FX layer
 * (enemies, towers, projectiles) without touching the path, background, or HUD.
 *
 * <pre>
 * // Usage in render():
 * bloomPipeline.beginCapture();
 *   ... render FX entities ...
 * bloomPipeline.endCaptureAndRender();  // composites bloom result on top of current screen
 * </pre>
 */
public final class FxBloomPipeline implements Disposable {

    private final VfxManager  vfxManager;
    private final BloomEffect bloomEffect;

    public FxBloomPipeline(int screenWidth, int screenHeight) {
        vfxManager  = new VfxManager(Pixmap.Format.RGBA8888);

        bloomEffect = new BloomEffect();
        bloomEffect.setBloomIntensity(0.60f);
        bloomEffect.setThreshold(0.55f);   // only bright pixels glow

        vfxManager.addEffect(bloomEffect);
        vfxManager.resize(screenWidth, screenHeight);
    }

    /** Call once per frame before rendering the FX entities. */
    public void beginCapture() {
        vfxManager.cleanUpBuffers();
        vfxManager.beginInputCapture();
    }

    /**
     * Ends the FX capture, applies the Bloom post-process, and composites the
     * result on top of whatever is already on the screen (background + path).
     */
    public void endCaptureAndRender() {
        vfxManager.endInputCapture();
        vfxManager.applyEffects();
        vfxManager.renderToScreen();
    }

    public void resize(int width, int height) {
        vfxManager.resize(width, height);
    }

    @Override
    public void dispose() {
        bloomEffect.dispose();
        vfxManager.dispose();
    }
}

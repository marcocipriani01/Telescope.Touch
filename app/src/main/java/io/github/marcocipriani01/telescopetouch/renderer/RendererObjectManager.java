/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.renderer;

import android.util.Log;

import java.util.EnumSet;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;


public abstract class RendererObjectManager implements Comparable<RendererObjectManager> {

    /**
     * Used to distinguish between different renderers, so we can have sets of them.
     */
    private static int sIndex = 0;
    private final TextureManager mTextureManager;
    private final int mLayer;
    private final int mIndex;
    private boolean mEnabled = true;
    private SkyRenderer.RenderState mRenderState = null;
    private UpdateListener mListener = null;
    private float mMaxRadiusOfView = 360;  // in degrees

    public RendererObjectManager(int layer, TextureManager textureManager) {
        mLayer = layer;
        mTextureManager = textureManager;
        synchronized (RendererObjectManager.class) {
            mIndex = sIndex++;
        }
    }

    public void enable(boolean enable) {
        mEnabled = enable;
    }

    public void setMaxRadiusOfView(float radiusOfView) {
        mMaxRadiusOfView = radiusOfView;
    }

    public int compareTo(RendererObjectManager rom) {
        if (getClass() != rom.getClass()) {
            return getClass().getName().compareTo(rom.getClass().getName());
        }
        return Integer.compare(mIndex, rom.mIndex);
    }

    final int getLayer() {
        return mLayer;
    }

    final void draw(GL10 gl) {
        if (mEnabled && mRenderState.getRadiusOfView() <= mMaxRadiusOfView) {
            drawInternal(gl);
        }
    }

    final SkyRenderer.RenderState getRenderState() {
        return mRenderState;
    }

    final void setRenderState(SkyRenderer.RenderState state) {
        mRenderState = state;
    }

    final void setUpdateListener(UpdateListener listener) {
        mListener = listener;
    }

    /**
     * Notifies the renderer that the manager must be reloaded before the next time it is drawn.
     */
    final void queueForReload() {
        mListener.queueForReload(this);
    }

    protected void logUpdateMismatch(String managerType, int expectedLength, int actualLength,
                                     EnumSet<RendererObjectManager.UpdateType> type) {
        Log.e("ImageObjectManager",
                "Trying to update objects in " + managerType + ", but number of input sources was "
                        + "different from the number currently set on the manager (" + actualLength
                        + " vs " + expectedLength + "\n"
                        + "Update options were: " + type + "\n"
                        + "Ignoring update");
    }

    protected TextureManager textureManager() {
        return mTextureManager;
    }

    /**
     * Reload all OpenGL resources needed by the object (ie, textures, VBOs).  If fullReload is true,
     * this means that the object needs to reload everything (this is the case when the object
     * is loaded for the first time, or when the activity is being recreated, and all the previous
     * resources have been invalid.  Sometimes a manager may only need to be partially reloaded (for
     * example, if new objects are set, they might need to be reloaded, but the texture shared
     * between them all is the same so it does not need to be).  The renderer will only ever do a
     * full reload - fullReload will only be false if the manager queues itself for a partial reload.
     */
    public abstract void reload(GL10 gl, boolean fullReload);

    protected abstract void drawInternal(GL10 gl);

    /**
     * Specifies options for updating a specific RendererObjectManager.
     */
    public enum UpdateType {
        Reset,            // Throw away any previous data and set entirely new data.
        UpdatePositions,  // Only update positions of existing objects.
        UpdateImages      // Only update images of existing objects.
    }

    interface UpdateListener {
        void queueForReload(RendererObjectManager rom);
    }
}
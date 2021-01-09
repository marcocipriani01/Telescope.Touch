package io.github.marcocipriani01.telescopetouch.renderer;

import android.content.res.Resources;
import android.graphics.Paint;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.renderer.util.IndexBuffer;
import io.github.marcocipriani01.telescopetouch.renderer.util.LabelMaker;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureReference;
import io.github.marcocipriani01.telescopetouch.renderer.util.VertexBuffer;

/**
 * Manages rendering of which appears at fixed points on the screen, rather
 * than text which appears at fixed points in the world.
 *
 * @author James Powell
 */
public class LabelOverlayManager {
    private final LabelMaker mLabelMaker = new LabelMaker(true);
    private final Paint mLabelPaint = new Paint();
    private Label[] mLabels = null;
    private TextureReference mTexture = null;
    private VertexBuffer mVertexBuffer = null;
    private IndexBuffer mIndexBuffer = null;

    public LabelOverlayManager() {
        mLabelPaint.setAntiAlias(true);

        mVertexBuffer = new VertexBuffer(4, false);
        mIndexBuffer = new IndexBuffer(6);

        mVertexBuffer.addPoint(0, 0, 0);  // Bottom left
        mVertexBuffer.addPoint(0, 1, 0);  // Top left
        mVertexBuffer.addPoint(1, 0, 0);  // Bottom right
        mVertexBuffer.addPoint(1, 1, 0);  // Top right

        // Triangle one: bottom left, top left, bottom right.
        mIndexBuffer.addIndex((short) 0);
        mIndexBuffer.addIndex((short) 1);
        mIndexBuffer.addIndex((short) 2);

        // Triangle two: bottom right, top left, top right.
        mIndexBuffer.addIndex((short) 2);
        mIndexBuffer.addIndex((short) 1);
        mIndexBuffer.addIndex((short) 3);
    }

    public void initialize(GL10 gl, Label[] labels, Resources res,
                           TextureManager textureManager) {
        mLabels = labels.clone();
        mTexture = mLabelMaker.initialize(gl, mLabelPaint, labels, res, textureManager);
    }

    public void releaseTexture(GL10 gl) {
        // TODO(jpowell): Figure out if LabelMaker should have a shutdown() method
        // and delete the texture or if I should do it myself.
        if (mTexture != null) {
            mLabelMaker.shutdown(gl);
            mTexture = null;
        }
    }

    public void draw(GL10 gl, int screenWidth, int screenHeight) {
        if (mLabels == null || mTexture == null) {
            return;
        }

        gl.glEnable(GL10.GL_TEXTURE_2D);
        mTexture.bind(gl);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_MODULATE);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        // Change to orthographic projection, where the units in model view space
        // are the same as in screen space.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof(0, screenWidth, 0, screenHeight, -100, 100);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();

        for (Label label : mLabels) {
            if (label.enabled()) {
                int x = label.mX - label.getWidthInPixels() / 2;
                int y = label.mY;

                gl.glLoadIdentity();

                // Move the label to the correct offset.
                gl.glTranslatef(x, y, 0.0f);

                // Scale the label to the correct size.
                gl.glScalef(label.getWidthInPixels(), label.getHeightInPixels(), 0.0f);

                // Set the alpha for the label.
                gl.glColor4f(1, 1, 1, label.getAlpha());

                // Draw the label.
                mVertexBuffer.set(gl);
                gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, label.getTexCoords());
                mIndexBuffer.draw(gl, GL10.GL_TRIANGLES);
            }
        }

        // Restore the old matrices.
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        gl.glDisable(GL10.GL_BLEND);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    public static class Label extends LabelMaker.LabelData {
        private boolean mEnabled = true;
        private int mX = 0, mY = 0;
        private float mAlpha = 1;

        public Label(String text, int color, int size) {
            super(text, color, size);
        }

        public boolean enabled() {
            return mEnabled;
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void setPosition(int x, int y) {
            mX = x;
            mY = y;
        }

        private float getAlpha() {
            return mAlpha;
        }

        public void setAlpha(float alpha) {
            mAlpha = alpha;
        }
    }
}
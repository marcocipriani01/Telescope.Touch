/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.renderer;

import javax.microedition.khronos.opengles.GL10;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.maths.MathsUtils;
import io.github.marcocipriani01.telescopetouch.maths.Vector3;
import io.github.marcocipriani01.telescopetouch.renderer.util.SearchHelper;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureManager;
import io.github.marcocipriani01.telescopetouch.renderer.util.TextureReference;
import io.github.marcocipriani01.telescopetouch.renderer.util.TexturedQuad;

public class SearchArrow {

    // The arrow quad is 10% of the screen width or height, whichever is smaller.
    private static final float ARROW_SIZE = 0.1f;
    // The circle quad is 40% of the screen width or height, whichever is smaller.
    private static final float CIRCLE_SIZE = 0.4f;

    // The target position is (1, theta, phi) in spherical coordinates.
    private float mTargetTheta = 0;
    private float mTargetPhi = 0;
    private TexturedQuad mCircleQuad = null;
    private TexturedQuad mArrowQuad = null;
    private float mArrowOffset = 0;
    private float mCircleSizeFactor = 1;
    private float mArrowSizeFactor = 1;
    private float mFullCircleScaleFactor = 1;

    private TextureReference mArrowTex = null;
    private TextureReference mCircleTex = null;

    // Given vectors v1 and v2, and an axis, this function returns the angle which you must rotate v1
    // by in order for it to be in the same plane as v2 and axis.  Assumes that all vectors are unit
    // vectors and v2 and axis are perpendicular.
    private static float angleBetweenVectorsWithRespectToAxis(Vector3 v1, Vector3 v2, Vector3 axis) {
        // Make v1 perpendicular to axis.  We want an orthonormal basis for the plane perpendicular
        // to axis.  After rotating v1, the projection of v1 and v2 into this plane should be equal.
        Vector3 v1proj = Vector3.difference(v1, Vector3.projectOntoUnit(v1, axis));
        v1proj = Vector3.normalized(v1proj);

        // Get the vector perpendicular to the one you're rotating and the axis.  Since axis and v1proj
        // are orthonormal, this one must be a unit vector perpendicular to all three.
        Vector3 perp = Vector3.vectorProduct(axis, v1proj);

        // v2 is perpendicular to axis, so therefore it's already in the same plane as v1proj perp.
        float cosAngle = (float) Vector3.scalarProduct(v1proj, v2);
        float sinAngle = (float) -Vector3.scalarProduct(perp, v2);

        return (float) Math.atan2(sinAngle, cosAngle);
    }

    public void reloadTextures(GL10 gl, TextureManager textureManager) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        mArrowTex = textureManager.getTextureFromResource(gl, R.drawable.arrow);
        mCircleTex = textureManager.getTextureFromResource(gl, R.drawable.arrowcircle);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    public void resize(int screenWidth, int screenHeight, float fullCircleSize) {
        mArrowSizeFactor = ARROW_SIZE * Math.min(screenWidth, screenHeight);
        mArrowQuad = new TexturedQuad(mArrowTex,
                0, 0, 0,
                0.5f, 0, 0,
                0, 0.5f, 0);
        mFullCircleScaleFactor = fullCircleSize;
        mCircleSizeFactor = CIRCLE_SIZE * mFullCircleScaleFactor;
        mCircleQuad = new TexturedQuad(mCircleTex,
                0, 0, 0,
                0.5f, 0, 0,
                0, 0.5f, 0);
        mArrowOffset = mCircleSizeFactor + mArrowSizeFactor;
    }

    public void draw(GL10 gl, Vector3 lookDir, Vector3 upDir, SearchHelper searchHelper, boolean nightVisionMode) {
        float lookPhi = (float) Math.acos(lookDir.y);
        float lookTheta = (float) Math.atan2(lookDir.z, lookDir.x);

        // Positive diffPhi means you need to look up.
        float diffPhi = lookPhi - mTargetPhi;

        // Positive diffTheta means you need to look right.
        float diffTheta = lookTheta - mTargetTheta;

        // diffTheta could potentially be in the range from (-2*Pi, 2*Pi), but we need it
        // in the range (-Pi, Pi).
        if (diffTheta > (float) Math.PI) {
            diffTheta -= 2f * (float) Math.PI;
        } else if (diffTheta < -(float) Math.PI) {
            diffTheta += 2f * (float) Math.PI;
        }

        // The image I'm using is an arrow pointing right, so an angle of 0 corresponds to that.
        // This is why we're taking arctan(diffPhi / diffTheta), because diffTheta corresponds to
        // the amount we need to rotate in the xz plane and diffPhi in the up direction.
        float angle = (float) Math.atan2(diffPhi, diffTheta);

        // Need to add on the camera roll, which is the amount you need to rotate the vector (0, 1, 0)
        // about the look direction in order to get it in the same plane as the up direction.
        float roll = angleBetweenVectorsWithRespectToAxis(new Vector3(0, 1, 0), upDir, lookDir);

        angle += roll;

        // Distance is a normalized value of the distance.
        float distance = (float) (1.0 / (1.414 * Math.PI) * Math.sqrt(diffTheta * diffTheta + diffPhi * diffPhi));

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glPushMatrix();
        gl.glRotatef(angle * 180.0f / (float) Math.PI, 0, 0, -1);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);

        // 0 means the circle is not expanded at all.  1 means fully expanded.
        float expandFactor = searchHelper.getTransitionFactor();

        if (expandFactor == 0) {
            gl.glColor4x(MathsUtils.ONE, MathsUtils.ONE, MathsUtils.ONE, MathsUtils.ONE);

            float redFactor, blueFactor;
            if (nightVisionMode) {
                redFactor = 0.6f;
                blueFactor = 0;
            } else {
                redFactor = 1.0f - distance;
                blueFactor = distance;
            }

            gl.glTexEnvfv(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_COLOR,
                    new float[]{redFactor, 0.0f, blueFactor, 0.0f}, 0);

            gl.glPushMatrix();
            float circleScale = mCircleSizeFactor;
            gl.glScalef(circleScale, circleScale, circleScale);
            mCircleQuad.draw(gl);
            gl.glPopMatrix();

            gl.glPushMatrix();
            float arrowScale = mArrowSizeFactor;
            gl.glTranslatef(mArrowOffset * 0.5f, 0, 0);
            gl.glScalef(arrowScale, arrowScale, arrowScale);
            mArrowQuad.draw(gl);
        } else {
            gl.glColor4x(MathsUtils.ONE, MathsUtils.ONE, MathsUtils.ONE,
                    MathsUtils.floatToFixedPoint(0.7f));

            gl.glTexEnvfv(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_COLOR,
                    new float[]{1, nightVisionMode ? 0 : 0.5f, 0, 0.0f}, 0);

            gl.glPushMatrix();
            float circleScale = mFullCircleScaleFactor * expandFactor +
                    mCircleSizeFactor * (1 - expandFactor);
            gl.glScalef(circleScale, circleScale, circleScale);
            mCircleQuad.draw(gl);
        }
        gl.glPopMatrix();
        gl.glPopMatrix();

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

        gl.glDisable(GL10.GL_BLEND);
    }

    public void setTarget(Vector3 position) {
        position = Vector3.normalized(position);
        mTargetPhi = (float) Math.acos(position.y);
        mTargetTheta = (float) Math.atan2(position.z, position.x);
    }
}
package top.someones.cardmatch.ui.game;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3d extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final boolean mReverse;
    private Camera mCamera;

    public Rotate3d(float fromDegrees, float toDegrees, float centerX, float centerY, float depthZ, boolean reverse) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mReverse = reverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
        final Matrix matrix = t.getMatrix();
        mCamera.save();
        if (mReverse) {
            mCamera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
            mCamera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        mCamera.rotateY(degrees);
        mCamera.getMatrix(matrix);
        mCamera.restore();
        matrix.preTranslate(-mCenterX, -mCenterY);
        matrix.postTranslate(mCenterX, mCenterY);
    }
}
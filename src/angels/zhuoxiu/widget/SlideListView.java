package angels.zhuoxiu.widget;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public class SlideListView extends ListView {
	static final String tag = SlideListView.class.getSimpleName();
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
	private Context mContext;
	private int mMaxYOverscrollDistance;
	View headerView;

	public SlideListView(Context context) {
		this(context, null);
	}

	public SlideListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initBounceListView();
	}

	private void initBounceListView() {
		final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		final float density = metrics.density;
		mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
	}

	@Override
	public void addHeaderView(View v) {
		if (v != null) {
			super.addHeaderView(v);
			headerView = v;
			headerView.setVisibility(View.GONE);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void removeView(final View child) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1) {
//			child.setVisibility(View.INVISIBLE);
//			android.view.ViewPropertyAnimator vpa = child.animate();
//			vpa.setDuration(1000);
//			child.setPivotX(0);
//			child.setPivotY(0);
//		//	vpa.translationY(getHeight());
//			vpa.scaleY(0);
//			vpa.start();
			animHideShowView(child,new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					child.setVisibility(GONE);
				}
			},child.getMeasuredHeight()+child.getPaddingBottom()+child.getPaddingTop(),false,500);
		}
//		super.removeView(child);
	}
	
    public static void animHideShowView(final View v, AnimationListener al, int measureHeight, final boolean show, int ainmTime) {

        if (measureHeight == 0) {
                measureHeight = v.getMeasuredHeight();
        }
        final int heightMeasure = measureHeight;
        Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                        if (interpolatedTime == 1) {
                               
                                v.setVisibility(show ? View.VISIBLE : View.GONE);
                        } else {
                                int height;
                                if (show) {
                                        height = (int) (heightMeasure * interpolatedTime);
                                } else {
                                        height = heightMeasure - (int) (heightMeasure * interpolatedTime);
                                }
                                v.getLayoutParams().height = height;
                                v.requestLayout();
                        }
                }

                @Override
                public boolean willChangeBounds() {
                        return true;
                }
        };

        if (al != null) {
                anim.setAnimationListener(al);
        }
        anim.setDuration(ainmTime);
        v.startAnimation(anim);
}

	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (headerView != null) {
			int h = headerView.getMeasuredHeight();
			if (h > 0) {
				final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
				final float density = metrics.density;
				mMaxYOverscrollDistance = (int) (density * h);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("NewApi")
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX,
			int maxOverScrollY, boolean isTouchEvent) {
		if (headerView != null) {
			if (-scrollY == mMaxYOverscrollDistance && headerView.getVisibility() != VISIBLE) {
				headerView.setVisibility(VISIBLE);
				headerView.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
			} else if (scrollY == 0 && headerView.getVisibility() == VISIBLE) {
				Animation animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
				animation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						headerView.setVisibility(GONE);
					}
				});
				headerView.startAnimation(animation);

			}

		}
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
		} else {
			return false;
		}
	}

}

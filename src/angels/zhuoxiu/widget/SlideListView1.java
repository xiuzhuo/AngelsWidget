package angels.zhuoxiu.widget;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ListView;

public class SlideListView1 extends ListView {

	public interface SlideAnimationListener {
		public void onAnimationStart(View view, int type);

		public void onAnimationEnd(View view, int type);
	}

	static final String tag = SlideListView1.class.getSimpleName();
	static final int ANIM_TIME_SHORT = 1000;
	static final int ANIM_TIME_MIDDLE = 2000;
	static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
	static final int TYPE_RESET = 1, TYPE_LEFT = 2, TYPE_RIGHT = 4, TYPE_UP = 8, TYPE_DOWN = 16, TYPE_HIDE = 32, TYPE_SHOW = 64, TYPE_SHRINK_X = 128,
			TYPE_STRETCH_X = 256, TYPE_SHRINK_Y = 512, TYPE_STRETCH_Y = 1024;

	private int mMaxYOverscrollDistance;
	View headerView;

	SlideAnimationListener animationListener;

	public SlideListView1(Context context) {
		this(context, null);
	}

	public SlideListView1(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideListView1(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initBounceListView();
	}

	private void initBounceListView() {
		final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (headerView != null) {
			int h = headerView.getMeasuredHeight();
			if (h > 0) {
				final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
				final float density = metrics.density;
				mMaxYOverscrollDistance = (int) (density * h);
			}
		}
	}

	public void setAnimationListener(SlideAnimationListener al) {
		this.animationListener = al;
	}

	public void startAnimation(final View v, int... types) {
		final int measuredWidth = v.getMeasuredWidth();
		final int measuredHeight = v.getMeasuredHeight();
		final float startAlpha = getAlpha(), startX = getX(), startY = getY();
		AnimationSet animSet = new AnimationSet(true);
		for (final int type : types) {
			Animation anim = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					v.setAlpha((type & TYPE_SHOW) > 0 ^ (type & TYPE_HIDE) > 0 ? ((type & TYPE_SHOW) > 0 ? interpolatedTime : 1 - interpolatedTime)
							: startAlpha);
					v.setX((type & TYPE_LEFT) > 0 ^ (type & TYPE_RIGHT) > 0 ? ((type & TYPE_LEFT) > 0 ? -1 : 1) * measuredWidth + (1 - interpolatedTime)
							* startX : startX);
					v.setY((type & TYPE_UP) > 0 ^ (type & TYPE_DOWN) > 0 ? ((type & TYPE_UP) > 0 ? -1 : 1) * measuredHeight + (1 - interpolatedTime) * startX
							: startY);
					v.getLayoutParams().width = (int) ((type & TYPE_STRETCH_X) > 0 ^ (type & TYPE_SHRINK_X) > 0 ? measuredWidth
							* ((type & TYPE_STRETCH_X) > 0 ? interpolatedTime : 1 - interpolatedTime) : measuredWidth);
					v.getLayoutParams().height = (int) ((type & TYPE_STRETCH_Y) > 0 ^ (type & TYPE_SHRINK_Y) > 0 ? measuredHeight
							* ((type & TYPE_STRETCH_Y) > 0 ? interpolatedTime : 1 - interpolatedTime) : measuredHeight);
				}
			};
			anim.setDuration(ANIM_TIME_MIDDLE);
			if (animationListener != null) {
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						animationListener.onAnimationStart(v, type);

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						animationListener.onAnimationEnd(v, type);
					}
				});
			}
			animSet.addAnimation(anim);
		}
		v.startAnimation(animSet);
	}

	public void animHideShowView(final View view, AnimationListener al, final boolean show) {
		final int heightMeasure = getMeasuredHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 0) {
					view.setVisibility(show ? View.GONE : View.VISIBLE);
				} else if (interpolatedTime == 1) {
					view.setVisibility(show ? View.VISIBLE : View.GONE);
				} else {
					int height;
					if (show) {
						height = (int) (heightMeasure * interpolatedTime);
					} else {
						height = heightMeasure - (int) (heightMeasure * interpolatedTime);
					}
					view.getLayoutParams().height = height;
					view.requestLayout();
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
		anim.setDuration(ANIM_TIME_MIDDLE);
		this.startAnimation(anim);
	}

	@SuppressLint("NewApi")
	public void animSlideLeftRight(final View view, AnimationListener al, final int direction) {
		final int widthMeasure = getMeasuredWidth();
		final float xStart = getX();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 0) {

				} else if (interpolatedTime == 1) {
				} else {
					float xNew = 0;
					switch (direction) {
					case TYPE_LEFT:
						xNew = -interpolatedTime * widthMeasure + (1 - interpolatedTime) * xStart;
						break;
					case TYPE_RIGHT:
						xNew = interpolatedTime * widthMeasure + (1 - interpolatedTime) * xStart;
						break;
					}
					view.setX(xNew);
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
		anim.setDuration(ANIM_TIME_MIDDLE);
		this.startAnimation(anim);
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

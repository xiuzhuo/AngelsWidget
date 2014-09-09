package angels.zhuoxiu.widget;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public class ElasticListView extends ListView {
	static final String tag = ElasticListView.class.getSimpleName();
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
	private Context mContext;
	private int mMaxYOverscrollDistance;
	View headerView;

	public ElasticListView(Context context) {
		super(context);
		mContext = context;
		initBounceListView();
	}

	public ElasticListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initBounceListView();
	}

	public ElasticListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initBounceListView();
	}

	private void initBounceListView() {
		// get the density of the screen and do some maths with it on the max
		// overscroll distance
		// variable so that you get similar behaviors no matter what the screen
		// size

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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (headerView != null) {
			int h = headerView.getMeasuredHeight();
			Log.i(tag, "h=" + h);
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
		// This is where the magic happens, we have replaced the incoming
		// maxOverScrollY with our own custom variable mMaxYOverscrollDistance; 
		Log.i(tag, "scrollY=" + scrollY + " mMaxYOverscrollDistance=" + mMaxYOverscrollDistance+" isTouchEvent "+isTouchEvent);
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

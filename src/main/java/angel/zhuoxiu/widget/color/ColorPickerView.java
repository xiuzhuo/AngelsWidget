package angel.zhuoxiu.widget.color;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import angel.zhuoxiu.widget.R;

/**
 * Created by zxui on 29/04/15.
 */
class ColorPickerView extends FrameLayout implements ColorPickerConstant {
    int[] colors = defaultColors;
    GridView gridView;
    ColorPickerListener colorPickedListener;
    ColorAdapter colorAdapter = new ColorAdapter();

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        setMinimumHeight(500);
        inflate(context, R.layout.color_grid, this);
        gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(colorAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (colorPickedListener != null) {
                    colorPickedListener.onColorPicked(position, colorAdapter.getItem(position));
                }
            }
        });
    }

    public void setColorPickedListener(ColorPickerListener colorPickedListener) {
        this.colorPickedListener = colorPickedListener;
    }

    public void setColors(int... colors) {
        this.colors = colors;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.color_grid, this);
    }

    class ColorAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            Log.i("ColorPickerView", "getCount " + colors.length);
            return colors.length;
        }

        @Override
        public Integer getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return colors[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i("ColorPickerView", "getView " + position + " convertView=" + convertView);
            if (convertView == null) {
                convertView = new ColorPickerCell(getContext());
            }
            convertView.setBackgroundColor(getItem(position));
            return convertView;
        }
    }

}

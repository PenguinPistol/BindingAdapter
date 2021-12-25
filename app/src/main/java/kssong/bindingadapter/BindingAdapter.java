package kssong.bindingadapter;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://developer.android.com/topic/libraries/data-binding">DataBinding</a> 을 이용한 Generic RecyclerView Adapter 이다.<br/>
 * 단일/다중 선택모드를 이용하여 아이템 선택관련 처리를 할 수 있다.<br/>
 *
 * @author Penguin Pistol(송경석) / <a href="https://github.com/penguinpistol">Github</a>
 * @version 1.0
 */
public abstract class BindingAdapter<T, B extends ViewDataBinding> extends RecyclerView.Adapter<BindingAdapter<T, B>.ViewHolder> {

    private static final String TAG = BindingAdapter.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SELECT_MODE_NONE, SELECT_MODE_SINGLE, SELECT_MODE_MULTI})
    public @interface SelectMode {}

    public static final int SELECT_MODE_NONE    = 0;
    public static final int SELECT_MODE_SINGLE  = 1;
    public static final int SELECT_MODE_MULTI   = 2;

    private final int layoutId;
    private final List<T> items;
    private final int checkMarkerViewId;
    private final SparseBooleanArray selectedItems;

    @SelectMode private int selectMode;
    private int lastSelectedItemPosition = -1;

    public BindingAdapter(@LayoutRes int layoutId) {
        this(layoutId, new ArrayList<>(), -1, SELECT_MODE_NONE);
    }

    public BindingAdapter(@LayoutRes int layoutId, @NonNull List<T> items) {
        this(layoutId, items, -1, SELECT_MODE_NONE);
    }

    public BindingAdapter(@LayoutRes int layoutId, @NonNull List<T> items, @IdRes int checkMarkerViewId) {
        this(layoutId, items, checkMarkerViewId, SELECT_MODE_NONE);
    }

    public BindingAdapter(@LayoutRes int layoutId, @NonNull List<T> items, @IdRes int checkMarkerViewId, @SelectMode int selectMode) {
        this.layoutId = layoutId;
        this.items = items;
        this.checkMarkerViewId = checkMarkerViewId;
        this.selectMode = selectMode;
        this.selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        B binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        int realPosition = viewHolder.getAdapterPosition();
        B binding = viewHolder.binding;
        T item = items.get(realPosition);

        initItemView(binding, item);

        // 선택된 아이템
        if(selectMode != SELECT_MODE_NONE) {
            if (checkMarkerViewId != -1) {
                View checkMarker = binding.getRoot().findViewById(checkMarkerViewId);
                if(checkMarker != null) {
                    checkMarker.setVisibility(selectedItems.get(realPosition) ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "onBindViewHolder >> Not Found Check Marker View in " + binding.getClass().getSimpleName());
                }
            }
        }

        binding.getRoot().setOnClickListener(v -> {
            setItemSelect(realPosition);
            onItemClick(binding, item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ItemView 초기화
     * @param binding ItemView Binding Object
     * @param item Item Data
     */
    public abstract void initItemView(B binding, T item);

    /**
     * ItemView 클릭 Callback
     * @param binding item view binding object
     * @param item Item data
     */
    public void onItemClick(B binding, T item) {
        // Override
    }

    /**
     * 선택모드를 변경한다.<br/>
     * 선택모드 변경시 선택된 아이템은 초기화 된다.<br/>
     * <br/>
     * SELECT_MODE_NONE - 보기모드<br/>
     * SELECT_MODE_SINGLE - 단일선택모드<br/>
     * SELECT_MODE_MULTI - 다중선택모드<br/>
     * <br/>
     *
     * @param selectMode {@link kssong.bindingadapter.BindingAdapter.SelectMode}
     */
    public void setSelectMode(int selectMode) {
        this.selectMode = selectMode;

        // 선택 초기화
        for(int i = 0 ; i < selectedItems.size(); i++){
            int position = selectedItems.keyAt(i);
            selectedItems.delete(position);
            notifyItemChanged(position);
        }
        lastSelectedItemPosition = -1;
    }

    /**
     * 현재 선택모드를 가져온다.
     * @return {@link kssong.bindingadapter.BindingAdapter.SelectMode}
     */
    @SelectMode
    public int getSelectMode() {
        return selectMode;
    }

    /**
     * 선택된 아이템 리스트를 가져온다.
     * @return {@link java.util.List}
     */
    @NonNull
    public List<T> getSelectedItems() {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            result.add(items.get(selectedItems.keyAt(i)));
        }
        return result;
    }

    /**
     * 선택된 아이템 수를 가져온다.
     * @return count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * 마지막으로 선택된 아이템의 위치을 가져온다.
     * @return position
     */
    public int getLastSelectedItemPosition() {
        return lastSelectedItemPosition;
    }

    /**
     * 특정 아이템을 선택처리한다.
     * @param position 선택할 아이템의 position
     */
    public void setItemSelect(int position) {
        switch(selectMode) {
            case SELECT_MODE_NONE:
                return;
            case SELECT_MODE_SINGLE:
                // 단일 선택모드
                if(selectedItems.get(position)) {
                    // 클릭한 아이템이 선택된 아이템인경우 선택해제
                    lastSelectedItemPosition = -1;
                    selectedItems.delete(position);
                } else {
                    if(lastSelectedItemPosition != -1) {
                        // 이미 선택된 다른 아이템이 있는겨우 선택해제
                        selectedItems.delete(lastSelectedItemPosition);
                        notifyItemChanged(lastSelectedItemPosition);
                    }
                    // 클릭한 아이템 선택처리
                    lastSelectedItemPosition = position;
                    selectedItems.put(position, true);
                }
                notifyItemChanged(position);
                break;
            case SELECT_MODE_MULTI:
                // 다중선택모드
                if(selectedItems.get(position)) {
                    selectedItems.delete(position);
                } else {
                    selectedItems.put(position, true);
                }
                lastSelectedItemPosition = position;
                notifyItemChanged(position);
                break;
        }
    }

    // View Holder
    class ViewHolder extends RecyclerView.ViewHolder {
        private final B binding;

        public ViewHolder(@NonNull B binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

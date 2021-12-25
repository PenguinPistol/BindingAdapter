package kssong.bindingadapter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kssong.bindingadapter.databinding.ActivityMainBinding;
import kssong.bindingadapter.databinding.LayoutListItemBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding mBinding;
    private BindingAdapter<String, LayoutListItemBinding> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        List<String> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add("item " + i);
        }

        adapter = new BindingAdapter<String, LayoutListItemBinding>(R.layout.layout_list_item, items, R.id.check) {
            @Override
            public void initItemView(LayoutListItemBinding binding, String item) {
                binding.setLabel(item);
            }

            @Override
            public void onItemClick(LayoutListItemBinding binding, String item) {
                Toast.makeText(getApplicationContext(), "Click : " + item, Toast.LENGTH_SHORT).show();
            }
        };

        mBinding.list.setAdapter(adapter);
        mBinding.btnModeNone.setOnClickListener(this);
        mBinding.btnModeSingle.setOnClickListener(this);
        mBinding.btnModeMulti.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == mBinding.btnModeNone) {
            adapter.setSelectMode(BindingAdapter.SELECT_MODE_NONE);
        } else if(v == mBinding.btnModeSingle) {
            adapter.setSelectMode(BindingAdapter.SELECT_MODE_SINGLE);
        } else if(v == mBinding.btnModeMulti) {
            adapter.setSelectMode(BindingAdapter.SELECT_MODE_MULTI);
        }
    }
}
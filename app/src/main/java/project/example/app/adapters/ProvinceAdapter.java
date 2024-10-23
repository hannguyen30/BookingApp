package project.example.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import project.example.app.R;
import project.example.app.classes.Province;

public class ProvinceAdapter extends ArrayAdapter<Province>{
    private Context mContext;
    private int mResource;

    public ProvinceAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Province> provinces) {
        super(context, resource, provinces);
        mContext = context;
        mResource = resource;
    }

    @NonNull

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_province, parent, false);
        }

        Province province = getItem(position);

        // Kiểm tra province có null không
        if (province != null) {
            TextView provinceNameTextView = convertView.findViewById(R.id.tvProvin);
            provinceNameTextView.setText(province.getName() != null ? province.getName() : "Unknown");
            // Bạn có thể cần làm tương tự cho các thuộc tính khác
        } else {
            // Xử lý khi province null, có thể đặt một giá trị mặc định hoặc thông báo
            TextView provinceNameTextView = convertView.findViewById(R.id.tvProvin);
            provinceNameTextView.setText("Unknown");
        }

        return convertView;
    }

}

package project.example.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import project.example.app.R;
import project.example.app.classes.Hotel;
import project.example.app.data.FirebaseHelper;

public class HotelAdapter extends ArrayAdapter<Hotel> implements Filterable {
    private final Context mContext;
    private final int mResource;
    private final FirebaseHelper firebaseHelper;
    private List<Hotel> hotelList; // Danh sách khách sạn gốc
    private List<Hotel> filteredList; // Danh sách khách sạn đã lọc
    private OnHotelActionListener actionListener;
    private boolean isManagerMode = false;
    public HotelAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Hotel> hotels) {
        super(context, resource, hotels);
        this.mContext = context;
        this.mResource = resource;
        this.firebaseHelper = new FirebaseHelper(); // Initialize FirebaseHelper
        this.hotelList = hotels; // Lưu danh sách khách sạn gốc
        this.filteredList = hotels; // Khởi tạo danh sách đã lọc
    }
    public void setManagerMode(boolean isManagerMode) {
        this.isManagerMode = isManagerMode;
        notifyDataSetChanged(); // Cập nhật giao diện khi thay đổi chế độ
    }


    public void setOnHotelActionListener(OnHotelActionListener listener) {
        this.actionListener = listener;
    }
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            viewHolder = new ViewHolder(convertView,isManagerMode);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Log.d("HotelAdapter", "isManagerMode: " + isManagerMode);
        Hotel hotel = getItem(position);
        if (hotel != null) {
            viewHolder.tv_Name.setText(hotel.getName());
            TextView tv_room_status = convertView.findViewById(R.id.tv_room_status);
            if (hotel.isAvailable()) {
                tv_room_status.setText("Còn phòng");
                tv_room_status.setTextColor(mContext.getResources().getColor(R.color.green_500));
            } else {
                tv_room_status.setText("Hết phòng");
                tv_room_status.setTextColor(mContext.getResources().getColor(R.color.red));
            }
            viewHolder.tv_Address.setText(hotel.getAddress());
            viewHolder.tv_Rate.setText("Rate: " + hotel.getRate());
            viewHolder.tv_NumReviews.setText("Lượt rate: " + hotel.getNumReviews());
            viewHolder.tv_Rooms.setText("Số phòng: " + hotel.getNumRooms());
            DecimalFormat df = new DecimalFormat("###,###");
            String formattedPrice = "VND " + df.format(hotel.getPrice()) + "/đêm";
            viewHolder.tv_Price.setText(formattedPrice);



            String amenities = hotel.getAmenities();
            String[] amenitiesArray = amenities.split(",");
            StringBuilder formattedAmenities = new StringBuilder();
            for (String amenity : amenitiesArray) {
                formattedAmenities.append(amenity.trim()).append(", ");
            }
            if (formattedAmenities.length() > 0) {
                formattedAmenities.setLength(formattedAmenities.length() - 2); // Remove last comma
            }
            viewHolder.tv_Extra.setText(formattedAmenities.toString());

            String url = hotel.getImageUrls().split(",")[0].trim();
            Picasso.get()
                    .load(Uri.parse(url))
                    .placeholder(R.drawable.app) // Placeholder image
                    .error(R.drawable.app) // Error image
                    .into(viewHolder.iv_Cover);


            if (isManagerMode) {
                viewHolder.btnEdit.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditClick(hotel); // Gọi phương thức sửa
                    }
                });

                viewHolder.btnDelete.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteClick(hotel); // Gọi phương thức xóa
                    }
                });
            }

            firebaseHelper.getProvinceNameById(hotel.getProvinceID(), new FirebaseHelper.ProvinceNameCallback() {
                @Override
                public void onCallback(String provinceName) {
                    if (provinceName != null) {
                        String fullAddress = hotel.getAddress() + " - " + provinceName;
                        viewHolder.tv_Address.setText(fullAddress); // Update address with province name

                    } else {
                        Log.e("HotelAdapter", "Province not found");
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("HotelAdapter", "Error fetching province name", e);
                }
            });
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Nullable
    @Override
    public Hotel getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase();
                List<Hotel> filteredResults = new ArrayList<>();
                FilterResults results = new FilterResults();

                if (charString.isEmpty()) {
                    // Nếu không có từ khóa, hiển thị toàn bộ danh sách
                    filteredResults = new ArrayList<>(hotelList);
                    results.values = filteredResults;
                    return results;
                } else {
                    // Tìm theo tên khách sạn trước
                    for (Hotel hotel : hotelList) {
                        if (hotel.getName().toLowerCase().contains(charString)) {
                            filteredResults.add(hotel);
                        }
                    }

                    // Nếu đã tìm thấy kết quả theo tên khách sạn, trả về ngay
                    if (!filteredResults.isEmpty()) {
                        results.values = filteredResults;
                        return results;
                    }

                    // Nếu không tìm thấy theo tên khách sạn, tiếp tục tìm theo tên tỉnh
                    List<Hotel> provinceFilteredResults = new ArrayList<>();
                    final int[] remainingRequests = {hotelList.size()}; // Đếm số yêu cầu chưa hoàn thành

                    for (Hotel hotel : hotelList) {
                        firebaseHelper.getProvinceNameById(hotel.getProvinceID(), new FirebaseHelper.ProvinceNameCallback() {
                            @Override
                            public void onCallback(String provinceName) {
                                if (provinceName != null && provinceName.toLowerCase().contains(charString)) {
                                    provinceFilteredResults.add(hotel); // Thêm vào kết quả nếu tên tỉnh khớp
                                }
                                remainingRequests[0]--;

                                // Khi tất cả các callback đã hoàn thành, gán kết quả vào results
                                if (remainingRequests[0] == 0) {
                                    results.values = provinceFilteredResults;
                                    publishResults(constraint, results);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("HotelAdapter", "Error fetching province name", e);
                                remainingRequests[0]--;

                                // Đảm bảo gọi publishResults ngay cả khi có lỗi
                                if (remainingRequests[0] == 0) {
                                    results.values = provinceFilteredResults;
                                    publishResults(constraint, results);
                                }
                            }
                        });
                    }

                    // Tạm thời trả về null, vì kết quả sẽ được publish trong callback khi tất cả yêu cầu hoàn thành
                    return null;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.values != null) {
                    filteredList = (List<Hotel>) results.values;
                } else {
                    filteredList = new ArrayList<>(); // Nếu không có kết quả, trả về danh sách rỗng
                }
                notifyDataSetChanged(); // Cập nhật giao diện
            }
        };
    }

    public void updateHotels(List<Hotel> newHotels) {
        filteredList.clear(); // Xóa danh sách hiện tại
        filteredList.addAll(newHotels); // Thêm danh sách mới
        notifyDataSetChanged(); // Thông báo adapter để cập nhật giao diện
    }
    private static class ViewHolder {
        TextView tv_Rooms;
        TextView tv_Name;
        TextView tv_Address;
        TextView tv_Price;
        TextView tv_Extra;
        TextView tv_Rate;
        TextView tv_NumReviews;
        ImageView iv_Cover;
        Button btnEdit;
        Button btnDelete;
        ViewHolder(View view, boolean isManagerMode) {
            tv_Name = view.findViewById(R.id.tv_hotelname);
            tv_Address = view.findViewById(R.id.tv_address);
            tv_Price = view.findViewById(R.id.tv_price);
            tv_Extra = view.findViewById(R.id.tv_extra);
            tv_Rate = view.findViewById(R.id.tv_rate);
            tv_NumReviews = view.findViewById(R.id.tv_numReviews);
            iv_Cover = view.findViewById(R.id.imgvw_cover);
            tv_Rooms = view.findViewById(R.id.tv_Rooms);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
    public interface OnHotelActionListener {
        void onEditClick(Hotel hotel);
        void onDeleteClick(Hotel hotel);
    }
}

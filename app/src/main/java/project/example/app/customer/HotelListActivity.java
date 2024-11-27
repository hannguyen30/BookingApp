package project.example.app.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import project.example.app.R;
import project.example.app.adapters.HotelAdapter;
import project.example.app.classes.Hotel;
import project.example.app.data.FirebaseHelper;

public class HotelListActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private ArrayList<Hotel> hotels;
    private ArrayList<Hotel> originalHotels;
    private HotelAdapter adapter;
    private Spinner spinner_price_filter,spinner_rate_filter,spinner_sort_filter;
    private TextView txtFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_customer_hotel_list);

        firebaseHelper = new FirebaseHelper();

        ListView lvw_customerHotelList = findViewById(R.id.lvw_customerHotels);
        Button btn_provinceName = findViewById(R.id.btn_provinceName);
        Button btn_groupDetail = findViewById(R.id.btn_groupDetail);
        spinner_price_filter = findViewById(R.id.spinner_price_filter);
        spinner_rate_filter = findViewById(R.id.spinner_rate_filter);
        spinner_sort_filter = findViewById(R.id.spinner_sort_filter);
        txtFilter = findViewById(R.id.txt_filter);

        spinner_price_filter = findViewById(R.id.spinner_price_filter);
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this,
                R.array.price_filter_options, android.R.layout.simple_spinner_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_price_filter.setAdapter(priceAdapter);

        spinner_rate_filter = findViewById(R.id.spinner_rate_filter);
        ArrayAdapter<CharSequence> rateAdapter = ArrayAdapter.createFromResource(this,
                R.array.rate_filter_options, android.R.layout.simple_spinner_item);
        rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_rate_filter.setAdapter(rateAdapter);

        spinner_sort_filter = findViewById(R.id.spinner_sort_filter);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.price_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sort_filter.setAdapter(sortAdapter);

        CurrentGroupDetail currentGroupDetail = CurrentGroupDetail.getInstance();
        String groupDetailFormat = String.valueOf(currentGroupDetail.getNumRoom()) + " phòng - "
                + String.valueOf(currentGroupDetail.getNumAdult()) + " người lớn - "
                + String.valueOf(currentGroupDetail.getNumKid()) + " trẻ em";
        btn_groupDetail.setText(groupDetailFormat);


        hotels = new ArrayList<>();
        originalHotels = new ArrayList<>(); // Khởi tạo danh sách gốc

        adapter = new HotelAdapter(this, R.layout.item_hotel, hotels);
        lvw_customerHotelList.setAdapter(adapter);

        Intent intent = getIntent();
        String provinceId = null;
        String provinceName = null;
        int numRooms = CurrentGroupDetail.getInstance().getNumRoom();
        if (intent != null) {
            provinceId = intent.getStringExtra("provinceId");
            provinceName = intent.getStringExtra("provinceName");

        }
        btn_provinceName.setText(provinceName);
        loadHotelsByProvinceID(provinceId, numRooms);

        // Set up item click listener for ListView
        lvw_customerHotelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Hotel selectedHotel = hotels.get(position);

                // Create Intent to start HotelDetailActivity
                Intent intent = new Intent(HotelListActivity.this, HotelDetailActivity.class);

                // Pass hotel details to the intent
                intent.putExtra("hotelId", selectedHotel.getId());
                intent.putExtra("ownerId", selectedHotel.getOwnerId());
                intent.putExtra("hotelName", selectedHotel.getName());
                intent.putExtra("hotelAddress", selectedHotel.getAddress());
                intent.putExtra("hotelProvinceID", selectedHotel.getProvinceID());
                intent.putExtra("hotelAmenities", selectedHotel.getAmenities());
                intent.putExtra("hotelImageUrls", selectedHotel.getImageUrls());
                intent.putExtra("hotelNumRooms", selectedHotel.getNumRooms());
                intent.putExtra("hotelNumMaxGuest", selectedHotel.getNumMaxGuest());
                intent.putExtra("hotelPrice", selectedHotel.getPrice());
                intent.putExtra("hotelRate", selectedHotel.getRate());
                intent.putExtra("hotelNumReviews", selectedHotel.getNumReviews());

                // Start HotelDetailActivity
                startActivity(intent);
            }
        });
        spinner_price_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterHotels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtFilter.setText("Bộ lọc đang áp dụng: Không có");
            }
        });

        spinner_rate_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterHotels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtFilter.setText("Bộ lọc đang áp dụng: Không có");
            }
        });
        spinner_sort_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortHotelsByPrice(); // Gọi hàm sắp xếp khi lựa chọn thay đổi
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtFilter.setText("Bộ lọc đang áp dụng: Không có");
            }
        });

    }
    private void loadHotelsByProvinceID(String provinceId, int numRooms) {
        firebaseHelper.getHotelsByProvinceID(provinceId, numRooms, new FirebaseHelper.HotelListCallback() {
            @Override
            public void onCallback(List<Hotel> hotelsList) {
                hotels.clear();
                hotels.addAll(hotelsList);

                originalHotels.clear(); // Xóa dữ liệu cũ trong originalHotels
                originalHotels.addAll(hotelsList); // Sao chép dữ liệu mới vào originalHotels
                adapter.notifyDataSetChanged();

                TextView txtv_empty = findViewById(R.id.txtv_emptyTxt);
                TextView txtv_emptyIcon = findViewById(R.id.txtv_emptyIcon);

                if (hotelsList.isEmpty()) {
                    txtv_emptyIcon.setVisibility(View.VISIBLE);
                    txtv_empty.setVisibility(View.VISIBLE);
                } else {
                    txtv_emptyIcon.setVisibility(View.GONE);
                    txtv_empty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                // Xử lý lỗi
            }
        });
    }

    private void filterHotels() {
        String selectedPrice = spinner_price_filter.getSelectedItem().toString();
        String selectedRating = spinner_rate_filter.getSelectedItem().toString();

        ArrayList<Hotel> filteredHotels = new ArrayList<>();
        for (Hotel hotel : originalHotels) {
            boolean matchesPrice = matchesPriceFilter(hotel, selectedPrice);
            boolean matchesRating = matchesRatingFilter(hotel, selectedRating);

            if (matchesPrice && matchesRating) {
                filteredHotels.add(hotel);
            }
        }

        adapter.updateHotels(filteredHotels);
        Log.d("HotelListActivity", "Số lượng khách sạn tìm thấy sau khi lọc: " + filteredHotels.size());
        sortHotelsByPrice();
        txtFilter.setText("Bộ lọc đang áp dụng: Giá: " + selectedPrice + ", Đánh giá: " + selectedRating);
    }


    private boolean matchesPriceFilter(Hotel hotel, String priceRange) {
        int price = hotel.getPrice();
        switch (priceRange) {
            case "Dưới 500.000 VNĐ":
                return price < 500000;
            case "500.000 - 1.000.000 VNĐ":
                return price >= 500000 && price <= 1000000;
            case "1.000.000 - 2.000.000 VNĐ":
                return price > 1000000 && price <= 2000000;
            case "Trên 2.000.000 VNĐ":
                return price > 2000000;
            default:
                return true; // Hiển thị tất cả nếu không có bộ lọc
        }
    }
    private boolean matchesRatingFilter(Hotel hotel, String ratingRange) {
        double rating = hotel.getRate();
        switch (ratingRange) {
            case "Dưới 2.0":
                return rating < 2.0;
            case "2.0 - 4.0":
                return rating >= 2.0 && rating <= 4.0;
            case "Trên 4.0":
                return rating > 4.0;
            default:
                return true; // Hiển thị tất cả nếu "Không có" được chọn
        }
    }
    private void sortHotelsByPrice() {
        String sortOption = spinner_sort_filter.getSelectedItem().toString();
        if (sortOption.equals("Giá tăng dần")) {
            // Sắp xếp theo giá tăng dần
            hotels.sort((hotel1, hotel2) -> Integer.compare(hotel1.getPrice(), hotel2.getPrice()));
        } else if (sortOption.equals("Giá giảm dần")) {
            // Sắp xếp theo giá giảm dần
            hotels.sort((hotel1, hotel2) -> Integer.compare(hotel2.getPrice(), hotel1.getPrice()));
        }

        adapter.notifyDataSetChanged(); // Cập nhật giao diện sau khi sắp xếp
        Log.d("HotelListActivity", "Danh sách khách sạn sau khi sắp xếp theo giá: " + hotels.size());
    }

}
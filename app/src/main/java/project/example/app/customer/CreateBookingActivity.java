package project.example.app.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Calendar;

import project.example.app.R;
import project.example.app.adapters.CustomerImageAdapter;
import project.example.app.authen.CurrentUserManager;
import project.example.app.classes.Hotel;
import project.example.app.classes.User;
import project.example.app.data.FirebaseHelper;

public class CreateBookingActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private RecyclerView recyclerViewImages;
    private CustomerImageAdapter imageAdapter;
    private TextView txtv_guestName, txtv_guestEmail, txtv_guestPhone,
            txtv_hotelName, txtv_hotelAddress, txtv_hotelProvince,
            txtv_bookingNumRoom, txtv_bookingNumAdult, txtv_bookingNumKid,
            txtv_bookingCheckin, txtv_bookingCheckout, txtv_price, txtv_totalPrice,
            txtv_hotelOwnerName, txtv_hotelOwnerEmail, txtv_hotelOwnerPhone;
    private Button btn_confirmBooking, btn_cancelBooking;
    private CheckBox checkBoxPool, checkBoxWifi, checkBoxTV, checkBox24;
    private Hotel selectedHotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_customer_create_booking);

        txtv_guestName = findViewById(R.id.txtv_guestName);
        txtv_guestEmail = findViewById(R.id.txtv_guestEmail);
        txtv_guestPhone = findViewById(R.id.txtv_guestPhone);
        txtv_hotelName = findViewById(R.id.txtv_hotelName);
        txtv_hotelAddress = findViewById(R.id.txtv_hotelAddress);
        txtv_hotelProvince = findViewById(R.id.txtv_hotelProvince);
        txtv_bookingNumRoom = findViewById(R.id.txtv_bookingNumRoom);
        txtv_bookingNumAdult = findViewById(R.id.txtv_bookingNumAdult);
        txtv_bookingNumKid = findViewById(R.id.txtv_bookingNumKid);
        txtv_bookingCheckin = findViewById(R.id.txtv_bookingCheckin);
        txtv_bookingCheckout = findViewById(R.id.txtv_bookingCheckout);
        txtv_price = findViewById(R.id.txtv_price);
        txtv_totalPrice = findViewById(R.id.txtv_totalPrice);
        txtv_hotelOwnerName = findViewById(R.id.txtv_hotelOwnerName);
        txtv_hotelOwnerEmail = findViewById(R.id.txtv_hotelOwnerEmail);
        txtv_hotelOwnerPhone = findViewById(R.id.txtv_hotelOwnerPhone);
        checkBoxPool = findViewById(R.id.checkBoxeditPool);
        checkBoxWifi = findViewById(R.id.checkBoxeditWifi);
        checkBoxTV = findViewById(R.id.checkBoxeditTV);
        checkBox24 = findViewById(R.id.checkBoxedit24);
        btn_confirmBooking = findViewById(R.id.btn_confirmBooking);
        btn_cancelBooking = findViewById(R.id.btn_cancelBooking);
        recyclerViewImages = findViewById(R.id.recyclerView_editImages);

        Intent intent = getIntent();
        selectedHotel = new Hotel(
                intent.getStringExtra("hotelId"),
                intent.getStringExtra("ownerId"),
                intent.getStringExtra("hotelName"),
                intent.getStringExtra("hotelAddress"),
                intent.getStringExtra("hotelProvinceID"),
                intent.getStringExtra("hotelAmenities"),
                intent.getStringExtra("hotelImageUrls"),
                intent.getIntExtra("hotelNumRooms", 0),
                intent.getIntExtra("hotelNumMaxGuest", 0),
                intent.getIntExtra("hotelPrice", 0),
                intent.getIntExtra("hotelNumReviews", 0),
                intent.getIntExtra("hotelRate", 0),
                intent.getBooleanExtra("isVariable",true)
        );

        CurrentGroupDetail currentGroupDetail = CurrentGroupDetail.getInstance();
        CurrentUserManager currentUserManager = CurrentUserManager.getInstance();

        String checkInDate = intent.getStringExtra("checkInDate");
        int dayNum = intent.getIntExtra("selectedDays", 0);
        int selectedRooms = intent.getIntExtra("selectedRooms", 0);
        int totalPrice = selectedHotel.getPrice() * dayNum * selectedRooms;

        String numRoomFormat = String.valueOf( selectedRooms + " phòng");
        String numAdultFormat = String.valueOf(currentGroupDetail.getNumAdult()) + " người lớn";
        String numKidFormat = String.valueOf(currentGroupDetail.getNumKid()) + " trẻ em";
        String priceFormat = formatCurrency(selectedHotel.getPrice());
        String totalPriceFormat = formatCurrency(totalPrice);

        txtv_guestName.setText(currentUserManager.getUserFullName());
        txtv_guestEmail.setText(currentUserManager.getUserEmail());
        txtv_guestPhone.setText(currentUserManager.getUserPhone());
        txtv_hotelName.setText(selectedHotel.getName());
        txtv_hotelAddress.setText(selectedHotel.getAddress());
        txtv_bookingNumRoom.setText(numRoomFormat);
        txtv_bookingNumAdult.setText(numAdultFormat);
        txtv_bookingNumKid.setText(numKidFormat);
        txtv_price.setText(priceFormat);
        txtv_totalPrice.setText(totalPriceFormat);

        firebaseHelper = new FirebaseHelper();
        firebaseHelper.getProvinceNameById(selectedHotel.getProvinceID(), new FirebaseHelper.ProvinceNameCallback() {
            @Override
            public void onCallback(String provinceName) {
                if (provinceName != null) {
                    txtv_hotelProvince.setText(provinceName);
                } else {
                    Log.e("GetProvinceName", "Province not found");
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("GetProvinceName", "Error fetching data", e);
            }
        });

        firebaseHelper.getUserById(selectedHotel.getOwnerId(), new FirebaseHelper.UserGetCallback(){
            public void onCallback(User user) {
                if (user != null) {
                    String hotelOwnerNameFormat = "Họ tên: " + String.valueOf(user.getFullName());
                    String hotelOwnerEmailFormat = "Email: " + String.valueOf(user.getEmail());
                    String hotelOwnerPhoneFormat = "Số điện thoại: " + String.valueOf(user.getPhone());

                    txtv_hotelOwnerName.setText(hotelOwnerNameFormat);
                    txtv_hotelOwnerEmail.setText(hotelOwnerEmailFormat);
                    txtv_hotelOwnerPhone.setText(hotelOwnerPhoneFormat);
                    Log.d("OwnerInfo", "Lấy owner info thành công!");

                } else {
                    Toast.makeText(CreateBookingActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("FirebaseError", Objects.requireNonNull(e.getMessage()));
            }
        });

        ArrayList<String> imageUrlList = new ArrayList<>(Arrays.asList(selectedHotel.getImageUrls().split(",")));

        setAmenities(selectedHotel.getAmenities());
        setupRecyclerView();
        loadImages(imageUrlList);

        //datehere
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            // Chuyển checkinDate từ String thành Date
            Date checkInDateObj = dateFormat.parse(checkInDate);

            if (checkInDateObj != null) {
                // Cài đặt đối tượng Calendar với checkinDate
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(checkInDateObj);

                // Cộng thêm dayNum ngày vào calendar
                calendar.add(Calendar.DAY_OF_YEAR, dayNum);

                // Tính toán checkoutDate
                String checkoutDate = dateFormat.format(calendar.getTime());

                // Hiển thị ngày trong các TextView
                txtv_bookingCheckin.setText(checkInDate);  // Hiển thị ngày check-in
                txtv_bookingCheckout.setText(checkoutDate);  // Hiển thị ngày checkout
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        String checkOutDate = txtv_bookingCheckout.getText().toString();
        int numRoom = selectedRooms;
        int numAdult = currentGroupDetail.getNumAdult();
        int numKid = currentGroupDetail.getNumKid();

        btn_confirmBooking.setOnClickListener(v -> {
            firebaseHelper.addBooking(currentUserManager.getUserId(), selectedHotel.getId(), checkInDate, checkOutDate, numRoom, numAdult, numKid, totalPrice,  new FirebaseHelper.BookingAddCallback() {
                @Override
                public void onSuccess(String savedId) {
                    // Hiển thị thông báo thành công
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBookingActivity.this, "Book thành công!", Toast.LENGTH_SHORT).show();
                        Intent newIntent = new Intent(CreateBookingActivity.this, HomeActivity.class);
                        startActivity(newIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    // Hiển thị thông báo lỗi
                    runOnUiThread(() -> {
                        Toast.makeText(CreateBookingActivity.this, "Book thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        btn_cancelBooking.setOnClickListener(v -> {
            // Cập nhật trạng thái phòng thành còn phòng
            firebaseHelper.updateHotelAvailability(selectedHotel.getId(), true, new FirebaseHelper.AvailabilityUpdateCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CreateBookingActivity.this, "Hủy đặt phòng thành công và đã cập nhật trạng thái phòng!", Toast.LENGTH_SHORT).show();
                    Intent newIntent = new Intent(CreateBookingActivity.this, HomeActivity.class);
                    startActivity(newIntent);
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CreateBookingActivity.this, "Hủy đặt phòng thành công nhưng không thể cập nhật trạng thái phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


    }


    private void setAmenities(String amenities) {
        // Tách chuỗi tiện ích thành một mảng các tiện ích riêng lẻ
        String[] amenitiesArray = amenities.split(",");

        // Đặt trạng thái của các CheckBox dựa trên các tiện ích đã tách được
        checkBoxPool.setChecked(false);
        checkBoxWifi.setChecked(false);
        checkBoxTV.setChecked(false);
        checkBox24.setChecked(false);

        for (String amenity : amenitiesArray) {
            switch (amenity.trim().toLowerCase()) {
                case "pool":
                    checkBoxPool.setChecked(true);
                    break;
                case "wifi":
                    checkBoxWifi.setChecked(true);
                    break;
                case "tv":
                    checkBoxTV.setChecked(true);
                    break;
                case "24/7":
                    checkBox24.setChecked(true);
                    break;
            }
        }
    }

    public static String formatCurrency(int amount) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return numberFormat.format(amount) + "đ";
    }

    private void setupRecyclerView() {
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new CustomerImageAdapter(this, new ArrayList<>());
        recyclerViewImages.setAdapter(imageAdapter);
    }

    private void loadImages(ArrayList<String> imageUrls) {
        // Tạo một adapter mới nếu cần
        if (imageAdapter == null) {
            imageAdapter = new CustomerImageAdapter(this, imageUrls);
            recyclerViewImages.setAdapter(imageAdapter);
        } else {
            // Cập nhật danh sách URL ảnh trong adapter
            imageAdapter.setImageUrls(imageUrls);
            // Thông báo cho adapter biết dữ liệu đã thay đổi
            imageAdapter.notifyDataSetChanged();
        }
    }
}
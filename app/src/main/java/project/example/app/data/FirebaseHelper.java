package project.example.app.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.example.app.classes.Booking;
import project.example.app.classes.Hotel;
import project.example.app.classes.Province;
import project.example.app.classes.Review;
import project.example.app.classes.Saved;
import project.example.app.classes.User;

public class FirebaseHelper {

    private FirebaseDatabase mDatabase;
    private final DatabaseReference mReferenceProvinces, mReferenceUsers, mReferenceHotels, mReferenceSaved, mReferenceBookings, mReferenceReviews;

    public FirebaseHelper() {
        mDatabase = FirebaseDatabase.getInstance();
        mReferenceProvinces = mDatabase.getReference("provinces");
        mReferenceUsers = mDatabase.getReference("users");
        mReferenceHotels = mDatabase.getReference("hotels");
        mReferenceSaved = mDatabase.getReference("saved");
        mReferenceBookings = mDatabase.getReference("bookings");
        mReferenceReviews = mDatabase.getReference("reviews");
    }

    // Lấy tên tỉnh theo id
    public void getProvinceNameById(String provinceID, ProvinceNameCallback callback) {
        DatabaseReference provinceRef = FirebaseDatabase.getInstance().getReference("provinces").child(provinceID);
        provinceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String provinceName = snapshot.child("name").getValue(String.class);
                    Log.d("FirebaseHelper", "Province found: " + provinceName); // Kiểm tra dữ liệu
                    callback.onCallback(provinceName);
                } else {
                    Log.e("FirebaseHelper", "Province with ID " + provinceID + " does not exist");
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseHelper", "Database error: " + error.getMessage());
                callback.onError(error.toException());
            }
        });
    }


    // Phương thức lấy tất cả tên của các tỉnh
    public void getAllProvinceNames(final AllProvinceNamesCallback callback) {
        mReferenceProvinces.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> provinceNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Province province = snapshot.getValue(Province.class);
                    if (province != null) {
                        provinceNames.add(province.getName());
                    }
                }
                callback.onCallback(provinceNames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    // Phương thức lấy tất cả các tỉnh
    public void getAllProvinces(final ProvinceListCallback callback) {
        mReferenceProvinces.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Province> provinces = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Province province = snapshot.getValue(Province.class);
                    if (province != null) {
                        provinces.add(province);
                    }
                }
                callback.onCallback(provinces);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //phương thức kiểm tra xác thực thông tin đăng nhập từ mail và password
    public void userAuthen(final String email, final String password, final UserAuthCallback callback) {
        mReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isAuthenticated = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userEmail = snapshot.child("email").getValue(String.class);
                    String userPassword = snapshot.child("password").getValue(String.class);
                    if (userEmail != null && userEmail.equals(email) && userPassword != null && userPassword.equals(password)) {
                        isAuthenticated = true;
                        break;
                    }
                }
                callback.onCallback(isAuthenticated);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //phương thức getUser để lấy toàn bộ thông tin của 1 user từ database
    public void getUser(final String email, final UserGetCallback callback) {
        mReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userEmail = snapshot.child("email").getValue(String.class);
                    if (userEmail != null && userEmail.equals(email)) {
                        User user = snapshot.getValue(User.class);
                        callback.onCallback(user);
                        return;
                    }
                }
                callback.onCallback(null); // Trả về null nếu không tìm thấy user
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getUserById(final String userId, final UserGetCallback callback) {
        mReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    if (id != null && id.equals(userId)) {
                        User user = snapshot.getValue(User.class);
                        callback.onCallback(user);
                        return;
                    }
                }
                callback.onCallback(null); // Trả về null nếu không tìm thấy user
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //adduser dùng cho việc đăng ký tài khoản
    public void addUser(User user, final UserAddCallback callback) {
        DatabaseReference newUserRef = mReferenceUsers.push();
        String userId = newUserRef.getKey();
        user.setId(userId);
        newUserRef.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException());
            }
        });
    }

    // Phương thức kiểm tra xem Email liệu đã tồn tại hay chưa
    public void isEmailExists(final String email, final EmailCheckCallback callback) {
        mReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userEmail = snapshot.child("email").getValue(String.class);
                    if (userEmail != null && userEmail.equals(email)) {
                        exists = true;
                        break;
                    }
                }
                callback.onCallback(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //Phương thức update thông tin user gồm 3 trường email, full name, phone
    public void updateUserInfo(String userId, String newEmail, String newFullName, String newPhone, final UserInfoUpdateCallback callback) {
        DatabaseReference userRef = mReferenceUsers.child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setEmail(newEmail);
                        user.setFullName(newFullName);
                        user.setPhone(newPhone);

                        userRef.setValue(user).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                callback.onError(task.getException());
                            }
                        });
                    } else {
                        callback.onError(new Exception("User data is null"));
                    }
                } else {
                    callback.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //Phương thức update password của user
    public void updateUserPassword(String userId, String newPassword, UserPasswordUpdateCallback callback) {
        // Tạo một HashMap để lưu thông tin mật khẩu mới
        Map<String, Object> passwordUpdates = new HashMap<>();
        passwordUpdates.put("password", newPassword);

        // Thực hiện cập nhật mật khẩu trong cơ sở dữ liệu
        mReferenceUsers.child(userId).updateChildren(passwordUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Nếu cập nhật thành công, gọi phương thức onSuccess của callback
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Nếu xảy ra lỗi, gọi phương thức onError của callback và truyền lỗi đó
                    callback.onError(e);
                });
    }

    public void updateAvatarUrl(String userId, String avatarUrl, final UserAvatarUpdateCallback callback) {
        DatabaseReference userRef = mReferenceUsers.child(userId);

        // Tạo một HashMap để lưu thông tin avatar mới
        Map<String, Object> avatarUpdates = new HashMap<>();
        avatarUpdates.put("avatarUrl", avatarUrl);

        // Thực hiện cập nhật avatar trong cơ sở dữ liệu
        userRef.updateChildren(avatarUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Nếu cập nhật thành công, gọi phương thức onSuccess của callback
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Nếu xảy ra lỗi, gọi phương thức onError của callback và truyền lỗi đó
                    callback.onError(e);
                });
    }

    // Interface callback để xử lý kết quả cập nhật avatar
    public interface UserAvatarUpdateCallback {
        void onSuccess(); // Phương thức được gọi khi cập nhật avatar thành công
        void onError(Exception e); // Phương thức được gọi khi xảy ra lỗi trong quá trình cập nhật avatar
    }


    // Phương thức lấy tất cả các khách sạn
    public void getAllHotels(final HotelListCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Hotel> hotels = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null) {
                        hotels.add(hotel);
                    }
                }
                callback.onCallback(hotels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    //tim ks theo province
    public void getHotelsByProvinceID(String provinceId, int numRooms, final HotelListCallback callback) {
        // Log provinceId để kiểm tra
        Log.d("GetHotels", "Tìm khách sạn với provinceId: " + provinceId + " và số phòng: " + numRooms);

        // Lấy dữ liệu từ Firebase theo provinceId
        mReferenceHotels.orderByChild("provinceId").equalTo(provinceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Hotel> hotels = new ArrayList<>();

                        // Kiểm tra xem có dữ liệu không
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot hotelSnapshot : dataSnapshot.getChildren()) {
                                Hotel hotel = hotelSnapshot.getValue(Hotel.class);
                                if (hotel != null) {
                                    // Kiểm tra số phòng
                                    if (hotel.getNumRooms() >= numRooms) {
                                        hotels.add(hotel);
                                    }
                                }
                            }
                            Log.d("LoadHotels", "Số lượng khách sạn tìm thấy: " + hotels.size());
                        } else {
                            Log.d("LoadHotels", "Không có khách sạn nào tìm thấy với provinceId: " + provinceId);
                        }

                        // Gọi callback với danh sách khách sạn đã lọc
                        callback.onCallback(hotels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                        Log.e("LoadHotels", "Lỗi khi tải dữ liệu: " + databaseError.getMessage());
                        callback.onError(databaseError.toException());
                    }
                });
    }
    //tim theo province hoac ten ks
    public void getHotelsByProvinceOrName(String query, FirebaseHelper.HotelListCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("hotels");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Hotel> hotels = new ArrayList<>();
                for (DataSnapshot hotelSnapshot : snapshot.getChildren()) {
                    Hotel hotel = hotelSnapshot.getValue(Hotel.class);
                    if (hotel != null) { // Kiểm tra hotel không null
                        if ((hotel.getName() != null && hotel.getName().toLowerCase().contains(query.toLowerCase())) ||
                                (hotel.getProvinceID() != null && hotel.getProvinceID().toLowerCase().contains(query.toLowerCase()))) {
                            hotels.add(hotel);
                        }
                    }
                }
                callback.onCallback(hotels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }




    // phương thức getHotelsByUserId
    public void getHotelsByUserId(String userId, final HotelListCallback callback) {
        mReferenceHotels.orderByChild("ownerId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Hotel> hotels = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null) {
                        hotels.add(hotel);
                    }
                }
                callback.onCallback(hotels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void addHotel(String ownerId, String hotelName, String address, String provinceID, String amenities,
                         String imageUrlsString, int numberOfRooms, int maxGuestsPerRoom, int price,boolean isAvailable,
                         final HotelAddCallback callback) {
        // Tạo một DatabaseReference mới để thêm khách sạn vào Firebase Realtime Database
        DatabaseReference newHotelRef = mReferenceHotels.push();
        String hotelId = newHotelRef.getKey();

        Hotel newHotel = new Hotel();
        newHotel.setId(hotelId);
        newHotel.setOwnerId(ownerId);
        newHotel.setName(hotelName);
        newHotel.setAddress(address);
        newHotel.setProvinceID(provinceID);
        newHotel.setAmenities(amenities);
        newHotel.setImageUrls(imageUrlsString);
        newHotel.setNumRooms(numberOfRooms);
        newHotel.setNumMaxGuest(maxGuestsPerRoom);
        newHotel.setPrice(price);
        newHotel.setAvailable(isAvailable);

        // Thêm khách sạn vào Firebase Realtime Database
        newHotelRef.setValue(newHotel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(newHotel.getId());
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void updateHotel(String hotelId, String ownerId, String hotelName, String address, String provinceID, String amenities,String imageUrlsString,
                            int numberOfRooms, int maxGuestsPerRoom, int price, boolean isAvailable, int currentNumReviews,double currentRate,
                            final HotelUpdateCallback callback) {

        // Tạo một DatabaseReference để trỏ đến khách sạn cần cập nhật
        DatabaseReference hotelRef = mReferenceHotels.child(hotelId);

        // Lắng nghe sự kiện một lần để lấy dữ liệu của khách sạn
        hotelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Tạo một đối tượng Hotel mới với thông tin đã được cập nhật
                    Hotel updatedHotel = new Hotel();
                    updatedHotel.setId(hotelId);
                    updatedHotel.setOwnerId(ownerId);
                    updatedHotel.setName(hotelName);
                    updatedHotel.setAddress(address);
                    updatedHotel.setProvinceID(provinceID);
                    updatedHotel.setAmenities(amenities);
                    updatedHotel.setImageUrls(imageUrlsString);
                    updatedHotel.setNumRooms(numberOfRooms);
                    updatedHotel.setNumMaxGuest(maxGuestsPerRoom);
                    updatedHotel.setPrice(price);
                    updatedHotel.setAvailable(isAvailable);
                    updatedHotel.setNumReviews(currentNumReviews);
                    updatedHotel.setRate(currentRate);

                    // Cập nhật thông tin của khách sạn vào Firebase Realtime Database
                    hotelRef.setValue(updatedHotel).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException());
                        }
                    });
                } else {
                    // Gọi phương thức onError nếu không tìm thấy khách sạn
                    callback.onError(new Exception("Hotel not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gọi phương thức onError nếu có lỗi xảy ra trong quá trình đọc dữ liệu
                callback.onError(databaseError.toException());
            }
        });
    }

    public void removeHotel(String hotelId, final HotelRemoveCallback callback) {
        // Tạo một DatabaseReference để trỏ đến khách sạn cần xoá
        DatabaseReference hotelRef = mReferenceHotels.child(hotelId);

        // Lắng nghe sự kiện một lần để kiểm tra xem khách sạn tồn tại hay không
        hotelRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Xoá khách sạn khỏi Firebase Realtime Database
                    hotelRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException());
                        }
                    });
                } else {
                    // Gọi phương thức onError nếu không tìm thấy khách sạn
                    callback.onError(new Exception("Hotel not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gọi phương thức onError nếu có lỗi xảy ra trong quá trình đọc dữ liệu
                callback.onError(databaseError.toException());
            }
        });
    }

    public void addSaved(String userId, String hotelId, final SavedAddCallback callback) {
        mReferenceSaved.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean alreadySaved = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Saved saved = snapshot.getValue(Saved.class);
                    if (saved != null && saved.getUserId().equals(userId) && saved.getHotelId().equals(hotelId)) {
                        alreadySaved = true;
                        break;
                    }
                }

                if (alreadySaved) {
                    callback.onAlreadyExists();
                } else {
                    DatabaseReference newSavedRef = mReferenceSaved.push();
                    String savedId = newSavedRef.getKey();

                    Saved newSaved = new Saved(savedId, hotelId, userId);
                    newSavedRef.setValue(newSaved).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess(savedId);
                        } else {
                            callback.onError(task.getException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getSavedHotelsByUserId(String userId, final SavedHotelsCallback callback) {
        mReferenceSaved.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> savedHotelIds = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Saved saved = snapshot.getValue(Saved.class);
                    if (saved != null && saved.getUserId().equals(userId)) {
                        savedHotelIds.add(saved.getHotelId());
                    }
                }

                if (savedHotelIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>()); // Return an empty list if no saved hotels found
                    return;
                }

                // Fetch hotel details based on the savedHotelIds
                fetchHotelDetails(savedHotelIds, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchHotelDetails(List<String> hotelIds, final SavedHotelsCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Hotel> hotels = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null && hotelIds.contains(hotel.getId())) {
                        hotels.add(hotel);
                    }
                }

                callback.onSuccess(hotels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void checkIfHotelIsSaved(String userId, String hotelId, final HotelSavedCheckCallback callback) {
        mReferenceSaved.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isSaved = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Saved saved = snapshot.getValue(Saved.class);
                    if (saved != null && saved.getUserId().equals(userId) && saved.getHotelId().equals(hotelId)) {
                        isSaved = true;
                        break;
                    }
                }

                callback.onCheckCompleted(isSaved);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void removeSaved(String userId, String hotelId, final SavedRemoveCallback callback) {
        mReferenceSaved.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Saved saved = snapshot.getValue(Saved.class);
                    if (saved != null && saved.getUserId().equals(userId) && saved.getHotelId().equals(hotelId)) {
                        snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                callback.onError(task.getException());
                            }
                        });
                        return; // Exit the loop after removing the item
                    }
                }
                callback.onError(new Exception("Saved item not found"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void addBooking(String userId, String hotelId, String checkInDate, String checkOutDate, int numberOfRooms, int numAdult, int numKid, int totalPrice, final BookingAddCallback callback) {
        // Tạo một DatabaseReference mới để thêm booking vào Firebase Realtime Database
        DatabaseReference newBookingRef = mReferenceBookings.push();
        String bookingId = newBookingRef.getKey();

        Booking newBooking = new Booking();
        newBooking.setId(bookingId);
        newBooking.setStatus("requestConfirm");
        newBooking.setUserId(userId);
        newBooking.setHotelId(hotelId);
        newBooking.setCheckInDate(checkInDate);
        newBooking.setCheckOutDate(checkOutDate);
        newBooking.setNumberOfRooms(numberOfRooms);
        newBooking.setNumAdult(numAdult);
        newBooking.setNumKid(numKid);
        newBooking.setTotalPrice(totalPrice);

        // Thêm booking vào Firebase Realtime Database
        newBookingRef.setValue(newBooking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật trạng thái phòng thành "đã hết phòng" sau khi đặt phòng thành công
                updateHotelAvailability(hotelId, false, new AvailabilityUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(newBooking.getId());
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e); // Trả về lỗi nếu cập nhật trạng thái không thành công
                    }
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }
    public void updateHotelAvailability(String hotelId, boolean isAvailable, final AvailabilityUpdateCallback callback) {
        DatabaseReference hotelRef = mReferenceHotels.child(hotelId);
        hotelRef.child("available").setValue(isAvailable).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException());
            }
        });
    }

    // Định nghĩa callback cho cập nhật trạng thái
    public interface AvailabilityUpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void updateBookingStatus(String bookingId, String newStatus, final BookingUpdateCallback callback) {
        // Tạo một HashMap để lưu thông tin trạng thái mới
        Map<String, Object> statusUpdates = new HashMap<>();
        statusUpdates.put("status", newStatus);

        // Thực hiện cập nhật trạng thái trong cơ sở dữ liệu
        mReferenceBookings.child(bookingId).updateChildren(statusUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Nếu cập nhật thành công, gọi phương thức onSuccess của callback
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Nếu xảy ra lỗi, gọi phương thức onError của callback và truyền lỗi đó
                    callback.onError(e);
                });
    }

    public void getAllConfirmHotelByGuestID(String userId, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> bookingIds = new ArrayList<>();
                Map<String, Booking> bookingMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && booking.getUserId().equals(userId) &&
                            ("requestConfirm".equals(booking.getStatus()) || "confirmed".equals(booking.getStatus()) || "requestComplete".equals(booking.getStatus()) || "requestCancel".equals(booking.getStatus()))) {
                        bookingIds.add(booking.getId());
                        bookingMap.put(booking.getId(), booking);
                    }
                }

                if (bookingIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching bookings found
                    return;
                }

                fetchHotelDetailsWithBookingIds(bookingMap, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getAllCancelHotelByGuestID(String userId, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> bookingIds = new ArrayList<>();
                Map<String, Booking> bookingMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && booking.getUserId().equals(userId) && "canceled".equals(booking.getStatus())) {
                        bookingIds.add(booking.getId());
                        bookingMap.put(booking.getId(), booking);
                    }
                }

                if (bookingIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching bookings found
                    return;
                }

                fetchHotelDetailsWithBookingIds(bookingMap, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getAllCompleteHotelByGuestID(String userId, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> bookingIds = new ArrayList<>();
                Map<String, Booking> bookingMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && booking.getUserId().equals(userId) && "completed".equals(booking.getStatus())) {
                        bookingIds.add(booking.getId());
                        bookingMap.put(booking.getId(), booking);
                    }
                }

                if (bookingIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching bookings found
                    return;
                }

                fetchHotelDetailsWithBookingIds(bookingMap, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchHotelDetailsWithBookingIds(Map<String, Booking> bookingMap, final HotelListWithBookingIdCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Hotel> hotels = new ArrayList<>();
                Map<String, String> hotelBookingIdMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null) {
                        for (Map.Entry<String, Booking> entry : bookingMap.entrySet()) {
                            String bookingId = entry.getKey();
                            Booking booking = entry.getValue();
                            if (hotel.getId().equals(booking.getHotelId())) {
                                hotels.add(hotel);
                                hotelBookingIdMap.put(hotel.getId(), bookingId);
                            }
                        }
                    }
                }

                callback.onSuccess(hotels, hotelBookingIdMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getBookingById(String bookingId, BookingCallback callback) {
        mReferenceBookings.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Booking booking = dataSnapshot.getValue(Booking.class);
                    callback.onSuccess(booking);
                } else {
                    callback.onError(new Exception("Booking not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getAllConfirmBookedHotels(String ownerId, final HotelListWithBookingIdCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot hotelSnapshot) {
                List<Hotel> ownerHotels = new ArrayList<>();
                for (DataSnapshot snapshot : hotelSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null && hotel.getOwnerId().equals(ownerId)) {
                        ownerHotels.add(hotel);
                    }
                }

                if (ownerHotels.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching hotels found
                    return;
                }

                fetchConfirmBookedHotelDetails(ownerHotels, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchConfirmBookedHotelDetails(List<Hotel> ownerHotels, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot bookingSnapshot) {
                List<Hotel> bookedHotels = new ArrayList<>();
                Map<String, String> bookingIdMap = new HashMap<>();

                for (DataSnapshot snapshot : bookingSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null &&
                            ("requestConfirm".equals(booking.getStatus()) || "confirmed".equals(booking.getStatus()) || "requestComplete".equals(booking.getStatus()) || "requestCancel".equals(booking.getStatus()))) {
                        for (Hotel hotel : ownerHotels) {
                            if (hotel.getId().equals(booking.getHotelId())) {
                                bookedHotels.add(hotel);
                                bookingIdMap.put(hotel.getId(), booking.getId()); // Use hotelId as key
                            }
                        }
                    }
                }

                callback.onSuccess(bookedHotels, bookingIdMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getAllCancelBookedHotels(String ownerId, final HotelListWithBookingIdCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot hotelSnapshot) {
                List<Hotel> ownerHotels = new ArrayList<>();
                for (DataSnapshot snapshot : hotelSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null && hotel.getOwnerId().equals(ownerId)) {
                        ownerHotels.add(hotel);
                    }
                }

                if (ownerHotels.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching hotels found
                    return;
                }

                fetchCancelBookedHotelDetails(ownerHotels, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchCancelBookedHotelDetails(List<Hotel> ownerHotels, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot bookingSnapshot) {
                List<Hotel> bookedHotels = new ArrayList<>();
                Map<String, String> bookingIdMap = new HashMap<>();

                for (DataSnapshot snapshot : bookingSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && ("canceled".equals(booking.getStatus()))) {
                        for (Hotel hotel : ownerHotels) {
                            if (hotel.getId().equals(booking.getHotelId())) {
                                bookedHotels.add(hotel);
                                bookingIdMap.put(hotel.getId(), booking.getId()); // Use hotelId as key
                            }
                        }
                    }
                }

                callback.onSuccess(bookedHotels, bookingIdMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getAllCompleteBookedHotels(String ownerId, final HotelListWithBookingIdCallback callback) {
        mReferenceHotels.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot hotelSnapshot) {
                List<Hotel> ownerHotels = new ArrayList<>();
                for (DataSnapshot snapshot : hotelSnapshot.getChildren()) {
                    Hotel hotel = snapshot.getValue(Hotel.class);
                    if (hotel != null && hotel.getOwnerId().equals(ownerId)) {
                        ownerHotels.add(hotel);
                    }
                }

                if (ownerHotels.isEmpty()) {
                    callback.onSuccess(new ArrayList<>(), new HashMap<>()); // Return empty list and map if no matching hotels found
                    return;
                }

                fetchCompleteBookedHotelDetails(ownerHotels, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void fetchCompleteBookedHotelDetails(List<Hotel> ownerHotels, final HotelListWithBookingIdCallback callback) {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot bookingSnapshot) {
                List<Hotel> bookedHotels = new ArrayList<>();
                Map<String, String> bookingIdMap = new HashMap<>();

                for (DataSnapshot snapshot : bookingSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && ("completed".equals(booking.getStatus()))) {
                        for (Hotel hotel : ownerHotels) {
                            if (hotel.getId().equals(booking.getHotelId())) {
                                bookedHotels.add(hotel);
                                bookingIdMap.put(hotel.getId(), booking.getId()); // Use hotelId as key
                            }
                        }
                    }
                }

                callback.onSuccess(bookedHotels, bookingIdMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getReviewsByHotelId(String hotelId, final ReviewCallback callback) {
        mReferenceReviews.orderByChild("hotelId").equalTo(hotelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Review> reviews = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Review review = snapshot.getValue(Review.class);
                    reviews.add(review);
                }
                callback.onSuccess(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    // Phương thức thêm review
    public void addReview(final Review review, final ReviewAddCallback callback) {
        DatabaseReference newReviewRef = mReferenceReviews.push();
        String reviewId = newReviewRef.getKey();
        review.setReviewId(reviewId);
        newReviewRef.setValue(review).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    // Phương thức kiểm tra review tồn tại
    public void isReviewDuplicate(String bookingId, String userMail, final ReviewCheckCallback callback) {
        mReferenceReviews.orderByChild("bookingId").equalTo(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isDuplicate = false;
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    String reviewerId = reviewSnapshot.child("userMail").getValue(String.class);
                    if (userMail.equals(reviewerId)) {
                        isDuplicate = true;
                        break;
                    }
                }
                callback.onCallback(isDuplicate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void getReviewsByUserMail(String userMail, final ReviewCallback callback) {
        mReferenceReviews.orderByChild("userMail").equalTo(userMail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Review> reviews = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Review review = snapshot.getValue(Review.class);
                    if (review != null) {
                        reviews.add(review);
                    }
                }
                callback.onSuccess(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void updateExpiredBookings() {
        mReferenceBookings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long currentTime = System.currentTimeMillis();
                List<String> expiredBookings = new ArrayList<>();
                Map<String, String> statusUpdates = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date checkOutDate = sdf.parse(booking.getCheckOutDate());
                            if (checkOutDate != null) {
                                long checkOutTime = checkOutDate.getTime();
                                long diffInDays = (currentTime - checkOutTime) / (1000 * 60 * 60 * 24);
                                if (diffInDays > 2) {
                                    String currentStatus = booking.getStatus();
                                    if (!"canceled".equals(currentStatus) && !"completed".equals(currentStatus)) {
                                        String newStatus = null;
                                        if ("confirmed".equals(currentStatus) || "requestComplete".equals(currentStatus)) {
                                            newStatus = "completed";
                                        } else if ("requestConfirm".equals(currentStatus) || "requestCancel".equals(currentStatus)) {
                                            newStatus = "canceled";
                                        }
                                        if (newStatus != null) {
                                            expiredBookings.add(booking.getId());
                                            statusUpdates.put(booking.getId(), newStatus);
                                        }
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("UpdateExpiredBookings", "Error parsing date: " + e.getMessage());
                        }
                    }
                }

                for (String bookingId : expiredBookings) {
                    String newStatus = statusUpdates.get(bookingId);
                    if (newStatus != null) {
                        updateBookingStatus(bookingId, newStatus, new BookingUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("UpdateExpiredBookings", "Booking ID " + bookingId + " updated to " + newStatus);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("UpdateExpiredBookings", "Error updating booking ID " + bookingId + ": " + e.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UpdateExpiredBookings", "Error accessing database: " + databaseError.getMessage());
            }
        });
    }

    public void updateHotelRate(String hotelId, int newReviewRating) {
        // Lấy thông tin khách sạn từ Firebase
        mReferenceHotels.child(hotelId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Hotel hotel = dataSnapshot.getValue(Hotel.class);
                    if (hotel != null) {
                        // Tăng số lượng đánh giá lên 1
                        int numReviews = hotel.getNumReviews() + 1;

                        // Tính toán xếp hạng trung bình mới
                        double totalRating = hotel.getRate() * hotel.getNumReviews();
                        totalRating += newReviewRating;
                        double newRate = totalRating / numReviews;

                        // Cập nhật thông tin khách sạn trong Firebase
                        mReferenceHotels.child(hotelId).child("numReviews").setValue(numReviews);
                        mReferenceHotels.child(hotelId).child("rate").setValue(newRate);
                    }
                } else {
                    Log.e("updateHotelRate", "Hotel not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("updateHotelRate", "Error: " + databaseError.getMessage());
            }
        });
    }

    // Callback interface for review check
    public interface ReviewCheckCallback {
        void onCallback(boolean isDuplicate);
        void onError(Exception e);
    }

    public interface ReviewAddCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface ReviewCallback {
        void onSuccess(List<Review> reviews);
        void onFailure(Exception e);
    }

    public interface BookingCallback {
        void onSuccess(Booking booking);
        void onError(Exception e);
    }

    // Callback interface for hotel list with booking ID
    public interface HotelListWithBookingIdCallback {
        void onSuccess(List<Hotel> hotels, Map<String, String> bookingIdMap);
        void onError(Exception e);
    }

    // Callback interface for addBooking method
    public interface BookingAddCallback {
        void onSuccess(String bookingId);
        void onError(Exception e);
    }

    // Callback interface for booking update methods
    public interface BookingUpdateCallback {
        void onSuccess(); // Phương thức được gọi khi cập nhật mật khẩu thành công
        void onError(Exception e); // Phương thức được gọi khi xảy ra lỗi trong quá trình cập nhật mật khẩu
    }

    // Interface callback để xử lý kết quả xác thực
    public interface ProvinceNameCallback {
        void onCallback(String provinceName);
        void onError(Exception e);
    }

    public interface AllProvinceNamesCallback {
        void onCallback(List<String> provinceNames);
        void onError(Exception e);
    }

    public interface ProvinceListCallback {
        void onCallback(List<Province> provinceList);
        void onError(Exception e);
    }

    public interface UserAuthCallback {
        void onCallback(boolean isAuthenticated);
        void onError(Exception e);
    }

    public interface UserGetCallback {
        void onCallback(User user);
        void onError(Exception e);
    }

    public interface UserAddCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface EmailCheckCallback {
        void onCallback(boolean exists);
        void onError(Exception e);
    }

    // Interface callback để xử lý kết quả cập nhật người dùng
    public interface UserInfoUpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface UserPasswordUpdateCallback {
        void onSuccess(); // Phương thức được gọi khi cập nhật mật khẩu thành công
        void onError(Exception e); // Phương thức được gọi khi xảy ra lỗi trong quá trình cập nhật mật khẩu
    }

    // Interface callback để xử lý kết quả lấy tất cả các khách sạn
    public interface HotelListCallback {
        void onCallback(List<Hotel> hotels);
        void onError(Exception e);
    }

    public interface HotelAddCallback {
        void onSuccess(String hotelId);
        void onError(Exception e);
    }

    // Interface callback để xử lý kết quả cập nhật thông tin khách sạn
    public interface HotelUpdateCallback {
        void onSuccess(); // Phương thức được gọi khi cập nhật thành công
        void onError(Exception e); // Phương thức được gọi khi xảy ra lỗi trong quá trình cập nhật
    }

    public interface HotelRemoveCallback {
        void onSuccess(); // Phương thức được gọi khi xoá thành công
        void onError(Exception e); // Phương thức được gọi khi xảy ra lỗi trong quá trình xoá
    }

    public interface SavedAddCallback {
        void onSuccess(String savedId);
        void onError(Exception e);
        void onAlreadyExists();
    }

    public interface SavedHotelsCallback {
        void onSuccess(List<Hotel> hotels);
        void onError(Exception e);
    }

    public interface HotelSavedCheckCallback {
        void onCheckCompleted(boolean isSaved);
        void onError(Exception e);
    }

    public interface SavedRemoveCallback {
        void onSuccess();
        void onError(Exception e);
    }
}

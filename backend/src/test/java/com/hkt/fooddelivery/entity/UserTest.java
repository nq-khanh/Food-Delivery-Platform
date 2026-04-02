package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.TokenType;
import com.hkt.fooddelivery.exception.BusinessException;
import com.hkt.fooddelivery.exception.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UserTest {

    private User user;
    private Point mockPoint;

    @BeforeEach
    void setUp() {
        mockPoint = mock(Point.class);
        user = new User("tungnth", "tung@example.com", "0912345678", "Tung", "Nguyen", "hashed_pwd");
    }

    @Test
    @DisplayName("Nên tự động đặt địa chỉ đầu tiên làm mặc định dù truyền vào là false")
    void firstAddress_ShouldAlwaysBeDefault() {
        user.addAddress("Address 1", mockPoint, false);

        assertTrue(user.getAddresses().get(0).isDefault());
    }

    @Test
    @DisplayName("Chỉ duy nhất một địa chỉ được phép làm mặc định tại một thời điểm")
    void defaultAddress_ShouldBeUnique() {
        user.addAddress("Address 1", mockPoint, true);
        user.addAddress("Address 2", mockPoint, true);

        List<UserAddress> addresses = user.getAddresses();
        assertEquals(2, addresses.size());
        assertFalse(addresses.get(0).isDefault(), "Địa chỉ cũ phải bị hủy mặc định");
        assertTrue(addresses.get(1).isDefault(), "Địa chỉ mới phải là mặc định");

        long count = addresses.stream().filter(UserAddress::isDefault).count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Nên ném lỗi nếu thêm quá số lượng 5 địa chỉ cho phép")
    void addAddress_ShouldThrowException_WhenExceedLimit() {
        for(int i = 0; i < 5; i++) {
            user.addAddress("Addr " + i, mockPoint, false);
        }

        assertThrows(BusinessException.class, () ->
                user.addAddress("Addr 6", mockPoint, false)
        );
    }

    @Test
    @DisplayName("Danh sách con trả về từ User phải là Read-only (Immutable)")
    void getters_ShouldReturnImmutableLists() {
        user.addAddress("Addr 1", mockPoint, true);

        assertThrows(TokenException.class, () -> user.getAddresses().clear());
        assertThrows(TokenException.class, () -> user.getTokens().clear());
    }

    @Test
    @DisplayName("Nên vô hiệu hóa (revoke) toàn bộ token của User thành công")
    void revokeAllTokens_ShouldMarkAllAsRevoked() {
        Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);
        user.addToken("token1", TokenType.REFRESH, expiry);
        user.addToken("token2", TokenType.RESET_PASSWORD, expiry);

        user.revokeAllTokens();

        assertTrue(user.getTokens().stream().allMatch(UserToken::isRevoked));
    }

    @Test
    @DisplayName("Email và Username phải được chuẩn hóa (Lowercase & Trim)")
    void userCreation_ShouldNormalizeData() {
        User user2 = new User(" TUNGnth ", " TUNG@gmail.com ", "0123", "F", "L", "pwd");
        assertEquals("tungnth", user2.getUsername());
        assertEquals("tung@gmail.com", user2.getEmail());
    }

    @Test
    @DisplayName("Không được verify user nếu tài khoản đang bị khóa (Inactive)")
    void verify_ShouldFail_WhenUserIsInactive() {
        user.deactivate();
        assertThrows(BusinessException.class, () -> user.verify());
    }
}
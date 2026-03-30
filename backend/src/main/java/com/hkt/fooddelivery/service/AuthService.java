package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.config.FrontendConfig;
import com.hkt.fooddelivery.dto.AuthResponse;
import com.hkt.fooddelivery.dto.LocationDTO;
import com.hkt.fooddelivery.dto.LoginRequest;
import com.hkt.fooddelivery.dto.RegisterRequest;
import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.Shipper;
import com.hkt.fooddelivery.entity.User;
import com.hkt.fooddelivery.entity.Wallet;
import com.hkt.fooddelivery.entity.enums.DayOfWeek;
import com.hkt.fooddelivery.entity.enums.Role;
import com.hkt.fooddelivery.entity.enums.TokenType;
import com.hkt.fooddelivery.exception.BusinessException;
import com.hkt.fooddelivery.exception.DuplicateResourceException;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.exception.TokenException;
import com.hkt.fooddelivery.repository.RestaurantRepository;
import com.hkt.fooddelivery.repository.ShipperRepository;
import com.hkt.fooddelivery.repository.UserRepository;
import com.hkt.fooddelivery.repository.WalletRepository;
import com.hkt.fooddelivery.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RestaurantRepository restaurantRepository;
    private final ShipperRepository shipperRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingService embeddingService;
    private final TokenCacheService tokenCacheService;
    private final FrontendConfig frontendConfig;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public void sendRegistrationLink(String email, String origin) {
        if(userRepository.existsByEmail(email))
            throw new DuplicateResourceException("Email đã được đăng ký");

        String token = tokenCacheService.createToken(email);

        String baseUrl = frontendConfig.getUrls().getOrDefault(origin.toLowerCase(), frontendConfig.getUrls().get("user"));
        String verifyLink = baseUrl + "/register/details?token=" + token;

        Map<String, Object> props = new HashMap<>();
        props.put("name", "Bạn");
        props.put("message", "Cảm ơn bạn đã lựa chọn HKT. Vui lòng xác thực email để hoàn tất đăng ký.");
        props.put("actionLink", verifyLink);
        props.put("actionText", "Xác nhận đăng ký");

        emailService.sendHtmlEmail(email, "Xác nhận đăng ký tài khoản", "email-template", props);
    }

    @Transactional
    public AuthResponse completeRegistration(RegisterRequest req) {
        String verifiedEmail = tokenCacheService.getEmailByToken(req.token());
        if (verifiedEmail == null) throw new BusinessException("Link xác nhận đã hết hạn hoặc không hợp lệ");

        if (userRepository.existsByUsername(req.username()))
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");

        if (userRepository.existsByPhone(req.phone()))
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng");

        if (userRepository.existsByEmail(verifiedEmail))
            throw new DuplicateResourceException("Email đã tồn tại");

        User user = new User(req.username(), verifiedEmail, req.phone(),
                req.firstName(), req.lastName(),
                passwordEncoder.encode(req.password()));
        user.changeRole(req.role());
        user.verify();

        User savedUser = userRepository.save(user);

        if (req.role() == Role.MERCHANT) {
            Wallet wallet = new Wallet(savedUser);
            walletRepository.save(wallet);

            Point geoPoint = convertToPoint(req.location());
            List<Double> vector = embeddingService.getVector(req.businessName());


            Restaurant res = new Restaurant(savedUser, req.businessName(), req.address(), geoPoint);

            for (DayOfWeek day : DayOfWeek.values()) {
                res.addOperatingHour(day, LocalTime.of(8, 0), LocalTime.of(22, 0));
            }

            res.setAIEmbedding(convertToFloatArray(vector));
            restaurantRepository.save(res);
        }
        else if (req.role() == Role.SHIPPER) {
            walletRepository.save(new Wallet(savedUser));
            shipperRepository.save(new Shipper(savedUser, req.licensePlate()));
        }
        else {
            user = userRepository.save(user);
        }

        tokenCacheService.deleteToken(req.token());

        return generateAuthTokens(user);
    }

    public void sendResetPasswordLink(String email, String origin) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại"));

        String token = tokenCacheService.createToken(email);

        String baseUrl = frontendConfig.getUrls().getOrDefault(origin.toLowerCase(), frontendConfig.getUrls().get("user"));
        String resetLink = baseUrl + "/reset-password?token=" + token;

        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getFirstName());
        props.put("message", "Nhấn vào nút dưới đây để đặt lại mật khẩu mới cho tài khoản của bạn.");
        props.put("actionLink", resetLink);
        props.put("actionText", "Đặt lại mật khẩu");

        emailService.sendHtmlEmail(email, "Khôi phục mật khẩu", "email-template", props);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = tokenCacheService.getEmailByToken(token);
        if (email == null) throw new BusinessException("Link khôi phục đã hết hạn hoặc không hợp lệ");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không còn tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.revokeAllTokens();
        userRepository.save(user);

        tokenCacheService.deleteToken(token);
    }


    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.usernameOrEmail())
                .or(() -> userRepository.findByEmail(req.usernameOrEmail()))
                .orElseThrow(() -> new BusinessException("Thông tin đăng nhập không chính xác"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash()))
            throw new BusinessException("Thông tin đăng nhập không chính xác");

        if (!user.isActive())
            throw new BusinessException("Tài khoản của bạn đã bị khóa");

        return generateAuthTokens(user);
    }

    public AuthResponse refreshToken(String rawRefreshToken) {
        String cleanToken = rawRefreshToken.replace("\"", "").trim();

        String username;
        try {
            username = jwtService.extractUsername(cleanToken);
        } catch (Exception e) {
            throw new TokenException("Token không đúng định dạng");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        boolean isValid = user.getTokens().stream()
                .anyMatch(t -> t.getTokenHash().equals(cleanToken) && t.isActive() && t.getType() == TokenType.REFRESH);

        if (!isValid) {
            user.revokeAllTokens();
            userRepository.save(user);
            throw new TokenException("Phiên làm việc đã hết hạn");
        }

        user.getTokens().removeIf(t -> t.getTokenHash().equals(cleanToken));

        return generateAuthTokens(user);
    }

    private Point convertToPoint(LocationDTO dto) {
        if (dto == null) return null;
        return geometryFactory.createPoint(new Coordinate(dto.lng(), dto.lat()));
    }

    private AuthResponse generateAuthTokens(User user) {
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        user.addToken(refresh, TokenType.REFRESH, Instant.now().plusMillis(jwtService.getRefreshExpirationMs()));
        userRepository.save(user);

        return new AuthResponse(access, refresh);
    }

    private float[] convertToFloatArray(List<Double> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).floatValue();
        return arr;
    }
}
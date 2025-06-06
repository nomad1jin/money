package Baeksa.money.global.redis.service;

import Baeksa.money.global.excepction.CustomException;
import Baeksa.money.global.excepction.code.ErrorCode;
import Baeksa.money.global.jwt.JWTUtil;
import Baeksa.money.global.redis.dtos.RedisDto;
import Baeksa.money.global.redis.RefreshToken;
import Baeksa.money.global.redis.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisConnectionFactory redisConnectionFactory;

    @Transactional
    public void save(String studentId, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(studentId, refreshToken));
    }

    @Transactional
    public Optional<RefreshToken> find(String studentId) {
        return refreshTokenRepository.findById(studentId);
    }

    @Transactional
    public void logout(String studentId) {
        if (studentId != null) {
            refreshTokenRepository.deleteById(studentId);
        } else {
            throw new CustomException(ErrorCode.INVALID_ID);
        }
    }

    //블랙리스트 등록
    public void blacklist(String accessToken, Long expirationMillis) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            String key = "blacklist:" + accessToken;
            connection.setEx(key.getBytes(), expirationMillis / 1000, "blacklisted".getBytes());
        } finally {
            connection.close();
        }
    }

    //블랙리스트 검사
    public boolean isBlacklisted(String accessToken){
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            String key = "blacklist:" + accessToken;
            return connection.get(key.getBytes()) != null;
        } finally {
            connection.close();
        }
    }


    public RedisDto.TokenResponse refreshValid(RedisDto.Refresh refresh) {

        //2. 만료 여부 체크
        try {
            jwtUtil.isExpired(refresh.getRefresh_token());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        //4. redis 토큰과 비교
        Optional<RefreshToken> redisTokenOpt = refreshTokenRepository.findById(jwtUtil.getStudentId(refresh.getRefresh_token()));
        if (redisTokenOpt.isEmpty() || !redisTokenOpt.get().getRefreshToken().equals(refresh.getRefresh_token())){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String username = jwtUtil.getUsername(refresh.getRefresh_token());
        String studentId = jwtUtil.getStudentId(refresh.getRefresh_token());
        String role = jwtUtil.getRole(refresh.getRefresh_token());
        String status = jwtUtil.getStatus(refresh.getRefresh_token());

        return new RedisDto.TokenResponse(studentId, role, username, status);
    }

    public void reissue(HttpServletResponse response, RedisDto.TokenResponse tokenResponse) {
        // 5. 기존 refresh삭제
        refreshTokenRepository.deleteById(tokenResponse.getStudentId());

//        //캐싱 삭제
//        userCacheService.evict(tokenResponse.getStudentId());
//        userCacheService.getUserDetails(tokenResponse.getStudentId());  // 삭제 후 재캐싱

        // 6. 재발급
        String newAccess = jwtUtil.createJwt("access_token", tokenResponse.getStudentId(), tokenResponse.getUsername(),
                tokenResponse.getRole(), tokenResponse.getStatus(), 3600000L);
        String newRefresh = jwtUtil.createJwt("refresh_token", tokenResponse.getStudentId(), tokenResponse.getUsername(),
                tokenResponse.getRole(), tokenResponse.getStatus(), 86400000L);

        // 7. Redis 저장 (rotate)
        refreshTokenRepository.save(new RefreshToken(tokenResponse.getStudentId(), newRefresh));

        // 8. 쿠키로 응답
        response.addCookie(createCookie("access_token", newAccess));
        response.addCookie(createCookie("refresh_token", newRefresh));

        log.info("토큰 재발행 완료");
    }

    public Cookie createCookie(String key, String value) {
        key = key.equals("access_token") ? "access_token" : "refresh_token";
        int expiry = key.equals("access_token") ? (60 * 60) : (60 * 60 * 24);

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(expiry);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    //쿠키삭제 - AuthController logout에서 사용
    public void deleteCookie(HttpServletResponse response, Cookie cookie) {
        cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (삭제)
        cookie.setPath("/");  // 경로를 루트로 설정
        response.addCookie(cookie);
    }

    //LoginFilter에서 사용함
    public String getToken(String studentId) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String key = "token:" + studentId; // redis String을 쓰라고 하네요
        byte[] bytes = connection.hGet(key.getBytes(), "refreshToken".getBytes());  //byte타입만 있네요

        return new String(bytes);
    }

    public Long getTtl(String studentId) {
        RedisConnection connection = redisConnectionFactory.getConnection();
        String key = "token:" + studentId; // redis String을 쓰라고 하네요
        return connection.ttl(key.getBytes()); // 초 단위 TTL 반환
    }

    public boolean existsRefresh(String studentId) {
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findById(studentId);
        if (tokenOptional.isEmpty()) {
            return false;
        }
        Long ttl = getTtl(studentId);
        return ttl != null && ttl > 300;    //5분이상 남았을때
    }
}




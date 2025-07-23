package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.domain.model.coupon.CouponUserMapper;
import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.cashmallow.api.infrastructure.fcm.FcmEventCode.COUPON_ISSUE;
import static com.cashmallow.api.infrastructure.fcm.FcmEventValue.CF;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponUserServiceImpl implements CouponUserService {

    private final NotificationService notificationService;
    private final CouponUserMapper couponUserMapper;

    @Override
    public List<CouponUser> getUserListCouponByUserIdsAndCouponId(List<Long> users, Long id) {
        return couponUserMapper.getUserListCouponByUserIdsAndCouponId(users, id);
    }

    @Override
    public List<CouponIssueUser> getUserCouponListByUserIdAndLikeCouponCode(Long userId, String couponCode) {
        return couponUserMapper.getUserCouponListByUserIdAndLikeCouponCode(userId, couponCode);
    }

    // 평생 1회만 받을 수 있는 쿠폰 조회용. 이벤트 유무에 따라 쿠폰 코드가 다를 수 있으므로 LIKE 로 조회
    @Override
    public List<Long> getUserCouponLikeCouponCode(Long userId, Long inviteUserId, String couponCode) {
        return couponUserMapper.getUserCouponLikeCouponCode(userId, inviteUserId, couponCode);
    }

    @Override
    public void sendCouponMessage(List<User> targetUsers) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            try {
                targetUsers.forEach(user ->
                        notificationService.sendFcmNotificationMsgAsync(user, COUPON_ISSUE, CF, 0L, null));

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        shutdownAndAwaitTermination(executorService);
    }

    @Override
    public void sendCouponPushMessage(List<User> targetUsers, FcmEventCode eventCode, FcmEventValue eventValue) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            try {
                targetUsers.forEach(user ->
                        notificationService.sendFcmNotificationMsgAsync(user, eventCode, eventValue, 0L, null));

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        shutdownAndAwaitTermination(executorService);
    }

    @Override
    public CouponIssueUser getCouponUserByIdAndStatus(Long couponUserId, AvailableStatus availableStatus) {
        return couponUserMapper.getCouponUserByIdAndStatus(couponUserId, availableStatus.name());
    }

    @Override
    public CouponIssueUser getCouponUserById(Long couponUserId) {
        return couponUserMapper.getCouponUserById(couponUserId);
    }

    @Override
    public CouponUser getFirstIssuedUserCouponByCouponId(Long couponId) {
        return couponUserMapper.getFirstIssuedUserCouponByCouponId(couponId);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

package com.cashmallow.api.domain.model.user;

public interface RefreshTokenMapper {

    // Read access_token

    /**
     * Get RefreshToken by token
     *
     * @param token
     * @return
     */
    RefreshToken getRefreshTokenByToken(String token);

    // Write access_token

    /**
     * Insert RefreshToken
     *
     * @param refreshToken
     * @return
     */
    int insertRefreshToken(RefreshToken refreshToken);

    /**
     * Delete RefreshToken by userId
     *
     * @param userId
     * @return
     */
    int deleteRefreshTokenByUserId(Long userId);

    /**
     * get RefreshToken by userId
     *
     * @param userid
     * @return
     */
    RefreshToken getRefreshTokenByUserId(Long userid);

}

package com.cashmallow.api.domain.model.user;

public interface AccessTokenMapper {

    // Read access_token

    /**
     * Get AccessToken by token
     *
     * @param token
     * @return
     */
    AccessToken getAccessTokenByToken(String token);

    // Write access_token

    /**
     * Insert AccessToken
     *
     * @param loginToken
     * @return
     */
    int insertAccessToken(AccessToken loginToken);

    /**
     * Delete AccessToken
     *
     * @param login
     * @return
     */
    int deleteAccessToken(String login);

}

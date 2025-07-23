package com.cashmallow.api.domain.model.user;

public interface EasyLoginMapper {

    /**
     * @param easyLoginParams
     * @return EasyLogin
     */
    int insertEasyLogin(EasyLogin easyLoginParams);

    /**
     * Get EasyLoginInfo by token.
     *
     * @param token
     * @return
     */
    EasyLogin getEasyLoginByToken(String token);


    /**
     * @param userId
     * @return EasyLogin
     */
    EasyLogin getEasyLoginByUserId(Long userId);

    /**
     * @param easyLoginParams
     * @return
     */
    int updateEasyLogin(EasyLogin easyLoginParams);

    /**
     * @param id
     * @return
     */
    int deleteEasyLogin(Long id);

    int deleteEasyLoginByUserId(Long id);

}

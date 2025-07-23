package com.cashmallow.api.infrastructure;

import java.util.concurrent.TimeUnit;


/**
 * Java Map과 유사하게 레디스템플릿을 렙핑
 * 하나의 거대한 Map으로 생각
 */
public interface RedisService {

    /**
     * walletId를 구분자로 사용
     */
    String REDIS_KEY_TRAVELER_WALLET = "TRAVELER_WALLET_";

    /**
     * travelerId를 구분자로 사용
     */
    String REDIS_KEY_HYPHEN = "HYPHEN_";

    /**
     * travelerId를 구분자로 사용
     */
    String REDIS_KEY_OPENBANK = "OPENBANK_";

    /**
     * userId를 구분자로 사용
     */
    String REDIS_KEY_SOCASH = "socashToken";

    /**
     * userId를 구분자로 사용
     */
    String REDIS_KEY_PINCODE = "PINCODE_";

    /**
     * userId를 구분자로 사용
     */
    String REDIS_KEY_PASSWORD_MATCH = "PASSWORD_MATCH_";

    String REDIS_KEY_MALLOWLINK_TRANSACTION = "MALLOWLINK_TRANSACTION";
    final String REDIS_KEY_COMPLYADVANTAGE_TOKEN = "COMPLYADVANTAGE_TOKEN";


    /**
     * key에 value 할당, 지정한 minutes 분 뒤에 삭제됨.
     *
     * @param key
     * @param value
     * @param minutes
     */
    void put(String key, String value, long minutes);

    /**
     * key에 value 할당, 지정한 시간 뒤에 삭제됨.
     *
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    void put(String key, String value, long time, TimeUnit timeUnit);

    /**
     * 저장 목적과 구분자를 지정해서 레디스 저장.
     *
     * @param type     사용처, 용도, RedisService에 상수로 지정.
     * @param key      구분자, userId, travelerId
     * @param value
     * @param time
     * @param timeUnit
     */
    void put(String type, String key, String value, long time, TimeUnit timeUnit);

    /**
     * 레디스에 저장된 값과 일회성 비교, 비교 후 true, false와 무관하게 내용은 삭제됨.
     *
     * @param type
     * @param key
     * @param value
     * @return 일치하면 True
     */
    boolean isMatch(String type, String key, String value);

    /**
     * key에 value 할당, 지울때까지 삭제되지 않음.
     *
     * @param key
     * @param value
     */
    void put(String key, String value);

    String get(String key);

    boolean contains(String key);

    Boolean remove(String key);

    String generateRedisKey(String object, String id, String property);

    Long increaseAndGetCount(String key, Long value);

    /**
     * key의 유지시간을 변경.
     *
     * @param key
     * @param seconds
     */
    void setTimeout(String key, int seconds);

    /**
     * 키가 있는지 체크하고 없으면 넣음.
     *
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     * @return 키에 값이 비어서 넣기 성공하면 True, 값이 있어서 넣기 실패하면 False.
     */
    boolean putIfAbsent(String key, String value, long time, TimeUnit timeUnit);
}
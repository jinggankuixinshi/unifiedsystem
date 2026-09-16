package com.unified.common.constant;

public class CacheConstants {

    public static final String TOKEN_PREFIX = "auth:token:";
    public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";
    public static final String USER_INFO = "user:info:";
    public static final String DICT_DATA = "dict:data:";
    public static final String AQL_CONFIG = "production:aql:config";
    public static final String IDEMPOTENT_KEY = "idempotent:";
    public static final String SUBMIT_TOKEN = "submit:token:";
    public static final String DISTRIBUTED_LOCK = "lock:";

    public static final int TOKEN_EXPIRE_HOURS = 2;
    public static final int CACHE_EXPIRE_MINUTES = 30;
    public static final int CACHE_EXPIRE_SHORT_MINUTES = 15;
    public static final int NULL_VALUE_EXPIRE_SECONDS = 60;
    public static final int RANDOM_OFFSET_PERCENT = 10;

    private CacheConstants() {}
}

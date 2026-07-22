package com.bleep.learnhub.constants;

public final class CookieConstants {

    private CookieConstants() {}

    /** Cookie name that holds the Redis session key after login. */
    public static final String SESSION_ID = "session_id";

    /** Max-age for the session cookie: 7 days in seconds. */
    public static final long SESSION_AGE = 604800L;

    /** Cookie name that holds the Redis OTP-session key during password setup/reset. */
    public static final String OTP_SESSION = "otp_session";

    /** Max-age for the OTP session cookie: 30 minutes in seconds. */
    public static final long OTP_SESSION_AGE = 1800L;

}
package org.server;

import io.netty.util.AttributeKey;

public class UserAttributes {
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");
    public static final AttributeKey<Boolean> AUTHENTICATED = AttributeKey.valueOf("authenticated");
}

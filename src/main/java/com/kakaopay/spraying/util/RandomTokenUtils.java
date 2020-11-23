package com.kakaopay.spraying.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RandomTokenUtils {
    private final static SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String turn() {
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int index = SECURE_RANDOM.nextInt(3);
            tokenBuilder.append(turn(index));
        }

        return tokenBuilder.toString();
    }

    private static Object turn(int index) {
        switch (index) {
            case 0:
                // a-z
                return (char) (SECURE_RANDOM.nextInt(26) + 97);
            case 1:
                // A-Z
                return (char) (SECURE_RANDOM.nextInt(26) + 65);
            default:
                // 0-9
                return SECURE_RANDOM.nextInt(10);
        }
    }
}

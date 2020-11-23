package com.kakaopay.spraying.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelMapperUtils {
    private static final ModelMapper modelMapper;
    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMethodAccessLevel(Configuration.AccessLevel.PROTECTED)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public static <T, D> D map(T object, Class<D> destinationType) {
        D destination = modelMapper.map(object, destinationType);
        modelMapper.validate();
        return destination;
    }
}

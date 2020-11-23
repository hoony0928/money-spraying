package com.kakaopay.spraying.work;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkEvent<T> extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final Object source;
    private final T what;

    private WorkEvent(Object source, T what) {
        super(source);
        this.source = source;
        this.what = what;
    }

    public static <T> WorkEvent<T> of(Object source, T what) {
        return new WorkEvent<>(source, what);
    }
}
package com.kakaopay.spraying.work;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkEventPublisher<T> {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(WorkEvent<T> workEvent){
        applicationEventPublisher.publishEvent(workEvent);
    }
}
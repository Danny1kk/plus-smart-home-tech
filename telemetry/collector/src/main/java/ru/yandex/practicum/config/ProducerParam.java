package ru.yandex.practicum.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ProducerParam {
    private final String topic;
    private final Integer partition;
    private final Long timestamp;
    private final String key;
    private final byte[] value;
    private final String eventClass;
    private final String eventType;

    public boolean isValid() {
        return topic != null && timestamp != null && key != null && value != null;
    }
}
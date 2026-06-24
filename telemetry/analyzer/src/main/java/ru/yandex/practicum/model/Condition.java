//package ru.yandex.practicum.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
//import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
//
//@Embeddable
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Condition {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    @Column(name = "id")
//    Long id;
//
////    @Column(name = "type")
//    @Enumerated(EnumType.STRING)
//    ConditionTypeAvro type;
//
////    @Column(name = "operation")
//    @Enumerated(EnumType.STRING)
//    ConditionOperationAvro operation;
//
////    @Column(name = "value")
//    Integer value;
//}

package ru.yandex.practicum.model;

import jakarta.persistence.*;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Embeddable
public class Condition {

    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    private ConditionOperationAvro operation;

    private Integer value;

    public Condition() {}

    public Condition(Long id, ConditionTypeAvro type, ConditionOperationAvro operation, Integer value) {
        this.id = id;
        this.type = type;
        this.operation = operation;
        this.value = value;
    }

    public static ConditionBuilder builder() {
        return new ConditionBuilder();
    }

    public static class ConditionBuilder {
        private Long id;
        private ConditionTypeAvro type;
        private ConditionOperationAvro operation;
        private Integer value;

        public ConditionBuilder id(Long id) { this.id = id; return this; }
        public ConditionBuilder type(ConditionTypeAvro type) { this.type = type; return this; }
        public ConditionBuilder operation(ConditionOperationAvro operation) { this.operation = operation; return this; }
        public ConditionBuilder value(Integer value) { this.value = value; return this; }

        public Condition build() {
            return new Condition(id, type, operation, value);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ConditionTypeAvro getType() { return type; }
    public void setType(ConditionTypeAvro type) { this.type = type; }
    public ConditionOperationAvro getOperation() { return operation; }
    public void setOperation(ConditionOperationAvro operation) { this.operation = operation; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
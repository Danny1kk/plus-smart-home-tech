//package ru.yandex.practicum.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
//
//@Embeddable
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Action {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    @Column(name = "id")
//    Long id;
//
//    //@Column(name = "type", columnDefinition = "VARCHAR(255)")
//    @Enumerated(EnumType.STRING)
//    ActionTypeAvro type;
//
//    //@Column(name = "action_value")
//    Integer value;
//}


package ru.yandex.practicum.model;

import jakarta.persistence.*;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Embeddable
public class Action {

    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionTypeAvro type;

    private Integer value;

    public Action() {}

    public Action(Long id, ActionTypeAvro type, Integer value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public static ActionBuilder builder() {
        return new ActionBuilder();
    }

    public static class ActionBuilder {
        private Long id;
        private ActionTypeAvro type;
        private Integer value;

        public ActionBuilder id(Long id) { this.id = id; return this; }
        public ActionBuilder type(ActionTypeAvro type) { this.type = type; return this; }
        public ActionBuilder value(Integer value) { this.value = value; return this; }

        public Action build() {
            return new Action(id, type, value);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ActionTypeAvro getType() { return type; }
    public void setType(ActionTypeAvro type) { this.type = type; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
}
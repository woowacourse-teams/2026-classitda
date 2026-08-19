package com.classitda.classes.application.student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentBookingStatusResolver {

    private final StudentBookingDecisionPolicy decisionPolicy;

    public StudentBookingStatus resolve(StudentBookingContext context) {
        return decisionPolicy.decide(context).legacyStatus();
    }
}

package com.classitda.classes.application.student;

import com.classitda.classes.domain.ClassSessionStatus;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingStatusResolver {

    public StudentBookingStatus resolve(StudentBookingContext context) {
        for (BookingStatusRule rule : BookingStatusRule.values()) {
            if (rule.matches(context)) {
                return rule.status;
            }
        }
        throw new IllegalStateException("회원 수업 예약 상태를 결정할 수 없습니다.");
    }

    private enum BookingStatusRule {
        CANCELED(StudentBookingStatus.CANCELED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.sessionStatus() == ClassSessionStatus.CANCELED;
            }
        },
        COMPLETED(StudentBookingStatus.COMPLETED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return !context.now().isBefore(context.endAt());
            }
        },
        RESERVED(StudentBookingStatus.RESERVED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownReservedCount() > 0;
            }
        },
        OFFERED(StudentBookingStatus.OFFERED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownOfferedCount() > 0;
            }
        },
        WAITING(StudentBookingStatus.WAITING) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownWaitingCount() > 0;
            }
        },
        CLOSED(StudentBookingStatus.CLOSED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.sessionStatus() == ClassSessionStatus.CLOSED
                        || !context.now().isBefore(
                                context.startAt().minusMinutes(context.reservationCloseMinutesBefore())
                        );
            }
        },
        AVAILABLE(StudentBookingStatus.AVAILABLE) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.remainingCapacity() > 0;
            }
        },
        WAITING_AVAILABLE(StudentBookingStatus.WAITING_AVAILABLE) {
            @Override
            boolean matches(StudentBookingContext context) {
                return true;
            }
        };

        private final StudentBookingStatus status;

        BookingStatusRule(StudentBookingStatus status) {
            this.status = status;
        }

        abstract boolean matches(StudentBookingContext context);
    }
}

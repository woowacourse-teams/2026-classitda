package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
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
        ABSENT(StudentBookingStatus.ABSENT) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.reservation().ownAbsentCount() > 0;
            }
        },
        ATTENDED(StudentBookingStatus.ATTENDED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.reservation().ownAttendedCount() > 0;
            }
        },
        DEFAULT_ATTENDED(StudentBookingStatus.ATTENDED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.reservation().ownReservedCount() > 0
                        && !context.now().isBefore(context.startAt());
            }
        },
        RESERVED(StudentBookingStatus.RESERVED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.reservation().ownReservedCount() > 0;
            }
        },
        OFFERED(StudentBookingStatus.OFFERED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.waiting().ownOfferedCount() > 0;
            }
        },
        WAITING(StudentBookingStatus.WAITING) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.waiting().ownWaitingCount() > 0;
            }
        },
        CLOSED(StudentBookingStatus.CLOSED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.bookingWindow() == BookingWindow.CLOSED;
            }
        },
        // TODO(#46, #68): 수강권 홀딩과 이용 가능 횟수 구현 후,
        // 호환 수강권 중 실제로 사용할 수 있는 수강권이 없으면 UNAVAILABLE을 우선 판정한다.
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

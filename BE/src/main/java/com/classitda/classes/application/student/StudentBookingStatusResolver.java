package com.classitda.classes.application.student;

import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.presentation.dto.MemberClassSessionBookingStatus;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingStatusResolver {

    public MemberClassSessionBookingStatus resolve(StudentBookingContext context) {
        for (BookingStatusRule rule : BookingStatusRule.values()) {
            if (rule.matches(context)) {
                return rule.status;
            }
        }
        throw new IllegalStateException("회원 수업 예약 상태를 결정할 수 없습니다.");
    }

    private enum BookingStatusRule {
        CANCELED(MemberClassSessionBookingStatus.CANCELED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.sessionStatus() == ClassSessionStatus.CANCELED;
            }
        },
        COMPLETED(MemberClassSessionBookingStatus.COMPLETED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return !context.now().isBefore(context.endAt());
            }
        },
        RESERVED(MemberClassSessionBookingStatus.RESERVED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownReservedCount() > 0;
            }
        },
        OFFERED(MemberClassSessionBookingStatus.OFFERED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownOfferedCount() > 0;
            }
        },
        WAITING(MemberClassSessionBookingStatus.WAITING) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.ownWaitingCount() > 0;
            }
        },
        CLOSED(MemberClassSessionBookingStatus.CLOSED) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.sessionStatus() == ClassSessionStatus.CLOSED
                        || !context.now().isBefore(
                                context.startAt().minusMinutes(context.reservationCloseMinutesBefore())
                        );
            }
        },
        AVAILABLE(MemberClassSessionBookingStatus.AVAILABLE) {
            @Override
            boolean matches(StudentBookingContext context) {
                return context.remainingCapacity() > 0;
            }
        },
        WAITING_AVAILABLE(MemberClassSessionBookingStatus.WAITING_AVAILABLE) {
            @Override
            boolean matches(StudentBookingContext context) {
                return true;
            }
        };

        private final MemberClassSessionBookingStatus status;

        BookingStatusRule(MemberClassSessionBookingStatus status) {
            this.status = status;
        }

        abstract boolean matches(StudentBookingContext context);
    }
}

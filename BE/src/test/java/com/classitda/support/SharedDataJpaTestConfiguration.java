package com.classitda.support;

import com.classitda.authentication.application.SignupAccountCreator;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.ClassTemplateCommandService;
import com.classitda.classes.application.ClassTemplateQueryService;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.instructor.daily.InstructorScheduleReader;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionQueryService;
import com.classitda.classes.application.student.StudentBookingDecisionPolicy;
import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.calendar.StudentCalendarSummaryReader;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.application.student.daily.StudentDailyScheduleReader;
import com.classitda.classes.application.student.daily.StudentDailySessionAssembler;
import com.classitda.classes.application.student.detail.StudentSessionDetailQueryService;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.common.config.TimeConfig;
import com.classitda.member.application.MemberCleanupService;
import com.classitda.member.application.MemberService;
import com.classitda.member.application.TermService;
import com.classitda.passproduct.application.PassProductService;
import com.classitda.studio.application.RoomService;
import com.classitda.studio.application.StudioMembershipService;
import com.classitda.studio.application.StudioMembershipTerminationService;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.application.StudioPolicyService;
import com.classitda.studio.application.StudioService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        SignupAccountCreator.class,
        ClassSessionCommandService.class,
        ClassTemplateCommandService.class,
        ClassTemplateQueryService.class,
        ClassTypeService.class,
        InstructorSessionAccessReader.class,
        InstructorCalendarQueryService.class,
        InstructorDailyQueryService.class,
        InstructorScheduleReader.class,
        ClassSessionInstructorEnrollmentCommandService.class,
        InstructorSessionQueryService.class,
        StudentBookingDecisionPolicy.class,
        StudentSessionAccessReader.class,
        StudentCalendarQueryService.class,
        StudentCalendarSummaryReader.class,
        StudentDailyQueryService.class,
        StudentDailyScheduleReader.class,
        StudentDailySessionAssembler.class,
        StudentSessionDetailQueryService.class,
        StudentEnrollmentDetailQueryService.class,
        StudentOwnedPassesReader.class,
        MemberCleanupService.class,
        MemberService.class,
        TermService.class,
        PassProductService.class,
        RoomService.class,
        StudioMembershipService.class,
        StudioMembershipTerminationService.class,
        StudioPermissionService.class,
        StudioPolicyService.class,
        StudioService.class,
        TimeConfig.class
})
@TestConfiguration(proxyBeanMethods = false)
public class SharedDataJpaTestConfiguration {
}

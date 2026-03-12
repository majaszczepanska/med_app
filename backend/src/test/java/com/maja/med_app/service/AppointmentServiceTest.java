package com.maja.med_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.maja.med_app.exception.AppValidationException;
import com.maja.med_app.model.AppUser;
import com.maja.med_app.model.Appointment;
import com.maja.med_app.model.Doctor;
import com.maja.med_app.model.Patient;
import com.maja.med_app.repository.AppointmentRepository;
import com.maja.med_app.repository.DoctorRepository;
import com.maja.med_app.repository.PatientRepository;
import com.maja.med_app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;



    //TEST FOR SUCCESSFUL APPOINTMENT CREATION AND EMAIL SENDING (valid visit time, no conflicts, user logged in)
    @Test
    void shouldSuccessfullyCreateAppointmentAndSendEmail() {
        //GIVEN
        //user authentication setup
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null); // Mock the authentication as needed
        SecurityContextHolder.setContext(securityContext);

        //data setup
        LocalDateTime validVisitTime = LocalDateTime.now()
            .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
            .withHour(10)
            .withMinute(15)
            .withSecond(0)
            .withNano(0);

        //doctor setup
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("Gregory");
        doctor.setLastName("House");

        //patient setup
        AppUser appUser = new AppUser();
        appUser.setEmail("patient@test.com");

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setFirstName("John");
        patient.setUser(appUser);

        //appointment setup
        Appointment appointment = new Appointment();
        appointment.setVisitTime(validVisitTime);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        //mocking repository calls
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        
        when(appointmentRepository.existsByDoctorIdAndVisitTimeAndStatusNot(any(), any(), any())).thenReturn(false); // No conflicting appointments for doctor
        when(appointmentRepository.existsByPatientIdAndVisitTimeAndStatusNot(any(), any(), any())).thenReturn(false); // No conflicting appointments for patient
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        //WHEN
        //(Call the method you want to test here)
        Appointment result = appointmentService.createAppointment(appointment);

        //THEN
        //(Assert the expected results here)
        assertNotNull(result);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));

        verify(emailService, times(1)).sendAppointmentConfirmation(
            eq("patient@test.com"), eq("John"), eq("Gregory House"), anyString());
    }


    //TEST FOR WEEKEND APPOINTMENT
    @Test
    void shouldThrowExceptionWhenBookingOnWeekend() {
        LocalDateTime weekendVisitTime = LocalDateTime.now()
            .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
            .withHour(10)
            .withMinute(15)
            .withSecond(0)
            .withNano(0);

        //doctor setup
        Doctor doctor = new Doctor();
        doctor.setId(1L);

        //patient setup
        Patient patient = new Patient();
        patient.setId(2L);

        //appointment setup
        Appointment weekendAppointment = new Appointment();
        weekendAppointment.setVisitTime(weekendVisitTime);
        weekendAppointment.setDoctor(doctor);
        weekendAppointment.setPatient(patient);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));


        AppValidationException exception = assertThrows(AppValidationException.class, () -> {
            appointmentService.createAppointment(weekendAppointment);
        });

        assertTrue(exception.getErrors().containsKey("visitTime"));
        assertTrue(exception.getErrors().get("visitTime").contains("Clinic is closed on weekends"));

        verify(appointmentRepository, never()).save(any());
    }


    //TEST
    //RODO - MASKING OTHER PATIENTS' DATA IN APPOINTMENTS FOR PATIENT ROLE
    @Test
    void shouldMaskOtherPatientsDataWhenRoleIsPatient() {

        //GIVEN
        //logged in user setup
        AppUser loggedUser = new AppUser();
        loggedUser.setEmail("user@test.com");
        loggedUser.setRole("PATIENT");

        //logged in patient setup
        Patient me = new Patient();
        me.setId(1L);
        me.setFirstName("John");
        me.setLastName("Smith");
        me.setUser(loggedUser);

        //other patient setup
        Patient otherPatient = new Patient();
        otherPatient.setId(2L);
        otherPatient.setFirstName("Tom");
        otherPatient.setLastName("Cat");

        //logged in patient's appointment setup
        Appointment myAppointment = new Appointment();
        myAppointment.setId(100L);
        myAppointment.setPatient(me);
        myAppointment.setDescription("headache");

        //other patient's appointment setup
        Appointment otherAppointment = new Appointment();
        otherAppointment.setId(101L);
        otherAppointment.setPatient(otherPatient);
        otherAppointment.setDescription("flu");

        //mocking repository calls
        List<Appointment> allAppointments = List.of(myAppointment, otherAppointment);

        //mocking authentication and security context
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user@test.com");
        when(auth.getPrincipal()).thenReturn("user");

        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(loggedUser));
        when(patientRepository.findByUser(loggedUser)).thenReturn(Optional.of(me));
        when(appointmentRepository.findAllByStatusNot(any())).thenReturn(allAppointments);
        
        //WHEN
        List<Appointment> result = appointmentService.getAllAppointments();

        //THEN
        assertEquals(2, result.size(), "Expected 2 appointments in the result");
    
        //Verify that the logged-in patient's appointment data is intact
        Appointment resultMyAppointment = result.stream()
            .filter(app -> app.getId().equals(100L))
            .findFirst()
            .orElse(null);
        assertEquals("John", resultMyAppointment.getPatient().getFirstName());
        assertEquals("headache", resultMyAppointment.getDescription());

        //Verify that the other patient's data is masked
        Appointment resultOtherAppointment = result.stream()
            .filter(app -> app.getId().equals(101L))
            .findFirst()
            .orElse(null);
        assertEquals("Reserved", resultOtherAppointment.getPatient().getFirstName(), "Expected other patient's first name to be masked");
        assertEquals("", resultOtherAppointment.getPatient().getLastName(), "Expected other patient's last name to be masked");
        assertEquals("Private Appointment", resultOtherAppointment.getDescription(), "Expected other appointment's description to be masked");
    }
}
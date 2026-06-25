package com.company.securityapp.config;

import com.company.securityapp.entity.Certificate;
import com.company.securityapp.entity.Course;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.EnrollmentStatus;
import com.company.securityapp.entity.Lesson;
import com.company.securityapp.entity.Role;
import com.company.securityapp.entity.User;
import com.company.securityapp.repository.CertificateRepository;
import com.company.securityapp.repository.CourseRepository;
import com.company.securityapp.repository.EnrollmentRepository;
import com.company.securityapp.repository.LessonRepository;
import com.company.securityapp.repository.UserRepository;
import com.company.securityapp.service.EncryptionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserInitializer {

    @Bean
    public CommandLineRunner demoDataRunner(
            UserRepository userRepository,
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            CertificateRepository certificateRepository,
            PasswordEncoder passwordEncoder,
            EncryptionService encryptionService,
            @Value("${app.seed.enabled}") boolean seedEnabled,
            @Value("${app.seed.student-password}") String studentPassword,
            @Value("${app.seed.admin-password}") String adminPassword) {
        return arguments -> {
            if (!seedEnabled || userRepository.count() > 0) {
                return;
            }

            User student1 = createUser(
                    userRepository,
                    passwordEncoder,
                    encryptionService,
                    "Student One",
                    "student1@example.com",
                    studentPassword,
                    Role.STUDENT,
                    "0901000001",
                    "123 Student Street");
            User student2 = createUser(
                    userRepository,
                    passwordEncoder,
                    encryptionService,
                    "Student Two",
                    "student2@example.com",
                    studentPassword,
                    Role.STUDENT,
                    "0902000002",
                    "456 Student Avenue");
            User admin = createUser(
                    userRepository,
                    passwordEncoder,
                    encryptionService,
                    "Admin Demo",
                    "admin@example.com",
                    adminPassword,
                    Role.ADMIN,
                    "0904000004",
                    "999 Admin Boulevard");

            Course course1 = createCourse(
                    courseRepository,
                    admin,
                    "Java Security Basics",
                    "JWT, bcrypt, and secure REST API fundamentals.",
                    "Introductory course showing authentication, hashing, and authorization basics in Spring Boot.",
                    BigDecimal.valueOf(199000),
                    true);
            Course course2 = createCourse(
                    courseRepository,
                    admin,
                    "Applied Cryptography for Beginners",
                    "AES-GCM, HMAC-SHA256, and secure key handling for students.",
                    "Focuses on practical cryptography in application code with database encryption and webhook signing.",
                    BigDecimal.valueOf(249000),
                    true);
            Course course3 = createCourse(
                    courseRepository,
                    admin,
                    "Secure RESTful API with Spring Boot",
                    "OWASP API Top 10, audit logs, rate limiting, and HTTPS/TLS.",
                    "Security-focused API engineering course for testing with Postman and OWASP ZAP.",
                    BigDecimal.valueOf(299000),
                    true);

            createLessons(lessonRepository, course1, List.of(
                    lesson("JWT Structure and Signature Validation", "Understand JWT header, payload, and signature.", 1),
                    lesson("BCrypt Password Storage", "Why password hashing must be one-way and salted.", 2)));
            createLessons(lessonRepository, course2, List.of(
                    lesson("AES-GCM for Data at Rest", "Encrypt sensitive fields with IV and authentication tag.", 1),
                    lesson("HMAC-SHA256 for Webhooks", "Verify source authenticity and integrity of payment callbacks.", 2)));
            createLessons(lessonRepository, course3, List.of(
                    lesson("BOLA and Ownership Checks", "Server-side authorization to stop IDOR attacks.", 1),
                    lesson("HTTPS, Swagger, and ZAP", "Secure transport and API testing workflow.", 2)));

            Enrollment activeEnrollment1 = createEnrollment(
                    enrollmentRepository,
                    encryptionService,
                    student1,
                    course1,
                    EnrollmentStatus.ACTIVE,
                    "PAY-STUDENT1-COURSE1",
                    true);
            Enrollment activeEnrollment2 = createEnrollment(
                    enrollmentRepository,
                    encryptionService,
                    student2,
                    course2,
                    EnrollmentStatus.ACTIVE,
                    "PAY-STUDENT2-COURSE2",
                    true);
            createEnrollment(
                    enrollmentRepository,
                    encryptionService,
                    student1,
                    course3,
                    EnrollmentStatus.PENDING,
                    "PAY-PENDING-DEMO",
                    false);

            createCertificate(certificateRepository, encryptionService, activeEnrollment1, BigDecimal.valueOf(91.50));
            createCertificate(certificateRepository, encryptionService, activeEnrollment2, BigDecimal.valueOf(95.00));
        };
    }

    private User createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EncryptionService encryptionService,
            String fullName,
            String email,
            String rawPassword,
            Role role,
            String phoneNumber,
            String billingAddress) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhoneNumberEncrypted(encryptionService.encryptUserField(phoneNumber, email, "phoneNumber"));
        user.setBillingAddressEncrypted(encryptionService.encryptUserField(billingAddress, email, "billingAddress"));
        return userRepository.save(user);
    }

    private Course createCourse(
            CourseRepository courseRepository,
            User instructor,
            String title,
            String summary,
            String description,
            BigDecimal price,
            boolean published) {
        Course course = new Course();
        course.setInstructor(instructor);
        course.setTitle(title);
        course.setSummary(summary);
        course.setDescription(description);
        course.setPrice(price);
        course.setPublished(published);
        return courseRepository.save(course);
    }

    private void createLessons(LessonRepository lessonRepository, Course course, List<LessonSeed> lessons) {
        for (LessonSeed lessonSeed : lessons) {
            Lesson lesson = new Lesson();
            lesson.setCourse(course);
            lesson.setTitle(lessonSeed.title());
            lesson.setPreviewText(lessonSeed.previewText());
            lesson.setContent(
                    lessonSeed.title()
                            + "\n\nThis lesson contains the unlocked course content used during the security demo.");
            lesson.setSortOrder(lessonSeed.sortOrder());
            lessonRepository.save(lesson);
        }
    }

    private Enrollment createEnrollment(
            EnrollmentRepository enrollmentRepository,
            EncryptionService encryptionService,
            User student,
            Course course,
            EnrollmentStatus status,
            String paymentReference,
            boolean activated) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(status);
        enrollment.setPaymentReferenceEncrypted(encryptionService.encryptEnrollmentField(
                paymentReference,
                student.getId(),
                course.getId(),
                "paymentReference"));
        if (activated) {
            enrollment.setActivatedAt(java.time.Instant.now());
        }
        return enrollmentRepository.save(enrollment);
    }

    private Certificate createCertificate(
            CertificateRepository certificateRepository,
            EncryptionService encryptionService,
            Enrollment enrollment,
            BigDecimal score) {
        Certificate certificate = new Certificate();
        certificate.setEnrollment(enrollment);
        certificate.setStudent(enrollment.getStudent());
        certificate.setCourse(enrollment.getCourse());
        certificate.setScore(score);
        certificate.setCertificateCodeEncrypted(encryptionService.encryptCertificateField(
                "CERT-"
                        + enrollment.getCourse().getId()
                        + "-"
                        + enrollment.getStudent().getId()
                        + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                enrollment.getStudent().getId(),
                enrollment.getCourse().getId(),
                "certificateCode"));
        return certificateRepository.save(certificate);
    }

    private LessonSeed lesson(String title, String previewText, int sortOrder) {
        return new LessonSeed(title, previewText, sortOrder);
    }

    private record LessonSeed(String title, String previewText, int sortOrder) {
    }
}

package com.company.securityapp.service;

import com.company.securityapp.dto.CertificateResponse;
import com.company.securityapp.entity.Certificate;
import com.company.securityapp.entity.Enrollment;
import com.company.securityapp.entity.User;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CertificateRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CurrentUserService currentUserService;
    private final AuthorizationService authorizationService;
    private final EncryptionService encryptionService;

    public CertificateService(
            CertificateRepository certificateRepository,
            CurrentUserService currentUserService,
            AuthorizationService authorizationService,
            EncryptionService encryptionService) {
        this.certificateRepository = certificateRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.encryptionService = encryptionService;
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates() {
        User currentUser = currentUserService.getRequiredUser();
        return certificateRepository.findAllByStudentIdOrderByIssuedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificate(Long certificateId) {
        User actor = currentUserService.getRequiredUser();
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Certificate was not found."));
        authorizationService.assertCanViewCertificate(actor, certificate);
        return toResponse(certificate);
    }

    public Certificate issueCertificateForEnrollment(Enrollment enrollment, BigDecimal score) {
        return certificateRepository.findByEnrollmentId(enrollment.getId()).orElseGet(() -> {
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
        });
    }

    private CertificateResponse toResponse(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getStudent().getId(),
                certificate.getStudent().getEmail(),
                certificate.getCourse().getId(),
                certificate.getCourse().getTitle(),
                certificate.getScore(),
                encryptionService.decryptCertificateField(
                        certificate.getCertificateCodeEncrypted(),
                        certificate.getStudent().getId(),
                        certificate.getCourse().getId(),
                        "certificateCode"),
                certificate.getIssuedAt());
    }
}

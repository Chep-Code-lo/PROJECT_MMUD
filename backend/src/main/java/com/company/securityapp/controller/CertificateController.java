package com.company.securityapp.controller;

import com.company.securityapp.dto.CertificateResponse;
import com.company.securityapp.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificate API", description = "Certificate APIs with server-side ownership checks for BOLA/IDOR defense.")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/me")
    @Operation(summary = "List certificates belonging to the current user")
    public List<CertificateResponse> myCertificates() {
        return certificateService.getMyCertificates();
    }

    @GetMapping("/{certificateId}")
    @Operation(summary = "Get a certificate with ownership checks to block BOLA/IDOR")
    public CertificateResponse getCertificate(@PathVariable Long certificateId) {
        return certificateService.getCertificate(certificateId);
    }
}

package com.company.securityapp.service;

import com.company.securityapp.dto.CustomerRequest;
import com.company.securityapp.dto.CustomerResponse;
import com.company.securityapp.entity.Customer;
import com.company.securityapp.exception.ApiException;
import com.company.securityapp.repository.CustomerRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    public CustomerService(
            CustomerRepository customerRepository,
            EncryptionService encryptionService,
            AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Customer email already exists.");
        }

        Customer customer = new Customer();
        applyRequest(customer, request, normalizedEmail);
        Customer savedCustomer = customerRepository.save(customer);
        auditLogService.logBusinessAction(
                "CREATE_CUSTOMER",
                "Customer",
                savedCustomer.getId(),
                true,
                "Created customer with email " + savedCustomer.getEmail());
        return toResponse(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        String normalizedEmail = normalizeEmail(request.email());

        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Customer email already exists.");
        }

        applyRequest(customer, request, normalizedEmail);
        Customer savedCustomer = customerRepository.save(customer);
        auditLogService.logBusinessAction(
                "UPDATE_CUSTOMER",
                "Customer",
                savedCustomer.getId(),
                true,
                "Updated customer with email " + savedCustomer.getEmail());
        return toResponse(savedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
        auditLogService.logBusinessAction(
                "DELETE_CUSTOMER",
                "Customer",
                customer.getId(),
                true,
                "Deleted customer with email " + customer.getEmail());
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found."));
    }

    private void applyRequest(Customer customer, CustomerRequest request, String normalizedEmail) {
        customer.setName(normalizeText(request.name()));
        customer.setEmail(normalizedEmail);
        customer.setKeyVersion(encryptionService.getCurrentKeyVersion());
        customer.setPhoneEncrypted(encryptionService.encryptCustomerField(
                normalizeText(request.phone()),
                normalizedEmail,
                "phone"));
        customer.setAddressEncrypted(encryptionService.encryptCustomerField(
                normalizeText(request.address()),
                normalizedEmail,
                "address"));
        customer.setTaxCodeEncrypted(encryptionService.encryptCustomerField(
                normalizeText(request.taxCode()),
                normalizedEmail,
                "taxCode"));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                encryptionService.decryptCustomerField(
                        customer.getPhoneEncrypted(),
                        customer.getEmail(),
                        "phone",
                        customer.getKeyVersion()),
                encryptionService.decryptCustomerField(
                        customer.getAddressEncrypted(),
                        customer.getEmail(),
                        "address",
                        customer.getKeyVersion()),
                encryptionService.decryptCustomerField(
                        customer.getTaxCodeEncrypted(),
                        customer.getEmail(),
                        "taxCode",
                        customer.getKeyVersion()),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }

    private String normalizeEmail(String email) {
        return normalizeText(email).toLowerCase();
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}


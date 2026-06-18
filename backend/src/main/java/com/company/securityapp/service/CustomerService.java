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
        customer.setPhoneEncrypted(encryptionService.encrypt(normalizeText(request.phone())));
        customer.setAddressEncrypted(encryptionService.encrypt(normalizeText(request.address())));
        customer.setTaxCodeEncrypted(encryptionService.encrypt(normalizeText(request.taxCode())));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                encryptionService.decrypt(customer.getPhoneEncrypted()),
                encryptionService.decrypt(customer.getAddressEncrypted()),
                encryptionService.decrypt(customer.getTaxCodeEncrypted()),
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


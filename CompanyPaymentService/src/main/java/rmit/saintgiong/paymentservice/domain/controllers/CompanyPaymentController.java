package rmit.saintgiong.paymentservice.domain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateCompanyPaymentRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateCompanyPaymentResponseDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;
import rmit.saintgiong.paymentapi.internal.services.CreateCompanyPaymentInterface;
import rmit.saintgiong.paymentapi.internal.services.QueryCompanyPaymentInterface;
import rmit.saintgiong.paymentapi.internal.services.UpdateCompanyPaymentInterface;
import rmit.saintgiong.paymentapi.internal.services.DeleteCompanyPaymentInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompanyPaymentController {

    private static final Logger log = LoggerFactory.getLogger(CompanyPaymentController.class);

    private final CreateCompanyPaymentInterface createService;
    private final QueryCompanyPaymentInterface queryService;
    private final UpdateCompanyPaymentInterface updateService;
    private final DeleteCompanyPaymentInterface deleteService;

    @PostMapping
    public ResponseEntity<CreateCompanyPaymentResponseDto> createTransaction(@Valid @RequestBody CreateCompanyPaymentRequestDto req) {
        String requestId = UUID.randomUUID().toString();
        CreateCompanyPaymentResponseDto response = createService.createCompanyPayment(req);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueryCompanyPaymentResponseDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(queryService.getCompanyPayment(id));
    }

    @GetMapping
    public ResponseEntity<List<QueryCompanyPaymentResponseDto>> list() {
        return ResponseEntity.ok(queryService.listCompanyPayments());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QueryCompanyPaymentResponseDto> update(@PathVariable String id, @RequestBody CreateCompanyPaymentRequestDto req) {
        return ResponseEntity.ok(updateService.updateCompanyPayment(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteService.deleteCompanyPayment(id);
        return ResponseEntity.noContent().build();
    }
}

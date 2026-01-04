package rmit.saintgiong.paymentapi.internal.services;

import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;

import java.util.List;
import java.util.UUID;

public interface QueryCompanyPaymentInterface {
    QueryCompanyPaymentResponseDto getCompanyPayment(String id);
    List<QueryCompanyPaymentResponseDto> listCompanyPayments();
}


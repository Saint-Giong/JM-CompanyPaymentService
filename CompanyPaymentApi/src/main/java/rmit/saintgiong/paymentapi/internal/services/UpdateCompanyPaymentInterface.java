package rmit.saintgiong.paymentapi.internal.services;

import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateCompanyPaymentRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;

public interface UpdateCompanyPaymentInterface {
    QueryCompanyPaymentResponseDto updateCompanyPayment(String id, CreateCompanyPaymentRequestDto req);
}

package rmit.saintgiong.paymentapi.internal.services;

import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateCompanyPaymentRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateCompanyPaymentResponseDto;

public interface CreateCompanyPaymentInterface {
    CreateCompanyPaymentResponseDto createCompanyPayment(CreateCompanyPaymentRequestDto req);
}

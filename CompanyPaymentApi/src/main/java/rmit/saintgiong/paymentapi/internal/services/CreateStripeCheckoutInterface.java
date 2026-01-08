package rmit.saintgiong.paymentapi.internal.services;

import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateStripeCheckoutRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateStripeCheckoutResponseDto;

public interface CreateStripeCheckoutInterface {
    CreateStripeCheckoutResponseDto createStripeCheckout(CreateStripeCheckoutRequestDto req);
}

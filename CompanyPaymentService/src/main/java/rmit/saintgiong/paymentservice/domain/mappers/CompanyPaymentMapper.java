package rmit.saintgiong.paymentservice.domain.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;

@Mapper(componentModel = "spring")
public interface CompanyPaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(entity.getPurchasedAt()==null?null:entity.getPurchasedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())")
    @Mapping(target = "method", expression = "java(entity.getMethod()==null?null:rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod.valueOf(entity.getMethod().name()))")
    QueryCompanyPaymentResponseDto toQueryResponse(CompanyPaymentEntity entity);
}

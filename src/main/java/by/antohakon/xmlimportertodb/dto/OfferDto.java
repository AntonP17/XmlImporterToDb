package by.antohakon.xmlimportertodb.dto;

import lombok.Builder;

@Builder
public record OfferDto(
         String id,
         String available,
         String url,
         String price,
         String currencyId,
         String categoryId,
         String picture,
         String name,
         String vendor,
         String vendorCode,
         String description,
         String param,
         String count
) {}

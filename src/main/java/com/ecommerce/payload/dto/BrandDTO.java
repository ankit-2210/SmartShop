package com.ecommerce.payload.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BrandDTO {
    private Long id;
    private String name;
    private Long productCount;


}

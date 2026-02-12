package com.example.shippingService.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {
    private String name;  // file name ("etiquette.pdf")
    private String url;   // file URL ("https://cdn.example.com/shipments/SHIP-12345/etiquette.pdf")
}

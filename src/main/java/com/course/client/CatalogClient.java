package com.course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("/api/v1/catalogs/{id}/exists")
    Boolean exists(@PathVariable("id") String id);
}

package com.security.backend.controller;

import com.security.backend.model.Laptop;
import com.security.backend.repository.LaptopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/laptops")
@RequiredArgsConstructor
public class LaptopController {

    private final LaptopRepository repository;

    @GetMapping
    public List<Laptop> getAllLaptops() {
        return repository.findAll();
    }

    @PostMapping
    public Laptop registerLaptop(@RequestBody Laptop laptop) {
        return repository.save(laptop);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laptop> getLaptop(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

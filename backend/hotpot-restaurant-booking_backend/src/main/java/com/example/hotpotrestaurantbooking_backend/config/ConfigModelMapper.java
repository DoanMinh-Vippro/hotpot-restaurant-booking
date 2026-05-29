package com.example.hotpotrestaurantbooking_backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigModelMapper {
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper mapper = new ModelMapper();
        // Ignore ambiguous property mappings so manual nested mappings in services can still be applied.
        mapper.getConfiguration().setAmbiguityIgnored(true);
        return mapper;
    }
}

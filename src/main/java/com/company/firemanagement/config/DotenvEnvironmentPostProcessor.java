package com.company.firemanagement.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            return;
        }

        Map<String, Object> dotenvProperties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    
                    // Remove enclosing quotes if present (e.g. "value" or 'value')
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    dotenvProperties.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("DotenvEnvironmentPostProcessor - Warning: Failed to load .env file: " + e.getMessage());
        }

        if (!dotenvProperties.isEmpty()) {
            // Add custom property source at the end so OS environment variables can still override it if necessary
            environment.getPropertySources().addLast(new MapPropertySource("dotenvProperties", dotenvProperties));
            System.out.println("DotenvEnvironmentPostProcessor - Successfully loaded environment variables from local .env file");
        }
    }
}

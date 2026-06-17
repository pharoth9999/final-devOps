package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TemplateService templateService;

    private Template sample() {
        return Template.builder().id(1L).code("DEFAULT").name("Default").build();
    }

    @Test
    void listReturnsAllTemplates() throws Exception {
        when(templateService.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DEFAULT"));
    }

    @Test
    void createReturnsCreatedTemplate() throws Exception {
        Template request = Template.builder().code("CLASSIC").name("Classic").build();
        when(templateService.create(any(Template.class))).thenReturn(sample());

        mockMvc.perform(post("/api/templates")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void existsReturnsBoolean() throws Exception {
        when(templateService.existsByCode("DEFAULT")).thenReturn(true);

        mockMvc.perform(get("/api/templates/exists/DEFAULT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}

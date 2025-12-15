package com.security.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.backend.model.SecurityEvent;
import com.security.backend.service.SecurityEventService;
import com.security.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityEventService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testReceiveEvent() throws Exception {
        SecurityEvent event = new SecurityEvent();
        event.setEventType("LOGIN_FAILURE");
        event.setDeviceId("WS-001");
        event.setSeverity("CRITICAL");
        event.setUsername("alice");
        event.setAuthenticationStatus("FAILURE");

        when(service.processEvent(any(SecurityEvent.class))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }
}

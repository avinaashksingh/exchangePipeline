package com.scetzhbook.exchangePipeline.api;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.scetzhbook.exchangePipeline.security.TokenService;

@SpringBootTest
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void tokenReturnsJwt() throws Exception {
        when(tokenService.mintToken(eq("user1"), anyList())).thenReturn("mocked-jwt");

        mockMvc.perform(post("/auth/token")
                .param("user", "user1")
                .param("role", "TRADER"))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked-jwt"));
    }

    @Test
    void tokenRejectsInvalidRole() throws Exception {
        mockMvc.perform(post("/auth/token")
                .param("user", "user1")
                .param("role", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}

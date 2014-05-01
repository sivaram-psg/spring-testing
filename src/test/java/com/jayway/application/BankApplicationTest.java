package com.jayway.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.config.ApplicationConfig;
import com.jayway.config.WebConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, ApplicationConfig.class})
@ActiveProfiles({"h2", "disableSecurity"})
@WebAppConfiguration
@Transactional
public class BankApplicationTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;


    @Before
    public void setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void shouldPrint() throws Exception {
        mockMvc
                .perform(get("/accounts/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void shouldGetAccount() throws Exception {
        mockMvc
                .perform(get("/accounts/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("accountNumber").value(1))
                .andExpect(jsonPath("balance").value(100));
    }


    @Test
    public void shouldGetAllAccounts() throws Exception {
        mockMvc
                .perform(get("/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("[0]").value(1))
                .andExpect(jsonPath("[1]").value(2));
    }


    @Test
    public void shouldDepositToAccount() throws Exception {
        Map<String, Long> body = Collections.singletonMap("amount", 50L);
        String json = toJsonString(body);

        mockMvc
                .perform(post("/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
    }


    @Test
    public void shouldDeleteAccount() throws Exception {
        mockMvc
                .perform(delete("/accounts/1"))
                .andExpect(status().isNoContent());
    }


    @Test
    public void shouldNotDepositNegativeAmount() throws Exception {
        Map<String, Long> body = Collections.singletonMap("amount", -50L);
        String json = toJsonString(body);

        mockMvc
                .perform(post("/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldWithdrawFromAccount() throws Exception {
        Map<String, Long> body = Collections.singletonMap("amount", 50L);
        String json = toJsonString(body);

        mockMvc
                .perform(post("/accounts/1/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accountNumber", is(1)))
                .andExpect(jsonPath("balance", is(50)));
    }


    @Test
    public void shouldNotWithdrawNegativeAmount() throws Exception {
        Map<String, Long> body = Collections.singletonMap("amount", -50L);
        String json = toJsonString(body);

        mockMvc
                .perform(post("/accounts/1/withdraw")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }


    private static String toJsonString(Map<String, ?> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}


package com.example.app.controller;

import com.example.app.models.Ticket;
import com.example.app.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@WebMvcTest(controllers = {TicketController.class})
public class TicketRepositoryUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${spring.kafka.topic}")
    private String topicName;

    @Test
    public void whenTicketCreated_thenControlFlowCorrect() throws Exception {
        Ticket ticketInput = new Ticket();
        ticketInput.setDescription("description");

        Ticket ticketOutputFromDb = new Ticket();
        ticketOutputFromDb.setTicketId(1);
        ticketOutputFromDb.setDescription(ticketInput.getDescription());

        String reqBody = "{\n" +
                "    \"description\" : \"description\"\n" +
                "}";

        Mockito.when(this.ticketRepository.save(ticketInput)).thenReturn(ticketOutputFromDb);
        Mockito.when(this.kafkaTemplate.send(topicName, "key1", ticketInput.getDescription())).thenReturn(new CompletableFuture<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(reqBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ticketId").value(ticketOutputFromDb.getTicketId()));

        Mockito.verify(this.ticketRepository, Mockito.times(1)).save(ticketInput);

    }
}

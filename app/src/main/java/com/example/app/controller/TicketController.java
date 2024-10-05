package com.example.app.controller;

import com.example.app.models.Comment;
import com.example.app.models.Ticket;
import com.example.app.repository.TicketRepository;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${spring.kafka.topic}")
    private String topicName;

    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket){
        Ticket createdTicket = null;
        try{
            createdTicket = ticketRepository.save(ticket);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, "key1", ticket.getDescription());
            // Blocking
            // future.get();

            // Non Blocking
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + ticket.getDescription() +
                            "] with offset=[" + result.getRecordMetadata().offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" +
                            ticket.getDescription() + "] due to : " + ex.getMessage());
                }
            });
       }
       catch (Exception e){
           System.out.println(e.getCause());
           return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
       }
       return ResponseEntity.status(200).body(createdTicket);
    }

    @GetMapping({"/tickets", "/tickets/{id}"})
    public ResponseEntity<List<Ticket>> getTickets(@PathVariable(required = false, name= "id") String ticketId){
        List<Ticket> listTickets = new ArrayList<>();
        try{
            if(ticketId != null && !ticketId.isEmpty()){
                Optional<Ticket> ticketOptional = ticketRepository.findById(Integer.parseInt(ticketId));
                if(ticketOptional.isPresent()){
                    listTickets.add(ticketOptional.get());
                }
            }
            else{
                Iterable<Ticket> tickets = ticketRepository.findAll();
                for(Ticket ticket : tickets){
                    listTickets.add(ticket);
                }
            }
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().body(listTickets);
    }

    @PostMapping("/tickets/{id}/comments")
    public ResponseEntity<?> addComment(@RequestBody @Valid Comment comment, @PathVariable String id){
        try{
            Integer ticketId = Integer.parseInt(id);
            Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
            if(ticketOptional.isPresent()){
                Ticket ticket = ticketOptional.get();
                ticket.getComments().add(comment);
                ticketRepository.save(ticket);
            }
            return ResponseEntity.status(200).build();
        }
        catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            //ResponseEntity<String> responseEntity = new ResponseEntity<>(e.getLocalizedMessage(), HttpStatusCode.valueOf(500));
            return ResponseEntity.internalServerError().build();
        }
    }
}

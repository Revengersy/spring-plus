package org.example.expert.domain.log.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String level;
    private String userEmail;
    private Long managerUserId;
    private Long todoId;
    private LocalDateTime createdAt;

    public Log(String message, String level, String userEmail, Long managerUserId, Long todoId) {
        this.message = message;
        this.level = level;
        this.userEmail = userEmail;
        this.managerUserId = managerUserId;
        this.todoId = todoId;
        this.createdAt = LocalDateTime.now();
    }
}
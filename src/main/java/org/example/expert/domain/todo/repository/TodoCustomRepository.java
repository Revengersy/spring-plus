package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

// Lv2: 일부 함수만 바꾸기 위해
public interface TodoCustomRepository {
    Optional<Todo> findByIdWithUserDsl(Long todoId);
}
package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

// Lv2: 일부 클래스만 바꾸기 위해
public interface TodoCustomRepository {
    Optional<Todo> findByIdWithUserDsl(Long todoId);

    Page<TodoSearchResponse> searchTodos(String title, String managerNickname, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
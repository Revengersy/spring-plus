package org.example.expert.domain.todo.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUserDsl(Long todoId) {
        QTodo todo = QTodo.todo;
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(String title, String managerNickname, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QManager manager = QManager.manager;
        QUser user = QUser.user;
        QComment comment = QComment.comment;

        // 조건 빌더 생성
        BooleanBuilder conditions = buildCondition(title, managerNickname, startDate, endDate, todo, manager, user);

        // 메인 쿼리 실행
        List<TodoSearchResponse> content = queryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.title,
                        manager.countDistinct().as("managerCount"),
                        comment.countDistinct().as("commentCount")
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(conditions)
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리 실행
        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(conditions)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }

    private BooleanBuilder buildCondition(String title, String managerNickname, LocalDate startDate, LocalDate endDate,
                                          QTodo todo, QManager manager, QUser user) {
        BooleanBuilder builder = new BooleanBuilder();

//        비어있지 않으면
        if (!(title == null || title.isEmpty())) {
            builder.and(todo.title.containsIgnoreCase(title));
        }

        if (!(managerNickname == null || managerNickname.isEmpty())) {
            builder.and(user.nickname.containsIgnoreCase(managerNickname));
        }

        if (startDate != null && endDate != null) {
            builder.and(todo.createdAt.between(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()));
        } else if (startDate != null) {
            builder.and(todo.createdAt.goe(startDate.atStartOfDay()));
        } else if (endDate != null) {
            builder.and(todo.createdAt.lt(endDate.plusDays(1).atStartOfDay()));
        }

        return builder;
    }
}
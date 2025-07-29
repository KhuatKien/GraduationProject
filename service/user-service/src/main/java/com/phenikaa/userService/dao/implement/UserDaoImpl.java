package com.phenikaa.userService.dao.implement;

import com.phenikaa.userService.dao.interfaces.UserDao;
import com.phenikaa.userService.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.userName = :username", User.class);
        query.setParameter("username", username);
        return Optional.ofNullable(query.getSingleResult());
    }

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            entityManager.persist(user);
            return user;
        }
        else {
            entityManager.merge(user);
            return user;
        }
    }
}

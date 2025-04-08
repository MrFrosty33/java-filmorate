package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    /**
     * В этом классе предполагается, что все ему передаваемые данные корректны.
     * Проверки на корректность данных должны быть проведены в других классах.
     */

    private final Map<Long, User> usersMap;

    @Override
    public User get(Long id) {
        return usersMap.get(id);
    }

    @Override
    public Collection<User> getAll() {
        return usersMap.values();
    }

    public Map<Long, User> getMap() {
        return usersMap;
    }

    @Override
    public User add(User user) {
        return usersMap.put(user.getId(), user);
    }

    @Override
    public User add(Long id, User user) {
        return usersMap.put(id, user);
    }

    @Override
    public User update(User user) {
        return usersMap.replace(user.getId(), user);
    }

    @Override
    public User update(Long id, User user) {
        return usersMap.replace(id, user);
    }

    @Override
    public void delete(Long id) {
        usersMap.remove(id);
    }

    @Override
    public void deleteAll() {
        usersMap.clear();
    }
}

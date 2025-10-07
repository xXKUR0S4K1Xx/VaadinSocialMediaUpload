package org.vaadin.example.social;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserEntity save(UserEntity user) {
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String getAvatarUrl(String username) {
        return findByUsername(username)
                .map(user -> {
                    String avatar = user.getAvatarUrl();
                    if (avatar == null || avatar.isEmpty()) {
                        return "/avatar/default.png";  // default avatar
                    }
                    return avatar;  // stored avatar path in DB
                })
                .orElse("/avatar/default.png");
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> getAvatarData(String username) {
        return findByUsername(username)
                .map(UserEntity::getAvatarData);
    }

    // New helper: update avatar bytes safely
    @Transactional
    public void updateAvatar(String username, byte[] avatarBytes) {
        findByUsername(username).ifPresent(user -> {
            user.setAvatarData(avatarBytes);
            repository.save(user);
        });
    }
    public void navigateToUser(String username) {
        VaadinSession.getCurrent().setAttribute("selectedUser", username);
        UI.getCurrent().navigate("userpage");
    }
}

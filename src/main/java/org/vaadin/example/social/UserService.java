package org.vaadin.example.social;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserEntity getCurrentLoggedInUser() {
        // Example: read from VaadinSession
        String username = (String) VaadinSession.getCurrent().getAttribute("loggedInUser");
        if (username == null) {
            throw new RuntimeException("No logged-in user in session");
        }
        return findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
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
    @Transactional
    public void saveAvatar(UserEntity user, byte[] avatarData) throws IOException {
        String avatarPath = "images/" + user.getUsername() + ".png"; // example path
        Files.write(Paths.get(avatarPath), avatarData);              // save file to disk

        user.setAvatarUrl("/" + avatarPath);                         // store relative URL in DB
        save(user);                                      // persist to DB
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

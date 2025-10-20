package com.example.btms.desktop.controller.auth;

import javax.swing.SwingWorker;
import java.util.Arrays;
import java.util.Objects;

import com.example.btms.model.auth.AuthResult;
import com.example.btms.service.auth.AuthService;
import com.example.btms.ui.auth.LoginFrame;
import com.example.btms.util.security.HashUtil;

public class LoginController {

    public interface Listener {
        void onLoggedIn(String username, Role role);

        default void onCancelled() {
        }
    }

    public enum Role {
        CLIENT, ADMIN
    }

    private final AuthService authService;
    private final LoginFrame view;
    private Listener listener;

    public LoginController(AuthService authService, LoginFrame view) {
        this.authService = Objects.requireNonNull(authService, "authService");
        this.view = Objects.requireNonNull(view, "view");
        wire();
        view.setDbReady(true);
        view.setInputsEnabled(true);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    private void wire() {
        view.getLoginButton().addActionListener(e -> doLoginAsync());
        view.getCancelButton().addActionListener(e -> {
            view.showErr("Đã huỷ đăng nhập.");
            if (listener != null)
                listener.onCancelled();
            view.closeView();
        });
    }

    private void doLoginAsync() {
        String user = view.getUsername();
        char[] pass = view.getPassword();

        if (user.isEmpty() || pass.length == 0) {
            view.showErr("Nhập tài khoản và mật khẩu.");
            return;
        }

        view.setInputsEnabled(false);
        view.showOk("Đang kiểm tra thông tin...");

        new SwingWorker<AuthResult, Void>() {
            String md5;

            @Override
            protected AuthResult doInBackground() throws Exception {
                md5 = HashUtil.md5Hex(pass);
                return authService.authenticate(user, md5);
            }

            @Override
            protected void done() {
                try {
                    AuthResult ar = get();
                    if (!ar.found()) {
                        view.showErr("Sai tài khoản hoặc mật khẩu.");
                        return;
                    }
                    if (ar.locked()) {
                        view.showErr("Tài khoản đã bị khoá (GESPERRT).");
                        return;
                    }

                    Role role = view.isAdminSelected() ? Role.ADMIN : Role.CLIENT;
                    view.showOk("Đăng nhập thành công: " + user + " (" + role + ")");
                    if (listener != null)
                        listener.onLoggedIn(user, role);
                    view.closeView();

                } catch (Exception ex) {
                    view.showErr("Lỗi DB: " + ex.getMessage());
                } finally {
                    Arrays.fill(pass, '\0');
                    view.clearPassword();
                    view.setInputsEnabled(true);
                }
            }
        }.execute();
    }
}
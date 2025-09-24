package com.example.btms.util.swing;

import javax.swing.JComboBox;

/**
 * Dùng để "bịt" tạm thời các ActionEvent khi bạn nạp dữ liệu / đổi selection
 * bằng code.
 * Cách dùng:
 * SelectionGuard guard = new SelectionGuard();
 * guard.runSilently(() -> {
 * combo.removeAllItems();
 * combo.addItem(...);
 * guard.setSelectedIndexSilently(combo, 0);
 * });
 *
 * Trong listener:
 * if (guard.isSuppressed()) return;
 */
public class SelectionGuard {
    private boolean suppress = false;

    public void runSilently(Runnable r) {
        boolean old = suppress;
        suppress = true;
        try {
            r.run();
        } finally {
            suppress = old;
        }
    }

    public boolean isSuppressed() {
        return suppress;
    }

    public void setSelectedIndexSilently(JComboBox<?> cb, int idx) {
        runSilently(() -> cb.setSelectedIndex(idx));
    }
}

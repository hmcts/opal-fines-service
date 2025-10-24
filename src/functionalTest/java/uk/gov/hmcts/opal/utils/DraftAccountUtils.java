package uk.gov.hmcts.opal.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DraftAccountUtils {
    private static final ThreadLocal<ArrayList<String>> draftAccountId = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<String> createdAtTime = ThreadLocal.withInitial(() -> "");
    private static final ThreadLocal<String> initialAccountStatusDate = ThreadLocal.withInitial(() -> "");

    public static void addDraftAccountId(String id) {
        draftAccountId.get().add(id);
    }

    public static List<String> getAllDraftAccountIds() {
        return Collections.unmodifiableList(new ArrayList<>(draftAccountId.get()));

    }

    public static void clearDraftAccountIds() {
        draftAccountId.remove();
    }

    public static void addDraftAccountCreatedAtTime(String time) {
        createdAtTime.set(time);
    }

    public static String getDraftAccountCreatedAtTime() {
        return createdAtTime.get();
    }

    public static void clearDraftAccountCreatedAtTime() {
        createdAtTime.remove();
    }

    public static void addInitialAccountStatusDate(String date) {
        initialAccountStatusDate.set(date);
    }

    public static String getInitialAccountStatusDate() {
        return initialAccountStatusDate.get();
    }

    public static void clearInitialAccountStatusDate() {
        initialAccountStatusDate.remove();
    }

}

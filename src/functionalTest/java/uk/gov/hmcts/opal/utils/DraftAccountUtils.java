package uk.gov.hmcts.opal.utils;

import java.util.ArrayList;

public class DraftAccountUtils {
    private static final ThreadLocal<ArrayList<String>> draftAccountId = ThreadLocal.withInitial(ArrayList::new);

    public static void addDraftAccountId(String id) {
        draftAccountId.get().add(id);
    }

    public static ArrayList<String> getAllDraftAccountIds() {
        return draftAccountId.get();
    }

    public static void clearDraftAccountIds() {
        draftAccountId.remove();
    }
}

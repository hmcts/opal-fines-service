package uk.gov.hmcts.opal.context;

/**
 * Provides access to the typed scenario context for the current test thread.
 */
public final class ScenarioContextHolder {

    private static final ThreadLocal<ScenarioContext> CONTEXT = ThreadLocal.withInitial(ScenarioContext::new);

    /**
     * Utility class.
     */
    private ScenarioContextHolder() {
    }

    /**
     * Returns the scenario context bound to the current test thread.
     *
     * @return current scenario context.
     */
    public static ScenarioContext current() {
        return CONTEXT.get();
    }

    /**
     * Replaces the current thread's scenario context with a fresh empty instance.
     */
    public static void reset() {
        current().reset();
    }

    /**
     * Removes the scenario context bound to the current thread.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}

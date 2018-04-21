package net.tvburger.sjawl.deploy.strategies;

@SuppressWarnings("unchecked")
public final class DeploymentStrategyProvider {

    private static final ActiveActiveStrategy ACTIVE_ACTIVE_STRATEGY = new ActiveActiveStrategy();
    private static final ActiveStandbyStrategy ACTIVE_STANDBY_STRATEGY = new ActiveStandbyStrategy();
    private static final FirstAvailableStrategy FIRST_AVAILABLE_STRATEGY = new FirstAvailableStrategy();
    private static final RoundRobinStrategy ROUND_ROBIN_STRATEGY = new RoundRobinStrategy();

    public static <T> ActiveActiveStrategy<T> getActiveActiveStrategy() {
        return ACTIVE_ACTIVE_STRATEGY;
    }

    public static <T> ActiveStandbyStrategy<T> getActiveStandbyStrategy() {
        return ACTIVE_STANDBY_STRATEGY;
    }

    public static <T> FirstAvailableStrategy<T> getFirstAvailableStrategy() {
        return FIRST_AVAILABLE_STRATEGY;
    }

    public static <T> RoundRobinStrategy<T> getRoundRobinStrategy() {
        return ROUND_ROBIN_STRATEGY;
    }

    private DeploymentStrategyProvider() {
    }

}

package code.ponfee.job.enums;

/**
 * The trigger type enum class
 * 
 * @author Ponfee
 */
public enum TriggerType {

    CRON      (1, "0/10 * * * * ?"), //
    ONCE      (2, "2000-01-01 00:00:00"), //
    DAILY     (3, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    WEEKLY    (4, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    MONTHLY   (5, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    QUARTERLY (6, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    SEMIANNUAL(7, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    ANNUAL    (8, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    HOURLY    (9, "{'origin':'2000-01-01 00:00:00', 'step':1}"), //
    ;

    private final int    type;
    private final String example;

    TriggerType(int type, String example) {
        this.type = type;
        this.example = example;
    }

    public int type() {
        return type;
    }

    public String example() {
        return example;
    }

    public static TriggerType of(int type) {
        for (TriggerType trigger : TriggerType.values()) {
            if (trigger.type == type) {
                return trigger;
            }
        }
        return CRON; // default trigger type
    }

}

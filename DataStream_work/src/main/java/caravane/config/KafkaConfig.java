package caravane.config;

import java.util.Arrays;
import java.util.List;

public class KafkaConfig {
        public static final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null
                        ? System.getenv("KAFKA_BOOTSTRAP_SERVERS")
                        : "localhost:9094";
        public static final String CONSUMER_GROUP_ID = "hivemind-flink-group";

        // ==================== PHASE 1: WORKSTATION ONLY ====================
        public static final String TOPIC_WORKSTATION = "device-events-workstation";

        // Phase 2: Other device types (defined but not actively used)
        public static final String TOPIC_IOT = "device-events-iot";
        public static final String TOPIC_NETWORK = "device-events-network";
        public static final String TOPIC_SERVER = "device-events-server";

        // Flink consumer topics - WORKSTATION ONLY in Phase 1
        public static final List<String> ALL_TOPICS = Arrays.asList(
                        TOPIC_WORKSTATION
        // Phase 2: Uncomment to add other device types
        // , TOPIC_IOT
        // , TOPIC_NETWORK
        // , TOPIC_SERVER
        );

        /**
         * Phase 1: All events route to workstation topic
         * Phase 2: Uncomment switch statement for multi-device routing
         */
        public static String getTopicForDeviceType(String deviceType) {
                // Phase 1: Everything goes to workstation
                return TOPIC_WORKSTATION;

                /*
                 * Phase 2: Uncomment for device-specific routing
                 * if (deviceType == null) {
                 * return TOPIC_WORKSTATION;
                 * }
                 * 
                 * switch (deviceType.toUpperCase()) {
                 * case "WORKSTATION":
                 * return TOPIC_WORKSTATION;
                 * case "IOT":
                 * return TOPIC_IOT;
                 * case "NETWORK":
                 * return TOPIC_NETWORK;
                 * case "SERVER":
                 * return TOPIC_SERVER;
                 * default:
                 * return TOPIC_WORKSTATION;
                 * }
                 */
        }
}

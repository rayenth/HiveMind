package com.hivemind.datastream;

import com.hivemind.datastream.config.KafkaConfig;
import com.hivemind.datastream.processor.EventProcessor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.HashSet;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        System.out.println("🚀 Starting HiveMind DataStream Job...");

        // Create Kafka Source
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(KafkaConfig.BOOTSTRAP_SERVERS)
                .setTopics(KafkaConfig.ALL_TOPICS)
                .setGroupId(KafkaConfig.CONSUMER_GROUP_ID)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Add Source
        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Process Stream
        DataStream<String> processedStream = stream.map(new EventProcessor());

        // Print result to stdout (for verification)
        processedStream.print();

        // Execute program
        env.execute("HiveMind DataStream Processing");
    }
}
